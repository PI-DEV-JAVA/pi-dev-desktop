package talentos.pidev.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HuggingFaceService {

    private static final String API_URL =
            "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2";

    private final String token;

    public HuggingFaceService() {
        token = System.getenv("HF_TOKEN");  // 🔥 ICI
    }

    public String generateQuiz(String prompt) throws Exception {

        URL url = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String body = """
        {
          "inputs": %s
        }
        """.formatted(toJsonString(prompt));

        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = con.getResponseCode();
        InputStream is = (code >= 200 && code < 300)
                ? con.getInputStream()
                : con.getErrorStream();

        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String toJsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n") + "\"";
    }
}