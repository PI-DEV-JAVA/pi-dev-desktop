package com.pi.controllers;

import com.pi.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainController {

    @FXML private BorderPane mainBorderPane;

    // Boutons de la sidebar
    @FXML private Button btnEvents;
    @FXML private Button btnParticipations;
    @FXML private Button btnPresences;
    @FXML private Button btnCalendrier;
    @FXML private Button btnFeedback;

    private List<Button> sidebarButtons;

    @FXML
    public void initialize() {
        // Liste des boutons pour faciliter la gestion du style
        sidebarButtons = Arrays.asList(btnEvents, btnParticipations, btnPresences, btnCalendrier, btnFeedback);

        // Activer Events par défaut
        setActiveButton(btnEvents);
    }

    private void setActiveButton(Button activeButton) {
        // Reset tous les boutons
        for (Button btn : sidebarButtons) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-size: 13px; -fx-font-weight: 600; -fx-alignment: CENTER_LEFT; -fx-padding: 12 20; -fx-cursor: hand;");
        }

        // Style pour le bouton actif
        activeButton.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-font-size: 13px; -fx-font-weight: 700; -fx-alignment: CENTER_LEFT; -fx-padding: 12 20; -fx-cursor: hand; -fx-border-color: transparent transparent transparent #6366f1; -fx-border-width: 0 0 0 3px;");
    }

    @FXML
    private void openEvenementGestion() {
        setActiveButton(btnEvents);
        loadPage("/com/pi/views/evenement/evenement_gestion.fxml");
    }

    @FXML
    private void openEvenementListe() {
        setActiveButton(btnEvents);
        loadPage("/com/pi/views/evenement/evenement_liste.fxml");
    }

    @FXML
    private void openParticipationGestion() {
        setActiveButton(btnParticipations);
        loadPage("/com/pi/views/participation/participation_gestion.fxml");
    }

    @FXML
    private void openParticipationListe() {
        setActiveButton(btnParticipations);
        loadPage("/com/pi/views/participation/participation_liste.fxml");
    }

    @FXML
    private void openPresence() {
        setActiveButton(btnPresences);
        loadPage("/com/pi/views/presence/presence.fxml");
    }

    @FXML
    private void openCalendrier() {
        setActiveButton(btnCalendrier);
        loadPage("/com/pi/views/calendrier/calendrier.fxml");
    }

    @FXML
    private void openFeedback() {
        setActiveButton(btnFeedback);
        loadPage("/com/pi/views/feedback/feedback.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            // Passer la référence du MainController aux sous-controllers
            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setMainController(this);
            }

            mainBorderPane.setCenter(page);
        } catch (IOException e) {
            AlertUtil.showError("Erreur", "Impossible de charger la page: " + e.getMessage());
            e.printStackTrace();
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