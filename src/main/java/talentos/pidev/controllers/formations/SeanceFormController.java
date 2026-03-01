package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import talentos.pidev.dao.SeanceDAO;
import talentos.pidev.models.Seance;
import talentos.pidev.services.GeoCodingService;

import java.awt.*;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import talentos.pidev.controllers.common.MapPickerController;
import talentos.pidev.services.TileProxyServer;

public class SeanceFormController {

    @FXML private Label titleLabel;

    @FXML private TextField tfTitre;
    @FXML private ComboBox<String> cbType;

    @FXML private DatePicker dpDebut;
    @FXML private Spinner<Integer> spDebutH;
    @FXML private Spinner<Integer> spDebutM;

    @FXML private DatePicker dpFin;
    @FXML private Spinner<Integer> spFinH;
    @FXML private Spinner<Integer> spFinM;

    // PRESENTIEL
    @FXML private VBox presentielBox;
    @FXML private TextField tfAdresse;
    @FXML private Label lbLatLng;
    @FXML private Button btnGeocode;
    @FXML private Button btnOpenMap;

    // EN LIGNE
    @FXML private VBox onlineBox;
    @FXML private TextField tfVideoLink;     // ✅ NEW
    @FXML private Button btnOpenLink;        // ✅ NEW
    @FXML private Spinner<Integer> spDuree;

    @FXML private Label errorLabel;

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final GeoCodingService geo = new GeoCodingService();

    private Seance editing;
    private int formationId;

    private Runnable onDone;

    public void setFormationId(int id) { this.formationId = id; }
    public void setOnDone(Runnable r) { this.onDone = r; }

    public void setSeanceToEdit(Seance s) {
        this.editing = s;

        if (s != null) {
            titleLabel.setText("Modifier séance");

            tfTitre.setText(nz(s.getTitre()));
            cbType.setValue(nz(s.getType()));

            if (s.getDateDebut() != null) {
                dpDebut.setValue(s.getDateDebut().toLocalDate());
                spDebutH.getValueFactory().setValue(s.getDateDebut().getHour());
                spDebutM.getValueFactory().setValue(s.getDateDebut().getMinute());
            }
            if (s.getDateFin() != null) {
                dpFin.setValue(s.getDateFin().toLocalDate());
                spFinH.getValueFactory().setValue(s.getDateFin().getHour());
                spFinM.getValueFactory().setValue(s.getDateFin().getMinute());
            }

            tfAdresse.setText(nz(s.getAdresse()));

            lbLatLng.setText((s.getLatitude() != null && s.getLongitude() != null)
                    ? ("Lat: " + s.getLatitude() + " / Lng: " + s.getLongitude())
                    : "Non géolocalisé");

            // ✅ EN_LIGNE : videoPath contient un URL
            tfVideoLink.setText(nz(s.getVideoPath()));
            if (s.getDureeMinutes() != null) spDuree.getValueFactory().setValue(s.getDureeMinutes());

        } else {
            titleLabel.setText("Ajouter séance");
            // defaults
            tfVideoLink.setText("");
            lbLatLng.setText("Non géolocalisé");
        }

        refreshBoxes();
    }

