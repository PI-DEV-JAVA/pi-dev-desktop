package talentos.pidev.services;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

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

    public static Result parse(String text) {
        try {
            if (text == null || text.isBlank()) {
                throw new RuntimeException("Réponse vide du modèle.");
            }

            String cleaned = stripMarkdownFences(text);
            String obj = extractFirstCompleteJsonObject(cleaned);

            if (!obj.trim().startsWith("{")) {
                throw new RuntimeException("Aucun objet JSON détecté.");
            }
            if (!isBalancedJsonObject(obj)) {
                throw new RuntimeException("JSON tronqué (accolades non équilibrées).");
            }

            JsonObject root = JsonParser.parseString(obj).getAsJsonObject();
            JsonArray qs = root.getAsJsonArray("questions");
            if (qs == null) throw new RuntimeException("Champ 'questions' manquant.");

            Result r = new Result();

            for (JsonElement qel : qs) {
                JsonObject qo = qel.getAsJsonObject();
                QuestionDTO q = new QuestionDTO();
                q.enonce = getString(qo, "enonce");

                JsonArray choices = qo.getAsJsonArray("choix");
                if (choices == null || choices.size() != 4) {
                    throw new RuntimeException("Chaque question doit avoir exactement 4 choix.");
                }

                for (JsonElement cel : choices) {
                    JsonObject co = cel.getAsJsonObject();
                    ChoixDTO c = new ChoixDTO();
                    c.texte = getString(co, "texte");
                    c.correct = co.has("correct") && !co.get("correct").isJsonNull() && co.get("correct").getAsBoolean();
                    q.choix.add(c);
                }

                // force: exactement 1 correct
                int correctCount = (int) q.choix.stream().filter(ch -> ch.correct).count();
                if (correctCount == 0) q.choix.get(0).correct = true;
                if (correctCount > 1) {
                    boolean kept = false;
                    for (ChoixDTO ch : q.choix) {
                        if (ch.correct) {
                            if (!kept) kept = true;
                            else ch.correct = false;
                        }
                    }
                }

                r.questions.add(q);
            }

            if (r.questions.isEmpty()) throw new RuntimeException("Aucune question parsée.");
            return r;

        } catch (Exception e) {
            throw new RuntimeException("QuizJsonParser.parse error: " + e.getMessage() + "\nRaw:\n" + text, e);
        }
    }

    private static String getString(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) return "";
        return o.get(key).getAsString();
    }

    private static String stripMarkdownFences(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            int firstNewLine = t.indexOf('\n');
            if (firstNewLine > 0) t = t.substring(firstNewLine + 1);
            int lastFence = t.lastIndexOf("```");
            if (lastFence >= 0) t = t.substring(0, lastFence);
        }
        return t.trim();
    }

    private static String extractFirstCompleteJsonObject(String s) {
        int start = s.indexOf('{');
        if (start == -1) return s;

        int depth = 0;
        boolean inString = false;
        boolean escape = false;

        for (int i = start; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (inString) {
                if (escape) escape = false;
                else if (ch == '\\') escape = true;
                else if (ch == '"') inString = false;
                continue;
            } else {
                if (ch == '"') { inString = true; continue; }
                if (ch == '{') depth++;
                if (ch == '}') depth--;
                if (depth == 0) return s.substring(start, i + 1).trim();
            }
        }
        return s.substring(start).trim();
    }

    private static boolean isBalancedJsonObject(String s) {
        int depth = 0;
        boolean inString = false;
        boolean escape = false;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (inString) {
                if (escape) escape = false;
                else if (ch == '\\') escape = true;
                else if (ch == '"') inString = false;
                continue;
            } else {
                if (ch == '"') { inString = true; continue; }
                if (ch == '{') depth++;
                if (ch == '}') depth--;
                if (depth < 0) return false;
            }
        }
        return depth == 0;
    }
}