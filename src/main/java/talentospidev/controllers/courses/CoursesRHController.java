package talentospidev.controllers.courses;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.coursesDAO.FormationDAO;
import talentospidev.models.User;
import talentospidev.models.courses.Formation;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CoursesRHController {

    @FXML private ListView<Formation> listView;
    @FXML private TextField searchField;
    @FXML private Button createBtn;
    @FXML private VBox sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final FormationDAO dao = new FormationDAO();
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        setupListView();
        loadData();
    }

    private void loadData() {
        try {
            User u = AuthService.getCurrentUser();
            List<Formation> list;
            if (u != null) {
                list = dao.getByRecruiterId(u.getId());
            } else {
                list = dao.getAll();
            }
            String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
            if (!q.isEmpty()) {
                list = list.stream().filter(f ->
                        (f.getNom() != null && f.getNom().toLowerCase().contains(q))
                        || (f.getCategorie() != null && f.getCategorie().toLowerCase().contains(q))
                ).toList();
            }
            listView.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void onSearch() { loadData(); }

    @FXML
    private void onCreate() {
        ViewContext.setSelectedFormationId(0); // 0 = new course
        SceneUtil.switchScene("Courses/CourseForm.fxml");
    }

    private void setupListView() {
        listView.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Formation f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) { setGraphic(null); setText(null); setStyle("-fx-background-color: transparent; -fx-padding: 0;"); return; }

                VBox card = new VBox(10);
                card.setPadding(new Insets(18));
                String baseStyle = "-fx-background-color: white; -fx-background-radius: 14; " +
                        "-fx-border-color: #e5e7eb; -fx-border-radius: 14; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2); -fx-cursor: hand;";
                card.setStyle(baseStyle);
                card.setOnMouseEntered(e -> card.setStyle(baseStyle.replace("white", "#fafbff")
                        .replace("rgba(0,0,0,0.04)", "rgba(99,102,241,0.08)")));
                card.setOnMouseExited(e -> card.setStyle(baseStyle));

                // Top row
                HBox topRow = new HBox(8);
                topRow.setAlignment(Pos.CENTER_LEFT);
                Label titleLbl = new Label(f.getNom());
                titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #111827;");
                HBox.setHgrow(titleLbl, Priority.ALWAYS);

                Label modeBadge = new Label(s(f.getMode()));
                modeBadge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-background-radius: 10; " +
                        "-fx-padding: 3 10; -fx-font-size: 10px; -fx-font-weight: 700;");

                Label statusBadge = new Label(s(f.getStatut()));
                String sc = "OUVERTE".equals(f.getStatut()) ? "#dcfce7;-fx-text-fill:#166534" :
                        "EN_COURS".equals(f.getStatut()) ? "#dbeafe;-fx-text-fill:#1d4ed8" : "#f3f4f6;-fx-text-fill:#6b7280";
                statusBadge.setStyle("-fx-background-color: " + sc.split(";")[0] + "; " + sc.split(";")[1] + "; " +
                        "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 10px; -fx-font-weight: 700;");

                topRow.getChildren().addAll(titleLbl, modeBadge, statusBadge);

                // Meta
                Label catLbl = new Label("📂 " + s(f.getCategorie()) + "  •  👤 " + s(f.getFormateur()) + "  •  👥 " + f.getCapaciteMax());
                catLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
                Label dateLbl = new Label("📅 " + (f.getDateDebut() != null ? dtFmt.format(f.getDateDebut()) : "") +
                        " → " + (f.getDateFin() != null ? dtFmt.format(f.getDateFin()) : ""));
                dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

                // Actions
                HBox actions = new HBox(8);
                actions.setAlignment(Pos.CENTER_RIGHT);
                Button viewBtn = new Button("View Details");
                viewBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 8; " +
                        "-fx-padding: 6 14; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> { ViewContext.setSelectedFormationId(f.getId()); SceneUtil.switchScene("Courses/CourseDetails.fxml"); });

                Button editBtn = new Button("✏ Edit");
                editBtn.setStyle("-fx-background-color: white; -fx-text-fill: #6366f1; -fx-background-radius: 8; " +
                        "-fx-padding: 6 12; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand; -fx-border-color: #6366f1; -fx-border-radius: 8;");
                editBtn.setOnAction(e -> {
                    ViewContext.setSelectedFormationId(f.getId());
                    SceneUtil.switchScene("Courses/CourseForm.fxml");
                });

                Button delBtn = new Button("🗑");
                delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 8; " +
                        "-fx-padding: 6 10; -fx-cursor: hand;");
                delBtn.setOnAction(e -> {
                    Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + f.getNom() + "\"?");
                    c.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { try { dao.delete(f.getId()); loadData(); } catch (Exception ex) { ex.printStackTrace(); } } });
                });
                actions.getChildren().addAll(viewBtn, editBtn, delBtn);

                Separator sep = new Separator();
                sep.setStyle("-fx-opacity: 0.3;");
                card.getChildren().addAll(topRow, catLbl, dateLbl, sep, actions);
                setGraphic(card);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 0 0 8 0;");
            }
            private String s(String v) { return v == null ? "" : v; }
        });
    }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleCourses()       { /* already here */ }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
