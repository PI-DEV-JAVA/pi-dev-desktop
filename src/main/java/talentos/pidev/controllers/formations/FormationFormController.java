package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import talentos.pidev.dao.FormationDAO;
import talentos.pidev.models.Formation;

public class FormationFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextArea descArea;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> difficulteCombo;
    @FXML private TextField categorieField;
    @FXML private TextField formateurField;
    @FXML private TextField statutField;
    @FXML private Label errorLabel;

    private final FormationDAO dao = new FormationDAO();
    private Formation editing = null;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        difficulteCombo.getItems().setAll("DEBUTANT","INTERMEDIAIRE","AVANCE");
        difficulteCombo.getSelectionModel().selectFirst();
        statutField.setText("OUVERTE");
    }

    public void setOnSaved(Runnable r) {
        this.onSaved = r;
    }

    public void setEditMode(Formation f) {
        this.editing = f;
        titleLabel.setText("Modifier formation");

        nomField.setText(f.getNom());
        descArea.setText(f.getDescription());
        dateDebutPicker.setValue(f.getDateDebut());
        dateFinPicker.setValue(f.getDateFin());
        difficulteCombo.setValue(f.getDifficulte());
        categorieField.setText(f.getCategorie());
        formateurField.setText(f.getFormateur());
        statutField.setText(f.getStatut());
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");

        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        if (nom.isEmpty()) { errorLabel.setText("Nom obligatoire."); return; }
        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            errorLabel.setText("Dates obligatoires."); return;
        }

        try {
            Formation f = (editing == null) ? new Formation() : editing;

            f.setNom(nom);
            f.setDescription(descArea.getText());
            f.setDateDebut(dateDebutPicker.getValue());
            f.setDateFin(dateFinPicker.getValue());
            f.setDifficulte(difficulteCombo.getValue());
            f.setCategorie(categorieField.getText());
            f.setFormateur(formateurField.getText());
            f.setStatut(statutField.getText());

            if (editing == null) dao.addFormation(f);
            else dao.updateFormation(f);

            if (onSaved != null) onSaved.run();
            close();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
