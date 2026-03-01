package talentos.pidev.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class WebRTCService {

    private static Process publisherProcess;
    private static Process subscriberProcess;
    private static final String SCRIPTS_PATH = "/home/speedweed/Desktop/PI_dev/webRTCHandler";
    static Gson gson = new Gson();
    static HttpClient httpClient = HttpClient.newHttpClient();

    public static void startScripts() {
        if (isAnyScriptRunning()) {
            System.out.println("Scripts already running. Skipping start.");
            return;
        }

        try {
            File directory = new File(SCRIPTS_PATH);

            ProcessBuilder pubBuilder = new ProcessBuilder(
                    "/home/speedweed/Desktop/PI_dev/webRTCHandler/venv/bin/python3", "publisher.py");
            pubBuilder.directory(directory);
            pubBuilder.inheritIO();
            publisherProcess = pubBuilder.start();

            ProcessBuilder subBuilder = new ProcessBuilder(
                    "/home/speedweed/Desktop/PI_dev/webRTCHandler/venv/bin/python3", "multi_subscribers.py");
            subBuilder.directory(directory);
            subBuilder.inheritIO();
            subscriberProcess = subBuilder.start();

            System.out.println("Python background services initialized in: " + SCRIPTS_PATH);
        } catch (IOException e) {
            System.err.println("Could not start Python scripts. Check if the path is correct!");
            e.printStackTrace();
        }
    }

    public static boolean isAnyScriptRunning() {
        return (publisherProcess != null && publisherProcess.isAlive()) ||
                (subscriberProcess != null && subscriberProcess.isAlive());
    }

    public static void stopScripts() {

        if (publisherProcess != null)
            publisherProcess.destroyForcibly();
        if (subscriberProcess != null)
            subscriberProcess.destroyForcibly();

        publisherProcess = null;
        subscriberProcess = null;
    }

    public static void createJanusRoomFlow(long roomId) {
        String baseUrl = "http://4.233.136.0:8088/janus";

        // 1. Create Session
        sendJanusRequest(baseUrl, "{\"janus\":\"create\",\"transaction\":\"ts1\"}")
                .thenCompose(resp -> {
                    long sessionId = resp.getAsJsonObject("data").get("id").getAsLong();
                    String attachBody = "{\"janus\":\"attach\",\"plugin\":\"janus.plugin.videoroom\",\"transaction\":\"ts2\"}";
                    return sendJanusRequest(baseUrl + "/" + sessionId, attachBody)
                            .thenApply(attachResp -> new long[] { sessionId,
                                    attachResp.getAsJsonObject("data").get("id").getAsLong() });
                })
                .thenCompose(ids -> {
                    long sessionId = ids[0];
                    long handleId = ids[1];
                    JsonObject createBody = new JsonObject();
                    createBody.addProperty("janus", "message");
                    createBody.addProperty("transaction", "ts3");

                    JsonObject innerBody = new JsonObject();
                    innerBody.addProperty("admin_key", "supersecret");
                    innerBody.addProperty("request", "create");
                    innerBody.addProperty("room", roomId);
                    innerBody.addProperty("description", "Interview " + roomId);
                    innerBody.addProperty("publishers", 4);
                    innerBody.addProperty("permanent", true);
                    createBody.add("body", innerBody);
                    return sendJanusRequest(baseUrl + "/" + sessionId + "/" + handleId, gson.toJson(createBody));
                })
                .thenAccept(finalResp -> System.out.println("Room Created Successfully: " + finalResp))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    // Helper to handle the HTTP logic
    public static CompletableFuture<JsonObject> sendJanusRequest(String url, String json) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> gson.fromJson(res.body(), JsonObject.class));
    }

}
