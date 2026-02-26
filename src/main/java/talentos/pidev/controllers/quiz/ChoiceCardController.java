package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentos.pidev.models.Choix;
import talentos.pidev.services.QuizService;

import java.sql.SQLException;
import java.util.Optional;

public class ChoiceCardController {

    @FXML private Label texteLabel;
    @FXML private CheckBox correctCheck;

    private Choix choix;
    private ChoicesRHController parent;

    private final QuizService quizService = new QuizService();
    private boolean lock = false;

    public void setData(Choix c, ChoicesRHController parent) {
        this.choix = c;
        this.parent = parent;

        texteLabel.setText(c.getTexte());
        correctCheck.setSelected(c.isEstCorrect());

        correctCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            if (lock) return;
            if (!newV) { // on ne permet pas décocher sans choisir un autre
                lock = true;
                correctCheck.setSelected(true);
                lock = false;
                return;
            }
            try {
                quizService.setChoiceCorrect(c.getId(), c.getQuestionId());
                if (parent != null) parent.reload();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).show();
            }
        });
    }

    @FXML
    private void onEdit() {
        if (parent != null) parent.openForm(choix);
    }

    @FXML
    private void onDelete() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Suppression");
        a.setHeaderText("Supprimer ce choix ?");
        a.setContentText(choix.getTexte());

        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                quizService.deleteChoice(choix.getId());
                if (parent != null) parent.reload();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur suppression: " + e.getMessage()).show();
            }
        }
    }
}