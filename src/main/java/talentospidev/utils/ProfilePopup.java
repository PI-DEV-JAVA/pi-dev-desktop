package talentospidev.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import talentospidev.dao.SyncDao;
import talentospidev.models.Sync;
import talentospidev.models.UserViewModel;
import talentospidev.services.AuthService;
import talentospidev.services.SyncService;

import java.util.List;

/**
 * Opens a styled modal window showing a user's detailed profile.
 * Now includes ⚡ Sync functionality and compatibility score.
 */
public class ProfilePopup {

    private static final SyncDao syncDao = new SyncDao();
    private static final SyncService syncService = new SyncService();

    public static void show(UserViewModel profile) {
        show(profile, null);
    }

    public static void show(UserViewModel profile, Runnable onClose) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(SceneUtil.getStage());
        popup.setTitle("Profile — " + profile.getFullName());
        popup.setResizable(false);

        var currentUser = AuthService.getCurrentUser();
        int myId = currentUser != null ? currentUser.getId() : 0;
        boolean isMyProfile = myId == profile.getId();

        // ============ HEADER ============
        VBox header = new VBox(6);
        header.getStyleClass().add("popup-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(profile.getFullName().substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("avatar-large");

        Label nameLabel = new Label(profile.getFullName());
        nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        String title = profile.getProfessionalTitle().isEmpty() ? profile.getRole() : profile.getProfessionalTitle();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.7);");

        // Sync count
        int syncCount = syncDao.getSyncCount(profile.getId());
        Label syncCountLabel = new Label("⚡ " + syncCount + " Sync" + (syncCount != 1 ? "s" : ""));
        syncCountLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: rgba(255,255,255,0.8);");

        VBox nameBox = new VBox(4, nameLabel, titleLabel, syncCountLabel);
        HBox headerRow = new HBox(18, avatar, nameBox);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Role badge
        Label roleBadge = new Label(profile.getRole().toUpperCase());
        roleBadge.getStyleClass().addAll("badge",
                profile.getRole().equalsIgnoreCase("CANDIDATE") ? "badge-blue" : "badge-purple");
        roleBadge.setStyle("-fx-text-fill: white; -fx-background-color: rgba(255,255,255,0.15);");

        HBox badgeRow = new HBox(roleBadge);
        badgeRow.setPadding(new Insets(6, 0, 0, 62));

        header.getChildren().addAll(headerRow, badgeRow);

        // ============ BODY ============
        VBox body = new VBox(18);
        body.getStyleClass().add("popup-body");

        // ── Compatibility Section (only for other users) ──
        if (!isMyProfile && currentUser != null) {
            int compat = syncService.calculateCompatibility(myId, profile.getId());
            List<String> shared = syncService.getSharedSkills(myId, profile.getId());

            HBox compatRow = new HBox(12);
            compatRow.setAlignment(Pos.CENTER_LEFT);
            compatRow.setPadding(new Insets(10, 16, 10, 16));
            String compatBg = compat >= 70 ? "#dcfce7" : compat >= 40 ? "#fef3c7" : "#f3f4f6";
            String compatFg = compat >= 70 ? "#16a34a" : compat >= 40 ? "#d97706" : "#9ca3af";
            compatRow.setStyle("-fx-background-color: " + compatBg + "; -fx-background-radius: 10;");

            Label compatLabel = new Label("⚡ " + compat + "% compatible");
            compatLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: " + compatFg + ";");

            if (!shared.isEmpty()) {
                Label sharedLabel = new Label("Shared: " + String.join(", ", shared));
                sharedLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");
                sharedLabel.setWrapText(true);
                compatRow.getChildren().addAll(compatLabel, new Region(), sharedLabel);
                HBox.setHgrow(compatRow.getChildren().get(1), Priority.ALWAYS);
            } else {
                compatRow.getChildren().add(compatLabel);
            }

            body.getChildren().add(compatRow);
        }

        // Info Grid
        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(16);

        addDetail(grid, 0, 0, "📧  Email", profile.getEmail());
        addDetail(grid, 1, 0, "📞  Phone", profile.getPhoneNumber());
        addDetail(grid, 0, 1, "📍  Location", profile.getLocation().isEmpty() ? "—" : profile.getLocation());
        addDetail(grid, 1, 1, "🎂  Age", profile.getAge() > 0 ? profile.getAge() + " years" : "—");
        addDetail(grid, 0, 2, "💼  Title",
                profile.getProfessionalTitle().isEmpty() ? "—" : profile.getProfessionalTitle());
        addDetail(grid, 1, 2, "📅  Joined", profile.getJoinDate() != null ? profile.getJoinDate().toString() : "—");

