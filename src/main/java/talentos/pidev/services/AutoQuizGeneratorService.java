package talentos.pidev.services;

import talentos.pidev.ai.HuggingFaceQuizService;
import talentos.pidev.dao.ChoixDAO;
import talentos.pidev.dao.QuestionDAO;
import talentos.pidev.models.Choix;
import talentos.pidev.models.Question;
import talentos.pidev.models.Quiz;


import java.sql.SQLException;
import java.util.List;

public class AutoQuizGeneratorService {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ChoixDAO choixDAO = new ChoixDAO();
    private final HuggingFaceQuizService hf = new HuggingFaceQuizService();

    public void generateIfEmpty(Quiz quiz) throws SQLException {

        boolean has = questionDAO.hasQuestionsAndChoices(quiz.getId());
        if (has) return;

        String prompt = """
        Génère 5 questions QCM sur le thème suivant:
        Titre: %s
        Description: %s

        Format EXACT JSON:
        {
          "questions":[
            {
              "enonce":"...",
              "choix":[
                {"texte":"...", "correct":true},
                {"texte":"...", "correct":false},
                {"texte":"...", "correct":false},
                {"texte":"...", "correct":false}
              ]
            }
          ]
        }
        """.formatted(quiz.getTitre(), quiz.getDescription());

        String response = hf.generate(prompt);

        // ⚠️ HuggingFace renvoie souvent un tableau JSON comme:
        // [ { "generated_text": "..." } ]
        String generatedText = extractGeneratedText(response);

        // Ici tu dois parser le JSON final (questions/choix)
        // ✅ pour simplifier: on peut faire un parse minimal
        // (si tu veux je te donne parser Gson/Jackson propre)
        QuizJsonParser.Result parsed = QuizJsonParser.parse(generatedText);

        for (QuizJsonParser.QuestionDTO qdto : parsed.questions) {
            Question q = new Question();
            q.setQuizId(quiz.getId());
            q.setEnonce(qdto.enonce);
            questionDAO.add(q);

            for (QuizJsonParser.ChoixDTO cdto : qdto.choix) {
                Choix c = new Choix();
                c.setQuestionId(q.getId());
                c.setTexte(cdto.texte);
                c.setEstCorrect(cdto.correct);
                choixDAO.add(c);
            }
        }
    }

    private String extractGeneratedText(String raw) {
        // extraction simple sans lib
        // cherche "generated_text"
        int i = raw.indexOf("generated_text");
        if (i == -1) return raw;
        int start = raw.indexOf(':', i) + 1;
        int firstQuote = raw.indexOf('"', start);
        int lastQuote = raw.lastIndexOf('"');
        if (firstQuote == -1 || lastQuote <= firstQuote) return raw;
        return raw.substring(firstQuote + 1, lastQuote).replace("\\n", "\n").replace("\\\"", "\"");
    }
}