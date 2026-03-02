package talentos.pidev.ai;

import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HuggingFaceQuizService {

    private static final String API_URL = "https://router.huggingface.co/v1/chat/completions";

    private final String token;
    private final String model;

    public HuggingFaceQuizService() {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMissing()
                .load();

        this.token = clean(dotenv.get("HF_TOKEN"));
        this.model = clean(dotenv.get("HF_MODEL", "katanemo/Arch-Router-1.5B:hf-inference"));

        if (token == null || token.isBlank()) {
            throw new RuntimeException("HF_TOKEN manquant dans .env");
        }
    }

    private String clean(String s) {
        if (s == null) return null;
        s = s.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    public String generateQuizJson(String theme, int nQuestions) {
        String userPrompt = """
Thème: %s
Niveau: débutant
Génère exactement %d questions QCM.
""".formatted(theme, nQuestions).trim();

        return chat(userPrompt, 1800);
    }

    private String chat(String userPrompt, int maxTokens) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + token);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setConnectTimeout(15000);
            con.setReadTimeout(90000);
            con.setDoOutput(true);

            JsonObject body = new JsonObject();
            body.addProperty("model", model);

            JsonArray messages = new JsonArray();

            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", systemPromptJsonOnly());
            messages.add(system);

            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", userPrompt);
            messages.add(user);

            body.add("messages", messages);

            body.addProperty("temperature", 0.1);
            body.addProperty("max_tokens", maxTokens);

            try (OutputStream os = con.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = con.getResponseCode();
            String raw = readAll((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());

            if (code < 200 || code >= 300) {
                throw new RuntimeException("HF Router Error " + code + " => " + raw);
            }

            return extractChatContent(raw);

        } catch (Exception e) {
            throw new RuntimeException("Erreur HuggingFace: " + e.getMessage(), e);
        }
    }

    private String systemPromptJsonOnly() {
        return """
Tu es un générateur de quiz QCM.
Tu dois répondre UNIQUEMENT par un JSON VALIDE.
AUCUN texte hors JSON. AUCUN markdown. AUCUN ```.

IMPORTANT: JSON MINIFIÉ sur UNE seule ligne (sans retours à la ligne).
Utilise uniquement des guillemets doubles " ".
Utilise true/false en minuscules.

Schéma STRICT (aucun champ en plus):
{"questions":[{"enonce":"...","choix":[{"texte":"...","correct":true},{"texte":"...","correct":false},{"texte":"...","correct":false},{"texte":"...","correct":false}]}]}

Règles:
- exactement 5 questions (sauf si l’utilisateur demande un autre nombre)
- exactement 4 choix par question
- exactement 1 seul choix correct par question
- textes courts (enonce <= 120 caractères, texte <= 80 caractères)
""".trim();
    }

    private String extractChatContent(String raw) {
        JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) return raw;

        JsonObject msg = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (msg != null && msg.has("content")) return msg.get("content").getAsString();

        return raw;
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
}