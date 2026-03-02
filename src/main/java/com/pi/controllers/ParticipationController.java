package com.pi.controllers;

import com.pi.models.Participation;
import com.pi.models.EvenementRh;
import com.pi.services.ParticipationService;
import com.pi.services.EvenementService;
import com.pi.utils.AlertUtil;
import com.pi.utils.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ParticipationController implements Initializable, BaseController {

    // Pour la gestion (formulaire)
    @FXML private TextField idField;
    @FXML private TextField rechercheIdField;
    @FXML private TextField idEventField;
    @FXML private TextField idUserField;
    @FXML private TextField statutField;

    // Pour la liste
    @FXML private TextField rechercheField;
    @FXML private TextField detailField;
    @FXML private Label totalParticipationsLabel;
    @FXML private ListView<String> listViewParticipations;

    private ParticipationService participationService;
    private EvenementService evenementService;
    private ObservableList<String> participationsAffichees;
    private List<Participation> participationsCompletes;
    private Participation participationEnCours;
    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        participationService = new ParticipationService();
        evenementService = new EvenementService();
        participationsAffichees = FXCollections.observableArrayList();
        participationEnCours = null;

        if (listViewParticipations != null) {
            listViewParticipations.setItems(participationsAffichees);
            listViewParticipations.setOnMouseClicked(this::afficherDetail);
            chargerListe();
        }
    }

    // ==================== MÉTHODES CRUD ====================

    @FXML
    private void ajouterParticipation() {
        if (!validerFormulaire()) return;

        try {
            int idEvent = Integer.parseInt(idEventField.getText());
            int idUser = Integer.parseInt(idUserField.getText());

            if (evenementService.getEvenementById(idEvent) == null) {
                AlertUtil.showError("Erreur", "L'événement avec ID " + idEvent + " n'existe pas.");
                return;
            }

            if (participationService.estInscrit(idEvent, idUser)) {
                AlertUtil.showWarning("Attention", "Cet utilisateur est déjà inscrit à cet événement.");
                return;
            }

            Participation participation = new Participation(idEvent, idUser, statutField.getText());
            participationService.ajouterParticipation(participation);
            effacerFormulaire();
            chargerListe();
            AlertUtil.showInfo("Succès", "Participation ajoutée avec succès!");

        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "Les ID doivent être des nombres.");
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void modifierStatut() {
        if (participationEnCours == null) {
            AlertUtil.showWarning("Attention", "Recherchez d'abord une participation à modifier.");
            return;
        }

        String nouveauStatut = statutField.getText();
        if (!Validator.estStatutParticipationValide(nouveauStatut)) {
            AlertUtil.showWarning("Validation", "Statut invalide. Utilisez 'Inscrit' ou 'Annulé'.");
            return;
        }

        try {
            participationService.modifierStatut(participationEnCours.getIdParticipation(), nouveauStatut);
            chargerListe();
            AlertUtil.showInfo("Succès", "Statut modifié avec succès!");

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerParticipation() {
        if (participationEnCours == null) {
            AlertUtil.showWarning("Attention", "Recherchez d'abord une participation à supprimer.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirmation", "Supprimer cette participation ?");

        if (confirm) {
            try {
                participationService.supprimerParticipation(participationEnCours.getIdParticipation());
                effacerFormulaire();
                chargerListe();
                AlertUtil.showInfo("Succès", "Participation supprimée avec succès!");

            } catch (SQLException e) {
                AlertUtil.showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechercherParId() {
        String idText = rechercheIdField.getText();
        if (idText.isEmpty()) {
            AlertUtil.showWarning("Attention", "Entrez un ID à rechercher.");
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            Participation participation = participationService.getParticipationById(id);

            if (participation == null) {
                AlertUtil.showWarning("Non trouvé", "Aucune participation avec l'ID " + id);
                effacerFormulaire();
            } else {
                remplirFormulaire(participation);
                participationEnCours = participation;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "L'ID doit être un nombre.");
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    // ==================== MÉTHODES POUR LA LISTE ====================

    @FXML
    private void rechercherParEvenement() {
        String idText = rechercheField.getText();
        if (idText.isEmpty()) {
            chargerListe();
            return;
        }

        try {
            int idEvent = Integer.parseInt(idText);
            participationsCompletes = participationService.getParticipationsByEvent(idEvent);
            mettreAJourListe();
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "L'ID doit être un nombre.");
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    @FXML
    private void rechercherParUtilisateur() {
        String idText = rechercheField.getText();
        if (idText.isEmpty()) {
            chargerListe();
            return;
        }

        try {
            int idUser = Integer.parseInt(idText);
            participationsCompletes = participationService.getParticipationsByUser(idUser);
            mettreAJourListe();
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "L'ID doit être un nombre.");
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    @FXML
    private void actualiser() {
        chargerListe();
        rechercheField.clear();
        detailField.clear();
        rechercheIdField.clear();
    }

    @FXML
    private void afficherToutes() {
        chargerListe();
        rechercheField.clear();
    }

    @FXML
    private void afficherInscrits() {
        try {
            List<Participation> toutes = participationService.getAllParticipations();
            participationsCompletes = new java.util.ArrayList<>();
            for (Participation p : toutes) {
                if ("Inscrit".equalsIgnoreCase(p.getStatut())) {
                    participationsCompletes.add(p);
                }
            }
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void afficherAnnules() {
        try {
            List<Participation> toutes = participationService.getAllParticipations();
            participationsCompletes = new java.util.ArrayList<>();
            for (Participation p : toutes) {
                if ("Annulé".equalsIgnoreCase(p.getStatut())) {
                    participationsCompletes.add(p);
                }
            }
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    private void chargerListe() {
        try {
            participationsCompletes = participationService.getAllParticipations();
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    // MÉTHODE MODIFIÉE - SANS ID DANS L'AFFICHAGE
    private void mettreAJourListe() {
        participationsAffichees.clear();
        try {
            for (Participation p : participationsCompletes) {
                EvenementRh event = evenementService.getEvenementById(p.getIdEvent());
                String nomEvent = (event != null) ? event.getTitre() : "Événement #" + p.getIdEvent();

                String affichage = String.format("%s - Utilisateur: %d - %s",
                        nomEvent,
                        p.getIdUser(),
                        p.getStatut()
                );
                participationsAffichees.add(affichage);
            }
        } catch (SQLException e) {
            // Fallback simple
            for (Participation p : participationsCompletes) {
                String affichage = String.format("Événement: %d | Utilisateur: %d | %s",
                        p.getIdEvent(), p.getIdUser(), p.getStatut());
                participationsAffichees.add(affichage);
            }
        }

        if (totalParticipationsLabel != null) {
            totalParticipationsLabel.setText("Total: " + participationsAffichees.size() + " participations");
        }
    }

    private void afficherDetail(MouseEvent event) {
        int index = listViewParticipations.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < participationsCompletes.size()) {
            Participation p = participationsCompletes.get(index);
            String details = String.format(
                    "Événement: %d\nUtilisateur: %d\nStatut: %s",
                    p.getIdEvent(), p.getIdUser(), p.getStatut()
            );
            detailField.setText(details);
        }
    }

    // ==================== MÉTHODES POUR LE FORMULAIRE ====================

    @FXML
    private void effacerFormulaire() {
        idField.clear();
        rechercheIdField.clear();
        idEventField.clear();
        idUserField.clear();
        statutField.clear();
        rechercheField.clear();
        participationEnCours = null;
    }

    private void remplirFormulaire(Participation participation) {
        idField.setText(String.valueOf(participation.getIdParticipation()));
        idEventField.setText(String.valueOf(participation.getIdEvent()));
        idUserField.setText(String.valueOf(participation.getIdUser()));
        statutField.setText(participation.getStatut());
    }

    private boolean validerFormulaire() {
        if (idEventField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "L'ID événement est obligatoire.");
            return false;
        }
        if (idUserField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "L'ID utilisateur est obligatoire.");
            return false;
        }
        if (statutField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le statut est obligatoire.");
            return false;
        }
        if (!Validator.estStatutParticipationValide(statutField.getText())) {
            AlertUtil.showWarning("Validation", "Statut invalide. Utilisez 'Inscrit' ou 'Annulé'.");
            return false;
        }
        return true;
    }
}