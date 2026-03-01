package com.pi.controllers;

import com.pi.models.EvenementRh;
import com.pi.services.FeedbackService;
import com.pi.services.EvenementService;
import com.pi.services.ParticipationService;
import com.pi.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class FeedbackController implements Initializable {

    @FXML private ComboBox<EvenementRh> evenementCombo;
    @FXML private ListView<String> feedbackListView;
    @FXML private Label noteMoyenneLabel;
    @FXML private Label totalFeedbacksLabel;
    @FXML private ProgressBar note1Bar;
    @FXML private ProgressBar note2Bar;
    @FXML private ProgressBar note3Bar;
    @FXML private ProgressBar note4Bar;
    @FXML private ProgressBar note5Bar;
    @FXML private Label note1Count;
    @FXML private Label note2Count;
    @FXML private Label note3Count;
    @FXML private Label note4Count;
    @FXML private Label note5Count;
    @FXML private ListView<String> topEvenementsList;

    private FeedbackService feedbackService;
    private EvenementService evenementService;
    private ParticipationService participationService;
    private ObservableList<EvenementRh> evenementList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        feedbackService = new FeedbackService();
        evenementService = new EvenementService();
        participationService = new ParticipationService();
        evenementList = FXCollections.observableArrayList();

        chargerEvenements();

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

        evenementCombo.setOnAction(event -> {
            EvenementRh selected = evenementCombo.getSelectionModel().getSelectedItem();
            if (selected != null) {
                chargerFeedbacks(selected.getIdEvent());
            }
        });

        chargerTopEvenements();
    }

    private void chargerEvenements() {
        try {
            List<EvenementRh> evenements = evenementService.getAllEvenements();
            evenementList.setAll(evenements);
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    private void chargerFeedbacks(int idEvent) {
        try {
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
            feedbackListView.setItems(items);

            double moyenne = feedbackService.getNoteMoyenne(idEvent);
            noteMoyenneLabel.setText(String.format("%.1f / 5", moyenne));
            totalFeedbacksLabel.setText(String.valueOf(feedbacks.size()));

            int[] distribution = feedbackService.getDistributionNotes(idEvent);
            int total = feedbacks.size();

            miseAJourBarres(distribution, total);

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les feedbacks: " + e.getMessage());
        }
    }

    private void miseAJourBarres(int[] distribution, int total) {
        if (total == 0) return;

        note1Bar.setProgress(distribution[0] / (double) total);
        note2Bar.setProgress(distribution[1] / (double) total);
        note3Bar.setProgress(distribution[2] / (double) total);
        note4Bar.setProgress(distribution[3] / (double) total);
        note5Bar.setProgress(distribution[4] / (double) total);

        note1Count.setText(String.valueOf(distribution[0]));
        note2Count.setText(String.valueOf(distribution[1]));
        note3Count.setText(String.valueOf(distribution[2]));
        note4Count.setText(String.valueOf(distribution[3]));
        note5Count.setText(String.valueOf(distribution[4]));
    }

    private void chargerTopEvenements() {
        try {
            List<EvenementRh> top = feedbackService.getTopEvenements(5);
            ObservableList<String> items = FXCollections.observableArrayList();
            for (EvenementRh e : top) {
                double moyenne = feedbackService.getNoteMoyenne(e.getIdEvent());
                items.add(String.format("%s - %.1f ⭐", e.getTitre(), moyenne));
            }
            topEvenementsList.setItems(items);
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger le top: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterFeedback() {
        Dialog<Feedback> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un feedback");
        dialog.setHeaderText("Donnez votre avis sur l'événement");

        ButtonType ajouterButton = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ajouterButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<EvenementRh> eventCombo = new ComboBox<>(evenementList);
        eventCombo.setConverter(evenementCombo.getConverter());

        TextField participationField = new TextField();
        participationField.setPromptText("ID Participation");

        ComboBox<Integer> noteCombo = new ComboBox<>();
        noteCombo.getItems().addAll(1, 2, 3, 4, 5);
        noteCombo.setValue(5);

        TextArea commentaireArea = new TextArea();
        commentaireArea.setPromptText("Votre commentaire...");
        commentaireArea.setPrefRowCount(3);

        CheckBox recommandeCheck = new CheckBox("Je recommanderais cet événement");
        recommandeCheck.setSelected(true);

        grid.add(new Label("Événement:"), 0, 0);
        grid.add(eventCombo, 1, 0);
        grid.add(new Label("ID Participation:"), 0, 1);
        grid.add(participationField, 1, 1);
        grid.add(new Label("Note:"), 0, 2);
        grid.add(noteCombo, 1, 2);
        grid.add(new Label("Commentaire:"), 0, 3);
        grid.add(commentaireArea, 1, 3);
        grid.add(recommandeCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ajouterButton) {
                try {
                    int idParticipation = Integer.parseInt(participationField.getText());
                    int note = noteCombo.getValue();
                    String commentaire = commentaireArea.getText();
                    boolean recommande = recommandeCheck.isSelected();

                    return new Feedback(idParticipation, note, commentaire, recommande);
                } catch (NumberFormatException e) {
                    AlertUtil.showError("Erreur", "ID Participation invalide");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(feedback -> {
            try {
                feedbackService.ajouterFeedback(
                        feedback.getIdParticipation(),
                        feedback.getNote(),
                        feedback.getCommentaire(),
                        feedback.isRecommanderait()
                );
                AlertUtil.showInfo("Succès", "Feedback ajouté avec succès!");

                EvenementRh selected = evenementCombo.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    chargerFeedbacks(selected.getIdEvent());
                }
                chargerTopEvenements();

            } catch (SQLException e) {
                AlertUtil.showError("Erreur", e.getMessage());
            }
        });
    }

    // Classe interne Feedback (si pas importée)
    public static class Feedback {
        private int idParticipation;
        private int note;
        private String commentaire;
        private boolean recommanderait;

        public Feedback(int idParticipation, int note, String commentaire, boolean recommanderait) {
            this.idParticipation = idParticipation;
            this.note = note;
            this.commentaire = commentaire;
            this.recommanderait = recommanderait;
        }

        public int getIdParticipation() { return idParticipation; }
        public int getNote() { return note; }
        public String getCommentaire() { return commentaire; }
        public boolean isRecommanderait() { return recommanderait; }
    }
}