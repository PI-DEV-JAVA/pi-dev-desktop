package talentos.pidev.controllers.common;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.util.Locale;
import java.util.function.BiConsumer;

public class MapPickerController {

    @FXML private WebView webView;
    @FXML private Button btnClose;

    private BiConsumer<Double, Double> onPicked;

    public void setOnPicked(BiConsumer<Double, Double> onPicked) {
        this.onPicked = onPicked;
    }

    public void load(double lat, double lon, int zoom, String tileProxyBase) {

        WebEngine engine = webView.getEngine();

        String html = String.format(Locale.US, """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                  <style>
                    html, body { height: 100%%; margin: 0; padding: 0; }
                    #map { height: 100%%; width: 100%%; }
                  </style>
                </head>
                <body>
                  <div id="map"></div>

                  <script>
                    window._map = L.map('map').setView([%f, %f], %d);

                    L.tileLayer('%s/tiles/{z}/{x}/{y}.png', {
                      maxZoom: 19,
                      attribution: '© OpenStreetMap'
                    }).addTo(window._map);

                    window._marker = L.marker([%f, %f]).addTo(window._map);

                    window._map.on('click', function(e){
                      var lat = e.latlng.lat;
                      var lng = e.latlng.lng;

                      if(window._marker) window._map.removeLayer(window._marker);
                      window._marker = L.marker([lat, lng]).addTo(window._map);

                      // 🔥 Bridge vers Java
                      if(window.javaBridge && window.javaBridge.pick){
                        window.javaBridge.pick(lat, lng);
                      } else {
                        console.log("javaBridge not ready yet");
                      }
                    });

                    // Debug errors
                    window.onerror = function(msg, url, line, col, err){
                      console.log("JS Error:", msg, url, line);
                    };
                  </script>
                </body>
                </html>
                """,
                lat, lon, zoom,
                tileProxyBase,
                lat, lon
        );

        engine.loadContent(html);

        engine.getLoadWorker().stateProperty().addListener((obs, oldS, newS) -> {
            if (newS == Worker.State.SUCCEEDED) {

                // ✅ inject bridge
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", new JavaBridge());

                // ✅ force resize leaflet
                Platform.runLater(() -> {
                    try { engine.executeScript("if(window._map){ window._map.invalidateSize(true); }"); }
                    catch (Exception ignored) {}
                });

                webView.widthProperty().addListener((o,a,b) -> {
                    try { engine.executeScript("if(window._map){ window._map.invalidateSize(true); }"); } catch (Exception ignored) {}
                });
                webView.heightProperty().addListener((o,a,b) -> {
                    try { engine.executeScript("if(window._map){ window._map.invalidateSize(true); }"); } catch (Exception ignored) {}
                });
            }
        });
    }

    public class JavaBridge {
        public void pick(double lat, double lng) {
            // ✅ log pour confirmer dans console
            System.out.println("Picked: " + lat + ", " + lng);

            if (onPicked != null) {
                onPicked.accept(lat, lng);
            }
        }
    }

    @FXML
    private void onClose() {
        webView.getScene().getWindow().hide();
    }
}