package com.pi.controllers;

import com.pi.models.EvenementRh;
import com.pi.services.EvenementService;
import com.pi.utils.AlertUtil;
import com.pi.utils.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class EvenementController implements Initializable, BaseController {

    // Pour la gestion (formulaire)
    @FXML private TextField rechercheTitreField;
    @FXML private TextField titreField;
    @FXML private TextField typeField;
    @FXML private DatePicker datePicker;
    @FXML private TextField lieuField;
    @FXML private TextField statutField;

    // Pour la liste
    @FXML private TextField rechercheField;
    @FXML private TextField detailField;
    @FXML private ListView<String> listViewEvenements;
    @FXML private Label totalEvenementsLabel;

    private EvenementService evenementService;
    private ObservableList<String> evenementsAffiches;
    private List<EvenementRh> evenementsComplets;
    private EvenementRh evenementEnCours;
    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        evenementService = new EvenementService();
        evenementsAffiches = FXCollections.observableArrayList();
        evenementEnCours = null;

        if (listViewEvenements != null) {
            listViewEvenements.setItems(evenementsAffiches);
            listViewEvenements.setOnMouseClicked(this::afficherDetail);
            chargerListe();
        }
    }

    // ==================== MÉTHODES CRUD ====================

    @FXML
    private void ajouterEvenement() {
        if (!validerFormulaire()) return;

        try {
            EvenementRh event = new EvenementRh(
                    titreField.getText(),
                    typeField.getText(),
                    datePicker.getValue(),
                    lieuField.getText(),
                    statutField.getText()
            );

            evenementService.ajouterEvenement(event);
            effacerFormulaire();
            chargerListe();
            AlertUtil.showInfo("Succès", "Événement ajouté avec succès!");

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void modifierEvenement() {
        if (evenementEnCours == null) {
            AlertUtil.showWarning("Attention", "Recherchez d'abord un événement à modifier.");
            return;
        }

        if (!validerFormulaire()) return;

        try {
            evenementEnCours.setTitre(titreField.getText());
            evenementEnCours.setTypeEvent(typeField.getText());
            evenementEnCours.setDateEvent(datePicker.getValue());
            evenementEnCours.setLieu(lieuField.getText());
            evenementEnCours.setStatut(statutField.getText());

            evenementService.modifierEvenement(evenementEnCours);
            chargerListe();
            AlertUtil.showInfo("Succès", "Événement modifié avec succès!");

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerEvenement() {
        if (evenementEnCours == null) {
            AlertUtil.showWarning("Attention", "Recherchez d'abord un événement à supprimer.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirmation", "Supprimer cet événement ?");

        if (confirm) {
            try {
                evenementService.supprimerEvenement(evenementEnCours.getIdEvent());
                effacerFormulaire();
                chargerListe();
                AlertUtil.showInfo("Succès", "Événement supprimé!");

            } catch (SQLException e) {
                AlertUtil.showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechercherParTitre() {
        String titre = rechercheTitreField.getText();
        if (titre.isEmpty()) {
            AlertUtil.showWarning("Attention", "Entrez un titre à rechercher.");
            return;
        }

        try {
            List<EvenementRh> resultats = evenementService.rechercherParTitre(titre);

            if (resultats.isEmpty()) {
                AlertUtil.showWarning("Non trouvé", "Aucun événement avec le titre '" + titre + "'");
                effacerFormulaire();
            } else {
                EvenementRh event = resultats.get(0);
                remplirFormulaire(event);
                evenementEnCours = event;

                if (resultats.size() > 1) {
                    AlertUtil.showInfo("Plusieurs résultats",
                            "Plusieurs événements correspondent. Le premier a été sélectionné.");
                }
            }
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    @FXML
    private void rechercher() {
        String recherche = rechercheField.getText().toLowerCase();
        if (recherche.isEmpty()) {
            chargerListe();
            return;
        }

        try {
            evenementsComplets = evenementService.rechercherParType(recherche);
            if (evenementsComplets.isEmpty()) {
                evenementsComplets = evenementService.rechercherParStatut(recherche);
            }
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    @FXML
    private void actualiser() {
        chargerListe();
        rechercheField.clear();
        detailField.clear();
        rechercheTitreField.clear();
    }

    @FXML
    private void afficherTous() {
        chargerListe();
        rechercheField.clear();
    }

    @FXML
    private void afficherActifs() {
        try {
            evenementsComplets = evenementService.rechercherParStatut("Actif");
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void afficherAVenir() {
        try {
            evenementsComplets = evenementService.getEvenementsAVenir();
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void afficherPasses() {
        try {
            evenementsComplets = evenementService.rechercherParStatut("Passé");
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void afficherAnnules() {
        try {
            evenementsComplets = evenementService.rechercherParStatut("Annulé");
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void partagerEvenement() {
        EvenementRh event = null;

        if (listViewEvenements != null) {
            int index = listViewEvenements.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < evenementsComplets.size()) {
                event = evenementsComplets.get(index);
            }
        }

        if (event == null && evenementEnCours != null) {
            event = evenementEnCours;
        }

        if (event == null) {
            AlertUtil.showWarning("Attention", "Veuillez sélectionner un événement à partager.");
            return;
        }

        final EvenementRh eventFinal = event;

        String message = "📅 " + eventFinal.getTitre() + "\n" +
                "📌 " + eventFinal.getTypeEvent() + "\n" +
                "📍 " + eventFinal.getLieu() + "\n" +
                "📆 " + eventFinal.getDateEvent();

        String messageEncode = message.replace("\n", "%0A").replace(" ", "%20");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Facebook",
                "Facebook",
                "LinkedIn",
                "WhatsApp",
                "Twitter");
        dialog.setTitle("Partager l'événement");
        dialog.setHeaderText("Choisissez un réseau social");
        dialog.setContentText("Partager sur:");

        dialog.showAndWait().ifPresent(reseau -> {
            try {
                String url = "";
                String baseUrl = "https://monapp.com/evenement/" + eventFinal.getIdEvent();

                switch (reseau) {
                    case "Facebook":
                        url = "https://www.facebook.com/sharer/sharer.php?u=" + baseUrl + "&quote=" + messageEncode;
                        break;
                    case "LinkedIn":
                        url = "https://www.linkedin.com/sharing/share-offsite/?url=" + baseUrl;
                        break;
                    case "WhatsApp":
                        url = "https://wa.me/?text=" + messageEncode;
                        break;
                    case "Twitter":
                        url = "https://twitter.com/intent/tweet?text=" + messageEncode + "&url=" + baseUrl;
                        break;
                }

                Desktop.getDesktop().browse(new URI(url));

            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible d'ouvrir le navigateur: " + e.getMessage());
            }
        });
    }

    private void afficherDetail(MouseEvent event) {
        int index = listViewEvenements.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < evenementsComplets.size()) {
            EvenementRh e = evenementsComplets.get(index);
            String details = String.format(
                    "Titre: %s\nType: %s\nDate: %s\nLieu: %s\nStatut: %s",
                    e.getTitre(), e.getTypeEvent(),
                    e.getDateEvent(), e.getLieu(), e.getStatut()
            );
            detailField.setText(details);
        }
    }

    @FXML
    private void effacerFormulaire() {
        rechercheTitreField.clear();
        titreField.clear();
        typeField.clear();
        datePicker.setValue(null);
        lieuField.clear();
        statutField.clear();
        evenementEnCours = null;
    }

    private void remplirFormulaire(EvenementRh event) {
        titreField.setText(event.getTitre());
        typeField.setText(event.getTypeEvent());
        datePicker.setValue(event.getDateEvent());
        lieuField.setText(event.getLieu());
        statutField.setText(event.getStatut());
    }

    private void chargerListe() {
        try {
            evenementsComplets = evenementService.getAllEvenements();
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    // MÉTHODE MODIFIÉE - SANS ID DANS L'AFFICHAGE
    private void mettreAJourListe() {
        evenementsAffiches.clear();
        for (EvenementRh e : evenementsComplets) {
            String affichage = String.format("%s - %s - %s - %s",
                    e.getTitre(),
                    e.getTypeEvent(),
                    e.getDateEvent(),
                    e.getStatut()
            );
            evenementsAffiches.add(affichage);
        }
        if (totalEvenementsLabel != null) {
            totalEvenementsLabel.setText("Total: " + evenementsAffiches.size() + " événements");
        }
    }

    private boolean validerFormulaire() {
        if (titreField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le titre est obligatoire.");
            return false;
        }
        if (typeField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le type est obligatoire.");
            return false;
        }
        if (datePicker.getValue() == null) {
            AlertUtil.showWarning("Validation", "La date est obligatoire.");
            return false;
        }
        if (lieuField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le lieu est obligatoire.");
            return false;
        }
        if (statutField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le statut est obligatoire.");
            return false;
        }
        return true;
    }
}