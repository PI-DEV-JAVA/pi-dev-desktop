package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import talentos.pidev.models.Quiz;
import talentos.pidev.services.QuizService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class QuizCardController {

    @FXML private Label titreLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dureeLabel;
    @FXML private Label actifLabel;
    @FXML private Label dateLabel;

    private Quiz quiz;
    private QuizRHController parent;

    private final QuizService quizService = new QuizService();

    public void setData(Quiz q, QuizRHController parent) {
        this.quiz = q;
        this.parent = parent;

        if (titreLabel != null) titreLabel.setText(q.getTitre());
        if (descriptionLabel != null) descriptionLabel.setText(q.getDescription() == null ? "" : q.getDescription());
        if (dureeLabel != null) dureeLabel.setText(q.getDureeMinutes() + " min");

        // ✅ NEW: dynamic badge using your theme classes
        if (actifLabel != null) {
            // remove any previous status-* classes (in case cards get reused)
            actifLabel.getStyleClass().removeIf(c ->
                    c.startsWith("status-") || c.equals("badge") || c.startsWith("badge-")
            );

            // always keep base badge class
            if (!actifLabel.getStyleClass().contains("status-badge")) {
                actifLabel.getStyleClass().add("status-badge");
            }

            if (q.isActif()) {
                actifLabel.setText("ACTIF");
                actifLabel.getStyleClass().add("status-open");   // green
            } else {
                actifLabel.setText("INACTIF");
                actifLabel.getStyleClass().add("status-closed"); // red
            }
        }

        if (dateLabel != null) {
            if (q.getDateCreation() != null) {
                dateLabel.setText(q.getDateCreation().format(DateTimeFormatter.ISO_DATE));
            } else {
                dateLabel.setText("");
            }
        }
    }

    @FXML
    private void onQuestions() {
        if (parent != null) parent.openQuestionsForQuiz(quiz);
    }

    @FXML
    private void onEdit() {
        if (parent != null) parent.openForm(quiz);
    }

    @FXML
    private void onDelete() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Suppression");
        a.setHeaderText("Supprimer le quiz ?");
        a.setContentText(quiz != null ? quiz.getTitre() : "");

        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                quizService.delete(quiz.getId());
                if (parent != null) parent.reload();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur suppression: " + e.getMessage()).show();
            }
        }
    }
}