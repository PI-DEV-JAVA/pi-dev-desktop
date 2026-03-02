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

    /**
     * Parse robuste:
     * - supporte JSON strict (" ")
     * - supporte sorties HF python-like (' ' + True/False)
     * - gère apostrophes L'interface, etc.
     * - extrait le premier objet JSON {...} même si texte avant/après
     */
    public static Result parse(String text) {
        try {
            if (text == null || text.isBlank()) {
                throw new RuntimeException("Réponse vide du modèle.");
            }

            String cleaned = stripMarkdownFences(text);
            String obj = extractFirstCompleteJsonObject(cleaned);

            if (obj == null || obj.isBlank()) {
                throw new RuntimeException("Aucun objet JSON détecté.");
            }

            // ✅ normaliser avant Gson (HF retourne parfois dict python)
            String normalized = normalizeToValidJson(obj);

            if (!normalized.trim().startsWith("{")) {
                throw new RuntimeException("Aucun objet JSON détecté après normalisation.");
            }
            if (!isBalancedJsonObject(normalized)) {
                throw new RuntimeException("JSON tronqué (accolades non équilibrées).");
            }

            JsonObject root = JsonParser.parseString(normalized).getAsJsonObject();
            JsonArray qs = root.getAsJsonArray("questions");
            if (qs == null) throw new RuntimeException("Champ 'questions' manquant.");

            Result r = new Result();

            for (JsonElement qel : qs) {
                if (!qel.isJsonObject()) continue;
                JsonObject qo = qel.getAsJsonObject();

                QuestionDTO q = new QuestionDTO();
                q.enonce = getString(qo, "enonce");

                JsonArray choices = qo.getAsJsonArray("choix");
                if (choices == null) {
                    throw new RuntimeException("Champ 'choix' manquant dans une question.");
                }

                // ✅ force exactement 4 choix (si HF en donne +/-, on ajuste)
                List<ChoixDTO> temp = new ArrayList<>();
                for (JsonElement cel : choices) {
                    if (!cel.isJsonObject()) continue;
                    JsonObject co = cel.getAsJsonObject();

                    ChoixDTO c = new ChoixDTO();
                    c.texte = getString(co, "texte");
                    c.correct = co.has("correct") && !co.get("correct").isJsonNull() && co.get("correct").getAsBoolean();
                    temp.add(c);
                }

                if (temp.size() < 4) {
                    // compléter avec choix vides si manque (rare)
                    while (temp.size() < 4) {
                        ChoixDTO fill = new ChoixDTO();
                        fill.texte = "Choix " + (temp.size() + 1);
                        fill.correct = false;
                        temp.add(fill);
                    }
                } else if (temp.size() > 4) {
                    // garder seulement 4
                    temp = temp.subList(0, 4);
                }

                q.choix.addAll(temp);

                // ✅ force: exactement 1 correct
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

    /** extrait le premier bloc JSON {...} complet */
    private static String extractFirstCompleteJsonObject(String s) {
        int start = s.indexOf('{');
        if (start == -1) return null;

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

    /**
     * HF retourne parfois:
     * {'questions': [{'enonce': 'L'interface ...', 'correct': False}]}
     * => on convertit en JSON valide.
     */
    private static String normalizeToValidJson(String s) {
        String out = s.trim();

        // 1) True/False (python) -> true/false (json)
        out = out.replace(": True", ": true")
                .replace(": False", ": false");

        // 2) Convert single quotes to double quotes BUT keep apostrophes inside words (L'interface)
        out = smartSingleQuotesToDouble(out);

        return out;
    }

    private static String smartSingleQuotesToDouble(String s) {
        StringBuilder sb = new StringBuilder();
        boolean inSingle = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\'') {
                char prev = (i > 0) ? s.charAt(i - 1) : '\0';
                char next = (i + 1 < s.length()) ? s.charAt(i + 1) : '\0';

                boolean apostropheInsideWord = Character.isLetter(prev) && Character.isLetter(next);

                if (apostropheInsideWord) {
                    // keep apostrophe
                    sb.append(c);
                } else {
                    // toggle single-quote string region, and replace with "
                    inSingle = !inSingle;
                    sb.append('"');
                }
            } else {
                // if we are inside converted string, escape double quotes
                if (inSingle && c == '"') sb.append("\\\"");
                else sb.append(c);
            }
        }

        return sb.toString();
    }
}