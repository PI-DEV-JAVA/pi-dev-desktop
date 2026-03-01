package talentos.pidev.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser simple pour extraire:
 * {
 *   "questions":[
 *     {"enonce":"...", "choix":[{"texte":"...", "correct":true}, ...]}
 *   ]
 * }
 *
 * ✅ Sans dépendances externes
 * ✅ Suffisant pour un prompt contrôlé (format EXACT JSON)
 */
public class QuizJsonParser {

    public static class Result {
        public List<QuestionDTO> questions = new ArrayList<>();
    }

    public static class QuestionDTO {
        public String enonce;
        public List<ChoixDTO> choix = new ArrayList<>();
    }

    public static class ChoixDTO {
        public String texte;
        public boolean correct;
    }

    /**
     * Extrait et parse le JSON depuis le texte généré.
     * Le modèle peut renvoyer du texte avant/après -> on cherche le premier '{' et dernier '}'.
     */
    public static Result parse(String generatedText) {
        String json = extractJsonObject(generatedText);
        Result out = new Result();

        // 1) Extraire bloc questions: "questions":[ ... ]
        String questionsArray = extractArray(json, "questions");
        if (questionsArray == null || questionsArray.isBlank()) return out;

        // 2) Extraire chaque objet question { ... }
        List<String> questionObjects = splitTopLevelObjects(questionsArray);

        for (String qObj : questionObjects) {
            QuestionDTO qdto = new QuestionDTO();

            qdto.enonce = unescape(extractStringField(qObj, "enonce"));

            String choixArray = extractArray(qObj, "choix");
            if (choixArray != null && !choixArray.isBlank()) {
                List<String> choixObjects = splitTopLevelObjects(choixArray);
                for (String cObj : choixObjects) {
                    ChoixDTO cdto = new ChoixDTO();
                    cdto.texte = unescape(extractStringField(cObj, "texte"));
                    cdto.correct = extractBooleanField(cObj, "correct");
                    qdto.choix.add(cdto);
                }
            }

            // garde seulement si enonce non vide et au moins 2 choix
            if (qdto.enonce != null && !qdto.enonce.isBlank() && qdto.choix.size() >= 2) {
                out.questions.add(qdto);
            }
        }

        return out;
    }

    // ---------------- helpers ----------------

    private static String extractJsonObject(String text) {
        if (text == null) return "{}";
        int a = text.indexOf('{');
        int b = text.lastIndexOf('}');
        if (a == -1 || b == -1 || b <= a) return "{}";
        return text.substring(a, b + 1);
    }

    private static String extractArray(String json, String field) {
        // match: "field" : [ .... ]
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\\[(.*?)]\\s*(,|})", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        return m.group(1).trim();
    }

    private static String extractStringField(String obj, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(obj);
        if (!m.find()) return "";
        return m.group(1);
    }

    private static boolean extractBooleanField(String obj, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(obj);
        if (!m.find()) return false;
        return Boolean.parseBoolean(m.group(1).toLowerCase());
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }

    /**
     * Split d'une liste d'objets JSON top-level: {..},{..},{..}
     * en respectant les accolades.
     */
    private static List<String> splitTopLevelObjects(String arrayContent) {
        List<String> out = new ArrayList<>();
        if (arrayContent == null) return out;

        int depth = 0;
        int start = -1;

        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);

            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    out.add(arrayContent.substring(start, i + 1).trim());
                    start = -1;
                }
            }
        }
        return out;
    }
}