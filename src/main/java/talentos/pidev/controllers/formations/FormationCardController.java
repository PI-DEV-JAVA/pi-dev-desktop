package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import talentos.pidev.dao.FormationDAO;
import talentos.pidev.models.Formation;

public class FormationCardController {

    @FXML private Label nomLabel;
    @FXML private Label statutLabel;
    @FXML private Label categorieLabel;
    @FXML private Label datesLabel;
    @FXML private Label difficulteLabel;
    @FXML private Label formateurLabel;

    @FXML private Button inscrireBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button inscriptionsBtn;

    private Formation formation;
    private Runnable onChanged;

    private final FormationDAO formationDAO = new FormationDAO();

    public void setData(Formation f) {
        this.formation = f;

        nomLabel.setText(f.getNom() == null ? "—" : f.getNom());
        statutLabel.setText(f.getStatut() == null ? "OUVERTE" : f.getStatut());
        categorieLabel.setText(f.getCategorie() == null ? "—" : f.getCategorie());

        String d1 = (f.getDateDebut() == null) ? "?" : f.getDateDebut().toString();
        String d2 = (f.getDateFin() == null) ? "?" : f.getDateFin().toString();
        datesLabel.setText("Du " + d1 + " au " + d2);

        difficulteLabel.setText("Difficulté : " + (f.getDifficulte() == null ? "—" : f.getDifficulte()));
        formateurLabel.setText("Formateur : " + (f.getFormateur() == null ? "—" : f.getFormateur()));
    }

    public void setOnChanged(Runnable r) {
        this.onChanged = r;
    }

    public void setRHMode(boolean isRH) {
        // RH: show edit/delete/inscriptions, hide inscrire
        inscrireBtn.setVisible(!isRH);
        inscrireBtn.setManaged(!isRH);

        editBtn.setVisible(isRH);
        editBtn.setManaged(isRH);

        deleteBtn.setVisible(isRH);
        deleteBtn.setManaged(isRH);

        inscriptionsBtn.setVisible(isRH);
        inscriptionsBtn.setManaged(isRH);
    }

    @FXML
    private void onDelete() {
        try {
            formationDAO.deleteFormation(formation.getId());
            if (onChanged != null) onChanged.run();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/FormationForm.fxml"));
            Scene scene = new Scene(loader.load());

            FormationFormController controller = loader.getController();
            controller.setEditMode(formation);
            controller.setOnSaved(() -> { if (onChanged != null) onChanged.run(); });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier formation");
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            showError("FormationForm.fxml introuvable ou erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onInscrire() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/InscriptionForm.fxml"));
            Scene scene = new Scene(loader.load());

            InscriptionFormController controller = loader.getController();
            controller.setFormation(formation.getId(), formation.getNom());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Inscription");
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onInscriptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/InscriptionsRH.fxml"));
            Scene scene = new Scene(loader.load());

            InscriptionsRHController controller = loader.getController();
            controller.setFormation(formation.getId(), formation.getNom());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Inscriptions");
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Erreur");
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
