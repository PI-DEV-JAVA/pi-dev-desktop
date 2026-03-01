package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import talentos.pidev.controllers.MainLayoutController;
import talentos.pidev.dao.InscriptionDAO;
import talentos.pidev.models.Inscription;
import talentos.pidev.utils.SessionCandidat;

public class InscriptionFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;

    private int formationId;
    private String formationNom;

    private final InscriptionDAO dao = new InscriptionDAO();

    private Runnable onSaved;
    private Runnable onCancel;

    // ✅ NEW: for redirect
    private MainLayoutController mainLayout;

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
    }

    public void setFormation(int formationId, String formationNom) {
        this.formationId = formationId;
        this.formationNom = formationNom;

        if (titleLabel != null) titleLabel.setText("Inscription à : " + formationNom);

        if (errorLabel != null) errorLabel.setText("");
        if (errorLabel != null) {
            errorLabel.managedProperty().bind(errorLabel.textProperty().isNotEmpty());
            errorLabel.visibleProperty().bind(errorLabel.textProperty().isNotEmpty());
        }

        // ✅ Prefill test candidate
        if (nomField != null) nomField.setText(SessionCandidat.NOM);
        if (emailField != null) emailField.setText(SessionCandidat.EMAIL);
    }

    public void setOnSaved(Runnable r) { this.onSaved = r; }
    public void setOnCancel(Runnable r) { this.onCancel = r; }

    @FXML
    private void onSubmit() {
        if (errorLabel != null) errorLabel.setText("");

        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();

        if (nom.isEmpty() || email.isEmpty()) {
            setError("Nom et email obligatoires.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            setError("Email invalide.");
            return;
        }

        try {
            // ✅ avoid duplicate inscription
            if (dao.exists(email, formationId)) {
                // already registered -> open details directly
                openDetails(formationId, formationNom, email);
                return;
            }

            Inscription i = new Inscription();
            i.setFormationId(formationId);
            i.setCandidatNom(nom);
            i.setCandidatEmail(email);

            // pour tester: tu peux mettre ACCEPTEE si tu veux
            i.setStatut("EN_ATTENTE");

            dao.addInscription(i);

            if (onSaved != null) onSaved.run();

            // ✅ redirect to details page
            openDetails(formationId, formationNom, email);

        } catch (Exception e) {
            setError("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openDetails(int formationId, String formationNom, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/FormationCandidatDetails.fxml"));
            Node view = loader.load();

            FormationCandidatDetailsController c = loader.getController();
            c.setMainLayout(mainLayout);
            c.init(formationId, formationNom, email);

            if (mainLayout != null) mainLayout.setView(view);

        } catch (Exception e) {
            e.printStackTrace();
            setError("Ouverture détails: " + e.getMessage());
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