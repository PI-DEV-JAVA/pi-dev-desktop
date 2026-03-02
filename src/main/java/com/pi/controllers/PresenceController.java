package com.pi.controllers;

import com.pi.models.Presence;
import com.pi.models.Participation;
import com.pi.services.PresenceService;
import com.pi.services.ParticipationService;
import com.pi.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PresenceController implements Initializable, BaseController {

    @FXML private TextField idParticipationField;
    @FXML private TextField scannerField;
    @FXML private Label statutLabel;
    @FXML private ImageView qrImageView;
    @FXML private Label presentsCountLabel;
    @FXML private Label tauxPresenceLabel;
    @FXML private TextField eventIdField;

    private PresenceService presenceService;
    private ParticipationService participationService;
    private MainController mainController;
    private int idEventActuel;
    private int totalInscrits;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        presenceService = new PresenceService();
        participationService = new ParticipationService();
        statutLabel.setText("En attente de scan");
    }

    @FXML
    private void genererQRCode() {
        String idText = idParticipationField.getText();
        if (idText.isEmpty()) {
            AlertUtil.showWarning("Attention", "Entrez un ID de participation.");
            return;
        }

        try {
            int idParticipation = Integer.parseInt(idText);

            Participation participation = participationService.getParticipationById(idParticipation);
            if (participation == null) {
                AlertUtil.showError("Erreur", "Participation inexistante.");
                return;
            }

            byte[] qrImage = presenceService.genererQRCode(idParticipation);

            Presence presence = new Presence();
            presence.setIdParticipation(idParticipation);
            presence.setEstPresent(false);
            presence.setDateScan(null);
            presence.setCodeQr("PARTICIPATION:" + idParticipation);

            presenceService.creerPresence(presence);

            Image image = new Image(new ByteArrayInputStream(qrImage));
            qrImageView.setImage(image);

            AlertUtil.showInfo("Succès", "QR code généré avec succès!");

        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "L'ID doit être un nombre.");
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void scannerPresence() {
        String code = scannerField.getText();
        if (code.isEmpty()) {
            AlertUtil.showWarning("Attention", "Scannez ou entrez le code QR.");
            return;
        }

        try {
            boolean success = presenceService.scannerPresence(code);
            if (success) {
                statutLabel.setText("✅ PRÉSENT");
                statutLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                AlertUtil.showInfo("Succès", "Présence enregistrée!");

                updateStats();
                scannerField.clear();
            } else {
                AlertUtil.showError("Erreur", "Code QR invalide.");
            }
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void chargerEvenement() {
        String idText = eventIdField.getText();
        if (idText.isEmpty()) {
            AlertUtil.showWarning("Attention", "Entrez un ID d'événement.");
            return;
        }

        try {
            idEventActuel = Integer.parseInt(idText);
            updateStats();
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "ID invalide.");
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    private void updateStats() throws SQLException {
        int presents = presenceService.compterPresents(idEventActuel);
        totalInscrits = participationService.compterParticipants(idEventActuel);

        presentsCountLabel.setText(String.valueOf(presents));

        double taux = 0;
        if (totalInscrits > 0) {
            taux = (double) presents / totalInscrits * 100;
        }
        tauxPresenceLabel.setText(String.format("%.1f%%", taux));
    }

    @FXML
    private void sauvegarderQRCode() {
        if (qrImageView.getImage() == null) {
            AlertUtil.showWarning("Attention", "Générez d'abord un QR code.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder QR Code");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG files", "*.png")
        );

        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            AlertUtil.showInfo("Succès", "QR code sauvegardé!");
        }
    }
}