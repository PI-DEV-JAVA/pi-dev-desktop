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
import talentospidev.dao.SupportTicketDao;
import talentospidev.dao.UserDao;
import talentospidev.models.SupportTicket;
import talentospidev.models.UserViewModel;
import talentospidev.services.AuthService;
import talentospidev.utils.ProfilePopup;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.TicketDetailPopup;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDashboardController {

    // ── Users tab ──
    @FXML
    private ListView<UserViewModel> userListView;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortByBox;
    @FXML
    private ComboBox<String> sortOrderBox;
    @FXML
    private ComboBox<String> roleFilterBox;

    // ── Stats ──
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label activeUsersLabel;
    @FXML
    private Label candidatesLabel;
    @FXML
    private Label recruitersLabel;
    @FXML
    private Label openTicketsLabel;

    // ── Support tab ──
    @FXML
    private TextField ticketSearchField;
    @FXML
    private ComboBox<String> ticketStatusFilter;
    @FXML
    private ComboBox<String> ticketCategoryFilter;
    @FXML
    private Label ticketCountLabel;
    @FXML
    private VBox supportTicketsList;
    @FXML
    private TabPane mainTabPane;

    @FXML
    private VBox sidebar;

    private final UserDao userDao = new UserDao();
    private final SupportTicketDao ticketDao = new SupportTicketDao();
    private ObservableList<UserViewModel> masterData = FXCollections.observableArrayList();
    private List<SupportTicket> allTickets;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.applySidebarIcons(sidebar);

        // ── Users tab setup ──
        sortByBox.setItems(FXCollections.observableArrayList("Name", "Job", "Age", "Location"));
        sortByBox.setValue("Name");
        sortOrderBox.setItems(FXCollections.observableArrayList("ASC", "DESC"));
        sortOrderBox.setValue("ASC");
        roleFilterBox.setItems(FXCollections.observableArrayList("All", "CANDIDATE", "HR", "ADMIN"));
        roleFilterBox.setValue("All");

        setupListView();
        loadUserData();

        searchField.textProperty().addListener((obs, o, n) -> applyUserFilter());
        sortByBox.valueProperty().addListener((obs, o, n) -> applyUserFilter());
        sortOrderBox.valueProperty().addListener((obs, o, n) -> applyUserFilter());
        roleFilterBox.valueProperty().addListener((obs, o, n) -> applyUserFilter());

        // ── Support tab setup ──
        ticketStatusFilter
                .setItems(FXCollections.observableArrayList("All", "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"));
        ticketStatusFilter.setValue("All");
        ticketCategoryFilter.setItems(FXCollections.observableArrayList("All", "BUG", "QUESTION", "FEATURE", "OTHER"));
        ticketCategoryFilter.setValue("All");

        ticketSearchField.textProperty().addListener((obs, o, n) -> applyTicketFilter());
        ticketStatusFilter.valueProperty().addListener((obs, o, n) -> applyTicketFilter());
        ticketCategoryFilter.valueProperty().addListener((obs, o, n) -> applyTicketFilter());

        loadTicketData();
    }

    // ════════════════════════════════════════════
    // USERS TAB
    // ════════════════════════════════════════════

    private void setupListView() {
        userListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(UserViewModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                } else {
                    setGraphic(createUserCard(item));
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 8 0;");
                }
            }
        });
    }

    private void loadUserData() {
        masterData.clear();
        masterData.addAll(userDao.findAllWithDetails());
        updateStats();
        applyUserFilter();
    }

    private void updateStats() {
        totalUsersLabel.setText(String.valueOf(masterData.size()));
        activeUsersLabel.setText(String.valueOf(
                masterData.stream().filter(u -> "active".equals(u.getStatus())).count()));
        candidatesLabel.setText(String.valueOf(
                masterData.stream().filter(u -> "CANDIDATE".equalsIgnoreCase(u.getRole())).count()));
        recruitersLabel.setText(String.valueOf(
                masterData.stream().filter(u -> "HR".equalsIgnoreCase(u.getRole())).count()));
        openTicketsLabel.setText(String.valueOf(ticketDao.countOpen()));
    }

    private void applyUserFilter() {
        String query = searchField.getText();
        String sortBy = sortByBox.getValue();
        String sortOrder = sortOrderBox.getValue();
        String roleFilter = roleFilterBox.getValue();

        List<UserViewModel> filtered = masterData.stream()
                .filter(p -> {
                    if (roleFilter != null && !"All".equals(roleFilter) && !p.getRole().equalsIgnoreCase(roleFilter))
                        return false;
                    if (query == null || query.trim().isEmpty())
                        return true;
                    String lower = query.toLowerCase();
                    return p.getFullName().toLowerCase().contains(lower)
                            || p.getProfessionalTitle().toLowerCase().contains(lower)
                            || p.getLocation().toLowerCase().contains(lower)
                            || p.getEmail().toLowerCase().contains(lower);
                })
                .collect(Collectors.toList());

        Comparator<UserViewModel> comparator = getUserComparator(sortBy);
        if ("DESC".equals(sortOrder))
            comparator = comparator.reversed();
        filtered.sort(comparator);

        userListView.setItems(FXCollections.observableArrayList(filtered));
    }

    private Comparator<UserViewModel> getUserComparator(String sortBy) {
        if (sortBy == null)
            return Comparator.comparing(UserViewModel::getFullName, String.CASE_INSENSITIVE_ORDER);
        return switch (sortBy) {
            case "Job" -> Comparator.comparing(UserViewModel::getProfessionalTitle, String.CASE_INSENSITIVE_ORDER);
            case "Age" -> Comparator.comparingInt(UserViewModel::getAge);
            case "Location" -> Comparator.comparing(UserViewModel::getLocation, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(UserViewModel::getFullName, String.CASE_INSENSITIVE_ORDER);
        };
    }

    private VBox createUserCard(UserViewModel user) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        String initial = user.getFullName().equals("N/A") ? "?" : user.getFullName().substring(0, 1).toUpperCase();
        Label avatar = new Label(initial);
        avatar.getStyleClass().add("avatar-small");

        VBox info = new VBox(2);
        Label name = new Label(user.getFullName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label email = new Label(user.getEmail());
        email.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        info.getChildren().addAll(name, email);

        VBox meta = new VBox(2);
        meta.setAlignment(Pos.CENTER_RIGHT);
        String titleStr = user.getProfessionalTitle().isEmpty() ? "No title" : user.getProfessionalTitle();
        Label title = new Label(titleStr);
        title.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        if (!user.getLocation().isEmpty()) {
            Label loc = new Label("📍 " + user.getLocation());
            loc.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
            meta.getChildren().add(loc);
        }
        meta.getChildren().add(title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(avatar, info, spacer, meta);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label roleBadge = new Label(user.getRole());
        String badgeColor = switch (user.getRole().toUpperCase()) {
            case "ADMIN" -> "badge-green";
            case "HR" -> "badge-purple";
            default -> "badge-blue";
        };
        roleBadge.getStyleClass().addAll("badge", badgeColor);

        Label statusBadge = new Label(user.getStatus().toUpperCase());
        statusBadge.getStyleClass().addAll("badge",
                "active".equals(user.getStatus()) ? "badge-green" : "badge-gray");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Button viewBtn = new Button("View Profile");
        viewBtn.getStyleClass().add("action-button");
        viewBtn.setOnAction(e -> ProfilePopup.show(user));

        Button toggleBtn = new Button("active".equals(user.getStatus()) ? "Deactivate" : "Activate");
        toggleBtn.getStyleClass().add("active".equals(user.getStatus()) ? "action-button-danger" : "action-button");
        toggleBtn.setOnAction(e -> {
            boolean currentlyActive = "active".equals(user.getStatus());
            userDao.setActive(user.getId(), !currentlyActive);
            loadUserData();
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-danger");
        deleteBtn.setOnAction(e -> {
            Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete user \"" + user.getFullName() + "\"? This cannot be undone.")
                    .showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                userDao.delete(user.getId());
                loadUserData();
            }
        });

        footer.getChildren().addAll(roleBadge, statusBadge, footerSpacer, viewBtn, toggleBtn, deleteBtn);
        card.getChildren().addAll(header, footer);
        return card;
    }

    // ════════════════════════════════════════════
    // SUPPORT TICKETS TAB
    // ════════════════════════════════════════════

    private void loadTicketData() {
        allTickets = ticketDao.findAll();
        applyTicketFilter();
    }

    private void applyTicketFilter() {
        String query = ticketSearchField.getText();
        String statusFilter = ticketStatusFilter.getValue();
        String categoryFilter = ticketCategoryFilter.getValue();

        List<SupportTicket> filtered = allTickets.stream()
                .filter(t -> {
                    if (statusFilter != null && !"All".equals(statusFilter)
                            && !t.getStatus().equalsIgnoreCase(statusFilter))
                        return false;
                    if (categoryFilter != null && !"All".equals(categoryFilter)
                            && !t.getCategory().equalsIgnoreCase(categoryFilter))
                        return false;
                    if (query == null || query.trim().isEmpty())
                        return true;
                    String lower = query.toLowerCase();
                    return t.getSubject().toLowerCase().contains(lower)
                            || (t.getUserFullName() != null && t.getUserFullName().toLowerCase().contains(lower))
                            || (t.getUserEmail() != null && t.getUserEmail().toLowerCase().contains(lower));
                })
                .collect(Collectors.toList());

        supportTicketsList.getChildren().clear();
        ticketCountLabel.setText(filtered.size() + " ticket" + (filtered.size() != 1 ? "s" : ""));

        for (SupportTicket ticket : filtered) {
            supportTicketsList.getChildren().add(createTicketCard(ticket));
        }
    }

    private VBox createTicketCard(SupportTicket ticket) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 18, 14, 18));
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1); -fx-cursor: hand;";
        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(baseStyle.replace("white", "#fafbff")
                .replace("rgba(0,0,0,0.04)", "rgba(99,102,241,0.08)")));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        // Row 1: Subject + badges
        HBox row1 = new HBox(8);
        row1.setAlignment(Pos.CENTER_LEFT);

        Label subject = new Label(ticket.getSubject());
        subject.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        HBox.setHgrow(subject, Priority.ALWAYS);

        Label catBadge = new Label(ticket.getCategory());
        catBadge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-padding: 2 8; " +
                "-fx-background-radius: 8; -fx-font-size: 9px; -fx-font-weight: 700;");

        Label statusBadge = new Label(ticket.getStatus().replace("_", " "));
        statusBadge.setStyle(getStatusStyle(ticket.getStatus()));

        row1.getChildren().addAll(subject, catBadge, statusBadge);

        // Row 2: User info + meta
        HBox row2 = new HBox(12);
        row2.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label(
                "👤 " + (ticket.getUserFullName() != null ? ticket.getUserFullName() : ticket.getUserEmail()));
        userLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");

        Label emailLabel = new Label("✉ " + (ticket.getUserEmail() != null ? ticket.getUserEmail() : ""));
        emailLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label("📅 " + (ticket.getCreatedAt() != null ? ticket.getCreatedAt().format(FMT) : ""));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        Label replyBadge = new Label("💬 " + ticket.getReplyCount());
        replyBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");

        row2.getChildren().addAll(userLabel, emailLabel, spacer, replyBadge, dateLabel);

        // Row 3: Message preview
        String preview = ticket.getMessage().length() > 100 ? ticket.getMessage().substring(0, 100) + "..."
                : ticket.getMessage();
        Label previewLabel = new Label(preview);
        previewLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        previewLabel.setWrapText(true);

        card.getChildren().addAll(row1, row2, previewLabel);

        // Click → open detail popup
        card.setOnMouseClicked(e -> TicketDetailPopup.show(ticket, () -> {
            loadTicketData();
            updateStats();
        }));

        return card;
    }

    private String getStatusStyle(String status) {
        String bg, fg;
        switch (status) {
            case "OPEN" -> {
                bg = "#fef3c7";
                fg = "#d97706";
            }
            case "IN_PROGRESS" -> {
                bg = "#dbeafe";
                fg = "#2563eb";
            }
            case "RESOLVED" -> {
                bg = "#dcfce7";
                fg = "#16a34a";
            }
            case "CLOSED" -> {
                bg = "#f3f4f6";
                fg = "#6b7280";
            }
            default -> {
                bg = "#f3f4f6";
                fg = "#6b7280";
            }
        }
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-padding: 2 8; " +
                "-fx-background-radius: 10; -fx-font-size: 9px; -fx-font-weight: 700;";
    }

    // ════════════════════════════════════════════
    // SIDEBAR NAVIGATION
    // ════════════════════════════════════════════

    @FXML
    private void handleDashboard() {
        SceneUtil.switchScene("admin_dashboard.fxml");
    }

    @FXML
    private void handleJobOffers() {
        SceneUtil.switchScene("OffersCardView.fxml");
    }
    @FXML private void handleTrends() { talentospidev.utils.SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews() { talentospidev.utils.SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @javafx.fxml.FXML
    private void handleEvents() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Events/EventsFeed.fxml" : "Events/EventsBrowse.fxml");
    }

    @javafx.fxml.FXML
    private void handleCourses() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Courses/CoursesRH.fxml" : "Courses/CoursesBrowse.fxml");
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
        new Alert(Alert.AlertType.INFORMATION, "Coming soon!").show();
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
