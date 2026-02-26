package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import talentos.pidev.controllers.MainLayoutController;
import talentos.pidev.dao.FormationDAO;
import talentos.pidev.models.Formation;

public class FormationCardController {

    @FXML private Label nomLabel;
    @FXML private Label statutLabel;
    @FXML private Label categorieLabel;
    @FXML private Label difficulteLabel;
    @FXML private Label datesLabel;
    @FXML private Label formateurLabel;

    @FXML private Button inscriptionsBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button inscrireBtn;

    private Formation formation;
    private Runnable onChanged;

    // ✅ NEW: to open views inside main content pane
    private MainLayoutController mainLayout;

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
    }

    public void setData(Formation f) {
        this.formation = f;

        nomLabel.setText(f.getNom());

        // Dates
        if (f.getDateDebut() != null && f.getDateFin() != null) {
            datesLabel.setText("Du " + f.getDateDebut() + " au " + f.getDateFin());
        } else {
            datesLabel.setText("Dates: -");
        }

        // Badges
        categorieLabel.setText((f.getCategorie() == null || f.getCategorie().isBlank()) ? "Catégorie" : f.getCategorie());
        difficulteLabel.setText((f.getDifficulte() == null || f.getDifficulte().isBlank()) ? "N/A" : f.getDifficulte());

        // Formateur
        formateurLabel.setText("Formateur : " + ((f.getFormateur() == null || f.getFormateur().isBlank()) ? "-" : f.getFormateur()));

        // Statut
        String st = (f.getStatut() == null || f.getStatut().isBlank()) ? "OUVERTE" : f.getStatut();
        statutLabel.setText(st);
        applyStatutBadge(st);
    }

    private void applyStatutBadge(String st) {
        // reset old badge classes
        statutLabel.getStyleClass().removeIf(c -> c.startsWith("status-") || c.startsWith("badge"));

        // Use your theme style:
        // status-badge + status-open/status-encours/status-filled/... etc
        if (!statutLabel.getStyleClass().contains("status-badge")) {
            statutLabel.getStyleClass().add("status-badge");
        }

        String s = (st == null) ? "" : st.trim().toUpperCase().replace(" ", "_");

        switch (s) {
            case "OUVERTE" -> statutLabel.getStyleClass().add("status-open");
            case "EN_COURS" -> statutLabel.getStyleClass().add("status-encours");
            case "TERMINEE" -> statutLabel.getStyleClass().add("status-closed"); // ou status-filled si tu préfères
            default -> statutLabel.getStyleClass().add("status-default");
        }
    }

    /** true = RH (modifier/supprimer/inscriptions), false = candidat (inscrire) */
    public void setRHMode(boolean rhMode) {
        if (inscriptionsBtn != null) inscriptionsBtn.setVisible(rhMode);
        if (editBtn != null) editBtn.setVisible(rhMode);
        if (deleteBtn != null) deleteBtn.setVisible(rhMode);

        if (inscrireBtn != null) inscrireBtn.setVisible(!rhMode);
    }

    public void setOnChanged(Runnable r) {
        this.onChanged = r;
    }


    @FXML
    private void onInscriptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/InscriptionsRH.fxml"));
            Node view = loader.load();

            InscriptionsRHController controller = loader.getController();
            controller.setFormation(formation.getId(), formation.getNom());

            // ✅ NEW: callbacks pour retour + refresh après actions
            controller.setOnBack(() -> {
                if (mainLayout != null) mainLayout.setContent("/fxml/formations/FormationsRH.fxml");
            });

            controller.setOnChanged(() -> {
                if (onChanged != null) onChanged.run(); // refresh cards
            });

            if (mainLayout != null) {
                mainLayout.setView(view);
            } else {
                System.out.println("MainLayout is null, cannot open inscriptions inside contentPane.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Candidat -> formulaire inscription (tu peux le laisser popup, ou pareil inside main)
    @FXML
    private void onInscrire() {
        if (onInscrireRequested != null) {
            onInscrireRequested.run();
            return;
        }
        System.out.println("onInscrireRequested is null (no navigation configured).");
    }
    // ✅ RH -> modifier : OPEN IN MAIN LAYOUT (no Stage)
    @FXML
    private void onEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/FormationForm.fxml"));
            Node view = loader.load();

            FormationFormController controller = loader.getController();
            controller.setFormation(formation);

            controller.setOnSaved(() -> {
                if (onChanged != null) onChanged.run();
                // back to list after save
                if (mainLayout != null) mainLayout.setContent("/fxml/formations/FormationsRH.fxml");
            });

            controller.setOnCancel(() -> {
                if (mainLayout != null) mainLayout.setContent("/fxml/formations/FormationsRH.fxml");
            });

            if (mainLayout != null) {
                mainLayout.setView(view);
            } else {
                System.out.println("MainLayout is null, cannot open inside contentPane.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer cette formation ?");
            alert.setContentText(formation.getNom());

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                new FormationDAO().deleteFormation(formation.getId());
                if (onChanged != null) onChanged.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Runnable onInscrireRequested;

    public void setOnInscrireRequested(Runnable r) {
        this.onInscrireRequested = r;
    }
}