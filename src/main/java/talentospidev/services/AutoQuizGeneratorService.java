package talentospidev.services;

import talentospidev.dao.coursesDAO.ChoixDAO;
import talentospidev.dao.coursesDAO.QuestionDAO;
import talentospidev.models.courses.Choix;
import talentospidev.models.courses.Question;
import talentospidev.models.courses.Quiz;

/**
 * Auto-generates quiz questions using HuggingFace AI.
 * Uses the same prompt style as the proven HuggingFaceQuizService.
 */
public class AutoQuizGeneratorService {

    private final HuggingFaceService hf = new HuggingFaceService();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ChoixDAO choixDAO = new ChoixDAO();

    public void generateIfEmpty(Quiz quiz) {
        try {
            if (quiz == null) return;
            int quizId = quiz.getId();

            if (questionDAO.hasQuestionsAndChoices(quizId)) return;

            String theme = buildTheme(quiz);

            // Use the EXACT same prompt format as the old working HuggingFaceQuizService
            String out = hf.generateQuiz(
                    "Thème: " + theme + "\n" +
                    "Niveau: débutant\n" +
                    "Génère exactement 5 questions QCM."
            );

            // Debug: log raw AI response
            System.out.println("=== AI RAW RESPONSE ===");
            System.out.println(out);
            System.out.println("=== END RAW RESPONSE ===");

            QuizJsonParser.Result parsed;
            try {
                parsed = QuizJsonParser.parse(out);
            } catch (RuntimeException e) {
                System.out.println("First parse failed: " + e.getMessage() + ", retrying...");
                // Retry
                String out2 = hf.generateQuiz(
                        "Thème: " + theme + "\n" +
                        "Génère exactement 5 questions QCM.\n" +
                        "Réponds UNIQUEMENT en JSON valide."
                );
                System.out.println("=== AI RETRY RESPONSE ===");
                System.out.println(out2);
                System.out.println("=== END RETRY ===");
                parsed = QuizJsonParser.parse(out2);
            }

            int order = 1;
            for (QuizJsonParser.QuestionDTO qdto : parsed.questions) {
                Question q = new Question();
                q.setQuizId(quizId);
                q.setEnonce(qdto.enonce);
                q.setPoints(1);
                q.setOrdre(order++);
                q.setPublished(true);
                questionDAO.add(q);

                if (q.getId() <= 0) throw new RuntimeException("Question insert failed");

                for (QuizJsonParser.ChoixDTO cdto : qdto.choix) {
                    Choix c = new Choix();
                    c.setQuestionId(q.getId());
                    c.setTexte(cdto.texte);
                    c.setEstCorrect(cdto.correct);
                    choixDAO.add(c);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Quiz generation error: " + ex.getMessage(), ex);
        }
    }

    private String buildTheme(Quiz quiz) {
        String t = quiz.getTitre() == null ? "" : quiz.getTitre().trim();
        String d = quiz.getDescription() == null ? "" : quiz.getDescription().trim();
        if (!t.isBlank() && !d.isBlank()) return t + " - " + d;
        if (!t.isBlank()) return t;
        return "General Knowledge";
    }
}
