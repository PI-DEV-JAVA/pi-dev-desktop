package talentospidev.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import talentospidev.dao.SupportTicketDao;
import talentospidev.models.SupportTicket;
import talentospidev.models.TicketReply;
import talentospidev.services.AuthService;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Popup dialog for viewing ticket details and replying.
 * Used by both admin (from dashboard) and users (from notifications).
 */
public class TicketDetailPopup {

    private static final SupportTicketDao dao = new SupportTicketDao();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    /**
     * Shows a ticket detail popup.
     * 
     * @param ticket  the ticket to display
     * @param onClose optional callback when popup closes (for refreshing lists)
     */
    public static void show(SupportTicket ticket, Runnable onClose) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Ticket #" + ticket.getId() + " — " + ticket.getSubject());

        var currentUser = AuthService.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == talentospidev.models.User.Role.ADMIN;

        // Mark replies as read for current user
        if (currentUser != null) {
            dao.markRepliesRead(ticket.getId(), currentUser.getId());
        }

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f0f2f5;");

        // ── Header ──
        VBox header = new VBox(6);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(ticket.getSubject());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Status badge
        Label statusBadge = new Label(ticket.getStatus());
        statusBadge.setStyle(getStatusStyle(ticket.getStatus()));
        titleRow.getChildren().addAll(titleLabel, statusBadge);

        // Category + user + date
        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label catBadge = new Label(ticket.getCategory());
        catBadge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-padding: 2 8; " +
                "-fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: 700;");
        Label userLabel = new Label(
                "👤 " + (ticket.getUserFullName() != null ? ticket.getUserFullName() : ticket.getUserEmail()));
        userLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        Label dateLabel = new Label("📅 " + (ticket.getCreatedAt() != null ? ticket.getCreatedAt().format(FMT) : ""));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        metaRow.getChildren().addAll(catBadge, userLabel, dateLabel);

        header.getChildren().addAll(titleRow, metaRow);

        // ── Original message ──
        VBox msgBox = new VBox(6);
        msgBox.setPadding(new Insets(16, 24, 16, 24));
        msgBox.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        Label msgLabel = new Label(ticket.getMessage());
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        Label msgHeader = new Label("Original Message");
        msgHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #9ca3af;");
        msgBox.getChildren().addAll(msgHeader, msgLabel);

        // ── Replies thread ──
        VBox repliesContainer = new VBox(8);
        repliesContainer.setPadding(new Insets(16, 24, 16, 24));

        List<TicketReply> replies = dao.getReplies(ticket.getId());
        if (replies.isEmpty()) {
            Label noReplies = new Label("No replies yet");
            noReplies.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-font-style: italic;");
            repliesContainer.getChildren().add(noReplies);
        } else {
            Label repliesHeader = new Label("Conversation (" + replies.size() + ")");
            repliesHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #6b7280;");
            repliesContainer.getChildren().add(repliesHeader);
            for (TicketReply reply : replies) {
                repliesContainer.getChildren().add(
                        createReplyBubble(reply, currentUser != null && reply.getSenderId() == currentUser.getId()));
            }
        }

        ScrollPane repliesScroll = new ScrollPane(repliesContainer);
        repliesScroll.setFitToWidth(true);
        repliesScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        repliesScroll.setPrefHeight(250);
        VBox.setVgrow(repliesScroll, Priority.ALWAYS);

        // ── Admin: Status changer ──
        HBox adminBar = new HBox(10);
        adminBar.setAlignment(Pos.CENTER_LEFT);
        adminBar.setPadding(new Insets(0, 24, 0, 24));
        if (isAdmin) {
            Label statusLabel = new Label("Change Status:");
            statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #6b7280;");
            ComboBox<String> statusBox = new ComboBox<>();
            statusBox.getItems().addAll("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");
            statusBox.setValue(ticket.getStatus());
            statusBox.setStyle("-fx-font-size: 11px;");
            statusBox.setOnAction(e -> {
                dao.updateStatus(ticket.getId(), statusBox.getValue());
                statusBadge.setText(statusBox.getValue());
                statusBadge.setStyle(getStatusStyle(statusBox.getValue()));
            });
            adminBar.getChildren().addAll(statusLabel, statusBox);
        }

        // ── Reply input ──
        VBox replySection = new VBox(8);
        replySection.setPadding(new Insets(12, 24, 16, 24));
        replySection.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Type your reply...");
        replyArea.setPrefRowCount(3);
        replyArea.setStyle("-fx-font-size: 12px;");

        HBox replyActions = new HBox(10);
        replyActions.setAlignment(Pos.CENTER_RIGHT);
        Button sendBtn = new Button("📤 Send Reply");
        sendBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-font-weight: 700; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        sendBtn.setOnAction(e -> {
            String text = replyArea.getText();
            if (text == null || text.trim().isEmpty())
                return;
            TicketReply reply = new TicketReply(ticket.getId(), currentUser.getId(), text.trim());
            dao.addReply(reply);
            replyArea.clear();
            // Refresh replies
            repliesContainer.getChildren().clear();
            List<TicketReply> fresh = dao.getReplies(ticket.getId());
            Label repliesHeader = new Label("Conversation (" + fresh.size() + ")");
            repliesHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #6b7280;");
            repliesContainer.getChildren().add(repliesHeader);
            for (TicketReply r : fresh) {
                repliesContainer.getChildren().add(createReplyBubble(r, r.getSenderId() == currentUser.getId()));
            }
        });
        replyActions.getChildren().add(sendBtn);
        replySection.getChildren().addAll(replyArea, replyActions);

        root.getChildren().addAll(header, msgBox, repliesScroll, adminBar, replySection);

        Scene scene = new Scene(root, 580, 560);
        ThemeManager.applyToScene(scene);
        popup.setScene(scene);
        popup.setOnHiding(e -> {
            if (onClose != null)
                onClose.run();
        });
        popup.showAndWait();
    }

    private static VBox createReplyBubble(TicketReply reply, boolean isMine) {
        VBox bubble = new VBox(4);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setMaxWidth(400);

        if (isMine) {
            bubble.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 12 12 4 12;");
            bubble.setAlignment(Pos.CENTER_RIGHT);
            VBox.setMargin(bubble, new Insets(0, 0, 0, 60));
        } else {
            bubble.setStyle("-fx-background-color: white; -fx-background-radius: 12 12 12 4; " +
                    "-fx-border-color: #e5e7eb; -fx-border-radius: 12 12 12 4;");
            VBox.setMargin(bubble, new Insets(0, 60, 0, 0));
        }

        HBox senderRow = new HBox(6);
        senderRow.setAlignment(Pos.CENTER_LEFT);
        Label senderLabel = new Label(reply.getSenderName());
        senderLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #6366f1;");
        String roleTag = reply.getSenderRole().equalsIgnoreCase("ADMIN") ? " (Admin)" : "";
        Label roleLabel = new Label(roleTag);
        roleLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #9ca3af;");
        Label timeLabel = new Label(reply.getCreatedAt() != null ? reply.getCreatedAt().format(FMT) : "");
        timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #9ca3af;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        senderRow.getChildren().addAll(senderLabel, roleLabel, spacer, timeLabel);

        Label msgLabel = new Label(reply.getMessage());
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        bubble.getChildren().addAll(senderRow, msgLabel);
        return bubble;
    }

    private static String getStatusStyle(String status) {
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
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-padding: 3 10; " +
                "-fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
    }
}
