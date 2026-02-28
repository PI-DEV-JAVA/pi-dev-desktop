package talentospidev.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import talentospidev.dao.ProfileDao;
import talentospidev.dao.UserDao;
import talentospidev.models.Application;
import talentospidev.models.Offer;
import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.models.UserViewModel;
import talentospidev.services.ApplicationService;
import talentospidev.services.AuthService;
import talentospidev.services.OfferService;
import talentospidev.utils.ProfilePopup;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    private VBox welcomeSection;
    @FXML
    private VBox applicationsSection;
    @FXML
    private ListView<UserViewModel> feedListView;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortByBox;
    @FXML
    private ComboBox<String> sortOrderBox;
    @FXML
    private Label countLabel;
    @FXML
    private VBox sidebar;

    private final UserDao userDao = new UserDao();
    private final ProfileDao profileDao = new ProfileDao();
    private final ApplicationService applicationService = new ApplicationService();
    private final OfferService offerService = new OfferService();
    private ObservableList<UserViewModel> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.applySidebarIcons(sidebar);
        User currentUser = AuthService.getCurrentUser();
        if (currentUser == null)
            return;

        if (currentUser.getRole() == User.Role.HR) {
            SceneUtil.switchScene("recruiter_dashboard.fxml");
            return;
        }

        buildWelcomeSection(currentUser);

        sortByBox.setItems(FXCollections.observableArrayList("Name", "Job", "Age", "Location"));
        sortByBox.setValue("Name");
        sortOrderBox.setItems(FXCollections.observableArrayList("ASC", "DESC"));
        sortOrderBox.setValue("ASC");

        setupListView();
        loadFeed(currentUser.getId());

        if (currentUser.getRole() == User.Role.CANDIDATE) {
            loadMyApplications(currentUser);
        } else {
            applicationsSection.setVisible(false);
            applicationsSection.setManaged(false);
        }

        searchField.textProperty().addListener((obs, o, n) -> applyFilterAndSort());
        sortByBox.valueProperty().addListener((obs, o, n) -> applyFilterAndSort());
        sortOrderBox.valueProperty().addListener((obs, o, n) -> applyFilterAndSort());
    }

    // ===== WELCOME SECTION WITH STATS =====

    private void buildWelcomeSection(User user) {
        welcomeSection.getChildren().clear();

        Profile profile = profileDao.findByUserId(user.getId());
        String name = "there";
        if (profile != null && profile.getFirstName() != null)
            name = profile.getFirstName();

        int hour = java.time.LocalTime.now().getHour();
        String greeting = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";

        // Welcome card
        HBox welcomeCard = new HBox(16);
        welcomeCard.setAlignment(Pos.CENTER_LEFT);
        welcomeCard.setPadding(new Insets(20, 24, 20, 24));
        welcomeCard.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); " +
                "-fx-background-radius: 14;");

        VBox welcomeText = new VBox(4);
        HBox.setHgrow(welcomeText, Priority.ALWAYS);
        Label greetingLabel = new Label(greeting + ", " + name + " 👋");
        greetingLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: white;");
        Label subtitleLabel = new Label("Here's what's happening with your career");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.75);");
        welcomeText.getChildren().addAll(greetingLabel, subtitleLabel);

        welcomeCard.getChildren().add(welcomeText);

        // Stats row (for candidates)
        if (user.getRole() == User.Role.CANDIDATE) {
            List<Application> apps = applicationService.getApplicationsByUserId(user.getId());
            long pending = apps.stream().filter(a -> !a.hasResponse()).count();
            long responded = apps.stream().filter(Application::hasResponse).count();

            HBox stats = new HBox(12);
            stats.setAlignment(Pos.CENTER);
            stats.getChildren().addAll(
                    createMiniStat("📋", String.valueOf(apps.size()), "Applied"),
                    createMiniStat("⏳", String.valueOf(pending), "Pending"),
                    createMiniStat("✉️", String.valueOf(responded), "Responded"));
            welcomeCard.getChildren().add(stats);
        }

        welcomeSection.getChildren().add(welcomeCard);
    }

    private VBox createMiniStat(String icon, String value, String label) {
        VBox stat = new VBox(2);
        stat.setAlignment(Pos.CENTER);
        stat.setPadding(new Insets(8, 14, 8, 14));
        stat.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 10;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white;");
        Label namLabel = new Label(label);
        namLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: 600;");

        stat.getChildren().addAll(iconLabel, valLabel, namLabel);
        return stat;
    }

    // ===== LEFT PANEL: MY APPLICATIONS =====

    private void loadMyApplications(User user) {
        List<Application> apps = applicationService.getApplicationsByUserId(user.getId());
        applicationsSection.getChildren().clear();

        // Section Header
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("📋 Mes Candidatures");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        if (!apps.isEmpty()) {
            Label countLbl = new Label(apps.size() + "");
            countLbl.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 11px; " +
                    "-fx-font-weight: 800; -fx-padding: 2 8; -fx-background-radius: 10;");
            headerRow.getChildren().addAll(sectionTitle, hSpacer, countLbl);
        } else {
            headerRow.getChildren().addAll(sectionTitle, hSpacer);
        }
        applicationsSection.getChildren().add(headerRow);

        if (apps.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(28));
            emptyState.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                    "-fx-border-color: #e5e7eb; -fx-border-radius: 14; -fx-border-style: dashed;");

            Label emptyIcon = new Label("🚀");
            emptyIcon.setStyle("-fx-font-size: 32px;");
            Label emptyText = new Label("Start your journey");
            emptyText.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: 700;");
            Label emptyHint = new Label("Browse job offers and apply to get started");
            emptyHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

            Button browseBtn = new Button("💼 Browse Offers");
            browseBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 12px; " +
                    "-fx-font-weight: 700; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
            browseBtn.setOnAction(e -> handleJobOffers());

            emptyState.getChildren().addAll(emptyIcon, emptyText, emptyHint, browseBtn);
            applicationsSection.getChildren().add(emptyState);
            return;
        }

        // Application cards (max 6)
        int limit = Math.min(apps.size(), 6);
        for (int i = 0; i < limit; i++) {
            Application app = apps.get(i);
            Offer offer = offerService.getOfferById(app.getOfferId());
            applicationsSection.getChildren().add(createApplicationCard(app, offer));
        }

        if (apps.size() > limit) {
            Label more = new Label("View all " + apps.size() + " applications →");
            more.setStyle(
                    "-fx-font-size: 11px; -fx-text-fill: #6366f1; -fx-font-weight: 700; -fx-cursor: hand; -fx-padding: 4 0 0 4;");
            more.setOnMouseClicked(e -> handleJobOffers());
            applicationsSection.getChildren().add(more);
        }
    }

    private VBox createApplicationCard(Application app, Offer offer) {
        VBox card = new VBox(6);

        boolean hasResponse = app.hasResponse();
        String leftAccent = hasResponse ? (app.getStatus().toLowerCase().contains("accept") ? "#22c55e" : "#ef4444")
                : "#6366f1";

        String base = "-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12 14 12 14; " +
                "-fx-border-color: transparent transparent transparent " + leftAccent + "; " +
                "-fx-border-width: 0 0 0 3; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 3, 0, 0, 1); -fx-cursor: hand;";
        String hover = "-fx-background-color: #fafbff; -fx-background-radius: 10; -fx-padding: 12 14 12 14; " +
                "-fx-border-color: transparent transparent transparent #6366f1; " +
                "-fx-border-width: 0 0 0 3; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.12), 6, 0, 0, 2); -fx-cursor: hand;";

        card.setStyle(base);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(base));

        // Title row
        HBox titleRow = new HBox(6);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        String offerTitle = offer != null ? offer.getTitle() : "Offer #" + app.getOfferId();
        Label title = new Label(offerTitle);
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        title.setWrapText(true);
        title.setMaxWidth(220);
        HBox.setHgrow(title, Priority.ALWAYS);
        Label statusBadge = new Label(getDisplayStatus(app));
        statusBadge.setStyle(getStatusBadgeStyle(app));
        titleRow.getChildren().addAll(title, statusBadge);

        // Meta row
        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        if (offer != null) {
            Label dept = new Label("🏢 " + offer.getDepartment());
            dept.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");
            metaRow.getChildren().add(dept);
        }
        long daysAgo = ChronoUnit.DAYS.between(app.getApplicationDate(), java.time.LocalDate.now());
        String timeText = daysAgo == 0 ? "Today" : daysAgo + "d ago";
        Label timeLabel = new Label("⏱ " + timeText);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        metaRow.getChildren().add(timeLabel);

        if (hasResponse) {
            Label respIcon = new Label("✉️");
            respIcon.setStyle("-fx-font-size: 10px;");
            metaRow.getChildren().add(respIcon);
        }

        card.getChildren().addAll(titleRow, metaRow);

        card.setOnMouseClicked(e -> {
            ViewContext.setSelectedApplicationId(app.getId());
            SceneUtil.switchScene("application_result.fxml");
        });

        return card;
    }

    private String getDisplayStatus(Application app) {
        if (app.hasResponse()) {
            String s = app.getStatus() != null ? app.getStatus().toLowerCase() : "";
            if (s.contains("accept"))
                return "✅ Accepted";
            if (s.contains("refus") || s.contains("reject"))
                return "❌ Rejected";
            return "📬 Responded";
        }
        return "⏳ Pending";
    }

    private String getStatusBadgeStyle(Application app) {
        if (app.hasResponse()) {
            String s = app.getStatus() != null ? app.getStatus().toLowerCase() : "";
            if (s.contains("accept"))
                return "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 2 8; -fx-background-radius: 12; -fx-font-size: 9px; -fx-font-weight: bold;";
            return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 2 8; -fx-background-radius: 12; -fx-font-size: 9px; -fx-font-weight: bold;";
        }
        return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 2 8; -fx-background-radius: 12; -fx-font-size: 9px; -fx-font-weight: bold;";
    }

    // ===== RIGHT PANEL: PROFILES FEED =====

    private void setupListView() {
        feedListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(UserViewModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                } else {
                    setGraphic(createProfileCard(item));
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 4 0;");
                }
            }
        });
    }

    private void loadFeed(int currentUserId) {
        masterData.clear();
        masterData.addAll(userDao.findAllActiveProfiles(currentUserId));
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        String query = searchField.getText();
        String sortBy = sortByBox.getValue();
        String sortOrder = sortOrderBox.getValue();

        List<UserViewModel> filtered = masterData.stream()
                .filter(p -> {
                    if (query == null || query.trim().isEmpty())
                        return true;
                    String lower = query.toLowerCase();
                    return p.getFullName().toLowerCase().contains(lower)
                            || p.getProfessionalTitle().toLowerCase().contains(lower)
                            || p.getLocation().toLowerCase().contains(lower);
                }).collect(Collectors.toList());

        Comparator<UserViewModel> comparator = getComparator(sortBy);
        if ("DESC".equals(sortOrder))
            comparator = comparator.reversed();
        filtered.sort(comparator);

        feedListView.setItems(FXCollections.observableArrayList(filtered));
        countLabel.setText(filtered.size() + " profiles");
    }

    private Comparator<UserViewModel> getComparator(String sortBy) {
        if (sortBy == null)
            return Comparator.comparing(UserViewModel::getFullName, String.CASE_INSENSITIVE_ORDER);
        return switch (sortBy) {
            case "Job" -> Comparator.comparing(UserViewModel::getProfessionalTitle, String.CASE_INSENSITIVE_ORDER);
            case "Age" -> Comparator.comparingInt(UserViewModel::getAge);
            case "Location" -> Comparator.comparing(UserViewModel::getLocation, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(UserViewModel::getFullName, String.CASE_INSENSITIVE_ORDER);
        };
    }

    private VBox createProfileCard(UserViewModel profile) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(profile.getFullName().substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("avatar-small");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(profile.getFullName());
        name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label subtitle = new Label(
                profile.getProfessionalTitle().isEmpty() ? profile.getRole() : profile.getProfessionalTitle());
        subtitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");
        info.getChildren().addAll(name, subtitle);

        VBox meta = new VBox(2);
        meta.setAlignment(Pos.CENTER_RIGHT);
        if (!profile.getLocation().isEmpty()) {
            Label loc = new Label("📍 " + profile.getLocation());
            loc.setStyle("-fx-font-size: 9px; -fx-text-fill: #9ca3af;");
            meta.getChildren().add(loc);
        }
        Label roleBadge = new Label(profile.getRole());
        roleBadge.getStyleClass().addAll("badge",
                profile.getRole().equalsIgnoreCase("CANDIDATE") ? "badge-blue" : "badge-purple");
        meta.getChildren().add(roleBadge);

        header.getChildren().addAll(avatar, info, meta);

        // Quick view button
        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button viewBtn = new Button("View Profile");
        viewBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-font-size: 10px; " +
                "-fx-font-weight: 700; -fx-padding: 4 12; -fx-background-radius: 6; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> ProfilePopup.show(profile));
        actions.getChildren().add(viewBtn);

        card.getChildren().addAll(header, actions);
        return card;
    }

    // ===== SIDEBAR NAVIGATION =====
    @FXML
    private void handleDashboard() {
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
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "This feature is coming soon!").show();
    }

    @FXML
    private void handleSettings() {
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
