package talentospidev.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Calls HuggingFace Inference API (via router.huggingface.co)
 * using the OpenAI-compatible chat completions endpoint.
 * Mirrors the proven working HuggingFaceQuizService from the Courses module.
 */
public class HuggingFaceService {

  
    private static final String MODEL = "katanemo/Arch-Router-1.5B:hf-inference";

    // System prompt — exact same as the working old version
    private static final String SYSTEM_PROMPT = """
        Tu es un générateur de quiz QCM.
        Tu dois répondre UNIQUEMENT par un JSON VALIDE.
        AUCUN texte hors JSON. AUCUN markdown. AUCUN ```.

        IMPORTANT: JSON MINIFIÉ sur UNE seule ligne (sans retours à la ligne).
        Utilise uniquement des guillemets doubles.
        Utilise true/false en minuscules.

        Schéma STRICT (aucun champ en plus):
        {"questions":[{"enonce":"...","choix":[{"texte":"...","correct":true},{"texte":"...","correct":false},{"texte":"...","correct":false},{"texte":"...","correct":false}]}]}

        Règles:
        - exactement 5 questions (sauf si l'utilisateur demande un autre nombre)
        - exactement 4 choix par question
        - exactement 1 seul choix correct par question
        - textes courts (enonce <= 120 caractères, texte <= 80 caractères)
        """.trim();

    public String generateQuiz(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);
        con.setConnectTimeout(15000);
        con.setReadTimeout(90000);

        // Build JSON using proper escaping (same approach as old working version)
        String body = "{" +
                "\"model\":\"" + MODEL + "\"," +
                "\"messages\":[" +
                    "{\"role\":\"system\",\"content\":" + toJsonString(SYSTEM_PROMPT) + "}," +
                    "{\"role\":\"user\",\"content\":" + toJsonString(prompt) + "}" +
                "]," +
                "\"temperature\":0.1," +
                "\"max_tokens\":1800" +
                "}";

        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = con.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream();
        String response = readAll(is);

        if (code < 200 || code >= 300) {
            throw new RuntimeException("HuggingFace API error (HTTP " + code + "): " + response);
        }

        // Extract the assistant message content from OpenAI-style response
        return extractChatContent(response);
    }

    /** Extracts content from {"choices":[{"message":{"content":"..."}}]} */
    private String extractChatContent(String json) {
        try {
            com.fasterxml.jackson.databind.JsonNode root =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                return root.get("choices").get(0).get("message").get("content").asText();
            }
        } catch (Exception e) { /* fall through */ }
        return json;
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    private String toJsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
}
