package talentospidev.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import talentospidev.config.GoogleOAuthConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * Handles the Google OAuth 2.0 flow:
 * 1. Build the consent URL
 * 2. Exchange authorization code for access token
 * 3. Fetch user info (email, Google ID, name)
 */
public class GoogleOAuthService {

    /**
     * Builds the Google consent page URL.
     * The user will be redirected here inside a WebView.
     */
    public static String getAuthUrl() {
        return GoogleOAuthConfig.AUTH_URL
                + "?client_id=" + encode(GoogleOAuthConfig.CLIENT_ID)
                + "&redirect_uri=" + encode(GoogleOAuthConfig.REDIRECT_URI)
                + "&response_type=code"
                + "&scope=" + encode(GoogleOAuthConfig.SCOPES)
                + "&access_type=offline"
                + "&prompt=consent";
    }

    /**
     * Exchanges the authorization code for an access token.
     * POST to Google's token endpoint.
     *
     * @param code The authorization code from the consent redirect
     * @return The access token string
     */
    public static String exchangeCodeForToken(String code) throws IOException {
        // The code may arrive URL-encoded from the WebView redirect — decode it first
        String decodedCode = java.net.URLDecoder.decode(code, StandardCharsets.UTF_8);

        // Build form body
        StringJoiner body = new StringJoiner("&");
        body.add("code=" + encode(decodedCode));
        body.add("client_id=" + encode(GoogleOAuthConfig.CLIENT_ID));
        body.add("client_secret=" + encode(GoogleOAuthConfig.CLIENT_SECRET));
        body.add("redirect_uri=" + encode(GoogleOAuthConfig.REDIRECT_URI));
        body.add("grant_type=authorization_code");

        System.out.println("[GoogleOAuth] Token exchange request body: " + body);

        // POST request
        HttpURLConnection conn = (HttpURLConnection) new URL(GoogleOAuthConfig.TOKEN_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            // Read the error body for debugging
            String errorBody = "";
            try {
                errorBody = readStream(conn.getErrorStream());
            } catch (Exception ignored) {
            }
            System.err.println("[GoogleOAuth] Token exchange FAILED — HTTP " + responseCode);
            System.err.println("[GoogleOAuth] Error body: " + errorBody);
            throw new IOException("Token exchange failed (HTTP " + responseCode + "): " + errorBody);
        }

        // Read success response
        String response = readStream(conn.getInputStream());
        System.out.println("[GoogleOAuth] Token response: " + response);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        return json.get("access_token").getAsString();
    }

    /**
     * Fetches user profile info from Google using the access token.
     * Returns a JsonObject with: id, email, name, picture, etc.
     */
    public static JsonObject getUserInfo(String accessToken) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(
                GoogleOAuthConfig.USERINFO_URL + "?access_token=" + encode(accessToken))
                .openConnection();
        conn.setRequestMethod("GET");

        String response = readStream(conn.getInputStream());
        return JsonParser.parseString(response).getAsJsonObject();
    }

    // ── Helpers ──

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String readStream(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
