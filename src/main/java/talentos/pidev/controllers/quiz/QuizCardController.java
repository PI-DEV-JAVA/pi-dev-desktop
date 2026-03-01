package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentos.pidev.dao.QuestionDAO;
import talentos.pidev.models.Quiz;
import talentos.pidev.services.AutoQuizGeneratorService;
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

    // ✅ Bouton auto-génération
    @FXML private Button btnAutoGen;

    private Quiz quiz;
    private QuizRHController parent;

    private final QuizService quizService = new QuizService();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final AutoQuizGeneratorService autoService = new AutoQuizGeneratorService();

    public void setData(Quiz q, QuizRHController parent) {
        this.quiz = q;
        this.parent = parent;

        if (titreLabel != null)
            titreLabel.setText(q.getTitre());

        if (descriptionLabel != null)
            descriptionLabel.setText(q.getDescription() == null ? "" : q.getDescription());

        if (dureeLabel != null)
            dureeLabel.setText(q.getDureeMinutes() + " min");

        // ✅ Badge ACTIF / INACTIF
        if (actifLabel != null) {
            actifLabel.getStyleClass().removeIf(c -> c.startsWith("status-"));
            actifLabel.getStyleClass().add("status-badge");

            if (q.isActif()) {
                actifLabel.setText("ACTIF");
                actifLabel.getStyleClass().add("status-open");
            } else {
                actifLabel.setText("INACTIF");
                actifLabel.getStyleClass().add("status-closed");
            }
        }

        if (dateLabel != null && q.getDateCreation() != null)
            dateLabel.setText(q.getDateCreation().format(DateTimeFormatter.ISO_DATE));

        updateAutoGenVisibility();
    }

    // ✅ Afficher bouton seulement si quiz vide
    private void updateAutoGenVisibility() {
        if (btnAutoGen == null || quiz == null) return;

        try {
            boolean hasContent = questionDAO.hasQuestionsAndChoices(quiz.getId());
            boolean show = !hasContent;

            btnAutoGen.setVisible(show);
            btnAutoGen.setManaged(show);

            if (show) {
                btnAutoGen.setTooltip(new Tooltip(
                        "Générer automatiquement des questions et choix avec IA"));
            }

        } catch (Exception e) {
            btnAutoGen.setVisible(false);
            btnAutoGen.setManaged(false);
        }
    }

    @FXML
    private void onQuestions() {
        if (parent != null)
            parent.openQuestionsForQuiz(quiz);
    }

    // ✅ IA génération
    @FXML
    private void onAutoGenerate() {
        if (quiz == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Génération automatique");
        confirm.setHeaderText("Générer automatiquement les questions ?");
        confirm.setContentText("Quiz : " + quiz.getTitre());

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK)
            return;

        try {
            btnAutoGen.setDisable(true);

            autoService.generateIfEmpty(quiz);

            new Alert(Alert.AlertType.INFORMATION,
                    "Questions et choix générés avec succès ✅").show();

            if (parent != null)
                parent.reload();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur génération : " + e.getMessage()).show();
        } finally {
            btnAutoGen.setDisable(false);
        }
    }

    @FXML
    private void onEdit() {
        if (parent != null)
            parent.openForm(quiz);
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
                if (parent != null)
                    parent.reload();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR,
                        "Erreur suppression: " + e.getMessage()).show();
            }
        }
    }
}