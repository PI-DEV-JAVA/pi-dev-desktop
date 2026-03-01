package talentos.pidev.controllers.formations;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import talentos.pidev.controllers.MainLayoutController;
import talentos.pidev.dao.FormationDAO;
import talentos.pidev.dao.InscriptionDAO;
import talentos.pidev.models.Formation;
import talentos.pidev.utils.SessionCandidat;

public class FormationCardController {

    @FXML private Label nomLabel;
    @FXML private Label statutLabel;
    @FXML private Label categorieLabel;
    @FXML private Label difficulteLabel;
    @FXML private Label datesLabel;
    @FXML private Label formateurLabel;

    // ✅ NEW labels (added in FXML)
    @FXML private Label modeLabel;
    @FXML private Label lieuLabel;
    @FXML private Label capaciteLabel;
    @FXML private Label descLabel;

    @FXML private Button inscriptionsBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button inscrireBtn;

    private Formation formation;
    private Runnable onChanged;
    private MainLayoutController mainLayout;

    private boolean rhMode = false;

    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

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

        categorieLabel.setText(blankOr(f.getCategorie(), "Catégorie"));
        difficulteLabel.setText(blankOr(f.getDifficulte(), "N/A"));

        formateurLabel.setText("Formateur : " + blankOr(f.getFormateur(), "-"));

        String st = blankOr(f.getStatut(), "OUVERTE");
        statutLabel.setText(st);
        applyStatutBadge(st);

        // ✅ EXTRA INFOS (candidat)
        if (modeLabel != null) modeLabel.setText("Mode : " + blankOr(f.getMode(), "-"));
        if (lieuLabel != null) lieuLabel.setText("Lieu : " + blankOr(f.getLieu(), "-"));
        if (capaciteLabel != null) capaciteLabel.setText("Capacité : " + (f.getCapaciteMax() <= 0 ? "-" : String.valueOf(f.getCapaciteMax())));

        if (descLabel != null) {
            String d = f.getDescription();
            if (d != null) {
                d = d.trim();
                if (d.length() > 90) d = d.substring(0, 90) + "…";
            }
            descLabel.setText(d == null ? "" : d);
        }

        // ✅ set button state for candidate
        refreshCandidateButtonState();
    }

    private void refreshCandidateButtonState() {
        if (rhMode) return; // RH ignore
        if (inscrireBtn == null || formation == null) return;

        try {
            boolean already = inscriptionDAO.exists(SessionCandidat.EMAIL, formation.getId());
            if (already) {
                inscrireBtn.setText("Ouvrir");
                inscrireBtn.getStyleClass().remove("btn-primary");
                if (!inscrireBtn.getStyleClass().contains("btn-success")) inscrireBtn.getStyleClass().add("btn-success");
            } else {
                inscrireBtn.setText("S'inscrire");
                inscrireBtn.getStyleClass().remove("btn-success");
                if (!inscrireBtn.getStyleClass().contains("btn-primary")) inscrireBtn.getStyleClass().add("btn-primary");
            }
        } catch (Exception e) {
            // en cas d’erreur DB, on laisse "S'inscrire"
            inscrireBtn.setText("S'inscrire");
        }
    }

    private void applyStatutBadge(String st) {
        statutLabel.getStyleClass().removeIf(c -> c.startsWith("status-") || c.startsWith("badge"));
        if (!statutLabel.getStyleClass().contains("status-badge")) statutLabel.getStyleClass().add("status-badge");

        String s = (st == null) ? "" : st.trim().toUpperCase().replace(" ", "_");
        switch (s) {
            case "OUVERTE" -> statutLabel.getStyleClass().add("status-open");
            case "EN_COURS" -> statutLabel.getStyleClass().add("status-encours");
            case "TERMINEE" -> statutLabel.getStyleClass().add("status-closed");
            default -> statutLabel.getStyleClass().add("status-default");
        }
    }

    /** true = RH, false = candidat */
    public void setRHMode(boolean rhMode) {
        this.rhMode = rhMode;

        if (inscriptionsBtn != null) inscriptionsBtn.setVisible(rhMode);
        if (editBtn != null) editBtn.setVisible(rhMode);
        if (deleteBtn != null) deleteBtn.setVisible(rhMode);

        if (inscrireBtn != null) inscrireBtn.setVisible(!rhMode);

        // refresh btn state when switching mode
        refreshCandidateButtonState();
    }

    public void setOnChanged(Runnable r) { this.onChanged = r; }

    // ---------------- RH actions ----------------

    @FXML
    private void onInscriptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/InscriptionsRH.fxml"));
            Node view = loader.load();

            InscriptionsRHController controller = loader.getController();
            controller.setFormation(formation.getId(), formation.getNom());

            controller.setOnBack(() -> {
                if (mainLayout != null) mainLayout.setContent("/fxml/formations/FormationsRH.fxml");
            });

            controller.setOnChanged(() -> {
                if (onChanged != null) onChanged.run();
            });

            if (mainLayout != null) mainLayout.setView(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/FormationForm.fxml"));
            Node view = loader.load();

            FormationFormController controller = loader.getController();

            // ✅ IMPORTANT: inject mainLayout (sinon btn Seance/Quiz ne naviguent pas)
            controller.setMainLayout(mainLayout);
            controller.setSelfView(view);

            // ✅ BONUS (recommandé): charger formation complète depuis DB
            Formation full = new FormationDAO().getById(formation.getId());
            controller.setFormation(full);

            controller.setOnSaved(() -> {
                if (onChanged != null) onChanged.run();
                if (mainLayout != null) mainLayout.setContent("/fxml/formations/FormationsRH.fxml");
            });

            controller.setOnCancel(() -> {
                if (mainLayout != null) mainLayout.setContent("/fxml/formations/FormationsRH.fxml");
            });

            if (mainLayout != null) mainLayout.setView(view);

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

    // ---------------- Candidate action ----------------

    private Runnable onInscrireRequested; // open inscription form

    public void setOnInscrireRequested(Runnable r) {
        this.onInscrireRequested = r;
    }

    @FXML
    private void onInscrire() {
        if (formation == null) return;

        // ✅ if already registered => open details directly
        try {
            boolean already = inscriptionDAO.exists(SessionCandidat.EMAIL, formation.getId());
            if (already) {
                openDetails();
                return;
            }
        } catch (Exception ignored) {}

        // else -> normal inscription flow
        if (onInscrireRequested != null) {
            onInscrireRequested.run();
            return;
        }
        System.out.println("onInscrireRequested is null (no navigation configured).");
    }

    private void openDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formations/FormationCandidatDetails.fxml"));
            Node view = loader.load();

            FormationCandidatDetailsController c = loader.getController();
            c.setMainLayout(mainLayout);
            c.init(formation.getId(), formation.getNom(), SessionCandidat.EMAIL);

            if (mainLayout != null) mainLayout.setView(view);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ouverture détails: " + e.getMessage()).show();
        }
    }

    private String blankOr(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }
}