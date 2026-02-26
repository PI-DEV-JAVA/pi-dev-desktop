package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import talentos.pidev.dao.InscriptionDAO;
import talentos.pidev.models.Inscription;

public class InscriptionFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;

    private int formationId;
    private final InscriptionDAO dao = new InscriptionDAO();

    private Runnable onSaved;
    private Runnable onCancel; // ✅ NEW

    public void setFormation(int formationId, String formationNom) {
        this.formationId = formationId;
        if (titleLabel != null) titleLabel.setText("Inscription à : " + formationNom);
        if (errorLabel != null) errorLabel.setText("");
        errorLabel.managedProperty().bind(errorLabel.textProperty().isNotEmpty());
        errorLabel.visibleProperty().bind(errorLabel.textProperty().isNotEmpty());
    }

    public void setOnSaved(Runnable r) { this.onSaved = r; }
    public void setOnCancel(Runnable r) { this.onCancel = r; } // ✅ NEW

    @FXML
    private void onSubmit() {
        if (errorLabel != null) errorLabel.setText("");

        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();

        if (nom.isEmpty() || email.isEmpty()) {
            setError("Nom et email obligatoires.");
            return;
        }

        // simple email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            setError("Email invalide.");
            return;
        }

        try {
            Inscription i = new Inscription();
            i.setFormationId(formationId);
            i.setCandidatNom(nom);
            i.setCandidatEmail(email);

            // ✅ keep DB-friendly value
            i.setStatut("EN_ATTENTE");

            dao.addInscription(i);

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
}