package talentospidev.controllers.courses;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import talentospidev.dao.coursesDAO.*;
import talentospidev.models.User;
import talentospidev.models.courses.*;
import talentospidev.services.AuthService;
import talentospidev.services.GeoCodingService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Candidate view of an accepted course — curriculum tree, WebView for video, progress tracking.
 */
public class CourseDetailCandidatController {

    @FXML private Label titleLbl, descLbl, metaLbl, progressLbl, progressPctLbl;
    @FXML private VBox sessionsContainer, videoContainer, progressCard, sidebar;
    @FXML private Label videoTitleLbl;
    @FXML private WebView videoWebView;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final FormationDAO formationDAO = new FormationDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final TentativeDAO tentativeDAO = new TentativeDAO();
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int fId = ViewContext.getSelectedFormationId();
        if (fId > 0) {
            try {
                Formation f = formationDAO.getById(fId);
                if (f != null) {
                    titleLbl.setText("📚 " + f.getNom());
                    descLbl.setText(f.getDescription() != null ? f.getDescription() : "No description.");
                    metaLbl.setText("📂 " + safe(f.getCategorie()) + "  •  " + safe(f.getDifficulte()) + "  •  👤 " + safe(f.getFormateur()));
                    loadSessions(f.getId());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadSessions(int formationId) throws Exception {
        sessionsContainer.getChildren().clear();
        List<Seance> seances = seanceDAO.getByFormationId(formationId);
        User u = AuthService.getCurrentUser();

        // Calculate progress
        int completed = 0;
        int total = seances.size();
        if (u != null) {
            for (Seance s : seances) {
                Quiz quiz = quizDAO.getBySeanceId(s.getId());
                if (quiz != null) {
                    TentativeQuiz best = tentativeDAO.getBestAttempt(u.getId(), quiz.getId());
                    if (best != null) completed++;
                }
            }
        }
        progressLbl.setText(completed + " / " + total + " sessions");
        int pct = total > 0 ? (100 * completed / total) : 0;
        progressPctLbl.setText(pct + "% completed");

        for (int idx = 0; idx < seances.size(); idx++) {
            Seance s = seances.get(idx);
            boolean isLast = (idx == seances.size() - 1);

            // Curriculum tree node
            HBox treeNode = new HBox(14);
            treeNode.setAlignment(Pos.TOP_LEFT);

            // Tree connector
            VBox connector = new VBox();
            connector.setAlignment(Pos.TOP_CENTER);
            connector.setPrefWidth(32); connector.setMinWidth(32);

            // Check if completed
            boolean sessionCompleted = false;
            Quiz quiz = quizDAO.getBySeanceId(s.getId());
            if (u != null && quiz != null) {
                TentativeQuiz best = tentativeDAO.getBestAttempt(u.getId(), quiz.getId());
                sessionCompleted = (best != null);
            }

            Label dot = new Label(sessionCompleted ? "✅" : String.valueOf(idx + 1));
            dot.setStyle(sessionCompleted ?
                    "-fx-font-size: 14px;" :
                    "-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: white; -fx-background-color: #6366f1; " +
                    "-fx-min-width: 24; -fx-max-width: 24; -fx-min-height: 24; -fx-max-height: 24; " +
                    "-fx-background-radius: 12; -fx-alignment: center;");
            connector.getChildren().add(dot);
            if (!isLast) {
                Region line = new Region();
                line.setStyle("-fx-background-color: " + (sessionCompleted ? "#22c55e" : "#d1d5db") + ";");
                line.setPrefWidth(2); line.setMinWidth(2); line.setMaxWidth(2);
                VBox.setVgrow(line, Priority.ALWAYS);
                connector.getChildren().add(line);
            }

            // Session card
            VBox card = new VBox(8);
            card.setPadding(new Insets(14, 18, 14, 18));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-border-color: " + (sessionCompleted ? "#dcfce7" : "#e5e7eb") + "; -fx-border-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);");
            card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("white", "#fafbff")));
            card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("#fafbff", "white")));
            HBox.setHgrow(card, Priority.ALWAYS);

            // Title
            Label tLbl = new Label(s.getTitre());
            tLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827;");
            String typeIcon = "PRESENTIEL".equals(s.getType()) ? "📍 In-Place" : "🌐 Online";
            Label infoLbl = new Label(typeIcon + "  •  " + (s.getDateDebut() != null ? dtFmt.format(s.getDateDebut()) : "")
                    + (s.getAdresse() != null && !s.getAdresse().isEmpty() ? "  •  " + s.getAdresse() : ""));
            infoLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

            // Action buttons
            HBox btns = new HBox(8);
            btns.setAlignment(Pos.CENTER_LEFT);

            // Map button for in-place sessions
            if ("PRESENTIEL".equals(s.getType()) && s.getLatitude() != null && s.getLongitude() != null) {
                Button mapBtn = new Button("🗺 Open Map");
                mapBtn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 8; " +
                        "-fx-padding: 6 14; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
                mapBtn.setOnAction(e -> { try { new GeoCodingService().openOSM(s.getLatitude(), s.getLongitude()); } catch (Exception ex) { ex.printStackTrace(); } });
                btns.getChildren().add(mapBtn);
            }

            // Video button — opens in WebView
            if ("EN_LIGNE".equals(s.getType()) && s.getVideoPath() != null && !s.getVideoPath().isEmpty()) {
                Button videoBtn = new Button("🎥 Watch Video");
                videoBtn.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-background-radius: 8; " +
                        "-fx-padding: 6 14; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
                videoBtn.setOnAction(e -> {
                    videoTitleLbl.setText("🎥 " + s.getTitre());
                    String url = s.getVideoPath();
                    // Convert YouTube URLs to embed format
                    if (url.contains("youtube.com/watch")) {
                        url = url.replace("watch?v=", "embed/");
                    } else if (url.contains("youtu.be/")) {
                        url = url.replace("youtu.be/", "youtube.com/embed/");
                    }
                    videoWebView.getEngine().load(url);
                    videoContainer.setVisible(true);
                    videoContainer.setManaged(true);
                });
                btns.getChildren().add(videoBtn);
            }

            // Quiz button with score
            if (quiz != null) {
                String quizLabel = "📝 Take Quiz";
                String quizStyle = "-fx-background-color: #eef2ff; -fx-text-fill: #6366f1;";
                if (u != null) {
                    TentativeQuiz best = tentativeDAO.getBestAttempt(u.getId(), quiz.getId());
                    if (best != null) {
                        double bestPct = best.getTotal() > 0 ? (100.0 * best.getScore() / best.getTotal()) : 0;
                        quizLabel = "📝 Score: " + best.getScore() + "/" + best.getTotal() + " (" + (int)bestPct + "%)";
                        quizStyle = "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
                    }
                }
                Button quizBtn = new Button(quizLabel);
                quizBtn.setStyle(quizStyle + " -fx-background-radius: 8; -fx-padding: 6 14; " +
                        "-fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
                quizBtn.setOnAction(e -> { ViewContext.setSelectedQuizId(quiz.getId()); SceneUtil.switchScene("Courses/QuizPassage.fxml"); });
                btns.getChildren().add(quizBtn);
            }

            card.getChildren().addAll(tLbl, infoLbl, btns);
            treeNode.getChildren().addAll(connector, card);
            sessionsContainer.getChildren().add(treeNode);
        }
    }

    @FXML private void onBack() { SceneUtil.switchScene("Courses/CoursesBrowse.fxml"); }
    private String safe(String s) { return s == null ? "" : s; }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private 
    @javafx.fxml.FXML
    private void handleEvents() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Events/EventsFeed.fxml" : "Events/EventsBrowse.fxml");
    }
    void handleCourses()       { SceneUtil.switchScene("Courses/CoursesBrowse.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
