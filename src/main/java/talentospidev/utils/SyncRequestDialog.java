package talentospidev.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import talentospidev.dao.SyncDao;

/**
 * Popup to pick a reason when sending a sync request.
 */
public class SyncRequestDialog {

    private static final SyncDao syncDao = new SyncDao();

    /**
     * Shows the sync reason picker dialog.
     * 
     * @param senderId     who is sending
     * @param receiverId   who they want to sync with
     * @param receiverName display name
     * @param onComplete   callback after request sent
     */
    public static void show(int senderId, int receiverId, String receiverName, Runnable onComplete) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("⚡ Sync with " + receiverName);

        VBox root = new VBox(16);
        root.setPadding(new Insets(28, 32, 24, 32));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white;");

        // Header
        Label emoji = new Label("⚡");
        emoji.setStyle("-fx-font-size: 36px;");
        Label title = new Label("Sync with " + receiverName);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Label subtitle = new Label("Why do you want to sync?");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");

        // Reason buttons
        VBox reasons = new VBox(8);
        reasons.setAlignment(Pos.CENTER);

        String[] reasonKeys = { "COLLABORATE", "LEARN", "MENTOR", "HIRE", "NETWORK" };
        String[] reasonLabels = { "🤝  Collaborate — Work on something together",
                "📚  Learn — Learn from their expertise",
                "🎓  Mentor — Seek or offer mentorship",
                "💼  Hire — Professional opportunity",
                "🌐  Network — Expand your circle" };

        final String[] selectedReason = { "NETWORK" }; // default

        ToggleGroup toggleGroup = new ToggleGroup();
        for (int i = 0; i < reasonKeys.length; i++) {
            RadioButton rb = new RadioButton(reasonLabels[i]);
            rb.setToggleGroup(toggleGroup);
            rb.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151; -fx-cursor: hand;");
            rb.setPadding(new Insets(8, 16, 8, 16));
            final String key = reasonKeys[i];
            rb.setOnAction(e -> selectedReason[0] = key);
            if (key.equals("NETWORK"))
                rb.setSelected(true);
            reasons.getChildren().add(rb);
        }

        // Actions
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-font-size: 12px; " +
                "-fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 10; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> popup.close());

        Button sendBtn = new Button("⚡ Send Sync Request");
        sendBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-font-weight: 700; -fx-padding: 10 24; -fx-background-radius: 10; -fx-cursor: hand;");
        sendBtn.setOnAction(e -> {
            syncDao.sendRequest(senderId, receiverId, selectedReason[0]);
            popup.close();
            // Success toast
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sync Request Sent!");
            alert.setHeaderText("⚡ Request sent to " + receiverName);
            alert.setContentText("They'll see your request and can accept it.");
            alert.show();
            if (onComplete != null)
                onComplete.run();
        });

        actions.getChildren().addAll(cancelBtn, sendBtn);

        root.getChildren().addAll(emoji, title, subtitle, reasons, actions);

        Scene scene = new Scene(root, 420, 400);
        ThemeManager.applyToScene(scene);
        popup.setScene(scene);
        popup.showAndWait();
    }
}
