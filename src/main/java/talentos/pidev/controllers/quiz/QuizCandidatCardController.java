package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import talentos.pidev.models.Quiz;

public class QuizCandidatCardController {

    @FXML private Label lbTitre;
    @FXML private Label lbDesc;
    @FXML private Label lbDuree;

    private Quiz quiz;
    private QuizCandidatController parent;

    public void setData(Quiz quiz, QuizCandidatController parent) {
        this.quiz = quiz;
        this.parent = parent;

        lbTitre.setText(quiz.getTitre());
        lbDesc.setText(quiz.getDescription() == null ? "" : quiz.getDescription());
        lbDuree.setText("Durée: " + quiz.getDureeMinutes() + " min");
    }

    @FXML
    private void onPasser() {
        if (parent != null) parent.openPassage(quiz);
    }
}