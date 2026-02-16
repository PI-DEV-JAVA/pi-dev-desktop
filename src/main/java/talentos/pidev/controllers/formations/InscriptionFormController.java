package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import talentos.pidev.dao.InscriptionDAO;
import talentos.pidev.models.Inscription;

public class InscriptionFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;

    private int formationId;
    private final InscriptionDAO dao = new InscriptionDAO();

    public void setFormation(int formationId, String formationNom) {
        this.formationId = formationId;
        titleLabel.setText("Inscription à : " + formationNom);
    }

    @FXML
    private void onSubmit() {
        errorLabel.setText("");

        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();

        if (nom.isEmpty() || email.isEmpty()) {
            errorLabel.setText("Nom et email obligatoires.");
            return;
        }

        try {
            Inscription i = new Inscription();
            i.setFormationId(formationId);
            i.setCandidatNom(nom);
            i.setCandidatEmail(email);
            i.setStatut("EN_ATTENTE");

            dao.addInscription(i);
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
