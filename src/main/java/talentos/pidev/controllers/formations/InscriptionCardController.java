package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import talentos.pidev.dao.InscriptionDAO;
import talentos.pidev.models.Inscription;

public class InscriptionCardController {

    @FXML private Label nomLabel;
    @FXML private Label emailLabel;
    @FXML private Label statutLabel;
    @FXML private Label scoreLabel;

    private Inscription inscription;
    private Runnable onChanged;

    private final InscriptionDAO dao = new InscriptionDAO();

    public void setData(Inscription i) {
        this.inscription = i;

        nomLabel.setText(i.getCandidatNom());
        emailLabel.setText(i.getCandidatEmail());

        statutLabel.setText(i.getStatut() == null ? "EN_ATTENTE" : i.getStatut());
        // badges selon statut (option)
        statutLabel.getStyleClass().removeAll("badge-green","badge-blue","badge-purple","badge-gray");
        statutLabel.getStyleClass().add("badge");
        statutLabel.getStyleClass().add(
                "TERMINEE".equalsIgnoreCase(statutLabel.getText()) ? "badge-green" : "badge-gray"
        );

        scoreLabel.setText(i.getScoreQuiz() == null ? "Score: -" : "Score: " + i.getScoreQuiz());
    }

    public void setOnChanged(Runnable r) {
        this.onChanged = r;
    }

    @FXML
    private void onDelete() {
        try {
            dao.delete(inscription.getId());
            if (onChanged != null) onChanged.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
