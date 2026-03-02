package talentospidev.controllers.interviews;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.interviewsDAO.InterviewDAO;
import talentospidev.dao.interviewsDAO.InterviewMeetDAO;
import talentospidev.models.User;
import talentospidev.models.interviews.Interview;
import talentospidev.models.interviews.InterviewMeet;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;
import talentospidev.utils.generator;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InterviewDetailsController {

    @FXML
    private VBox detailsContainer;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private Interview interview;
    private static final String IND = "#6366f1";
    private static final String GRN = "#22c55e";
    private static final String RED = "#ef4444";
    private static final String AMB = "#f59e0b";
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int interviewId = ViewContext.getSelectedInterviewId();
        if (interviewId <= 0) {
            showError("No interview selected.");
            return;
        }
        InterviewDAO dao;
        try {
            dao = new InterviewDAO();
        } catch (java.sql.SQLException e) {
            showError("Database connection error.");
            return;
        }
        interview = dao.findById(interviewId);
        if (interview == null) {
            showError("Interview not found.");
            return;
        }
        buildDetailsView();
    }

    private void buildDetailsView() {
        detailsContainer.getChildren().clear();

        // Back button
        Button backBtn = new Button("← Back to Interviews");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + IND
                + "; -fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 0;");
        backBtn.setOnAction(e -> SceneUtil.switchScene("Interviews/InterviewView.fxml"));

        // Header card
        VBox headerCard = createCard();
        VBox headerContent = new VBox(12);
        headerContent.setPadding(new Insets(24, 28, 24, 28));

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("🎤 " + interview.getTitle());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        HBox.setHgrow(title, Priority.ALWAYS);
        Label statusBadge = new Label(interview.getStatus());
        statusBadge.setStyle(statusStyle(interview.getStatus()));
        titleRow.getChildren().addAll(title, statusBadge);

        HBox infoRow = new HBox(24);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.getChildren().addAll(
                infoTag("Grade",
                        interview.getGeneralGrade() != null ? String.format("%.1f / 20", interview.getGeneralGrade())
                                : "—",
                        IND),
                infoTag("Created", interview.getCreatedAt() != null ? interview.getCreatedAt().format(dtFmt) : "—",
                        "#3b82f6"),
                infoTag("Candidate", "#" + interview.getCandidateId(), "#8b5cf6"));

        headerContent.getChildren().addAll(titleRow, infoRow);
        headerCard.getChildren().add(headerContent);

        // Sessions section
        VBox sessionsCard = createCard();
        VBox sessionsContent = new VBox(14);
        sessionsContent.setPadding(new Insets(24, 28, 24, 28));

        HBox sessionsHeader = new HBox(12);
        sessionsHeader.setAlignment(Pos.CENTER_LEFT);
        Label sessTitle = new Label("📅 Meeting Sessions");
        sessTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        User u = AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == User.Role.HR || u.getRole() == User.Role.ADMIN);

        if (isRecruiter) {
            Button addBtn = new Button("+ Add Session");
            addBtn.setStyle("-fx-background-color: " + GRN
                    + "; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-weight: 700; -fx-cursor: hand;");
            addBtn.setOnAction(e -> showAddSessionForm(sessionsContent));
            sessionsHeader.getChildren().addAll(sessTitle, sp, addBtn);
        } else {
            sessionsHeader.getChildren().addAll(sessTitle, sp);
        }

        sessionsContent.getChildren().add(sessionsHeader);
        loadSessions(sessionsContent);
        sessionsCard.getChildren().add(sessionsContent);

        detailsContainer.getChildren().addAll(backBtn, headerCard, sessionsCard);
    }

    private void loadSessions(VBox container) {
        try {
            InterviewMeetDAO meetDAO = new InterviewMeetDAO();
            List<InterviewMeet> meets = meetDAO.listByInterview(interview.getId());
            for (int i = 0; i < meets.size(); i++) {
                container.getChildren().add(createSessionRow(meets.get(i), i));
            }
            if (meets.isEmpty()) {
                Label empty = new Label("No sessions scheduled yet");
                empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-padding: 16;");
                container.getChildren().add(empty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createSessionRow(InterviewMeet meet, int index) {
        HBox row = new HBox(16);
        row.setPadding(new Insets(14, 16, 14, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        String bg = index % 2 == 0 ? "#f9fafb" : "white";
        row.setStyle("-fx-background-color: " + bg
                + "; -fx-background-radius: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10;");

        VBox uuidBox = new VBox(2);
        Label uuidLbl = new Label("ID");
        uuidLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        Label uuidVal = new Label(meet.getUuid());
        uuidVal.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        uuidBox.getChildren().addAll(uuidLbl, uuidVal);

        VBox schedBox = new VBox(2);
        Label schedLbl = new Label("Scheduled");
        schedLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        Label schedVal = new Label(meet.getScheduledAt() != null ? meet.getScheduledAt().format(dtFmt) : "—");
        schedVal.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #374151;");
        schedBox.getChildren().addAll(schedLbl, schedVal);

        Label statusLbl = new Label(meet.getStatus());
        statusLbl.setStyle(statusStyle(meet.getStatus()));

        VBox gradeBox = new VBox(2);
        Label grLbl = new Label("Grade");
        grLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        Label grVal = new Label(meet.getGrade() != null ? String.format("%.1f", meet.getGrade()) : "—");
        grVal.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: " + IND + ";");
        gradeBox.getChildren().addAll(grLbl, grVal);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button joinBtn = new Button("Join ▶");
        joinBtn.setStyle("-fx-background-color: " + IND
                + "; -fx-text-fill: white; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 700; -fx-font-size: 11px; -fx-cursor: hand;");
        joinBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/fxml/Interviews/videoPreview.fxml"));
                javafx.scene.Parent root = loader.load();
                MediaController ctrl = loader.getController();
                ctrl.initData(meet);
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Interview Meeting - " + meet.getUuid());
                stage.setScene(new javafx.scene.Scene(root));
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        row.getChildren().addAll(uuidBox, schedBox, statusLbl, gradeBox, spacer, joinBtn);
        return row;
    }

    private void showAddSessionForm(VBox parent) {
        VBox form = new VBox(12);
        form.setPadding(new Insets(16));
        form.setStyle("-fx-background-color: #f5f3ff; -fx-background-radius: 12; -fx-border-color: " + IND
                + "; -fx-border-radius: 12;");

        Label formTitle = new Label("➕ New Session");
        formTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: " + IND + ";");

        HBox dateRow = new HBox(12);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        DatePicker dp = new DatePicker();
        dp.setPromptText("Date");
        TextField timeFld = new TextField();
        timeFld.setPromptText("HH:mm");
        timeFld.setPrefWidth(80);
        dateRow.getChildren().addAll(new Label("📅"), dp, new Label("🕐"), timeFld);

        HBox gradeRow = new HBox(12);
        gradeRow.setAlignment(Pos.CENTER_LEFT);
        TextField gradeFld = new TextField();
        gradeFld.setPromptText("Grade (0-20)");
        gradeFld.setPrefWidth(120);
        ChoiceBox<String> statusCb = new ChoiceBox<>();
        statusCb.getItems().addAll("PENDING", "IN_PROGRESS", "COMPLETED");
        statusCb.setValue("PENDING");
        gradeRow.getChildren().addAll(new Label("📊"), gradeFld, statusCb);

        HBox btns = new HBox(10);
        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: " + GRN
                + "; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 8; -fx-font-weight: 700; -fx-cursor: hand;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> parent.getChildren().remove(form));
        btns.getChildren().addAll(saveBtn, cancelBtn);

        saveBtn.setOnAction(e -> {
            if (dp.getValue() == null || timeFld.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Date and time required").showAndWait();
                return;
            }
            try {
                String[] parts = timeFld.getText().split(":");
                int h = Integer.parseInt(parts[0]), m = Integer.parseInt(parts[1]);
                java.time.LocalDateTime scheduled = dp.getValue().atTime(h, m);
                if (scheduled.isBefore(java.time.LocalDateTime.now())) {
                    new Alert(Alert.AlertType.WARNING, "Cannot schedule in the past").showAndWait();
                    return;
                }
                Double grade = null;
                if (!gradeFld.getText().isEmpty())
                    grade = Double.parseDouble(gradeFld.getText());

                InterviewMeet meet = new InterviewMeet();
                meet.setInterviewId(interview.getId());
                meet.setScheduledAt(scheduled);
                meet.setGrade(grade);
                meet.setStatus(statusCb.getValue());
                meet.setUuid(generator.generateUUID8());

                new InterviewMeetDAO().create(meet);
                parent.getChildren().remove(form);
                // Reload
                int idx = 1; // After header
                while (idx < parent.getChildren().size()) {
                    if (parent.getChildren().get(idx) instanceof HBox)
                        parent.getChildren().remove(idx);
                    else
                        idx++;
                }
                loadSessions(parent);
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
            }
        });

        form.getChildren().addAll(formTitle, dateRow, gradeRow, btns);
        parent.getChildren().add(1, form); // After section title
    }

    // ===== UI Helpers =====
    private VBox createCard() {
        VBox card = new VBox();
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3);");
        return card;
    }

    private VBox infoTag(String label, String value, String color) {
        VBox tag = new VBox(2);
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        tag.getChildren().addAll(l, v);
        return tag;
    }

    private String statusStyle(String s) {
        if (s == null)
            s = "";
        switch (s.toUpperCase()) {
            case "COMPLETED":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "PENDING":
                return "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "IN_PROGRESS":
                return "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
        }
    }

    private void showError(String msg) {
        Label err = new Label("⚠ " + msg);
        err.setStyle("-fx-font-size: 16px; -fx-text-fill: " + RED + "; -fx-padding: 40;");
        detailsContainer.getChildren().add(err);
    }

    // ===== Sidebar =====
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
        SceneUtil.switchScene("Interviews/InterviewView.fxml");
    }
    @javafx.fxml.FXML
    private void handleCourses() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Courses/CoursesRH.fxml" : "Courses/CoursesBrowse.fxml");
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
