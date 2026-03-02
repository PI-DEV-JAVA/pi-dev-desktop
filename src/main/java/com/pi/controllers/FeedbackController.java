package com.pi.controllers;

import com.pi.models.EvenementRh;
import com.pi.services.FeedbackService;
import com.pi.services.EvenementService;
import com.pi.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class FeedbackController implements Initializable, BaseController {

    @FXML private ComboBox<EvenementRh> evenementCombo;
    @FXML private ListView<String> commentairesListView;
    @FXML private Label noteMoyenneLabel;
    @FXML private Label totalFeedbacksLabel;
    @FXML private TextField idParticipationField;
    @FXML private ComboBox<Integer> noteCombo;
    @FXML private TextArea commentaireField;
    @FXML private CheckBox recommandeCheck;

    private FeedbackService feedbackService;
    private EvenementService evenementService;
    private ObservableList<EvenementRh> evenementList;
    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        feedbackService = new FeedbackService();
        evenementService = new EvenementService();
        evenementList = FXCollections.observableArrayList();

        if (noteCombo != null) {
            noteCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
            noteCombo.setValue(5);
        }

        chargerEvenements();

        if (evenementCombo != null) {
            evenementCombo.setItems(evenementList);
            evenementCombo.setConverter(new javafx.util.StringConverter<EvenementRh>() {
                @Override
                public String toString(EvenementRh e) {
                    return e == null ? "" : e.getTitre() + " (" + e.getDateEvent() + ")";
                }

                @Override
                public EvenementRh fromString(String string) {
                    return null;
                }
            });
        }
    }

    private void chargerEvenements() {
        try {
            List<EvenementRh> evenements = evenementService.getAllEvenements();
            evenementList.setAll(evenements);
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    @FXML
    private void chargerFeedbacks() {
        EvenementRh selected = evenementCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Attention", "Veuillez sélectionner un événement.");
            return;
        }

        try {
            int idEvent = selected.getIdEvent();
            List<FeedbackService.FeedbackAvecDetails> feedbacks =
                    feedbackService.getFeedbacksAvecDetails(idEvent);

            ObservableList<String> items = FXCollections.observableArrayList();
            for (FeedbackService.FeedbackAvecDetails f : feedbacks) {
                String etoiles = "⭐".repeat(f.getFeedback().getNote());
                String texte = String.format("Utilisateur %d: %s\n   %s\n   %s",
                        f.getIdUser(),
                        etoiles,
                        f.getFeedback().getCommentaire(),
                        f.getFeedback().isRecommanderait() ? "👍 Recommande" : "👎 Ne recommande pas"
                );
                items.add(texte);
            }
            commentairesListView.setItems(items);

            double moyenne = feedbackService.getNoteMoyenne(idEvent);
            noteMoyenneLabel.setText(String.format("%.1f", moyenne));
            totalFeedbacksLabel.setText(String.valueOf(feedbacks.size()));

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les feedbacks: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterFeedback() {
        String idText = idParticipationField.getText();
        if (idText.isEmpty()) {
            AlertUtil.showWarning("Attention", "Veuillez entrer un ID de participation.");
            return;
        }

        try {
            int idParticipation = Integer.parseInt(idText);
            int note = noteCombo.getValue();
            String commentaire = commentaireField.getText();
            boolean recommande = recommandeCheck.isSelected();

            feedbackService.ajouterFeedback(idParticipation, note, commentaire, recommande);

            AlertUtil.showInfo("Succès", "Feedback ajouté avec succès!");

            idParticipationField.clear();
            commentaireField.clear();
            noteCombo.setValue(5);
            recommandeCheck.setSelected(true);

            if (evenementCombo.getValue() != null) {
                chargerFeedbacks();
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "L'ID de participation doit être un nombre.");
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void effacerFormulaire() {
        idParticipationField.clear();
        commentaireField.clear();
        noteCombo.setValue(5);
        recommandeCheck.setSelected(true);
    }
}