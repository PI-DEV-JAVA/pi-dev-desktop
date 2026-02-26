package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import talentos.pidev.dao.InscriptionDAO;
import talentos.pidev.models.Inscription;

public class InscriptionCardController {

    @FXML private Label nomLabel;
    @FXML private Label emailLabel;
    @FXML private Label statutLabel;

    @FXML private Button acceptBtn;
    @FXML private Button rejectBtn;
    @FXML private Button deleteBtn;

    private Inscription inscription;
    private Runnable onChanged;

    private final InscriptionDAO dao = new InscriptionDAO();

    public void setData(Inscription i) {
        this.inscription = i;

        nomLabel.setText(i.getCandidatNom());
        emailLabel.setText(i.getCandidatEmail());

        String st = (i.getStatut() == null || i.getStatut().isBlank()) ? "EN_ATTENTE" : i.getStatut();
        statutLabel.setText(st);
        applyBadge(st);

        boolean pending = st.equalsIgnoreCase("EN_ATTENTE");
        acceptBtn.setDisable(!pending);
        rejectBtn.setDisable(!pending);
    }

    public void setOnChanged(Runnable r) {
        this.onChanged = r;
    }

    private void applyBadge(String st) {
        statutLabel.getStyleClass().removeAll("badge-green", "badge-blue", "badge-gray", "badge-purple");
        if (!statutLabel.getStyleClass().contains("badge")) statutLabel.getStyleClass().add("badge");

        String s = st == null ? "" : st.toUpperCase();

        if (s.contains("ACCEP")) statutLabel.getStyleClass().add("badge-green");
        else if (s.contains("REFUS")) statutLabel.getStyleClass().add("badge-purple");
        else statutLabel.getStyleClass().add("badge-gray");
    }

    @FXML
    private void onAccept() {
        updateStatut("ACCEPTEE");
    }

    @FXML
    private void onReject() {
        updateStatut("REFUSEE");
    }

    private void updateStatut(String newStatut) {
        try {
            dao.updateStatutInscription(inscription.getId(), newStatut);
            if (onChanged != null) onChanged.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer cette inscription ?");
            alert.setContentText(inscription.getCandidatNom() + " - " + inscription.getCandidatEmail());

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                dao.deleteInscription(inscription.getId());
                if (onChanged != null) onChanged.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