    @FXML
    public void initialize() {
        cbType.getItems().setAll("PRESENTIEL", "EN_LIGNE");
        cbType.getSelectionModel().selectFirst();

        spDebutH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        spDebutM.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spFinH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 11));
        spFinM.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        spDuree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 600, 60));

        dpDebut.setValue(LocalDate.now());
        dpFin.setValue(LocalDate.now());

        cbType.valueProperty().addListener((obs, o, n) -> refreshBoxes());
        refreshBoxes();

        // au cas où : map doit marcher même sans geocode
        btnOpenMap.setDisable(false);
    }

    private void refreshBoxes() {
        String type = cbType.getValue();
        boolean pres = "PRESENTIEL".equals(type);

        presentielBox.setManaged(pres);
        presentielBox.setVisible(pres);

        onlineBox.setManaged(!pres);
        onlineBox.setVisible(!pres);
    }

    // ===================== PRESENTIEL =====================

    @FXML
    private void onGeocode() {
        try {
            String adr = nz(tfAdresse.getText());
            if (adr.isEmpty()) { setError("Adresse obligatoire"); return; }

            var res = geo.geocode(adr);
            if (res == null) { setError("Adresse introuvable."); return; }

            if (editing == null) editing = new Seance();
            editing.setAdresse(res.displayName);
            editing.setLatitude(res.lat);
            editing.setLongitude(res.lon);

            lbLatLng.setText("Lat: " + res.lat + " / Lng: " + res.lon);

        } catch (Exception e) {
            setError("Geocoding: " + e.getMessage());
        }
    }

    /**
     * ✅ NEW: Ouvrir Map marche directement:
     * - si lat/lng manquent => fait geocode puis ouvre.
     */
    @FXML
    private void onOpenMap() {
        try {
            if (editing != null && editing.getLatitude() != null && editing.getLongitude() != null) {
                geo.openOSM(editing.getLatitude(), editing.getLongitude());
            } else {
                setError("Séance non géolocalisée. Choisis sur la map.");
            }
        } catch (Exception e) {
            setError("Map: " + e.getMessage());
        }
    }
    // ===================== EN_LIGNE =====================

    @FXML
    private void onOpenLink() {
        try {
            String url = nz(tfVideoLink.getText());
            if (url.isEmpty()) { setError("Lien obligatoire."); return; }
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            setError("Ouverture lien: " + e.getMessage());
        }
    }

    // ===================== SAVE / CANCEL =====================

    @FXML
    private void onSave() {
        if (errorLabel != null) errorLabel.setText("");

        String titre = nz(tfTitre.getText());
        if (titre.isEmpty()) { setError("Titre obligatoire"); return; }

        LocalDate d1 = dpDebut.getValue();
        LocalDate d2 = dpFin.getValue();
        if (d1 == null || d2 == null) { setError("Dates obligatoires"); return; }

        LocalDateTime dtDebut = d1.atTime(spDebutH.getValue(), spDebutM.getValue());
        LocalDateTime dtFin = d2.atTime(spFinH.getValue(), spFinM.getValue());
        if (dtFin.isBefore(dtDebut)) { setError("Fin doit être après début."); return; }

        Seance s = (editing == null) ? new Seance() : editing;

        s.setFormationId(formationId);
        s.setTitre(titre);
        s.setType(cbType.getValue());
        s.setDateDebut(dtDebut);
        s.setDateFin(dtFin);
        s.setStatut("PLANIFIEE");

        if ("PRESENTIEL".equals(s.getType())) {
            String adr = nz(tfAdresse.getText());
            if (adr.isEmpty()) { setError("Adresse obligatoire (présentiel)."); return; }

            // si geocode auto n'a pas été fait, on garde l’adresse saisie
            if (s.getAdresse() == null || s.getAdresse().isBlank()) s.setAdresse(adr);

            // lat/lng restent optionnels (mais map fait geocode auto)
            s.setVideoPath(null);
            s.setDureeMinutes(null);

        } else {
            // ✅ EN_LIGNE => lien obligatoire
            String url = nz(tfVideoLink.getText());
            if (url.isEmpty()) { setError("Lien Drive obligatoire (en ligne)."); return; }

            s.setVideoPath(url);
            s.setDureeMinutes(spDuree.getValue());

            // nettoyer présentiel
            s.setAdresse(null);
            s.setLatitude(null);
            s.setLongitude(null);
        }

        try {
            if (s.getId() == 0) {
                int id = seanceDAO.add(s);
                s.setId(id);
            } else {
                seanceDAO.update(s);
            }
            if (onDone != null) onDone.run();

        } catch (Exception e) {
            setError("Save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onPickFromMap() {
        try {
            // 1) start proxy local sur port libre
            TileProxyServer proxy = new TileProxyServer();
            int port = proxy.start(0); // ✅ port libre
            String proxyBase = "http://127.0.0.1:" + port;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/common/MapPicker.fxml"));
            Parent root = loader.load();

            MapPickerController c = loader.getController();

            double lat0 = (editing != null && editing.getLatitude() != null) ? editing.getLatitude() : 36.899;
            double lon0 = (editing != null && editing.getLongitude() != null) ? editing.getLongitude() : 10.189;

            c.setOnPicked((lat, lon) -> {
                if (editing == null) editing = new Seance();
                editing.setLatitude(lat);
                editing.setLongitude(lon);

                lbLatLng.setText("Lat: " + lat + " / Lng: " + lon);

                // si tu veux garder l’adresse écrite
                // if (editing.getAdresse() == null || editing.getAdresse().isBlank()) {
                //    editing.setAdresse(nz(tfAdresse.getText()));
                // }
            });

            c.load(lat0, lon0, 13, proxyBase);

            Stage st = new Stage();
            st.setTitle("Map - Choisir emplacement");
            st.setScene(new Scene(root, 900, 600));
            st.setOnHidden(ev -> proxy.stop()); // ✅ stop proxy quand on ferme
            st.show();

        } catch (Exception e) {
            setError("Map: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void onCancel() {
        if (onDone != null) onDone.run();
    }

    private void setError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
        else new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private String nz(String s) { return s == null ? "" : s.trim(); }
}