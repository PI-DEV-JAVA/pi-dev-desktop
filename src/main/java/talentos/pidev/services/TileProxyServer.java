package talentos.pidev.services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TileProxyServer {

    private HttpServer server;
    private ExecutorService pool;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final Path cacheDir;

    public TileProxyServer() {
        this.cacheDir = Paths.get("cache", "tiles");
        try { Files.createDirectories(cacheDir); } catch (IOException ignored) {}
    }

    /**
     * @param port 0 => port libre auto
     * @return port réellement utilisé
     */
    public int start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/tiles", this::handleTile);

        // ⚠️ 4-6 threads = stable (évite trop de requêtes)
        pool = Executors.newFixedThreadPool(6);
        server.setExecutor(pool);

        server.start();
        return server.getAddress().getPort();
    }

    public void stop() {
        try {
            if (server != null) server.stop(0);
        } catch (Exception ignored) {}
        try {
            if (pool != null) pool.shutdownNow();
        } catch (Exception ignored) {}
    }

    private void handleTile(HttpExchange ex) throws IOException {
        try {
            // /tiles/{z}/{x}/{y}.png
            String path = ex.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 5) {
                ex.sendResponseHeaders(400, -1);
                return;
            }

            String z = parts[2];
            String x = parts[3];
            String y = parts[4]; // "123.png" ou "123@2x.png"

            if (!z.matches("\\d+") || !x.matches("\\d+") || !y.matches("\\d+(@2x)?\\.png")) {
                ex.sendResponseHeaders(400, -1);
                return;
            }

            Path dir = cacheDir.resolve(z).resolve(x);
            Files.createDirectories(dir);
            Path file = dir.resolve(y);

            // cache hit
            if (Files.exists(file) && Files.size(file) > 0) {
                sendPng(ex, Files.readAllBytes(file));
                return;
            }

            String url = "https://tile.openstreetmap.org/" + z + "/" + x + "/" + y;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TalentOS-JavaFX/1.0 (tiles proxy)")
                    .header("Accept", "image/png,image/*;q=0.9,*/*;q=0.8")
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            HttpResponse<byte[]> res = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

            if (res.statusCode() != 200 || res.body() == null || res.body().length == 0) {
                ex.sendResponseHeaders(503, -1);
                return;
            }

            // save cache
            Files.write(file, res.body(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            sendPng(ex, res.body());

        } catch (Exception e) {
            try { ex.sendResponseHeaders(503, -1); } catch (Exception ignored) {}
        } finally {
            try { ex.close(); } catch (Exception ignored) {}
        }
    }

    private void sendPng(HttpExchange ex, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "image/png");
        ex.getResponseHeaders().set("Cache-Control", "public, max-age=86400");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }
}