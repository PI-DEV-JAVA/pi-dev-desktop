package com.pi.controllers;

import com.pi.models.Participation;
import com.pi.services.ParticipationService;
import com.pi.services.EvenementService;
import com.pi.utils.AlertUtil;
import com.pi.utils.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ParticipationController implements Initializable {

    // Pour la gestion (formulaire)
    @FXML private TextField idField;
    @FXML private TextField rechercheIdField;
    @FXML private TextField idEventField;
    @FXML private TextField idUserField;
    @FXML private TextField statutField;

    // Pour la liste
    @FXML private TextField rechercheField;
    @FXML private TextField detailField;
    @FXML private Label infoLabel;
    @FXML private ListView<String> listViewParticipations;

    private ParticipationService participationService;
    private EvenementService evenementService;
    private ObservableList<String> participationsAffichees;
    private List<Participation> participationsCompletes;
    private Participation participationEnCours;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        participationService = new ParticipationService();
        evenementService = new EvenementService();
        participationsAffichees = FXCollections.observableArrayList();
        participationEnCours = null;

        // Initialisation de la liste si elle existe
        if (listViewParticipations != null) {
            listViewParticipations.setItems(participationsAffichees);
            listViewParticipations.setOnMouseClicked(this::afficherDetail);
            chargerListe();
        }
    }

    // ==================== MÉTHODES POUR LA GESTION ====================

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
            AlertUtil.showInfo("Succès", "Participation ajoutée avec succès!");

            if (listViewParticipations != null) {
                chargerListe();
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "Les ID doivent être des nombres.");
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void modifierStatut() {
        if (participationEnCours == null) {
            AlertUtil.showWarning("Attention", "Recherchez d'abord une participation à modifier avec l'ID.");
            return;
        }

        String nouveauStatut = statutField.getText();
        if (!Validator.estStatutParticipationValide(nouveauStatut)) {
            AlertUtil.showWarning("Validation", "Statut invalide. Utilisez 'Inscrit' ou 'Annulé'.");
            return;
        }

        try {
            participationService.modifierStatut(participationEnCours.getIdParticipation(), nouveauStatut);
            AlertUtil.showInfo("Succès", "Statut modifié avec succès!");

            if (listViewParticipations != null) {
                chargerListe();
            }

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerParticipation() {
        if (participationEnCours == null) {
            AlertUtil.showWarning("Attention", "Recherchez d'abord une participation à supprimer avec l'ID.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirmation", "Supprimer cette participation ?");

        if (confirm) {
            try {
                participationService.supprimerParticipation(participationEnCours.getIdParticipation());
                effacerFormulaire();
                AlertUtil.showInfo("Succès", "Participation supprimée avec succès!");

                if (listViewParticipations != null) {
                    chargerListe();
                }

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

    @FXML
    private void effacerFormulaire() {
        if (idField != null) idField.clear();
        if (rechercheIdField != null) rechercheIdField.clear();
        if (idEventField != null) idEventField.clear();
        if (idUserField != null) idUserField.clear();
        if (statutField != null) statutField.clear();
        if (rechercheField != null) rechercheField.clear();
        if (detailField != null) detailField.clear();
        participationEnCours = null;
    }

    private void remplirFormulaire(Participation participation) {
        if (idField != null) idField.setText(String.valueOf(participation.getIdParticipation()));
        if (idEventField != null) idEventField.setText(String.valueOf(participation.getIdEvent()));
        if (idUserField != null) idUserField.setText(String.valueOf(participation.getIdUser()));
        if (statutField != null) statutField.setText(participation.getStatut());
    }

    private boolean validerFormulaire() {
        if (idEventField == null || idEventField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "L'ID événement est obligatoire.");
            return false;
        }
        if (idUserField == null || idUserField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "L'ID utilisateur est obligatoire.");
            return false;
        }
        if (statutField == null || statutField.getText().isEmpty()) {
            AlertUtil.showWarning("Validation", "Le statut est obligatoire.");
            return false;
        }
        if (!Validator.estStatutParticipationValide(statutField.getText())) {
            AlertUtil.showWarning("Validation", "Statut invalide. Utilisez 'Inscrit' ou 'Annulé'.");
            return false;
        }
        return true;
    }

    // ==================== MÉTHODES POUR LA LISTE ====================

    @FXML
    private void actualiser() {
        chargerListe();
        if (rechercheField != null) rechercheField.clear();
        if (detailField != null) detailField.clear();
    }

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

    private void chargerListe() {
        try {
            participationsCompletes = participationService.getAllParticipations();
            mettreAJourListe();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    private void mettreAJourListe() {
        participationsAffichees.clear();
        for (Participation p : participationsCompletes) {
            String affichage = String.format("ID: %d | Événement: %d | Utilisateur: %d | %s",
                    p.getIdParticipation(), p.getIdEvent(), p.getIdUser(), p.getStatut());
            participationsAffichees.add(affichage);
        }
        if (infoLabel != null) {
            infoLabel.setText("Total: " + participationsAffichees.size() + " participation(s)");
        }
    }

    private void afficherDetail(MouseEvent event) {
        if (listViewParticipations == null) return;

        int index = listViewParticipations.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < participationsCompletes.size()) {
            Participation p = participationsCompletes.get(index);
            String details = String.format(
                    "ID Participation: %d\nID Événement: %d\nID Utilisateur: %d\nStatut: %s",
                    p.getIdParticipation(), p.getIdEvent(), p.getIdUser(), p.getStatut()
            );
            if (detailField != null) {
                detailField.setText(details);
            }
        }
    }
}