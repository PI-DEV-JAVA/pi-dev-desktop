package talentos.pidev.ai;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class HuggingFaceQuizService {

    private final String token;
    private final String model;

    public HuggingFaceQuizService() {
        Properties p = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/config.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) {}

        token = System.getenv("HF_TOKEN") != null ? System.getenv("HF_TOKEN") : p.getProperty("HF_TOKEN", "");
        model = p.getProperty("HF_MODEL", "mistralai/Mistral-7B-Instruct-v0.2");
    }

    public String generate(String prompt) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("HF_TOKEN manquant. Ajoute-le dans config.properties ou variable d'environnement.");
        }

        try {
            URL url = new URL("https://router.huggingface.co/models/" + model);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + token);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String body = """
            {
              "inputs": %s,
              "parameters": { "max_new_tokens": 700, "temperature": 0.5 }
            }
            """.formatted(toJsonString(prompt));

            try (OutputStream os = con.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = con.getResponseCode();
            String resp;
            try (InputStream is = (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream()) {
                resp = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            if (code < 200 || code >= 300) {
                throw new RuntimeException("HF API Error " + code + " => " + resp);
            }

            return resp;

        } catch (Exception e) {
            throw new RuntimeException("Erreur HuggingFace: " + e.getMessage(), e);
        }
    }

    private String toJsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n") + "\"";
    }
}