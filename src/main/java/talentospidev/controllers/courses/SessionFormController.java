package talentospidev.controllers.courses;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import talentospidev.dao.coursesDAO.SeanceDAO;
import talentospidev.models.courses.Seance;
import talentospidev.services.AuthService;
import talentospidev.services.GeoCodingService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Session form with Leaflet map picker for in-place sessions.
 */
public class SessionFormController {

    @FXML private Label pageTitleLbl, errorLbl, coordsLbl;
    @FXML private TextField titreField, startTimeField, endTimeField, videoField, addressField;
    @FXML private ComboBox<String> typeBox;
    @FXML private DatePicker datePicker;
    @FXML private VBox onlineSection, inPlaceSection, sidebar;
    @FXML private WebView mapWebView;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private double selectedLat = 0, selectedLon = 0;
    private boolean locationPicked = false;

    // Keep strong reference to prevent GC of bridge object
    private MapBridge mapBridge;

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);

        typeBox.setItems(FXCollections.observableArrayList("PRESENTIEL", "EN_LIGNE"));
        typeBox.setValue("PRESENTIEL");
        datePicker.setValue(LocalDate.now());
        startTimeField.setText("09:00");
        endTimeField.setText("11:00");

        // Toggle online/in-place sections
        typeBox.valueProperty().addListener((o, a, v) -> {
            boolean inPlace = "PRESENTIEL".equals(v);
            inPlaceSection.setVisible(inPlace); inPlaceSection.setManaged(inPlace);
            onlineSection.setVisible(!inPlace); onlineSection.setManaged(!inPlace);
        });
        onlineSection.setVisible(false); onlineSection.setManaged(false);

        // Load Leaflet map
        loadMap();
    }

    private void loadMap() {
        WebEngine engine = mapWebView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Keep strong reference to bridge
        mapBridge = new MapBridge();

        // Set up bridge AFTER page finishes loading
        engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", mapBridge);
            }
        });

        // Write HTML to a temp file so WebView can resolve relative URLs for CDN
        try {
            File tmpHtml = File.createTempFile("talentos_map_", ".html");
            tmpHtml.deleteOnExit();
            try (FileWriter fw = new FileWriter(tmpHtml)) {
                fw.write(getMapHtml());
            }
            engine.load(tmpHtml.toURI().toString());
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: load content directly
            engine.loadContent(getMapHtml());
        }
    }

    private String getMapHtml() {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.min.css"/>
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; }
                #map { width: 100%; height: 100%; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.min.js"></script>
            <script>
                var map = L.map('map').setView([36.8065, 10.1815], 12);
                L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; OpenStreetMap'
                }).addTo(map);

                var marker = null;

                map.on('click', function(e) {
                    if (marker) map.removeLayer(marker);
                    marker = L.marker(e.latlng).addTo(map);
                    marker.bindPopup("Lat: " + e.latlng.lat.toFixed(5) + "<br>Lng: " + e.latlng.lng.toFixed(5)).openPopup();
                    try {
                        if (window.javaBridge) {
                            window.javaBridge.onLocationPicked(e.latlng.lat, e.latlng.lng);
                        }
                    } catch(err) { console.log(err); }
                });

                function searchAndMove(lat, lon, name) {
                    map.setView([lat, lon], 16);
                    if (marker) map.removeLayer(marker);
                    marker = L.marker([lat, lon]).addTo(map);
                    marker.bindPopup(name).openPopup();
                }
            </script>
        </body>
        </html>
        """;
    }

    /** Java bridge called from Leaflet JS when user clicks the map */
    public class MapBridge {
        public void onLocationPicked(double lat, double lon) {
            Platform.runLater(() -> {
                selectedLat = lat;
                selectedLon = lon;
                locationPicked = true;
                coordsLbl.setText("📍 Selected: " + String.format("%.5f, %.5f", lat, lon));
                coordsLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #16a34a; -fx-font-weight: 700;");

                // Reverse geocode to show address
                try {
                    String displayName = new GeoCodingService().reverseGeocode(lat, lon);
                    if (displayName != null) {
                        addressField.setText(displayName);
                        coordsLbl.setText("📍 " + displayName);
                    }
                } catch (Exception e) { /* ignore */ }
            });
        }
    }

    @FXML
    private void onSearchAddress() {
        String addr = addressField.getText().trim();
        if (addr.isEmpty()) return;
        try {
            GeoCodingService.GeoResult result = new GeoCodingService().geocode(addr);
            if (result != null) {
                selectedLat = result.lat;
                selectedLon = result.lon;
                locationPicked = true;
                coordsLbl.setText("📍 " + result.displayName);
                coordsLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #16a34a; -fx-font-weight: 700;");
                mapWebView.getEngine().executeScript(
                        "searchAndMove(" + result.lat + "," + result.lon + ",'" + result.displayName.replace("'", "\\'") + "')");
            } else {
                coordsLbl.setText("⚠ Address not found");
                coordsLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626;");
            }
        } catch (Exception e) {
            coordsLbl.setText("⚠ Geocoding error: " + e.getMessage());
            coordsLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626;");
        }
    }

    @FXML
    private void onSave() {
        StringBuilder err = new StringBuilder();
        if (titreField.getText().trim().isEmpty()) err.append("Title required. ");
        if (datePicker.getValue() == null) err.append("Date required. ");
        if ("PRESENTIEL".equals(typeBox.getValue()) && !locationPicked) err.append("Please pick a location on the map. ");
        try {
            LocalTime.parse(startTimeField.getText().trim());
        } catch (Exception e) { err.append("Invalid start time (use HH:mm). "); }
        try {
            LocalTime.parse(endTimeField.getText().trim());
        } catch (Exception e) { err.append("Invalid end time (use HH:mm). "); }

        if (!err.isEmpty()) {
            errorLbl.setText("⚠ " + err.toString().trim());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
            return;
        }

        try {
            Seance s = new Seance();
            s.setFormationId(ViewContext.getSelectedFormationId());
            s.setTitre(titreField.getText().trim());
            s.setType(typeBox.getValue());
            LocalDate d = datePicker.getValue();
            s.setDateDebut(LocalDateTime.of(d, LocalTime.parse(startTimeField.getText().trim())));
            s.setDateFin(LocalDateTime.of(d, LocalTime.parse(endTimeField.getText().trim())));
            s.setStatut("PLANIFIEE");

            if ("PRESENTIEL".equals(typeBox.getValue())) {
                s.setAdresse(addressField.getText().trim());
                s.setLatitude(selectedLat);
                s.setLongitude(selectedLon);
            } else {
                s.setVideoPath(videoField.getText().trim());
            }

            seanceDAO.add(s);
            SceneUtil.switchScene("Courses/CourseDetails.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLbl.setText("⚠ " + ex.getMessage());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
        }
    }

    @FXML private void onCancel() { SceneUtil.switchScene("Courses/CourseDetails.fxml"); }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleEvents() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Events/EventsFeed.fxml" : "Events/EventsBrowse.fxml");
    }
    @FXML private void handleCourses()       { SceneUtil.switchScene("Courses/CoursesRH.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
