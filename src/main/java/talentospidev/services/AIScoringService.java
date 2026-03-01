package talentospidev.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * AI-powered resume-to-job matching via ApyHub SharpAPI.
 */
public class AIScoringService {

    private static final String API_URL = "https://api.apyhub.com/sharpapi/api/v1/hr/resume_job_match_score";
    private static final String API_TOKEN = "APY08cGGA2Q6jDeoH1Umq6qYAUx40o5lr4znXveOKu6H9z3bUYJhf0b5yaOckBYHcMeNSrn";

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
     * Submit a scoring job to the API.
     * 
     * @return the job ID for polling
     */
    public String submitScoringJob(File cvFile, String jobDescription, String language)
            throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", cvFile.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), cvFile))
                .addFormDataPart("content", jobDescription)
                .addFormDataPart("language", language != null ? language : "English")
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
                throw new IOException("API error (" + response.code() + "): " + errorBody);
            }

            String responseBody = response.body().string();
            System.out.println("✅ Submit response: " + responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String jobId = jsonNode.path("job_id").asText();
            if (jobId.isEmpty()) {
                throw new IOException("No job_id found in API response");
            }

            System.out.println("✅ Job ID received: " + jobId);
            return jobId;
        }
    }

    /**
     * Poll the scoring result for a given job ID.
     * 
     * @return result JsonNode if ready, null if still processing
     */
    public JsonNode getScoringResult(String jobId) throws IOException {
        String statusUrl = API_URL + "/job/status/" + jobId;

        Request request = new Request.Builder()
                .url(statusUrl)
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("apy-token", API_TOKEN)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404)
                    return null; // not ready yet
                throw new IOException("API error (" + response.code() + ")");
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode attributes = jsonNode.path("data").path("attributes");
            String status = attributes.path("status").asText();

            if ("success".equals(status) && attributes.has("result")) {
                return jsonNode;
            }
            return null; // not ready
        }
    }

    /**
     * Synchronous helper: polls until result is ready or max attempts exceeded.
     */
    public JsonNode getScoringResultSync(String jobId, int maxAttempts)
            throws IOException, InterruptedException {
        JsonNode result = null;
        int attempts = 0;
        while (result == null && attempts < maxAttempts) {
            Thread.sleep(5000);
            result = getScoringResult(jobId);
            attempts++;
            System.out.println("   Attempt " + attempts + "/" + maxAttempts +
                    (result == null ? " (processing...)" : " (done!)"));
        }
        return result;
    }
}
