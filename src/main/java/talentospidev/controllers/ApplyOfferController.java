package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import talentospidev.models.Application;
import talentospidev.models.Offer;
import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.dao.ProfileDao;
import talentospidev.services.ApplicationService;
import talentospidev.services.AuthService;
import talentospidev.services.OfferService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.io.File;

public class ApplyOfferController {

    @FXML
    private VBox formContainer;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final OfferService offerService = new OfferService();
    private final ApplicationService applicationService = new ApplicationService();
    private final ProfileDao profileDao = new ProfileDao();
    private File selectedCvFile;

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        User currentUser = AuthService.getCurrentUser();
        if (currentUser == null) {
            showError("Vous devez être connecté pour postuler.");
            return;
        }

        int offerId = ViewContext.getSelectedOfferId();
        Offer offer = (offerId > 0) ? offerService.getOfferById(offerId) : null;

        if (offer == null) {
            showError("Offre introuvable.");
            return;
        }

        // Already-applied guard
        if (applicationService.hasUserApplied(currentUser.getId(), offerId)) {
            buildAlreadyAppliedView(offer);
            return;
        }

        buildApplyForm(offer, currentUser);
    }

    private void buildAlreadyAppliedView(Offer offer) {
        formContainer.getChildren().clear();

        Button backBtn = createBackButton(offer.getId());

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 16; -fx-padding: 40; " +
                "-fx-border-color: #86efac; -fx-border-radius: 16;");

        Label icon = new Label("✅");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Vous avez déjà postulé à cette offre");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #16a34a;");

        Label subtitle = new Label("« " + offer.getTitle() + " »");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");

        Button dashBtn = new Button("📊  Voir mes candidatures");
        dashBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: 700; -fx-padding: 12 28; -fx-background-radius: 10; -fx-cursor: hand;");
        dashBtn.setOnAction(e -> handleDashboard());

        card.getChildren().addAll(icon, title, subtitle, dashBtn);
        formContainer.getChildren().addAll(backBtn, card);
    }

    private void buildApplyForm(Offer offer, User currentUser) {
        formContainer.getChildren().clear();

        // === Back button ===
        Button backBtn = createBackButton(offer.getId());

        // === Offer summary card ===
        VBox summaryCard = new VBox(8);
        summaryCard.setStyle("-fx-background-color: linear-gradient(to right, #eef2ff, #faf5ff); " +
                "-fx-background-radius: 16; -fx-padding: 22; -fx-border-color: #c7d2fe; -fx-border-radius: 16;");

        Label offerTitle = new Label(offer.getTitle());
        offerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #4338ca;");

        Label offerInfo = new Label("🏢 " + offer.getDepartment() + "  •  " + offer.getContractType() +
                "  •  📍 " + offer.getLocation());
        offerInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #6366f1;");

        Label salary = new Label(String.format("💰 %.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        salary.setStyle("-fx-font-size: 13px; -fx-text-fill: #22c55e; -fx-font-weight: 700;");

        summaryCard.getChildren().addAll(offerTitle, offerInfo, salary);

        // === Profile summary card (auto-fill from profile) ===
        Profile profile = profileDao.findByUserId(currentUser.getId());

        VBox profileCard = new VBox(10);
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 22; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16;");

        Label profileTitle = new Label("👤 Votre profil");
        profileTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        String fullName = "—";
        String phone = "—";
        String location = "—";
        String title = "—";
        int completionPercent = 30; // base: just has account

        if (profile != null) {
            if (profile.getFirstName() != null && profile.getLastName() != null) {
                fullName = profile.getFirstName() + " " + profile.getLastName();
                completionPercent += 15;
            }
            if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()) {
                phone = profile.getPhoneNumber();
                completionPercent += 15;
            }
            if (profile.getLocation() != null && !profile.getLocation().isEmpty()) {
                location = profile.getLocation();
                completionPercent += 15;
            }
            if (profile.getProfessionalTitle() != null && !profile.getProfessionalTitle().isEmpty()) {
                title = profile.getProfessionalTitle();
                completionPercent += 15;
            }
            if (profile.getSummary() != null && !profile.getSummary().isEmpty()) {
                completionPercent += 10;
            }
        }

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(30);
        profileGrid.setVgap(8);
        addProfileRow(profileGrid, "Nom", fullName, 0);
        addProfileRow(profileGrid, "Email", currentUser.getEmail(), 1);
        addProfileRow(profileGrid, "Téléphone", phone, 2);
        addProfileRow(profileGrid, "Localisation", location, 3);
        addProfileRow(profileGrid, "Titre", title, 4);

        // Completion indicator
        HBox completionBox = new HBox(10);
        completionBox.setAlignment(Pos.CENTER_LEFT);
        completionBox.setStyle("-fx-padding: 8 0 0 0;");

        ProgressBar progressBar = new ProgressBar(completionPercent / 100.0);
        progressBar.setPrefWidth(160);
        progressBar.setStyle("-fx-accent: "
                + (completionPercent >= 80 ? "#22c55e" : completionPercent >= 50 ? "#f59e0b" : "#ef4444") + ";");

        String completionColor = completionPercent >= 80 ? "#22c55e" : completionPercent >= 50 ? "#f59e0b" : "#ef4444";
        Label completionLabel = new Label(completionPercent + "% complet");
        completionLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + completionColor + ";");

        Label completionHint = new Label(
                completionPercent < 80 ? "— Un profil complet augmente vos chances !" : "— Excellent profil !");
        completionHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        completionBox.getChildren().addAll(progressBar, completionLabel, completionHint);

        profileCard.getChildren().addAll(profileTitle, profileGrid, completionBox);

        // === Form card — only CV + Motivation ===
        VBox formCard = new VBox(20);
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        Label formTitle = new Label("📝 Compléter votre candidature");
        formTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        // CV Upload
        VBox cvBox = new VBox(6);
        Label cvLabel = new Label("CV (PDF) *");
        cvLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        HBox cvRow = new HBox(12);
        cvRow.setAlignment(Pos.CENTER_LEFT);

        Label cvFileName = new Label("Aucun fichier sélectionné");
        cvFileName.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af;");

        Button cvBtn = new Button("📎 Parcourir");
        cvBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-font-size: 12px; " +
                "-fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
        cvBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir votre CV");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            File file = chooser.showOpenDialog(formContainer.getScene().getWindow());
            if (file != null) {
                selectedCvFile = file;
                cvFileName.setText(file.getName());
                cvFileName.setStyle("-fx-font-size: 13px; -fx-text-fill: #22c55e; -fx-font-weight: 600;");
            }
        });

        cvRow.getChildren().addAll(cvBtn, cvFileName);
        cvBox.getChildren().addAll(cvLabel, cvRow);

        // Motivation letter
        VBox motivBox = new VBox(6);
        Label motivLabel = new Label("Lettre de motivation");
        motivLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        TextArea motivArea = new TextArea();
        motivArea.setPromptText("Expliquez pourquoi vous êtes le candidat idéal pour ce poste...");
        motivArea.setPrefRowCount(6);
        motivArea.setWrapText(true);
        motivArea.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10; -fx-padding: 12; " +
                "-fx-font-size: 13px; -fx-border-color: #e5e7eb; -fx-border-radius: 10;");
        motivArea.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                motivArea.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12; " +
                        "-fx-font-size: 13px; -fx-border-color: #6366f1; -fx-border-radius: 10; -fx-border-width: 2;");
            } else {
                motivArea.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10; -fx-padding: 12; " +
                        "-fx-font-size: 13px; -fx-border-color: #e5e7eb; -fx-border-radius: 10;");
            }
        });

        motivBox.getChildren().addAll(motivLabel, motivArea);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
        errorLabel.setVisible(false);

        // Submit button
        Button submitBtn = new Button("📤  Envoyer ma candidature");
        submitBtn.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); " +
                "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; " +
                "-fx-padding: 14 36; -fx-background-radius: 12; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.35), 12, 0, 0, 4);");

        submitBtn.setOnAction(e -> {
            String motivation = motivArea.getText().trim();

            Application app = new Application(
                    currentUser.getId(),
                    offer.getId(),
                    selectedCvFile != null ? selectedCvFile.getAbsolutePath() : null,
                    motivation);

            if (applicationService.createApplication(app)) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setHeaderText(null);
                success.setContentText("Votre candidature a été envoyée avec succès !");
                success.showAndWait();
                SceneUtil.switchScene("OffersCardView.fxml");
            } else {
                errorLabel.setText("⚠ Erreur lors de l'envoi ou candidature déjà existante.");
                errorLabel.setVisible(true);
            }
        });

        formCard.getChildren().addAll(formTitle, cvBox, motivBox, errorLabel, submitBtn);
        formContainer.getChildren().addAll(backBtn, summaryCard, profileCard, formCard);
    }

    private void addProfileRow(GridPane grid, String label, String value, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-font-weight: 600;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private Button createBackButton(int offerId) {
        Button backBtn = new Button("← Retour aux détails");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6366f1; -fx-font-size: 14px; " +
                "-fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 0;");
        backBtn.setOnAction(e -> {
            ViewContext.setSelectedOfferId(offerId);
            SceneUtil.switchScene("offer_details.fxml");
        });
        return backBtn;
    }

    private void showError(String msg) {
        Label err = new Label("⚠ " + msg);
        err.setStyle("-fx-font-size: 16px; -fx-text-fill: #ef4444; -fx-padding: 40;");
        formContainer.getChildren().add(err);
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        User u = AuthService.getCurrentUser();
        if (u != null && u.getRole() == User.Role.HR)
            SceneUtil.switchScene("recruiter_dashboard.fxml");
        else if (u != null && u.getRole() == User.Role.ADMIN)
            SceneUtil.switchScene("admin_dashboard.fxml");
        else
            SceneUtil.switchScene("dashboard.fxml");
    }

    @FXML
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleJobOffers() {
        SceneUtil.switchScene("OffersCardView.fxml");
    }
    @FXML private void handleTrends() { talentospidev.utils.SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews() { talentospidev.utils.SceneUtil.switchScene("Interviews/InterviewView.fxml"); }

    @FXML
    private void handleToDo() {
        SceneUtil.switchScene("activities/activity_employee.fxml");
    }

    @FXML
    private void handleActivities() {
        SceneUtil.switchScene("activities/activities.fxml");
    }

    @FXML
    private void handleProjects() {
        SceneUtil.switchScene("projects/projects.fxml");
    }

    @FXML
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Fonctionnalité bientôt disponible !").showAndWait();
    }

    @FXML
    private void handleSettings() {
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handleMyCircle() {
        SceneUtil.switchScene("my_circle.fxml");
    }

    @FXML
    private void handleNotifications() {
        SceneUtil.switchScene("notifications.fxml");
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
