package talentospidev.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses JSON quiz output from HuggingFace into Question+Choice DTOs.
 * Handles various response formats including HF's wrapped output.
 */
public class QuizJsonParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static class ChoixDTO {
        public String texte;
        public boolean correct;
    }

    public static class QuestionDTO {
        public String enonce;
        public List<ChoixDTO> choix = new ArrayList<>();
    }

    public static class Result {
        public List<QuestionDTO> questions = new ArrayList<>();
    }

    public static Result parse(String raw) {
        try {
            // HuggingFace wraps responses: [{"generated_text": "..."}]
            String content = unwrapHFResponse(raw);

            // Strip markdown code fences if present
            content = content.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "");

            // Extract JSON array from content
            String json = extractJson(content.trim());

            // Fix single quotes → double quotes (common AI output issue)
            if (json.contains("'") && !json.contains("\"")) {
                json = json.replace("'", "\"");
            }
            // Also handle mixed: replace single-quoted keys/values
            json = json.replaceAll("(?<=[{,\\[])\\s*'", " \"")
                       .replaceAll("'\\s*(?=[:}\\],])", "\"")
                       .replace("True", "true").replace("False", "false");

            JsonNode root = mapper.readTree(json);

            Result result = new Result();
            JsonNode questionsNode = root.isArray() ? root : root.get("questions");
            if (questionsNode == null || !questionsNode.isArray()) {
                throw new RuntimeException("No questions array found in: " + json.substring(0, Math.min(200, json.length())));
            }

            for (JsonNode qNode : questionsNode) {
                QuestionDTO q = new QuestionDTO();
                q.enonce = qNode.has("enonce") ? qNode.get("enonce").asText()
                        : qNode.has("question") ? qNode.get("question").asText()
                        : qNode.has("text") ? qNode.get("text").asText() : "?";

                JsonNode choicesNode = qNode.has("choix") ? qNode.get("choix")
                        : qNode.has("choices") ? qNode.get("choices")
                        : qNode.has("options") ? qNode.get("options")
                        : qNode.has("answers") ? qNode.get("answers") : null;

                if (choicesNode != null && choicesNode.isArray()) {
                    for (JsonNode cNode : choicesNode) {
                        ChoixDTO c = new ChoixDTO();
                        c.texte = cNode.has("texte") ? cNode.get("texte").asText()
                                : cNode.has("text") ? cNode.get("text").asText()
                                : cNode.has("answer") ? cNode.get("answer").asText()
                                : cNode.asText();
                        c.correct = cNode.has("est_correct") ? cNode.get("est_correct").asBoolean()
                                : cNode.has("correct") ? cNode.get("correct").asBoolean()
                                : cNode.has("is_correct") ? cNode.get("is_correct").asBoolean() : false;
                        q.choix.add(c);
                    }
                }
                if (!q.enonce.equals("?")) result.questions.add(q);
            }

            if (result.questions.isEmpty()) {
                throw new RuntimeException("Parsed 0 valid questions from AI output");
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse quiz JSON: " + e.getMessage(), e);
        }
    }

    /** Unwrap HuggingFace response format: [{"generated_text": "..."}] */
    private static String unwrapHFResponse(String raw) {
        try {
            JsonNode root = mapper.readTree(raw);
            if (root.isArray() && root.size() > 0) {
                JsonNode first = root.get(0);
                if (first.has("generated_text")) {
                    return first.get("generated_text").asText();
                }
            }
        } catch (Exception ignored) {}
        return raw;
    }

    private static String extractJson(String raw) {
        // Try to extract JSON array [...] from response
        Pattern p = Pattern.compile("\\[\\s*\\{.*}\\s*]", Pattern.DOTALL);
        Matcher m = p.matcher(raw);
        if (m.find()) return m.group();

        // Try to extract {"questions": [...]} format
        Pattern p2 = Pattern.compile("\\{\\s*\"questions\".*}", Pattern.DOTALL);
        Matcher m2 = p2.matcher(raw);
        if (m2.find()) return m2.group();

        return raw;
    }
}
