package talentospidev.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import talentospidev.dao.SupportTicketDao;
import talentospidev.models.SupportTicket;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.SidebarUtil;
import talentospidev.utils.TicketDetailPopup;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsController {

    @FXML
    private VBox sidebar;
    @FXML
    private Label unreadBadge;
    @FXML
    private ComboBox<String> statusFilterBox;
    @FXML
    private Label ticketCountLabel;
    @FXML
    private VBox ticketsList;
    @FXML
    private VBox emptyState;

    private final SupportTicketDao ticketDao = new SupportTicketDao();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private List<SupportTicket> allTickets;

    @FXML
    public void initialize() {
        SidebarUtil.applySidebarIcons(sidebar);

        statusFilterBox.setItems(FXCollections.observableArrayList(
                "All", "Open", "In Progress", "Resolved", "Closed"));
        statusFilterBox.setValue("All");
        statusFilterBox.valueProperty().addListener((obs, o, n) -> applyFilter());

        loadTickets();
    }

    private void loadTickets() {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;

        allTickets = ticketDao.findByUserId(user.getId());

        // Unread badge
        int unread = ticketDao.countUnreadForUser(user.getId());
        if (unread > 0) {
            unreadBadge.setText(unread + " new");
            unreadBadge.setVisible(true);
            unreadBadge.setManaged(true);
        } else {
            unreadBadge.setVisible(false);
            unreadBadge.setManaged(false);
        }

        applyFilter();
    }

    private void applyFilter() {
        String filter = statusFilterBox.getValue();
        List<SupportTicket> filtered;
        if (filter == null || "All".equals(filter)) {
            filtered = allTickets;
        } else {
            String dbStatus = switch (filter) {
                case "Open" -> "OPEN";
                case "In Progress" -> "IN_PROGRESS";
                case "Resolved" -> "RESOLVED";
                case "Closed" -> "CLOSED";
                default -> "";
            };
            filtered = allTickets.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(dbStatus))
                    .collect(Collectors.toList());
        }

        ticketsList.getChildren().clear();
        ticketCountLabel.setText(filtered.size() + " ticket" + (filtered.size() != 1 ? "s" : ""));

        if (filtered.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
        } else {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            for (SupportTicket ticket : filtered) {
                ticketsList.getChildren().add(createTicketCard(ticket));
            }
        }
    }

    private VBox createTicketCard(SupportTicket ticket) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16, 20, 16, 20));
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1); -fx-cursor: hand;";
        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(baseStyle.replace("white", "#fafbff")
                .replace("rgba(0,0,0,0.04)", "rgba(99,102,241,0.08)")));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        // Row 1: Subject + status + unread dot
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        if (ticket.getUnreadCount() > 0) {
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 10px;");
            titleRow.getChildren().add(dot);
        }

        Label subject = new Label(ticket.getSubject());
        subject.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        HBox.setHgrow(subject, Priority.ALWAYS);

        Label statusBadge = new Label(ticket.getStatus().replace("_", " "));
        statusBadge.setStyle(getStatusStyle(ticket.getStatus()));

        Label catBadge = new Label(ticket.getCategory());
        catBadge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-padding: 2 8; " +
                "-fx-background-radius: 8; -fx-font-size: 9px; -fx-font-weight: 700;");

        titleRow.getChildren().addAll(subject, catBadge, statusBadge);

        // Row 2: Preview + meta
        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        String preview = ticket.getMessage().length() > 80 ? ticket.getMessage().substring(0, 80) + "..."
                : ticket.getMessage();
        Label previewLabel = new Label(preview);
        previewLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        HBox.setHgrow(previewLabel, Priority.ALWAYS);

        Label dateLabel = new Label("📅 " + (ticket.getCreatedAt() != null ? ticket.getCreatedAt().format(FMT) : ""));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        Label replyCount = new Label("💬 " + ticket.getReplyCount());
        replyCount.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        metaRow.getChildren().addAll(previewLabel, replyCount, dateLabel);

        card.getChildren().addAll(titleRow, metaRow);

        // Click to open detail
        card.setOnMouseClicked(e -> TicketDetailPopup.show(ticket, this::loadTickets));

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

    // ═══ SIDEBAR ═══

    @FXML
    private void handleDashboard() {
        var user = AuthService.getCurrentUser();
        if (user == null)
            return;
        switch (user.getRole()) {
            case ADMIN -> SceneUtil.switchScene("admin_dashboard.fxml");
            case HR -> SceneUtil.switchScene("recruiter_dashboard.fxml");
            default -> SceneUtil.switchScene("dashboard.fxml");
        }
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
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleSettings() {
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handleNotifications() {
        /* Already here */ }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