        // Summary
        VBox summaryBox = new VBox(4);
        Label summaryLabel = new Label("About");
        summaryLabel.getStyleClass().add("detail-label");
        String summaryText = profile.getSummary().isEmpty() ? "No summary provided." : profile.getSummary();
        Label summaryValue = new Label(summaryText);
        summaryValue.getStyleClass().add("detail-value");
        summaryValue.setWrapText(true);
        summaryValue.setMaxWidth(380);
        summaryBox.getChildren().addAll(summaryLabel, summaryValue);

        // Skills Section
        var skillDao = new talentospidev.dao.SkillDao();
        var userSkills = skillDao.getSkills(profile.getId());
        VBox skillsBox = new VBox(6);
        Label skillsLabel = new Label("Skills");
        skillsLabel.getStyleClass().add("detail-label");
        javafx.scene.layout.FlowPane skillsFlow = new javafx.scene.layout.FlowPane(6, 6);
        if (userSkills.isEmpty()) {
            Label noSkills = new Label("No skills listed");
            noSkills.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px; -fx-font-style: italic;");
            skillsFlow.getChildren().add(noSkills);
        } else {
            for (String skill : userSkills) {
                Label badge = new Label(skill);
                badge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-padding: 3 10; " +
                        "-fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;");
                skillsFlow.getChildren().add(badge);
            }
        }
        skillsBox.getChildren().addAll(skillsLabel, skillsFlow);

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.3;");

        // ── Footer with Sync button ──
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("secondary-button");
        closeBtn.setOnAction(e -> popup.close());

        footer.getChildren().add(closeBtn);

        // Add Sync button if viewing another user's profile
        if (!isMyProfile && currentUser != null) {
            Sync existing = syncDao.getSyncBetween(myId, profile.getId());

            if (existing == null) {
                // No relationship — show "Sync" button
                Button syncBtn = new Button("⚡ Sync");
                syncBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 12px; " +
                        "-fx-font-weight: 700; -fx-padding: 8 20; -fx-background-radius: 10; -fx-cursor: hand;");
                syncBtn.setOnAction(e -> {
                    popup.close();
                    SyncRequestDialog.show(myId, profile.getId(), profile.getFullName(), null);
                });
                footer.getChildren().add(0, syncBtn);
            } else if ("ACCEPTED".equals(existing.getStatus())) {
                // Already synced — show "Pulse" button
                Label syncedLabel = new Label("✅ In Sync");
                syncedLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #16a34a; " +
                        "-fx-background-color: #dcfce7; -fx-padding: 6 12; -fx-background-radius: 8;");
                Button pulseBtn = new Button("💬 Pulse");
                pulseBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 12px; " +
                        "-fx-font-weight: 700; -fx-padding: 8 16; -fx-background-radius: 10; -fx-cursor: hand;");
                pulseBtn.setOnAction(e -> {
                    popup.close();
                    PulseChatPopup.show(existing.getId(), profile.getFullName(), onClose);
                });
                footer.getChildren().addAll(0, java.util.List.of(syncedLabel, pulseBtn));
            } else if ("PENDING".equals(existing.getStatus())) {
                Label pendingLabel = new Label("⏳ Sync Pending");
                pendingLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #d97706; " +
                        "-fx-background-color: #fef3c7; -fx-padding: 6 12; -fx-background-radius: 8;");
                footer.getChildren().add(0, pendingLabel);
            }
        }

        body.getChildren().addAll(grid, sep, summaryBox, skillsBox, footer);

        // ============ ROOT ============
        VBox root = new VBox(header, body);
        root.getStyleClass().add("popup-root");

        Scene scene = new Scene(root, 500, 620);
        scene.getStylesheets().add(ProfilePopup.class.getResource("/style/app.css").toExternalForm());
        ThemeManager.applyToScene(scene);
        popup.setScene(scene);
        popup.setOnHiding(e -> {
            if (onClose != null)
                onClose.run();
        });
        popup.showAndWait();
    }

    private static void addDetail(GridPane grid, int col, int row, String label, String value) {
        VBox box = new VBox(2);
        Label l = new Label(label);
        l.getStyleClass().add("detail-label");
        Label v = new Label(value != null ? value : "—");
        v.getStyleClass().add("detail-value");
        box.getChildren().addAll(l, v);
        grid.add(box, col, row);
    }
}
