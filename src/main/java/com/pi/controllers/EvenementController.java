package com.pi.controllers;

import com.pi.models.EvenementRh;
import com.pi.services.EvenementService;
import com.pi.utils.AlertUtil;
import com.pi.utils.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class EvenementController implements Initializable {

    // Pour la gestion (formulaire)
    @FXML private TextField idField;
    @FXML private TextField titreField;
    @FXML private TextField typeField;
    @FXML private DatePicker datePicker;
    @FXML private TextField lieuField;
    @FXML private TextField statutField;

    // Pour la liste
    @FXML private TextField rechercheField;
    @FXML private TextField detailField;
    @FXML private ListView<String> listViewEvenements;

    private EvenementService evenementService;
    private ObservableList<String> evenementsAffiches;
    private List<EvenementRh> evenementsComplets;
    private EvenementRh evenementEnCours;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        evenementService = new EvenementService();
        evenementsAffiches = FXCollections.observableArrayList();
        evenementEnCours = null;

        // Initialisation de la liste si elle existe
        if (listViewEvenements != null) {
            listViewEvenements.setItems(evenementsAffiches);
            listViewEvenements.setOnMouseClicked(this::afficherDetail);
            chargerListe();
        }
    }

    // ==================== MÉTHODES POUR LA GESTION ====================

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
            AlertUtil.showInfo("Succès", "Événement ajouté avec succès!");

            if (listViewEvenements != null) {
                chargerListe();
            }

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void modifierEvenement() {
        if (evenementEnCours == null) {
            AlertUtil.showWarning("Attention", "Recherchez d'abord un événement à modifier avec l'ID.");
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
            AlertUtil.showInfo("Succès", "Événement modifié avec succès!");

            if (listViewEvenements != null) {
                chargerListe();
            }

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerEvenement() {
        if (evenementEnCours == null) {
            AlertUtil.showWarning("Attention", "Recherchez d'abord un événement à supprimer avec l'ID.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirmation", "Supprimer cet événement ?");

        if (confirm) {
            try {
                evenementService.supprimerEvenement(evenementEnCours.getIdEvent());
                effacerFormulaire();
                AlertUtil.showInfo("Succès", "Événement supprimé!");

                if (listViewEvenements != null) {
                    chargerListe();
                }

            } catch (SQLException e) {
                AlertUtil.showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechercherParId() {
        String idText = idField.getText();
        if (idText.isEmpty()) {
            AlertUtil.showWarning("Attention", "Entrez un ID à rechercher.");
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            EvenementRh event = evenementService.getEvenementById(id);

            if (event == null) {
                AlertUtil.showWarning("Non trouvé", "Aucun événement avec l'ID " + id);
                effacerFormulaire();
            } else {
                remplirFormulaire(event);
                evenementEnCours = event;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "L'ID doit être un nombre.");
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    @FXML
    private void effacerFormulaire() {
        idField.clear();
        titreField.clear();
        typeField.clear();
        datePicker.setValue(null);
        lieuField.clear();
        statutField.clear();
        evenementEnCours = null;
    }

    private void remplirFormulaire(EvenementRh event) {
        idField.setText(String.valueOf(event.getIdEvent()));
        titreField.setText(event.getTitre());
        typeField.setText(event.getTypeEvent());
        datePicker.setValue(event.getDateEvent());
        lieuField.setText(event.getLieu());
        statutField.setText(event.getStatut());
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

    // ==================== MÉTHODES POUR LA LISTE ====================

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

    private void chargerListe() {
        try {
            evenementsComplets = evenementService.getAllEvenements();
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    private void mettreAJourListe() {
        evenementsAffiches.clear();
        for (EvenementRh e : evenementsComplets) {
            String affichage = String.format("%d - %s (%s) - %s",
                    e.getIdEvent(), e.getTitre(), e.getTypeEvent(), e.getDateEvent());
            evenementsAffiches.add(affichage);
        }
    }

    private void afficherDetail(MouseEvent event) {
        int index = listViewEvenements.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < evenementsComplets.size()) {
            EvenementRh e = evenementsComplets.get(index);
            String details = String.format(
                    "ID: %d\nTitre: %s\nType: %s\nDate: %s\nLieu: %s\nStatut: %s",
                    e.getIdEvent(), e.getTitre(), e.getTypeEvent(),
                    e.getDateEvent(), e.getLieu(), e.getStatut()
            );
            detailField.setText(details);
        }
    }
}