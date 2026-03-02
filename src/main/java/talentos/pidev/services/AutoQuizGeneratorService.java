package talentos.pidev.services;

import talentos.pidev.ai.HuggingFaceQuizService;
import talentos.pidev.dao.ChoixDAO;
import talentos.pidev.dao.QuestionDAO;
import talentos.pidev.models.Choix;
import talentos.pidev.models.Question;
import talentos.pidev.models.Quiz;

public class AutoQuizGeneratorService {

    private final HuggingFaceQuizService hf = new HuggingFaceQuizService();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ChoixDAO choixDAO = new ChoixDAO();

    /**
     * Appelé depuis QuizCardController:
     * autoService.generateIfEmpty(quiz);
     */
    public void generateIfEmpty(Quiz quiz) {
        try {
            if (quiz == null) return;

            int quizId = quiz.getId();

            // ✅ Si déjà rempli => stop
            if (questionDAO.hasQuestionsAndChoices(quizId)) {
                return;
            }

            String theme = buildThemeFromQuiz(quiz);

            // ✅ Prompt strict (diminue erreurs)
            String strictRules = """
                    IMPORTANT:
                    - Réponds UNIQUEMENT avec un JSON valide.
                    - Utilise des guillemets doubles " partout (pas de ').
                    - Utilise true/false (pas True/False).
                    - Aucun texte avant/après le JSON.
                    - EXACTEMENT 5 questions.
                    - EXACTEMENT 4 choix par question.
                    - EXACTEMENT 1 seul choix correct par question.
                    """;

            // ✅ 1) Génération HF
            String out1 = hf.generateQuizJson(theme + "\n" + strictRules, 5);

            QuizJsonParser.Result parsed;
            try {
                parsed = QuizJsonParser.parse(out1);
            } catch (RuntimeException e) {
                // ✅ Retry 1: demander JSON minifié 1 ligne
                String out2 = hf.generateQuizJson(
                        theme + "\n" + strictRules + "\nRENVOIE LE JSON MINIFIÉ SUR UNE SEULE LIGNE.",
                        5
                );
                try {
                    parsed = QuizJsonParser.parse(out2);
                } catch (RuntimeException e2) {
                    // ✅ Retry 2: ajouter instruction "pas d'apostrophes"
                    String out3 = hf.generateQuizJson(
                            theme + "\n" + strictRules +
                                    "\nÉVITE les apostrophes dans le texte (remplace L' par Le ).",
                            5
                    );
                    parsed = QuizJsonParser.parse(out3);
                }
            }

            // ✅ 2) Insertion DB: Question puis Choix
            for (QuizJsonParser.QuestionDTO qdto : parsed.questions) {

                Question q = new Question();
                q.setQuizId(quizId);
                q.setEnonce(qdto.enonce);

                // ✅ Ton DAO met l'ID généré dans q.setId(...)
                questionDAO.add(q);

                int questionId = q.getId();
                if (questionId <= 0) {
                    throw new RuntimeException("Insertion question échouée: ID non généré.");
                }

                for (QuizJsonParser.ChoixDTO cdto : qdto.choix) {
                    Choix c = new Choix();
                    c.setQuestionId(questionId);
                    c.setTexte(cdto.texte);
                    c.setEstCorrect(cdto.correct);
                    choixDAO.add(c);
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("Erreur génération : " + ex.getMessage(), ex);
        }
    }

    private String buildThemeFromQuiz(Quiz quiz) {
        String t = quiz.getTitre() == null ? "" : quiz.getTitre().trim();
        String d = quiz.getDescription() == null ? "" : quiz.getDescription().trim();

        if (!t.isBlank() && !d.isBlank()) return t + " - " + d;
        if (!t.isBlank()) return t;

        return "JavaFX et SceneBuilder";
    }
}