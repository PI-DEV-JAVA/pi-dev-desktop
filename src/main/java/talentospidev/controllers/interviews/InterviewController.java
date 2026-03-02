package talentospidev.controllers.interviews;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import talentospidev.dao.interviewsDAO.InterviewDAO;
import talentospidev.models.User;
import talentospidev.models.interviews.Interview;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InterviewController {

    @FXML
    private TextField searchField;
    @FXML
    private ListView<Interview> listView;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;
    @FXML
    private Button createBtn;
    @FXML
    private Button calendarBtn;

    private InterviewDAO dao;
    private boolean isRecruiter;
    private static final String IND = "#6366f1";
    private static final String GRN = "#22c55e";
    private static final String RED = "#ef4444";
    private static final String AMB = "#f59e0b";
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    public void initialize() throws SQLException {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        dao = new InterviewDAO();

        // Candidate can only see Calendar, not edit/delete
        User u = AuthService.getCurrentUser();
        isRecruiter = u != null && (u.getRole() == User.Role.HR || u.getRole() == User.Role.ADMIN);
        if (createBtn != null) { createBtn.setVisible(isRecruiter); createBtn.setManaged(isRecruiter); }

        loadData();
        setupListView();
    }

    private void loadData() {
        User u = AuthService.getCurrentUser();
        List<Interview> list;
        if (isRecruiter) {
            list = dao.list(searchField.getText(), true);
        } else {
            // Candidate only sees their own interviews
            list = dao.listByCandidateId(u != null ? u.getId() : 0, searchField.getText(), true);
        }
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

    @FXML
    private void handleCalendar() {
        SceneUtil.switchScene("Interviews/calendarView.fxml");
    }

    // ===== Card-based ListView =====

    private void setupListView() {
        listView.setCellFactory(param -> new ListCell<>() {
            private final VBox card = new VBox(10);
            private final Label titleLbl = new Label();
            private final Label statusBadge = new Label();
            private final Label gradeLbl = new Label();
            private final Label createdLbl = new Label();
            private final Button editBtn = new Button("✏ Edit");
            private final Button deleteBtn = new Button("🗑 Delete");
            private final HBox topRow = new HBox(8);
            private final HBox actions = new HBox(10, editBtn, deleteBtn);
            private final DropShadow shadow = new DropShadow();

            {
                card.setPadding(new Insets(18));
                card.setStyle(
                        "-fx-background-color: white; -fx-background-radius: 14; -fx-border-radius: 14; -fx-border-color: #e5e7eb;");
                shadow.setRadius(8);
                shadow.setOffsetY(2);
                shadow.setColor(Color.rgb(0, 0, 0, 0.06));
                card.setEffect(shadow);

                titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #111827;");
                gradeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
                createdLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

                editBtn.setStyle("-fx-background-color: " + IND
                        + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14; -fx-font-size: 11px; -fx-font-weight: 700;");
                deleteBtn.setStyle("-fx-background-color: " + RED
                        + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 14; -fx-font-size: 11px; -fx-font-weight: 700;");

                topRow.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(titleLbl, Priority.ALWAYS);
                topRow.getChildren().addAll(titleLbl, statusBadge);

                actions.setAlignment(Pos.CENTER_LEFT);
                actions.setPadding(new Insets(6, 0, 0, 0));

                if (isRecruiter) {
                    card.getChildren().addAll(topRow, gradeLbl, createdLbl, new Separator(), actions);
                } else {
                    card.getChildren().addAll(topRow, gradeLbl, createdLbl);
                }

                card.setOnMouseEntered(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(120), card);
                    st.setToX(1.01);
                    st.setToY(1.01);
                    st.play();
                    shadow.setRadius(14);
                    shadow.setColor(Color.rgb(99, 102, 241, 0.15));
                });
                card.setOnMouseExited(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(120), card);
                    st.setToX(1);
                    st.setToY(1);
                    st.play();
                    shadow.setRadius(8);
                    shadow.setColor(Color.rgb(0, 0, 0, 0.06));
                });

                editBtn.setOnAction(e -> openForm(getItem()));
                deleteBtn.setOnAction(e -> confirmDelete(getItem()));
                card.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                        openDetails(getItem());
                    }
                });
            }

            @Override
            protected void updateItem(Interview interview, boolean empty) {
                super.updateItem(interview, empty);
                if (empty || interview == null) {
                    setGraphic(null);
                } else {
                    titleLbl.setText(interview.getTitle());
                    gradeLbl.setText("Grade: " + (interview.getGeneralGrade() != null
                            ? String.format("%.1f / 20", interview.getGeneralGrade())
                            : "—"));
                    createdLbl.setText("Created: "
                            + (interview.getCreatedAt() != null ? interview.getCreatedAt().format(dtFmt) : "—"));
                    String status = interview.getStatus();
                    statusBadge.setText(status != null ? status : "—");
                    statusBadge.setStyle(getStatusStyle(status));
                    setGraphic(card);
                }
            }
        });
    }

    private String getStatusStyle(String status) {
        if (status == null)
            status = "";
        switch (status.toUpperCase()) {
            case "COMPLETED":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "PENDING":
                return "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "IN_PROGRESS":
                return "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
        }
    }

    private void confirmDelete(Interview interview) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Interview");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Delete interview: " + interview.getTitle() + "?");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                dao.delete(interview.getId());
                loadData();
            }
        });
    }

    private void openForm(Interview interview) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Interviews/InterviewForm.fxml"));
            Parent root = loader.load();
            InterviewFormController ctrl = loader.getController();
            ctrl.setInterview(interview);
            ctrl.setDao(dao);
            ctrl.setParentController(this);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(interview == null ? "New Interview" : "Edit Interview");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDetails(Interview interview) {
        ViewContext.setSelectedInterviewId((int) interview.getId());
        SceneUtil.switchScene("Interviews/InterviewDetails.fxml");
    }

    // ===== Sidebar Navigation =====
    @FXML
    private void handleDashboard() {
        User u = AuthService.getCurrentUser();
        if (u != null && u.getRole() == User.Role.HR)
            SceneUtil.switchScene("recruiter_dashboard.fxml");
        else if (u != null && u.getRole() == User.Role.ADMIN)
            SceneUtil.switchScene("admin_dashboard.fxml");
        else
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
    private void handleTrends() {
        SceneUtil.switchScene("MarketTrendsView.fxml");
    }

    @FXML
    private void handleInterviews() {
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
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Coming soon!").showAndWait();
    }

    @FXML
    private void handleMyCircle() {
        SceneUtil.switchScene("my_circle.fxml");
    }

    @FXML
    private void handleNotifications() {
        SceneUtil.switchScene("notifications.fxml");
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
