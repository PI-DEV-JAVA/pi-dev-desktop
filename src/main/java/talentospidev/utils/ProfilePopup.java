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
import talentospidev.models.UserViewModel;

/**
 * Opens a styled modal window showing a user's detailed profile.
 */
public class ProfilePopup {

    public static void show(UserViewModel profile) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(SceneUtil.getStage());
        popup.setTitle("Profile — " + profile.getFullName());
        popup.setResizable(false);

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

        HBox headerRow = new HBox(18, avatar, new VBox(4, nameLabel, titleLabel));
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

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.3;");

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("secondary-button");
        closeBtn.setOnAction(e -> popup.close());

        HBox footer = new HBox(closeBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);

        body.getChildren().addAll(grid, sep, summaryBox, footer);

        // ============ ROOT ============
        VBox root = new VBox(header, body);
        root.getStyleClass().add("popup-root");

        Scene scene = new Scene(root, 480, 500);
        scene.getStylesheets().add(ProfilePopup.class.getResource("/style/app.css").toExternalForm());
        popup.setScene(scene);
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
