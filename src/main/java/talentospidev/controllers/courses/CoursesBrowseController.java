package talentospidev.controllers.courses;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.coursesDAO.FormationDAO;
import talentospidev.dao.coursesDAO.InscriptionDAO;
import talentospidev.models.User;
import talentospidev.models.courses.Formation;
import talentospidev.models.courses.Inscription;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CoursesBrowseController {

    @FXML private ListView<Formation> listView;
    @FXML private TextField searchField;
    @FXML private ToggleButton browseToggle, myCoursesToggle;
    @FXML private VBox sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final FormationDAO formationDAO = new FormationDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private boolean showMyCourses = false;

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);

        ToggleGroup tg = new ToggleGroup();
        browseToggle.setToggleGroup(tg); myCoursesToggle.setToggleGroup(tg);
        browseToggle.setSelected(true);

        String activeStyle = "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 10; " +
                "-fx-padding: 8 18; -fx-font-weight: 700; -fx-cursor: hand; -fx-font-size: 12px;";
        String inactiveStyle = "-fx-background-color: white; -fx-text-fill: #374151; -fx-background-radius: 10; " +
                "-fx-padding: 8 18; -fx-font-weight: 700; -fx-cursor: hand; -fx-font-size: 12px; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 10;";

        browseToggle.setOnAction(e -> {
            showMyCourses = false;
            browseToggle.setStyle(activeStyle);
            myCoursesToggle.setStyle(inactiveStyle);
            loadData();
        });
        myCoursesToggle.setOnAction(e -> {
            showMyCourses = true;
            myCoursesToggle.setStyle(activeStyle);
            browseToggle.setStyle(inactiveStyle);
            loadData();
        });

        setupListView();
        loadData();
    }

    private void loadData() {
        try {
            User u = AuthService.getCurrentUser();
            List<Formation> list;

            if (showMyCourses && u != null) {
                // Only show courses where enrollment is ACCEPTED
                List<Inscription> accepted = inscriptionDAO.getAcceptedByUserId(u.getId());
                list = accepted.stream().map(i -> {
                    try { return formationDAO.getById(i.getFormationId()); } catch (Exception e) { return null; }
                }).filter(f -> f != null).toList();
            } else {
                list = formationDAO.getOpen();
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

                // Title row
                HBox topRow = new HBox(8);
                topRow.setAlignment(Pos.CENTER_LEFT);
                Label titleLbl = new Label(f.getNom());
                titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #111827;");
                HBox.setHgrow(titleLbl, Priority.ALWAYS);

                Label modeBadge = new Label(s(f.getMode()));
                modeBadge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-background-radius: 10; " +
                        "-fx-padding: 3 10; -fx-font-size: 10px; -fx-font-weight: 700;");

                Label diffBadge = new Label(s(f.getDifficulte()));
                String diffColor = "DEBUTANT".equals(f.getDifficulte()) ? "#dcfce7;-fx-text-fill:#166534" :
                        "INTERMEDIAIRE".equals(f.getDifficulte()) ? "#fef3c7;-fx-text-fill:#d97706" : "#fee2e2;-fx-text-fill:#dc2626";
                diffBadge.setStyle("-fx-background-color: " + diffColor.split(";")[0] + "; " + diffColor.split(";")[1] + "; " +
                        "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 10px; -fx-font-weight: 700;");

                topRow.getChildren().addAll(titleLbl, modeBadge, diffBadge);

                // Meta
                Label catLbl = new Label("📂 " + s(f.getCategorie()) + "  •  👤 " + s(f.getFormateur()) + "  •  👥 Max: " + f.getCapaciteMax());
                catLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
                Label dateLbl = new Label("📅 " + (f.getDateDebut() != null ? dtFmt.format(f.getDateDebut()) : "") +
                        " → " + (f.getDateFin() != null ? dtFmt.format(f.getDateFin()) : ""));
                dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

                // Content preview
                if (f.getContenu() != null && !f.getContenu().isEmpty()) {
                    String preview = f.getContenu().length() > 100 ? f.getContenu().substring(0, 100) + "..." : f.getContenu();
                    Label contentLbl = new Label(preview);
                    contentLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
                    contentLbl.setWrapText(true);
                    card.getChildren().add(contentLbl);
                }

                // Actions
                Separator sep = new Separator(); sep.setStyle("-fx-opacity: 0.3;");
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_LEFT);

                if (showMyCourses) {
                    // My Courses tab — show "Continue Learning"
                    Button viewBtn = new Button("📖 Continue Learning");
                    viewBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 8; " +
                            "-fx-padding: 6 16; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
                    viewBtn.setOnAction(e -> {
                        ViewContext.setSelectedFormationId(f.getId());
                        SceneUtil.switchScene("Courses/CourseDetailCandidat.fxml");
                    });
                    actions.getChildren().add(viewBtn);
                } else {
                    // Browse tab — show enrollment status
                    User u = AuthService.getCurrentUser();
                    if (u != null) {
                        try {
                            String status = inscriptionDAO.getEnrollmentStatus(u.getId(), f.getId());
                            if (status == null) {
                                // Not enrolled yet — show Enroll button
                                Button enrollBtn = new Button("📝 Enroll Now");
                                enrollBtn.setStyle("-fx-background-color: linear-gradient(to right, #22c55e, #16a34a); -fx-text-fill: white; " +
                                        "-fx-background-radius: 8; -fx-padding: 6 16; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
                                enrollBtn.setOnAction(e -> {
                                    try {
                                        Inscription ins = new Inscription();
                                        ins.setFormationId(f.getId());
                                        ins.setUserId(u.getId());
                                        ins.setCandidatNom(u.getEmail());
                                        ins.setCandidatEmail(u.getEmail());
                                        ins.setStatut("EN_ATTENTE");
                                        inscriptionDAO.add(ins);
                                        loadData();
                                    } catch (Exception ex) { ex.printStackTrace(); }
                                });
                                actions.getChildren().add(enrollBtn);
                            } else if ("EN_ATTENTE".equals(status)) {
                                // Pending
                                Label pendingLbl = new Label("⏳ Enrollment Pending");
                                pendingLbl.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-background-radius: 8; " +
                                        "-fx-padding: 6 14; -fx-font-size: 12px; -fx-font-weight: 700;");
                                actions.getChildren().add(pendingLbl);
                            } else if ("ACCEPTEE".equals(status)) {
                                // Accepted — show "Start Learning"
                                Button startBtn = new Button("🚀 Start Learning");
                                startBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 8; " +
                                        "-fx-padding: 6 16; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
                                startBtn.setOnAction(e -> {
                                    ViewContext.setSelectedFormationId(f.getId());
                                    SceneUtil.switchScene("Courses/CourseDetailCandidat.fxml");
                                });
                                actions.getChildren().add(startBtn);
                            } else if ("REFUSEE".equals(status)) {
                                Label rejectedLbl = new Label("❌ Enrollment Rejected");
                                rejectedLbl.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 8; " +
                                        "-fx-padding: 6 14; -fx-font-size: 12px; -fx-font-weight: 700;");
                                actions.getChildren().add(rejectedLbl);
                            }
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }

                card.getChildren().addAll(0, List.of(topRow, catLbl, dateLbl));
                card.getChildren().addAll(sep, actions);
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
