package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import talentospidev.models.Offer;
import talentospidev.models.User;
import talentospidev.services.ApplicationService;
import talentospidev.services.AuthService;
import talentospidev.services.OfferService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class OfferDetailsController {

    @FXML
    private VBox detailsContainer;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final OfferService offerService = new OfferService();
    private final ApplicationService applicationService = new ApplicationService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int offerId = ViewContext.getSelectedOfferId();
        if (offerId <= 0) {
            showError("Aucune offre sélectionnée.");
            return;
        }

        Offer offer = offerService.getOfferById(offerId);
        if (offer == null) {
            showError("Offre introuvable.");
            return;
        }

        buildDetailsView(offer);
    }

    private void buildDetailsView(Offer offer) {
        detailsContainer.getChildren().clear();

        // === Back Button ===
        Button backBtn = new Button("← Retour aux offres");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6366f1; -fx-font-size: 14px; " +
                "-fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 0;");
        backBtn.setOnAction(e -> handleJobOffers());

        // === Title Card ===
        VBox headerCard = new VBox(12);
        headerCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(offer.getTitle());
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        title.setWrapText(true);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label statusBadge = new Label(offer.getStatus());
        statusBadge.setStyle(getStatusStyle(offer.getStatus()));

        titleRow.getChildren().addAll(title, statusBadge);

        Label subtitle = new Label(
                "🏢 " + offer.getDepartment() + "  •  " + offer.getContractType() + "  •  📍 " + offer.getLocation());
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");

        headerCard.getChildren().addAll(titleRow, subtitle);

        // === Info Grid ===
        VBox infoCard = new VBox(18);
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        Label infoTitle = new Label("Détails de l'offre");
        infoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(16);

        addInfoRow(grid, "💰 Salaire", String.format("%.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()), 0,
                "#22c55e");
        addInfoRow(grid, "📊 Expérience", offer.getExperienceLevel(), 1, "#6366f1");
        addInfoRow(grid, "📅 Date de publication", offer.getPublishDate().format(dateFormatter), 2, "#3b82f6");
        addInfoRow(grid, "📅 Date de clôture", offer.getClosingDate().format(dateFormatter), 3, "#ef4444");

        long daysLeft = ChronoUnit.DAYS.between(java.time.LocalDate.now(), offer.getClosingDate());
        String daysText = daysLeft > 0 ? daysLeft + " jours restants" : "Expiré";
        addInfoRow(grid, "⏳ Délai", daysText, 4, daysLeft > 0 ? "#f59e0b" : "#ef4444");
        addInfoRow(grid, "👥 Postes disponibles", String.valueOf(offer.getPositionsAvailable()), 5, "#8b5cf6");
        addInfoRow(grid, "📬 Candidatures reçues", String.valueOf(offer.getApplicationsReceived()), 6, "#06b6d4");

        infoCard.getChildren().addAll(infoTitle, grid);

        // === Description ===
        VBox descCard = new VBox(12);
        descCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        Label descTitle = new Label("Description du poste");
        descTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        Text descText = new Text(
                offer.getDescription() != null ? offer.getDescription() : "Aucune description disponible.");
        descText.setStyle("-fx-font-size: 14px; -fx-fill: #374151;");
        TextFlow descFlow = new TextFlow(descText);
        descFlow.setLineSpacing(4);

        descCard.getChildren().addAll(descTitle, descFlow);

        // === Action Buttons ===
        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == User.Role.CANDIDATE
                && "Ouverte".equals(offer.getStatus())) {
            boolean alreadyApplied = applicationService.hasUserApplied(currentUser.getId(), offer.getId());
            if (alreadyApplied) {
                Button appliedBtn = new Button("✅  Déjà postulé");
                appliedBtn.setStyle("-fx-background-color: #f0fdf4; " +
                        "-fx-text-fill: #16a34a; -fx-font-size: 15px; -fx-font-weight: 700; " +
                        "-fx-padding: 14 36; -fx-background-radius: 12; " +
                        "-fx-border-color: #86efac; -fx-border-radius: 12;");
                appliedBtn.setDisable(true);
                appliedBtn.setOpacity(0.85);
                actions.getChildren().add(appliedBtn);
            } else {
                Button applyBtn = new Button("📤  Postuler maintenant");
                applyBtn.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); " +
                        "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; " +
                        "-fx-padding: 14 36; -fx-background-radius: 12; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.35), 12, 0, 0, 4);");
                applyBtn.setOnAction(e -> {
                    ViewContext.setSelectedOfferId(offer.getId());
                    SceneUtil.switchScene("apply_offer.fxml");
                });
                actions.getChildren().add(applyBtn);
            }
        }

        detailsContainer.getChildren().addAll(backBtn, headerCard, infoCard, descCard, actions);
    }

    private void addInfoRow(GridPane grid, String label, String value, int row, String color) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-text-fill: " + color + "; -fx-font-weight: 700;");

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "Ouverte":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "Fermée":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
        }
    }

    private void showError(String msg) {
        Label err = new Label("⚠ " + msg);
        err.setStyle("-fx-font-size: 16px; -fx-text-fill: #ef4444; -fx-padding: 40;");
        detailsContainer.getChildren().add(err);
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
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
