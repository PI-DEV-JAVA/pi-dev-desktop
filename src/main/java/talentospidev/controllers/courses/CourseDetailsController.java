package talentospidev.controllers.courses;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.coursesDAO.*;
import talentospidev.models.courses.*;
import talentospidev.services.AuthService;
import talentospidev.services.GeoCodingService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CourseDetailsController {

    @FXML private Label titleLbl, descLbl, metaLbl;
    @FXML private VBox sessionsContainer, enrollmentsContainer, curriculumContainer;
    @FXML private Button addSessionBtn, backBtn;
    @FXML private VBox sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final FormationDAO formationDAO = new FormationDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private Formation formation;
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int fId = ViewContext.getSelectedFormationId();
        if (fId > 0) {
            try {
                formation = formationDAO.getById(fId);
                if (formation != null) {
                    titleLbl.setText("📚 " + formation.getNom());
                    descLbl.setText(formation.getDescription() != null ? formation.getDescription() : "No description provided.");
                    metaLbl.setText("📂 " + s(formation.getCategorie()) + "  •  " + s(formation.getDifficulte())
                            + "  •  " + s(formation.getMode()) + "  •  👤 " + s(formation.getFormateur()));
                    loadSessions();
                    loadEnrollments();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadSessions() throws Exception {
        sessionsContainer.getChildren().clear();
        List<Seance> seances = seanceDAO.getByFormationId(formation.getId());

        if (seances.isEmpty()) {
            Label empty = new Label("No sessions yet. Click '+ Add Session' to create the first one.");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px; -fx-padding: 20 0;");
            sessionsContainer.getChildren().add(empty);
            return;
        }

        for (int idx = 0; idx < seances.size(); idx++) {
            Seance sn = seances.get(idx);
            boolean isLast = (idx == seances.size() - 1);

            HBox treeNode = new HBox(14);
            treeNode.setAlignment(Pos.TOP_LEFT);

            // Tree connector (dot + vertical line)
            VBox connector = new VBox();
            connector.setAlignment(Pos.TOP_CENTER);
            connector.setPrefWidth(32); connector.setMinWidth(32);
            Label dot = new Label("●");
            dot.setStyle("-fx-font-size: 14px; -fx-text-fill: #6366f1;");
            connector.getChildren().add(dot);
            if (!isLast) {
                Region line = new Region();
                line.setStyle("-fx-background-color: #d1d5db;");
                line.setPrefWidth(2); line.setMinWidth(2); line.setMaxWidth(2);
                VBox.setVgrow(line, Priority.ALWAYS);
                connector.getChildren().add(line);
            }

            // Session card
            VBox card = new VBox(8);
            card.setPadding(new Insets(14, 18, 14, 18));
            String cardBase = "-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-border-color: #e5e7eb; -fx-border-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);";
            card.setStyle(cardBase);
            card.setOnMouseEntered(e -> card.setStyle(cardBase.replace("white", "#fafbff")));
            card.setOnMouseExited(e -> card.setStyle(cardBase));
            HBox.setHgrow(card, Priority.ALWAYS);

            // Session header with number badge
            HBox sHeader = new HBox(8);
            sHeader.setAlignment(Pos.CENTER_LEFT);
            Label numLbl = new Label("Session " + (idx + 1));
            numLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #6366f1; -fx-font-weight: 700; " +
                    "-fx-background-color: #eef2ff; -fx-background-radius: 6; -fx-padding: 2 8;");
            Label tLbl = new Label(sn.getTitre());
            tLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827;");
            HBox.setHgrow(tLbl, Priority.ALWAYS);
            sHeader.getChildren().addAll(numLbl, tLbl);

            // Session meta
            String typeIcon = "PRESENTIEL".equals(sn.getType()) ? "📍 In-Place" : "🌐 Online";
            Label infoLbl = new Label(typeIcon + "  •  " + (sn.getDateDebut() != null ? dtFmt.format(sn.getDateDebut()) : "")
                    + (sn.getAdresse() != null && !sn.getAdresse().isEmpty() ? "  •  " + sn.getAdresse() : ""));
            infoLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

            // Actions
            HBox btns = new HBox(6);
            btns.setAlignment(Pos.CENTER_LEFT);

            if ("PRESENTIEL".equals(sn.getType()) && sn.getLatitude() != null && sn.getLongitude() != null) {
                Button mapBtn = new Button("🗺 Map");
                mapBtn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 8; " +
                        "-fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
                mapBtn.setOnAction(e -> { try { new GeoCodingService().openOSM(sn.getLatitude(), sn.getLongitude()); } catch (Exception ex) { ex.printStackTrace(); } });
                btns.getChildren().add(mapBtn);
            }

            Quiz quiz = quizDAO.getBySeanceId(sn.getId());
            if (quiz != null) {
                Button quizBtn = new Button("📝 Quiz: " + quiz.getTitre());
                quizBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-background-radius: 8; " +
                        "-fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
                quizBtn.setOnAction(e -> { ViewContext.setSelectedQuizId(quiz.getId()); SceneUtil.switchScene("Courses/QuizRH.fxml"); });
                btns.getChildren().add(quizBtn);
            } else {
                Button createQuizBtn = new Button("＋ Quiz");
                createQuizBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-background-radius: 8; " +
                        "-fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
                createQuizBtn.setOnAction(e -> {
                    ViewContext.setSelectedSeanceId(sn.getId());
                    SceneUtil.switchScene("Courses/QuizForm.fxml");
                });
                btns.getChildren().add(createQuizBtn);
            }

            Button delBtn = new Button("🗑");
            delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 8; " +
                    "-fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 11px;");
            delBtn.setOnAction(e -> { try { seanceDAO.delete(sn.getId()); loadSessions(); } catch (Exception ex) { ex.printStackTrace(); } });
            btns.getChildren().add(delBtn);

            card.getChildren().addAll(sHeader, infoLbl, btns);
            treeNode.getChildren().addAll(connector, card);
            sessionsContainer.getChildren().add(treeNode);
        }
    }

    private void loadEnrollments() throws Exception {
        enrollmentsContainer.getChildren().clear();
        List<Inscription> list = inscriptionDAO.getByFormationId(formation.getId());

        if (list.isEmpty()) {
            Label empty = new Label("No enrollment requests yet.");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px; -fx-padding: 12 0;");
            enrollmentsContainer.getChildren().add(empty);
            return;
        }

        for (Inscription i : list) {
            HBox row = new HBox(12);
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #fafafa; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #f3f4f6;");

            Label avatar = new Label(s(i.getCandidatNom()).isEmpty() ? "?" : s(i.getCandidatNom()).substring(0, 1).toUpperCase());
            avatar.setStyle("-fx-min-width: 36; -fx-max-width: 36; -fx-min-height: 36; -fx-max-height: 36; " +
                    "-fx-background-color: #eef2ff; -fx-background-radius: 18; -fx-alignment: center; " +
                    "-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #6366f1;");

            VBox info = new VBox(2);
            Label nameLbl = new Label(s(i.getCandidatNom()));
            nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #111827;");
            Label emailLbl = new Label(s(i.getCandidatEmail()));
            emailLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
            info.getChildren().addAll(nameLbl, emailLbl);
            HBox.setHgrow(info, Priority.ALWAYS);

            String statusColor = "EN_ATTENTE".equals(i.getStatut()) ? "#f59e0b" : "ACCEPTEE".equals(i.getStatut()) ? "#22c55e" : "#ef4444";
            Label statusLbl = new Label(i.getStatut().replace("_", " "));
            statusLbl.setStyle("-fx-background-color: " + statusColor + "18; -fx-text-fill: " + statusColor + "; " +
                    "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 10px; -fx-font-weight: 700;");

            HBox actions = new HBox(4);
            actions.setAlignment(Pos.CENTER_RIGHT);
            if ("EN_ATTENTE".equals(i.getStatut())) {
                Button acceptBtn = new Button("✅ Accept");
                acceptBtn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 8; " +
                        "-fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: 600;");
                acceptBtn.setOnAction(e -> { try { inscriptionDAO.updateStatut(i.getId(), "ACCEPTEE"); loadEnrollments(); } catch (Exception ex) { ex.printStackTrace(); } });
                Button rejectBtn = new Button("❌ Reject");
                rejectBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 8; " +
                        "-fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: 600;");
                rejectBtn.setOnAction(e -> { try { inscriptionDAO.updateStatut(i.getId(), "REFUSEE"); loadEnrollments(); } catch (Exception ex) { ex.printStackTrace(); } });
                actions.getChildren().addAll(acceptBtn, rejectBtn);
            }

            row.getChildren().addAll(avatar, info, statusLbl, actions);
            enrollmentsContainer.getChildren().add(row);
        }
    }

    @FXML
    private void onAddSession() {
        // Navigate to separate session form page
        SceneUtil.switchScene("Courses/SessionForm.fxml");
    }

    @FXML private void onBack() { SceneUtil.switchScene("Courses/CoursesRH.fxml"); }
    private String s(String v) { return v == null ? "" : v; }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleEvents() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Events/EventsFeed.fxml" : "Events/EventsBrowse.fxml");
    }
    @FXML private void handleCourses()       { SceneUtil.switchScene("Courses/CoursesRH.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
