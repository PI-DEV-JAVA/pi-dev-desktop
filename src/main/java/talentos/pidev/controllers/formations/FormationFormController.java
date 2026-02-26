package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentos.pidev.dao.FormationDAO;
import talentos.pidev.models.Formation;

public class FormationFormController {

    @FXML private Label titleLabel;

    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextArea contenuArea;

    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> difficulteCombo;
    @FXML private ComboBox<String> statutCombo;

    @FXML private ComboBox<String> modeCombo;
    @FXML private TextField lieuField;

    @FXML private TextField formateurField;
    @FXML private TextArea prerequisArea;

    @FXML private TextField capaciteField;

    @FXML private Label errorLabel;

    private final FormationDAO dao = new FormationDAO();
    private Formation editing = null;
    private Runnable onSaved;
    private Runnable onCancel;

    @FXML
    public void initialize() {

        categorieCombo.getItems().setAll("Développement", "Réseaux", "RH", "Soft Skills", "Data", "Autre");
        difficulteCombo.getItems().setAll("DEBUTANT", "INTERMEDIAIRE", "AVANCE");
        statutCombo.getItems().setAll("OUVERTE", "EN_COURS", "TERMINEE");
        modeCombo.getItems().setAll("PRESENTIEL", "EN_LIGNE", "HYBRIDE");

        // defaults
        categorieCombo.getSelectionModel().selectFirst();
        difficulteCombo.getSelectionModel().selectFirst();
        statutCombo.getSelectionModel().selectFirst();
        modeCombo.getSelectionModel().selectFirst();

        // default title
        if (titleLabel != null) titleLabel.setText("Ajouter Formation");

        // hide error by default
        if (errorLabel != null) errorLabel.setText("");
    }

    public void setOnSaved(Runnable r) {
        this.onSaved = r;
    }

    public void setOnCancel(Runnable r) {   // ✅ NEW
        this.onCancel = r;
    }

    public void setFormation(Formation f) {
        this.editing = f;

        if (titleLabel != null) titleLabel.setText("Modifier Formation");

        nomField.setText(nz(f.getNom()));
        descriptionArea.setText(nz(f.getDescription()));
        contenuArea.setText(nz(f.getContenu()));

        dateDebutPicker.setValue(f.getDateDebut());
        dateFinPicker.setValue(f.getDateFin());

        categorieCombo.setValue(nzNull(f.getCategorie()));
        difficulteCombo.setValue(nzNull(f.getDifficulte()));
        statutCombo.setValue(nzNull(f.getStatut()));
        modeCombo.setValue(nzNull(f.getMode()));

        lieuField.setText(nz(f.getLieu()));
        formateurField.setText(nz(f.getFormateur()));
        prerequisArea.setText(nz(f.getPrerequis()));

        if (f.getCapaciteMax() > 0) capaciteField.setText(String.valueOf(f.getCapaciteMax()));
        else capaciteField.setText("");
    }

    @FXML
    private void onSave() {
        if (errorLabel != null) errorLabel.setText("");

        String nom = nz(nomField.getText());
        if (nom.isEmpty()) {
            setError("Nom obligatoire.");
            return;
        }

        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            setError("Dates obligatoires.");
            return;
        }

        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            setError("Date fin doit être après date début.");
            return;
        }

        int cap = 0;
        try {
            String capTxt = nz(capaciteField.getText());
            if (!capTxt.isEmpty()) cap = Integer.parseInt(capTxt);
            if (cap < 0) {
                setError("Capacité invalide.");
                return;
            }
        } catch (NumberFormatException e) {
            setError("Capacité max doit être un nombre (ex: 20).");
            return;
        }

        try {
            Formation f = (editing == null) ? new Formation() : editing;

            f.setNom(nom);
            f.setDescription(nz(descriptionArea.getText()));
            f.setDateDebut(dateDebutPicker.getValue());
            f.setDateFin(dateFinPicker.getValue());
            f.setContenu(nz(contenuArea.getText()));

            f.setCategorie(categorieCombo.getValue());
            f.setDifficulte(difficulteCombo.getValue());
            f.setStatut(statutCombo.getValue());

            f.setMode(modeCombo.getValue());
            f.setLieu(nz(lieuField.getText()));
            f.setFormateur(nz(formateurField.getText()));
            f.setPrerequis(nz(prerequisArea.getText()));
            f.setCapaciteMax(cap);

            if (editing == null) dao.addFormation(f);
            else dao.updateFormation(f);

            if (onSaved != null) onSaved.run();

        } catch (Exception e) {
            setError("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        if (onCancel != null) onCancel.run();
    }

    private void setError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
    }

    private String nz(String s) { return s == null ? "" : s.trim(); }
    private String nzNull(String s) { return (s == null || s.isBlank()) ? null : s; }
}