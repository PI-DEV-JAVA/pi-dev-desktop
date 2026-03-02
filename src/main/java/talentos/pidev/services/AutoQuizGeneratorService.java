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

            // ✅ 1) Génération HF
            String out1 = hf.generateQuizJson(theme, 5);

            QuizJsonParser.Result parsed;
            try {
                parsed = QuizJsonParser.parse(out1);
            } catch (RuntimeException e) {
                // ✅ Retry 1 fois si tronqué / invalide
                String msg = e.getMessage() == null ? "" : e.getMessage();
                if (msg.contains("tronqué") || msg.contains("Aucun objet JSON") || msg.contains("Champ 'questions'")) {
                    String out2 = hf.generateQuizJson(
                            theme + " IMPORTANT: renvoie le JSON COMPLET minifié sur une seule ligne, aucun texte.",
                            5
                    );
                    parsed = QuizJsonParser.parse(out2);
                } else {
                    throw e;
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

                    // selon ton modèle: setEstCorrect / setCorrect / setCorrecte...
                    // ici je suppose setEstCorrect(boolean)
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