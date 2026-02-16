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

    @FXML private TextField idField;
    @FXML private TextField idEventField;
    @FXML private TextField idUserField;
    @FXML private TextField statutField;
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

        if (listViewParticipations != null) {
            listViewParticipations.setItems(participationsAffichees);
            listViewParticipations.setOnMouseClicked(this::afficherDetail);
            chargerListe();
        }

        if (idField != null) {
            idField.setEditable(false);
        }
    }

    @FXML
    private void ajouterParticipation() {
        if (!validerFormulaire()) return;

        try {
            int idEvent = Integer.parseInt(idEventField.getText());
            int idUser = Integer.parseInt(idUserField.getText());

            if (evenementService.getEvenementById(idEvent) == null) {
                AlertUtil.showError("Erreur", "L'événement n'existe pas.");
                return;
            }

            if (participationService.estInscrit(idEvent, idUser)) {
                AlertUtil.showWarning("Attention", "Cet utilisateur est déjà inscrit.");
                return;
            }

            Participation participation = new Participation(idEvent, idUser, statutField.getText());
            participationService.ajouterParticipation(participation);
            effacerFormulaire();
            AlertUtil.showInfo("Succès", "Participation ajoutée!");

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
            AlertUtil.showWarning("Attention", "Sélectionnez d'abord une participation.");
            return;
        }

        String nouveauStatut = statutField.getText();
        if (!Validator.estStatutParticipationValide(nouveauStatut)) {
            AlertUtil.showWarning("Validation", "Statut invalide (Inscrit/Annulé).");
            return;
        }

        try {
            participationService.modifierStatut(participationEnCours.getIdParticipation(), nouveauStatut);
            AlertUtil.showInfo("Succès", "Statut modifié!");

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
            AlertUtil.showWarning("Attention", "Sélectionnez d'abord une participation.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Confirmation", "Supprimer cette participation ?");

        if (confirm) {
            try {
                participationService.supprimerParticipation(participationEnCours.getIdParticipation());
                effacerFormulaire();
                AlertUtil.showInfo("Succès", "Participation supprimée!");

                if (listViewParticipations != null) {
                    chargerListe();
                }

            } catch (SQLException e) {
                AlertUtil.showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechercherParEvenement() {
        String idStr = rechercheField.getText();
        if (idStr.isEmpty()) {
            chargerListe();
            return;
        }

        try {
            int idEvent = Integer.parseInt(idStr);
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
        String idStr = rechercheField.getText();
        if (idStr.isEmpty()) {
            chargerListe();
            return;
        }

        try {
            int idUser = Integer.parseInt(idStr);
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
    }

    @FXML
    private void effacerFormulaire() {
        idField.clear();
        idEventField.clear();
        idUserField.clear();
        statutField.clear();
        participationEnCours = null;
    }

    private void chargerListe() {
        try {
            participationsCompletes = participationService.getAllParticipations();
            mettreAJourListe();
            updateInfo();
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
        updateInfo();
    }

    private void afficherDetail(MouseEvent event) {
        int index = listViewParticipations.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < participationsCompletes.size()) {
            Participation p = participationsCompletes.get(index);
            remplirFormulaire(p);
            String details = String.format(
                    "ID Participation: %d\nID Événement: %d\nID Utilisateur: %d\nStatut: %s",
                    p.getIdParticipation(), p.getIdEvent(), p.getIdUser(), p.getStatut()
            );
            detailField.setText(details);
        }
    }

    private void remplirFormulaire(Participation p) {
        idField.setText(String.valueOf(p.getIdParticipation()));
        idEventField.setText(String.valueOf(p.getIdEvent()));
        idUserField.setText(String.valueOf(p.getIdUser()));
        statutField.setText(p.getStatut());
        participationEnCours = p;
    }

    private void updateInfo() {
        if (infoLabel != null) {
            infoLabel.setText("Total: " + participationsAffichees.size() + " participation(s)");
        }
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
            AlertUtil.showWarning("Validation", "Statut invalide (Inscrit/Annulé).");
            return false;
        }
        return true;
    }
}