package talentos.pidev.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AIScoringService {

    private static final String API_URL = "https://api.apyhub.com/sharpapi/api/v1/hr/resume_job_match_score";
    private static final String API_TOKEN = "APY0MQPaCOe9k4UZbhvSacP8yCUjaHEkmyzjhw81xdjFRM5yh4DeZMcRvS9JLwtd"; // Remplace par ton vrai token

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public AIScoringService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Soumet une demande de scoring à l'API
     */
    public String submitScoringJob(File cvFile, String jobDescription, String language) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", cvFile.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), cvFile))
                .addFormDataPart("content", jobDescription)
                .addFormDataPart("language", language != null ? language : "French")
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("apy-token", API_TOKEN)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("Erreur API (" + response.code() + "): " + errorBody);
            }

            String responseBody = response.body().string();
            System.out.println("✅ Réponse submit: " + responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // Récupérer le job_id
            String jobId = jsonNode.path("job_id").asText();

            if (jobId.isEmpty()) {
                throw new IOException("Impossible de trouver job_id dans la réponse");
            }

            System.out.println("✅ Job ID reçu: " + jobId);
            return jobId;
        }
    }

    /**
     * Récupère le résultat du scoring - VERSION CORRIGÉE
     */
    public JsonNode getScoringResult(String jobId) throws IOException {
        String statusUrl = "https://api.apyhub.com/sharpapi/api/v1/hr/resume_job_match_score/job/status/" + jobId;

        Request request = new Request.Builder()
                .url(statusUrl)
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("apy-token", API_TOKEN)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    return null; // Pas encore prêt
                }
                throw new IOException("Erreur API (" + response.code() + ")");
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // ✅ Vérifier si le résultat est présent
            JsonNode attributes = jsonNode.path("data").path("attributes");
            String status = attributes.path("status").asText();

            // Si le statut est "success" et qu'il y a un résultat, retourne le résultat
            if ("success".equals(status) && attributes.has("result")) {
                return jsonNode; // Retourne tout le nœud pour que parseResult puisse l'analyser
            }

            // Sinon, pas encore prêt
            return null;
        }
    }
    /**
     * Version simplifiée pour tester (attends le résultat final directement)
     */
    public JsonNode getScoringResultSync(String jobId, int maxAttempts) throws IOException, InterruptedException {
        JsonNode result = null;
        int attempts = 0;

        while (result == null && attempts < maxAttempts) {
            Thread.sleep(5000); // Attendre 5 secondes entre chaque tentative
            result = getScoringResult(jobId);
            attempts++;
            System.out.println("   Tentative " + attempts + "/" + maxAttempts +
                    (result == null ? " (en cours...)" : " (terminé!)"));
        }

        return result;
    }
}