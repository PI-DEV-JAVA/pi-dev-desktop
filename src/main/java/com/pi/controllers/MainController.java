package com.pi.controllers;

import com.pi.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private void openEvenementGestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/views/evenement/evenement_gestion.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gestion des Événements");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }

    @FXML
    private void openEvenementListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/views/evenement/evenement_liste.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Événements");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }

    @FXML
    private void openParticipationGestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/views/participation/participation_gestion.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gestion des Participations");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }

    @FXML
    private void openParticipationListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/views/participation/participation_liste.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Participations");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }

    @FXML
    private void openPresence() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/views/presence/presence.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gestion des Présences");
            stage.setScene(new Scene(root, 900, 700));
            stage.show();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }

    @FXML
    private void openCalendrier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/views/calendrier/calendrier.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Calendrier des Événements");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le calendrier: " + e.getMessage());
        }
    }

    @FXML
    private void openFeedback() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/views/feedback/feedback.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Satisfaction et Feedback");
            stage.setScene(new Scene(root, 900, 700));
            stage.show();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }

    @FXML
    private void quit() {
        boolean confirm = AlertUtil.showConfirmation("Confirmation", "Voulez-vous vraiment quitter ?");
        if (confirm) {
            System.exit(0);
        }
    }
}