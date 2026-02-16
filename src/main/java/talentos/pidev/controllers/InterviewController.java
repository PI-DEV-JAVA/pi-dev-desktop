package talentos.pidev.controllers;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import talentos.pidev.models.dao.InterviewDAO;
import talentos.pidev.models.schema.Interview;

import java.sql.SQLException;
import java.util.List;

public class InterviewController {

    @FXML private TextField searchField;
    @FXML private ListView<Interview> listView;

    private InterviewDAO dao;

    @FXML
    public void initialize() throws SQLException {
        dao = new InterviewDAO();
        loadData();
        setupListView();
    }

    /* ===============================
       DATA LOADING
    =============================== */

    private void loadData() {
        List<Interview> list = dao.list(searchField.getText(), true);
        listView.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void onSearch() {
        loadData();
    }

    @FXML
    private void onCreate() {
        openForm(null);
    }

    public void refresh() {
        loadData();
    }

    /* ===============================
       LIST VIEW SETUP (Modern Cards)
    =============================== */

    private void setupListView() {

        listView.setCellFactory(param -> new ListCell<>() {

            private final VBox card = new VBox(12);

            private final Label title = new Label();
            private final Label statusBadge = new Label();
            private final Label grade = new Label();
            private final Label created = new Label();

            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            private final HBox topRow = new HBox();
            private final HBox actions = new HBox(10, editBtn, deleteBtn);

            private final DropShadow shadow = new DropShadow();

            {
                /* ---------- Typography ---------- */

                title.setStyle("-fx-font-size:17px; -fx-font-weight:600; -fx-text-fill:#0f172a;");
                grade.setStyle("-fx-text-fill:#475569;");
                created.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11px;");

                /* ---------- Buttons ---------- */

                editBtn.setStyle("""
                        -fx-background-color:#0D203B;
                        -fx-text-fill:white;
                        -fx-background-radius:6;
                        -fx-cursor:hand;
                        """);

                deleteBtn.setStyle("""
                        -fx-background-color:#ef4444;
                        -fx-text-fill:white;
                        -fx-background-radius:6;
                        -fx-cursor:hand;
                        """);

                /* ---------- Layout ---------- */

                topRow.getChildren().addAll(title);
                HBox.setHgrow(title, Priority.ALWAYS);
                topRow.getChildren().add(statusBadge);

                card.getChildren().addAll(topRow, grade, created, actions);

                card.setStyle("""
                        -fx-background-color:white;
                        -fx-padding:18;
                        -fx-background-radius:14;
                        -fx-border-radius:14;
                        -fx-border-color:#e2e8f0;
                        """);

                /* ---------- Shadow ---------- */

                shadow.setRadius(8);
                shadow.setOffsetY(2);
                shadow.setColor(Color.rgb(0,0,0,0.08));
                card.setEffect(shadow);

                /* ---------- Hover Animation ---------- */

                card.setOnMouseEntered(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(120), card);
                    st.setToX(1.02);
                    st.setToY(1.02);
                    st.play();

                    shadow.setRadius(18);
                    shadow.setColor(Color.rgb(0,0,0,0.15));
                });

                card.setOnMouseExited(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(120), card);
                    st.setToX(1);
                    st.setToY(1);
                    st.play();

                    shadow.setRadius(8);
                    shadow.setColor(Color.rgb(0,0,0,0.08));
                });

                /* ---------- Actions ---------- */

                editBtn.setOnAction(e -> openForm(getItem()));
                deleteBtn.setOnAction(e -> confirmDelete(getItem()));

                /* ---------- Double Click ---------- */

                card.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY
                            && event.getClickCount() == 2) {
                        openForm(getItem());
                    }
                });
            }

            @Override
            protected void updateItem(Interview interview, boolean empty) {
                super.updateItem(interview, empty);

                if (empty || interview == null) {
                    setGraphic(null);
                } else {
                    title.setText(interview.getTitle());
                    grade.setText("Grade: " + interview.getGeneralGrade());
                    created.setText("Created: " + interview.getCreatedAt());

                    String status = interview.getStatus();
                    statusBadge.setText(status.toUpperCase());
                    statusBadge.setStyle(getStatusStyle(status));

                    setGraphic(card);
                }
            }
        });
    }

    /* ===============================
       STATUS BADGE STYLE
    =============================== */

    private String getStatusStyle(String status) {

        switch (status.toLowerCase()) {

            case "completed":
                return """
                        -fx-background-color:#dcfce7;
                        -fx-text-fill:#166534;
                        -fx-padding:4 10 4 10;
                        -fx-background-radius:20;
                        -fx-font-size:11px;
                        -fx-font-weight:bold;
                        """;

            case "pending":
                return """
                        -fx-background-color:#fef9c3;
                        -fx-text-fill:#854d0e;
                        -fx-padding:4 10 4 10;
                        -fx-background-radius:20;
                        -fx-font-size:11px;
                        -fx-font-weight:bold;
                        """;

            case "in_progress":
                return """
                        -fx-background-color:#fee2e2;
                        -fx-text-fill:#991b1b;
                        -fx-padding:4 10 4 10;
                        -fx-background-radius:20;
                        -fx-font-size:11px;
                        -fx-font-weight:bold;
                        """;

            default:
                return """
                        -fx-background-color:#e2e8f0;
                        -fx-text-fill:#334155;
                        -fx-padding:4 10 4 10;
                        -fx-background-radius:20;
                        -fx-font-size:11px;
                        -fx-font-weight:bold;
                        """;
        }
    }

    /* ===============================
       DELETE CONFIRMATION
    =============================== */

    private void confirmDelete(Interview interview) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Interview");
        alert.setContentText(
                "Are you sure you want to delete interview: "
                        + interview.getTitle() + " ?"
        );

        alert.initModality(Modality.APPLICATION_MODAL);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dao.delete(interview.getId());
                loadData();
            }
        });
    }

    /* ===============================
       OPEN FORM
    =============================== */

    private void openForm(Interview interview) {

        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/fxml/InterviewForm.fxml"));

            VBox root = loader.load();

            InterviewFormController formController = loader.getController();
            formController.setInterview(interview);
            formController.setDao(dao);
            formController.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(
                    interview == null
                            ? "New Interview"
                            : "Edit Interview"
            );

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
