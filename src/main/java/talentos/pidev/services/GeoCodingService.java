package talentos.pidev.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GeoCodingService {

    public static class GeoResult {
        public double lat;
        public double lon;
        public String displayName;

        public GeoResult(double lat, double lon, String displayName) {
            this.lat = lat;
            this.lon = lon;
            this.displayName = displayName;
        }
    }

    // =========================
    // GEOCODE (adresse → lat/lng)
    // =========================
    public GeoResult geocode(String address) throws Exception {

        String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encoded;

        JSONArray arr = readJsonArrayFromUrl(url);

        if (arr == null || arr.length() == 0) return null;

        JSONObject obj = arr.getJSONObject(0);

        double lat = Double.parseDouble(obj.getString("lat"));
        double lon = Double.parseDouble(obj.getString("lon"));
        String name = obj.getString("display_name");

        return new GeoResult(lat, lon, name);
    }

    // =========================
    // REVERSE GEOCODE (lat/lng → adresse)
    // =========================
    public String reverseGeocode(double lat, double lon) throws Exception {

        String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" +
                lat + "&lon=" + lon;

        JSONObject obj = readJsonObjectFromUrl(url);
        if (obj == null) return null;

        return obj.optString("display_name", null);
    }

    // =========================
    // OPEN IN BROWSER
    // =========================
    public void openOSM(double lat, double lon) throws Exception {
        String osmUrl = "https://www.openstreetmap.org/?mlat=" +
                lat + "&mlon=" + lon + "#map=16/" + lat + "/" + lon;

        Desktop.getDesktop().browse(new URI(osmUrl));
    }

    // =========================
    // INTERNAL JSON METHODS
    // =========================

    private JSONArray readJsonArrayFromUrl(String urlString) throws Exception {
        String content = readRaw(urlString);
        if (content == null) return null;
        return new JSONArray(content);
    }

    private JSONObject readJsonObjectFromUrl(String urlString) throws Exception {
        String content = readRaw(urlString);
        if (content == null) return null;
        return new JSONObject(content);
    }

    private String readRaw(String urlString) throws Exception {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "TalentOS-App");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        }
    }
    public void openUrl(String url) throws Exception {
        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
    }
}