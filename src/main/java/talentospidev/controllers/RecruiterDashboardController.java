package talentospidev.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.ProfileDao;
import talentospidev.models.Application;
import talentospidev.models.Offer;
import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.services.ApplicationService;
import talentospidev.services.AuthService;
import talentospidev.services.OfferService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecruiterDashboardController {

    @FXML
    private HBox statsRow;
    @FXML
    private TextField searchField;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private VBox sidebar;

    private final OfferService offerService = new OfferService();
    private final ApplicationService applicationService = new ApplicationService();
    private final ProfileDao profileDao = new ProfileDao();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private ObservableList<Offer> allOffers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.applySidebarIcons(sidebar);
        loadStats();
        loadOffers();
        searchField.textProperty().addListener((obs, old, val) -> filterOffers());
    }

    private void loadStats() {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;

        int total = offerService.getTotalOffersByRecruiter(user.getId());
        int open = 0;
        int totalApps = offerService.getTotalApplicationsByRecruiter(user.getId());

        for (Offer o : offerService.getOffersByRecruiterId(user.getId())) {
            if ("Ouverte".equals(o.getStatus()))
                open++;
        }

        statsRow.getChildren().clear();
        statsRow.getChildren().addAll(
                createStatCard("📋", "Total Offres", String.valueOf(total), "#6366f1", "#eef2ff"),
                createStatCard("✅", "Ouvertes", String.valueOf(open), "#22c55e", "#f0fdf4"),
                createStatCard("📬", "Candidatures", String.valueOf(totalApps), "#f59e0b", "#fffbeb"));
    }

    private VBox createStatCard(String icon, String title, String value, String color, String bgColor) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 14; -fx-padding: 20; " +
                "-fx-border-color: " + color + "22; -fx-border-radius: 14;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28px;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void loadOffers() {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;
        allOffers.setAll(offerService.getOffersByRecruiterId(user.getId()));
        displayCards(allOffers);
    }

    private void filterOffers() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            displayCards(allOffers);
            return;
        }
        displayCards(allOffers.filtered(o -> o.getTitle().toLowerCase().contains(query) ||
                o.getDepartment().toLowerCase().contains(query) ||
                o.getLocation().toLowerCase().contains(query)));
    }

    private void displayCards(ObservableList<Offer> offers) {
        cardsContainer.getChildren().clear();
        for (Offer offer : offers)
            cardsContainer.getChildren().add(createOfferCard(offer));
        if (offers.isEmpty()) {
            Label empty = new Label("📭 No offers yet. Create one!");
            empty.setStyle("-fx-font-size: 15px; -fx-text-fill: #9ca3af; -fx-padding: 40;");
            cardsContainer.getChildren().add(empty);
        }
    }

    private VBox createOfferCard(Offer offer) {
        VBox card = new VBox(10);
        String base = "-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 20; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);";
        String hover = "-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 20; " +
                "-fx-border-color: #6366f1; -fx-border-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.18), 12, 0, 0, 4); -fx-translate-y: -2;";
        card.setStyle(base);
        card.setPrefWidth(320);
        card.setMinHeight(260);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(base));

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(offer.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        title.setWrapText(true);
        title.setMaxWidth(190);
        HBox.setHgrow(title, Priority.ALWAYS);
        Label status = new Label(offer.getStatus());
        status.setStyle(getStatusStyle(offer.getStatus()));
        header.getChildren().addAll(title, status);

        // Info
        Label dept = new Label("🏢 " + offer.getDepartment() + " • " + offer.getContractType());
        dept.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Label location = new Label("📍 " + offer.getLocation());
        location.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Label salary = new Label(String.format("💰 %.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        salary.setStyle("-fx-font-size: 12px; -fx-text-fill: #22c55e; -fx-font-weight: 700;");

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label date = new Label("📅 " + offer.getClosingDate().format(dateFormatter));
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        Label apps = new Label("👥 " + offer.getApplicationsReceived() + " candidatures");
        apps.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        footer.getChildren().addAll(date, apps);

        // Actions
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e5e7eb;");

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        Button viewBtn = createBtn("👁 Voir", "#eef2ff", "#6366f1");
        viewBtn.setOnAction(e -> {
            ViewContext.setSelectedOfferId(offer.getId());
            SceneUtil.switchScene("offer_details.fxml");
        });

        Button appsBtn = createBtn("📬 Candidatures", "#fffbeb", "#d97706");
        appsBtn.setOnAction(e -> viewApplications(offer));

        Button editBtn = createBtn("✏ Modifier", "#fef3c7", "#d97706");
        editBtn.setOnAction(e -> {
            ViewContext.setSelectedOfferId(offer.getId());
            SceneUtil.switchScene("add_offer.fxml");
        });

        Button deleteBtn = createBtn("🗑", "#fee2e2", "#ef4444");
        deleteBtn.setOnAction(e -> deleteOffer(offer));

        actions.getChildren().addAll(viewBtn, appsBtn, editBtn, deleteBtn);

        card.getChildren().addAll(header, dept, location, salary, footer, sep, actions);
        return card;
    }

    // ===== VIEW & RESPOND TO APPLICATIONS =====

    private void viewApplications(Offer offer) {
        List<Application> apps = applicationService.getApplicationsByOffer(offer.getId());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Candidatures — " + offer.getTitle());
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.setPrefWidth(650);
        pane.setPrefHeight(520);
        pane.setStyle("-fx-background-color: #f8fafc;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        if (apps.isEmpty()) {
            Label empty = new Label("📭 Aucune candidature pour cette offre.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af; -fx-padding: 30;");
            content.getChildren().add(empty);
        } else {
            Label count = new Label(apps.size() + " candidature" + (apps.size() > 1 ? "s" : ""));
            count.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
            content.getChildren().add(count);

            for (Application app : apps) {
                content.getChildren().add(createApplicationRow(app, offer));
            }
        }

        scroll.setContent(content);
        pane.setContent(scroll);
        dialog.showAndWait();
    }

    private HBox createApplicationRow(Application app, Offer offer) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        // Avatar
        String candidateName = getCandidateDisplayName(app.getUserId());
        Label avatar = new Label(candidateName.substring(0, 1).toUpperCase());
        avatar.setMinWidth(40);
        avatar.setMinHeight(40);
        avatar.setMaxWidth(40);
        avatar.setMaxHeight(40);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color: #6366f1; -fx-background-radius: 20; " +
                "-fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 16px;");

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLabel = new Label(candidateName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        Label dateLabel = new Label("📅 " + app.getApplicationDate().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        info.getChildren().addAll(nameLabel, dateLabel);

        // Status badge
        Label statusBadge = new Label(app.getStatus());
        String badgeStyle;
        if (app.hasResponse()) {
            badgeStyle = app.getStatus().toLowerCase().contains("accept")
                    ? "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;"
                    : "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;";
        } else {
            badgeStyle = "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;";
        }
        statusBadge.setStyle(badgeStyle
                + " -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;");

        // Respond button
        Button respondBtn = new Button(app.hasResponse() ? "✏️ Modifier" : "📝 Répondre");
        respondBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 11px; " +
                "-fx-font-weight: 700; -fx-padding: 6 14; -fx-background-radius: 8; -fx-cursor: hand;");
        respondBtn.setOnAction(e -> respondToApplication(app, offer));

        row.getChildren().addAll(avatar, info, statusBadge, respondBtn);
        return row;
    }

    private void respondToApplication(Application app, Offer offer) {
        String candidateName = getCandidateDisplayName(app.getUserId());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Répondre à " + candidateName);
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setPrefWidth(450);
        pane.setStyle("-fx-background-color: #f9fafb;");

        ButtonType acceptBtn = new ButtonType("✅ Accepter", ButtonBar.ButtonData.YES);
        ButtonType rejectBtn = new ButtonType("❌ Refuser", ButtonBar.ButtonData.NO);
        pane.getButtonTypes().addAll(acceptBtn, rejectBtn, ButtonType.CANCEL);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));

        Label heading = new Label("Réponse pour: " + candidateName);
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        Label offerLabel = new Label("📋 " + offer.getTitle());
        offerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        TextArea responseField = new TextArea();
        responseField.setPromptText("Votre message pour le candidat (optionnel)...");
        responseField.setPrefRowCount(4);
        responseField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e5e7eb; " +
                "-fx-font-size: 13px;");

        content.getChildren().addAll(heading, offerLabel, responseField);
        pane.setContent(content);

        dialog.showAndWait().ifPresent(result -> {
            String response = responseField.getText().trim();
            String newStatus;
            if (result == acceptBtn) {
                newStatus = "Acceptée";
                if (response.isEmpty())
                    response = "Félicitations ! Votre candidature a été acceptée.";
            } else if (result == rejectBtn) {
                newStatus = "Refusée";
                if (response.isEmpty())
                    response = "Nous vous remercions de votre intérêt. Malheureusement, votre candidature n'a pas été retenue.";
            } else {
                return;
            }

            app.setStatus(newStatus);
            applicationService.respondToApplication(app.getId(), response, newStatus);
            loadStats();
            loadOffers();
            showAlert("Réponse envoyée", "La réponse a été envoyée au candidat.", Alert.AlertType.INFORMATION);
        });
    }

    private String getCandidateDisplayName(int userId) {
        Profile profile = profileDao.findByUserId(userId);
        if (profile != null && profile.getFirstName() != null && profile.getLastName() != null) {
            return profile.getFirstName() + " " + profile.getLastName();
        }
        return "Candidat #" + userId;
    }

    // ===== CRUD =====

    @FXML
    private void addNewOffer() {
        ViewContext.setSelectedOfferId(0); // 0 = create mode
        SceneUtil.switchScene("add_offer.fxml");
    }

    private void deleteOffer(Offer offer) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer « " + offer.getTitle() + " » ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (offerService.deleteOffer(offer.getId())) {
                    loadStats();
                    loadOffers();
                    showAlert("Supprimé", "L'offre a été supprimée.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Impossible de supprimer.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private Button createBtn(String text, String bg, String fg) {
        Button btn = new Button(text);
        String normal = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg +
                "; -fx-padding: 5 10; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: 700; -fx-cursor: hand;";
        String hovered = "-fx-background-color: derive(" + bg + ", -10%); -fx-text-fill: " + fg +
                "; -fx-padding: 5 10; -fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: 700; -fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hovered));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    private String getStatusStyle(String status) {
        return switch (status) {
            case "Ouverte" ->
                "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "Fermée" ->
                "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "En attente" ->
                "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            default ->
                "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
        };
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        /* Already on dashboard */ }

    @FXML
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleJobOffers() {
        SceneUtil.switchScene("OffersCardView.fxml");
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
    private void handleNotifications() {
        talentospidev.utils.SceneUtil.switchScene("notifications.fxml");
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
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
