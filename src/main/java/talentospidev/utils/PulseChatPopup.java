package talentospidev.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import talentospidev.dao.SyncMessageDao;
import talentospidev.models.SyncMessage;
import talentospidev.services.AuthService;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Clean chat popup for Pulse messaging between synced users.
 */
public class PulseChatPopup {

    private static final SyncMessageDao msgDao = new SyncMessageDao();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d");

    /**
     * Opens a chat window for a specific sync.
     * 
     * @param syncId        the sync relationship ID
     * @param otherUserName name of the other person
     * @param onClose       callback when popup closes
     */
    public static void show(int syncId, String otherUserName, Runnable onClose) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("💬 Pulse — " + otherUserName);

        var currentUser = AuthService.getCurrentUser();
        int myId = currentUser != null ? currentUser.getId() : 0;

        // Mark messages as read
        msgDao.markRead(syncId, myId);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f0f2f5;");

        // ── Header ──
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color: #6366f1;");

        Label avatar = new Label(otherUserName.substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: 800; -fx-min-width: 36; -fx-min-height: 36; " +
                "-fx-max-width: 36; -fx-max-height: 36; -fx-alignment: center; -fx-background-radius: 18;");
        Label nameLabel = new Label(otherUserName);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: white;");
        Label statusDot = new Label("● In Sync");
        statusDot.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.7);");
        VBox nameBox = new VBox(2, nameLabel, statusDot);
        header.getChildren().addAll(avatar, nameBox);

        // ── Messages area ──
        VBox messagesContainer = new VBox(6);
        messagesContainer.setPadding(new Insets(16, 16, 16, 16));

        ScrollPane scrollPane = new ScrollPane(messagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPrefHeight(350);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Load messages
        loadMessages(messagesContainer, syncId, myId);

        // Auto-scroll to bottom
        scrollPane.setVvalue(1.0);
        messagesContainer.heightProperty().addListener((obs, o, n) -> scrollPane.setVvalue(1.0));

        // ── Input area ──
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setPadding(new Insets(12, 16, 14, 16));
        inputArea.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        TextField inputField = new TextField();
        inputField.setPromptText("Type a pulse...");
        inputField.setStyle("-fx-background-color: #f3f4f6; -fx-border-color: transparent; " +
                "-fx-background-radius: 20; -fx-padding: 10 16; -fx-font-size: 13px;");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button sendBtn = new Button("📤");
        sendBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-min-width: 40; -fx-min-height: 40; -fx-background-radius: 20; -fx-cursor: hand;");

        Runnable sendAction = () -> {
            String text = inputField.getText();
            if (text == null || text.trim().isEmpty())
                return;
            SyncMessage msg = new SyncMessage(syncId, myId, text.trim());
            msgDao.send(msg);
            inputField.clear();
            loadMessages(messagesContainer, syncId, myId);
        };

        sendBtn.setOnAction(e -> sendAction.run());
        inputField.setOnAction(e -> sendAction.run());

        inputArea.getChildren().addAll(inputField, sendBtn);

        root.getChildren().addAll(header, scrollPane, inputArea);

        Scene scene = new Scene(root, 440, 520);
        ThemeManager.applyToScene(scene);
        popup.setScene(scene);
        popup.setOnHiding(e -> {
            if (onClose != null)
                onClose.run();
        });
        popup.showAndWait();
    }

    private static void loadMessages(VBox container, int syncId, int myId) {
        container.getChildren().clear();
        List<SyncMessage> messages = msgDao.getMessages(syncId);

        if (messages.isEmpty()) {
            Label empty = new Label("💬 Start the conversation!\nSend your first pulse.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-text-alignment: center;");
            empty.setWrapText(true);
            VBox emptyBox = new VBox(empty);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60, 0, 0, 0));
            container.getChildren().add(emptyBox);
            return;
        }

        String lastDate = "";
        for (SyncMessage msg : messages) {
            // Date separator
            if (msg.getCreatedAt() != null) {
                String date = msg.getCreatedAt().format(DATE_FMT);
                if (!date.equals(lastDate)) {
                    Label dateSep = new Label(date);
                    dateSep.setStyle("-fx-font-size: 9px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
                    HBox dateBox = new HBox(dateSep);
                    dateBox.setAlignment(Pos.CENTER);
                    dateBox.setPadding(new Insets(8, 0, 4, 0));
                    container.getChildren().add(dateBox);
                    lastDate = date;
                }
            }

            boolean isMine = msg.getSenderId() == myId;
            container.getChildren().add(createBubble(msg, isMine));
        }
    }

    private static HBox createBubble(SyncMessage msg, boolean isMine) {
        VBox bubble = new VBox(2);
        bubble.setPadding(new Insets(8, 12, 8, 12));
        bubble.setMaxWidth(280);

        Label text = new Label(msg.getMessage());
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 13px;");

        Label time = new Label(msg.getCreatedAt() != null ? msg.getCreatedAt().format(TIME_FMT) : "");
        time.setStyle("-fx-font-size: 9px;");

        if (isMine) {
            bubble.setStyle("-fx-background-color: #6366f1; -fx-background-radius: 16 16 4 16;");
            text.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");
            time.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(255,255,255,0.6);");
        } else {
            bubble.setStyle("-fx-background-color: white; -fx-background-radius: 16 16 16 4; " +
                    "-fx-border-color: #e5e7eb; -fx-border-radius: 16 16 16 4;");
            text.setStyle("-fx-font-size: 13px; -fx-text-fill: #111827;");
            time.setStyle("-fx-font-size: 9px; -fx-text-fill: #9ca3af;");
        }

        bubble.getChildren().addAll(text, time);

        HBox row = new HBox(bubble);
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 2, 0));
        return row;
    }
}
