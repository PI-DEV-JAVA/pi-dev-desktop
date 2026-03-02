package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.SyncDao;
import talentospidev.dao.SyncMessageDao;
import talentospidev.models.Sync;
import talentospidev.models.SyncMessage;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.services.SyncService;
import talentospidev.utils.PulseChatPopup;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.SidebarUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MyCircleController {

    @FXML
    private VBox sidebar;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;
    @FXML
    private Label circleStatsLabel;
    @FXML
    private Label pendingBadge;
    @FXML
    private TabPane circleTabPane;
    @FXML
    private Tab requestsTab;
    @FXML
    private VBox syncsGrid;
    @FXML
    private VBox requestsList;
    @FXML
    private VBox pulseList;

    private final SyncDao syncDao = new SyncDao();
    private final SyncMessageDao msgDao = new SyncMessageDao();
    private final SyncService syncService = new SyncService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private int myId;

    @FXML
    public void initialize() {
        SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;
        myId = user.getId();

        loadAll();
    }

    private void loadAll() {
        loadStats();
        loadSyncs();
        loadRequests();
        loadPulse();
    }

    // ════════════════════════════════
    // STATS
    // ════════════════════════════════

    private void loadStats() {
        int count = syncDao.getSyncCount(myId);
        Map<String, Integer> byReason = syncDao.getSyncCountByReason(myId);

        StringBuilder sb = new StringBuilder();
        sb.append(count).append(" Sync").append(count != 1 ? "s" : "");
        for (var entry : byReason.entrySet()) {
            String emoji = switch (entry.getKey()) {
                case "COLLABORATE" -> " · 🤝 ";
                case "LEARN" -> " · 📚 ";
                case "MENTOR" -> " · 🎓 ";
                case "HIRE" -> " · 💼 ";
                default -> " · 🌐 ";
            };
            sb.append(emoji).append(entry.getValue());
        }
        circleStatsLabel.setText(sb.toString());

        int pending = syncDao.getPendingCount(myId);
        if (pending > 0) {
            pendingBadge.setText(pending + " pending");
            pendingBadge.setVisible(true);
            pendingBadge.setManaged(true);
        } else {
            pendingBadge.setVisible(false);
            pendingBadge.setManaged(false);
        }
    }

    // ════════════════════════════════
    // SYNCS TAB
    // ════════════════════════════════

    private void loadSyncs() {
        syncsGrid.getChildren().clear();
        List<Sync> syncs = syncDao.getSyncsByUser(myId);

        if (syncs.isEmpty()) {
            syncsGrid.getChildren().add(createEmptyState("⚡", "No syncs yet",
                    "Discover people on your Dashboard and start syncing!"));
            return;
        }

        // Use a FlowPane for grid-like cards
        FlowPane grid = new FlowPane(12, 12);
        grid.setPadding(new Insets(0));

        for (Sync sync : syncs) {
            grid.getChildren().add(createSyncCard(sync));
        }
        syncsGrid.getChildren().add(grid);
    }

    private VBox createSyncCard(Sync sync) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1); -fx-cursor: hand;";
        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(baseStyle.replace("white", "#fafbff")
                .replace("rgba(0,0,0,0.04)", "rgba(99,102,241,0.1)")));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        // Avatar
        String name = sync.getOtherUserName(myId);
        Label avatar = new Label(name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?");
        avatar.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 18px; " +
                "-fx-font-weight: 800; -fx-min-width: 44; -fx-min-height: 44; -fx-max-width: 44; " +
                "-fx-max-height: 44; -fx-alignment: center; -fx-background-radius: 22;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        String title = sync.getOtherUserTitle(myId);
        Label titleLabel = new Label(title != null && !title.isEmpty() ? title : "No title");
        titleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        // Reason badge
        Label reasonBadge = new Label(sync.getReasonEmoji() + " " + sync.getReasonLabel());
        reasonBadge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-padding: 2 8; " +
                "-fx-background-radius: 8; -fx-font-size: 9px; -fx-font-weight: 700;");

        // Compatibility
        int compat = syncService.calculateCompatibility(myId, sync.getOtherUserId(myId));
        Label compatLabel = new Label("⚡ " + compat + "% compatible");
        compatLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: " +
                (compat >= 70 ? "#16a34a" : compat >= 40 ? "#d97706" : "#9ca3af") + "; -fx-font-weight: 600;");

        // Chat button
        Button chatBtn = new Button("💬 Pulse");
        chatBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 10px; " +
                "-fx-font-weight: 700; -fx-padding: 4 12; -fx-background-radius: 8; -fx-cursor: hand;");
        chatBtn.setOnAction(e -> PulseChatPopup.show(sync.getId(), name, this::loadAll));

        card.getChildren().addAll(avatar, nameLabel, titleLabel, reasonBadge, compatLabel, chatBtn);
        return card;
    }

    // ════════════════════════════════
    // REQUESTS TAB
    // ════════════════════════════════

    private void loadRequests() {
        requestsList.getChildren().clear();
        List<Sync> pending = syncDao.getPendingForUser(myId);

        if (pending.isEmpty()) {
            requestsList.getChildren().add(createEmptyState("📬", "No pending requests",
                    "When someone sends you a sync request, it'll appear here."));
            return;
        }

        // Update tab text with count
        requestsTab.setText("📬 Requests (" + pending.size() + ")");

        for (Sync req : pending) {
            requestsList.getChildren().add(createRequestCard(req));
        }
    }

    private VBox createRequestCard(Sync req) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1);");

        // Row: avatar + name + reason
        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);

        String name = req.getSenderName();
        Label avatar = new Label(name.substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-font-weight: 800; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; " +
                "-fx-max-height: 40; -fx-alignment: center; -fx-background-radius: 20;");

        VBox info = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        String title = req.getSenderTitle();
        Label titleLabel = new Label(title != null && !title.isEmpty() ? title : req.getSenderRole());
        titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        info.getChildren().addAll(nameLabel, titleLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label reasonBadge = new Label(req.getReasonEmoji() + " " + req.getReasonLabel());
        reasonBadge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 4 10; " +
                "-fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: 700;");

        row1.getChildren().addAll(avatar, info, reasonBadge);

        // Compatibility
        int compat = syncService.calculateCompatibility(myId, req.getSenderId());
        Label compatLabel = new Label("⚡ " + compat + "% compatible with you");
        compatLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6366f1; -fx-font-weight: 600;");

        // Date
        Label dateLabel = new Label("Sent " + (req.getCreatedAt() != null ? req.getCreatedAt().format(FMT) : ""));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button declineBtn = new Button("✖ Decline");
        declineBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-font-size: 11px; " +
                "-fx-font-weight: 600; -fx-padding: 6 16; -fx-background-radius: 8; -fx-cursor: hand;");
        declineBtn.setOnAction(e -> {
            syncDao.declineRequest(req.getId());
            loadAll();
        });

        Button acceptBtn = new Button("⚡ Accept Sync");
        acceptBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 11px; " +
                "-fx-font-weight: 700; -fx-padding: 6 16; -fx-background-radius: 8; -fx-cursor: hand;");
        acceptBtn.setOnAction(e -> {
            syncDao.acceptRequest(req.getId());
            loadAll();
        });

        actions.getChildren().addAll(declineBtn, acceptBtn);

        card.getChildren().addAll(row1, compatLabel, dateLabel, actions);
        return card;
    }

    // ════════════════════════════════
    // PULSE TAB
    // ════════════════════════════════

    private void loadPulse() {
        pulseList.getChildren().clear();
        List<SyncMessage> chats = msgDao.getRecentChats(myId);

        if (chats.isEmpty()) {
            pulseList.getChildren().add(createEmptyState("💬", "No conversations yet",
                    "Open a synced profile and start a Pulse chat!"));
            return;
        }

        for (SyncMessage chat : chats) {
            pulseList.getChildren().add(createChatRow(chat));
        }
    }

    private HBox createChatRow(SyncMessage chat) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-cursor: hand;";
        row.setStyle(baseStyle);
        row.setOnMouseEntered(e -> row.setStyle(baseStyle.replace("white", "#fafbff")));
        row.setOnMouseExited(e -> row.setStyle(baseStyle));

        // Avatar
        String name = chat.getOtherUserName();
        Label avatar = new Label(name.substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: 800; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; " +
                "-fx-max-height: 40; -fx-alignment: center; -fx-background-radius: 20;");

        // Name + preview
        VBox info = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        String preview = chat.getMessage() != null
                ? (chat.getMessage().length() > 40 ? chat.getMessage().substring(0, 40) + "..." : chat.getMessage())
                : "";
        Label previewLabel = new Label(preview);
        previewLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        info.getChildren().addAll(nameLabel, previewLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Right side: time + unread badge
        VBox meta = new VBox(4);
        meta.setAlignment(Pos.CENTER_RIGHT);
        Label timeLabel = new Label(chat.getCreatedAt() != null ? chat.getCreatedAt().format(TIME_FMT) : "");
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        meta.getChildren().add(timeLabel);

        if (chat.getUnreadCount() > 0) {
            Label unread = new Label(String.valueOf(chat.getUnreadCount()));
            unread.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 9px; " +
                    "-fx-font-weight: 800; -fx-min-width: 20; -fx-min-height: 20; -fx-max-width: 20; " +
                    "-fx-max-height: 20; -fx-alignment: center; -fx-background-radius: 10;");
            meta.getChildren().add(unread);
        }

        row.getChildren().addAll(avatar, info, meta);

        // Click to open chat
        row.setOnMouseClicked(e -> PulseChatPopup.show(chat.getSyncId(), name, this::loadAll));

        return row;
    }

    // ════════════════════════════════
    // HELPERS
    // ════════════════════════════════

    private VBox createEmptyState(String emoji, String title, String subtitle) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 0, 0, 0));
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 40px;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #374151;");
        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        subLabel.setWrapText(true);
        box.getChildren().addAll(emojiLabel, titleLabel, subLabel);
        return box;
    }

    // ════════════════════════════════
    // SIDEBAR NAVIGATION
    // ════════════════════════════════

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
    @FXML private void handleTrends() { talentospidev.utils.SceneUtil.switchScene("MarketTrendsView.fxml"); }

    @FXML
    private void handleMyCircle() {
        /* Already here */ }

    @FXML
    private void handleNotifications() {
        SceneUtil.switchScene("notifications.fxml");
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
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
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
