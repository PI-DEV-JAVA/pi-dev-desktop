package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import talentospidev.models.Application;
import talentospidev.models.Offer;
import talentospidev.models.User;
import talentospidev.services.ApplicationService;
import talentospidev.services.AuthService;
import talentospidev.services.OfferService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ApplicationResultController {

    @FXML
    private VBox resultContainer;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final ApplicationService applicationService = new ApplicationService();
    private final OfferService offerService = new OfferService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int appId = ViewContext.getSelectedApplicationId();
        if (appId <= 0) {
            showError("Aucune candidature sélectionnée.");
            return;
        }

        Application app = applicationService.getApplicationById(appId);
        if (app == null) {
            showError("Candidature introuvable.");
            return;
        }

        Offer offer = offerService.getOfferById(app.getOfferId());
        buildResultView(app, offer);
    }

    private void buildResultView(Application app, Offer offer) {
        resultContainer.getChildren().clear();

        // === Back Button ===
        Button backBtn = new Button("← Retour au dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6366f1; -fx-font-size: 14px; " +
                "-fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 0;");
        backBtn.setOnAction(e -> handleDashboard());

        // === Offer Summary Card ===
        VBox offerCard = new VBox(8);
        offerCard.setStyle("-fx-background-color: linear-gradient(to right, #eef2ff, #faf5ff); " +
                "-fx-background-radius: 16; -fx-padding: 22; -fx-border-color: #c7d2fe; -fx-border-radius: 16;");

        if (offer != null) {
            Label offerTitle = new Label(offer.getTitle());
            offerTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #4338ca;");

            Label offerInfo = new Label("🏢 " + offer.getDepartment() + "  •  " + offer.getContractType() +
                    "  •  📍 " + offer.getLocation());
            offerInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #6366f1;");

            Label salary = new Label(String.format("💰 %.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
            salary.setStyle("-fx-font-size: 13px; -fx-text-fill: #22c55e; -fx-font-weight: 700;");

            offerCard.getChildren().addAll(offerTitle, offerInfo, salary);
        } else {
            Label unknown = new Label("Offre #" + app.getOfferId());
            unknown.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #6b7280;");
            offerCard.getChildren().add(unknown);
        }

        // === Main Content: Timeline + Details side by side ===
        HBox mainContent = new HBox(30);
        mainContent.setAlignment(Pos.TOP_LEFT);

        // --- Left: Timeline ---
        VBox timeline = buildTimeline(app);

        // --- Right: Details ---
        VBox details = new VBox(16);
        HBox.setHgrow(details, Priority.ALWAYS);

        // Application Info Card
        VBox appInfoCard = new VBox(12);
        appInfoCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 24; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16;");

        Label appInfoTitle = new Label("📋 Ma candidature");
        appInfoTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        GridPane appGrid = new GridPane();
        appGrid.setHgap(30);
        appGrid.setVgap(10);

        addInfoRow(appGrid, "📅 Date de candidature", app.getApplicationDate().format(dateFormatter), 0);

        long daysAgo = ChronoUnit.DAYS.between(app.getApplicationDate(), java.time.LocalDate.now());
        addInfoRow(appGrid, "⏳ Il y a", daysAgo == 0 ? "Aujourd'hui" : daysAgo + " jour" + (daysAgo > 1 ? "s" : ""), 1);

        addInfoRow(appGrid, "📄 CV", app.getCvFilePath() != null ? "✅ Envoyé" : "❌ Non fourni", 2);
        addInfoRow(appGrid, "📝 Statut", app.getStatus(), 3);

        if (app.getMotivationLetter() != null && !app.getMotivationLetter().isEmpty()) {
            Label motivTitle = new Label("Lettre de motivation");
            motivTitle.setStyle(
                    "-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #6b7280; -fx-padding: 8 0 4 0;");

            Label motivText = new Label(app.getMotivationLetter());
            motivText.setWrapText(true);
            motivText.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-background-color: #f9fafb; " +
                    "-fx-background-radius: 10; -fx-padding: 12;");

            appInfoCard.getChildren().addAll(appInfoTitle, appGrid, motivTitle, motivText);
        } else {
            appInfoCard.getChildren().addAll(appInfoTitle, appGrid);
        }

        // Response Card (prominent)
        VBox responseCard = buildResponseCard(app);

        // Interview Card (if applicable)
        details.getChildren().addAll(appInfoCard, responseCard);

        if (app.getInterviewer() != null && !app.getInterviewer().isEmpty()) {
            VBox interviewCard = new VBox(10);
            interviewCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 24; " +
                    "-fx-border-color: #e5e7eb; -fx-border-radius: 16;");

            Label interviewTitle = new Label("🎙 Entretien");
            interviewTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");

            GridPane intGrid = new GridPane();
            intGrid.setHgap(30);
            intGrid.setVgap(8);
            addInfoRow(intGrid, "Intervieweur", app.getInterviewer(), 0);
            if (app.getInterviewDate() != null) {
                addInfoRow(intGrid, "Date", app.getInterviewDate().format(dateFormatter), 1);
            }
            if (app.getInterviewResult() != null) {
                addInfoRow(intGrid, "Résultat", app.getInterviewResult(), 2);
            }

            interviewCard.getChildren().addAll(interviewTitle, intGrid);
            details.getChildren().add(interviewCard);
        }

        mainContent.getChildren().addAll(timeline, details);
        resultContainer.getChildren().addAll(backBtn, offerCard, mainContent);
    }

    private VBox buildTimeline(Application app) {
        VBox timeline = new VBox(0);
        timeline.setAlignment(Pos.TOP_CENTER);
        timeline.setMinWidth(180);
        timeline.setMaxWidth(180);
        timeline.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 24 16; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16;");

        Label timelineTitle = new Label("📍 Suivi");
        timelineTitle
                .setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827; -fx-padding: 0 0 12 0;");
        timeline.getChildren().add(timelineTitle);

        // Determine completed steps
        boolean applied = true;
        boolean underReview = !"Nouvelle".equals(app.getStatus());
        boolean interviewed = app.getInterviewer() != null && !app.getInterviewer().isEmpty();
        boolean decided = app.hasResponse();

        addTimelineStep(timeline, "Candidature envoyée", applied, true);
        addTimelineLine(timeline, underReview);
        addTimelineStep(timeline, "En cours d'examen", underReview, !decided && underReview);
        addTimelineLine(timeline, interviewed);
        addTimelineStep(timeline, "Entretien", interviewed, !decided && interviewed);
        addTimelineLine(timeline, decided);
        addTimelineStep(timeline, "Décision", decided, decided);

        return timeline;
    }

    private void addTimelineStep(VBox container, String label, boolean completed, boolean current) {
        HBox step = new HBox(10);
        step.setAlignment(Pos.CENTER_LEFT);
        step.setPadding(new Insets(4, 0, 4, 0));

        Circle dot = new Circle(8);
        if (current && completed) {
            dot.setStyle("-fx-fill: #6366f1; -fx-effect: dropshadow(gaussian, rgba(99,102,241,0.4), 6, 0, 0, 0);");
        } else if (completed) {
            dot.setStyle("-fx-fill: #22c55e;");
        } else {
            dot.setStyle("-fx-fill: #e5e7eb;");
        }

        Label text = new Label(label);
        text.setWrapText(true);
        text.setMaxWidth(120);
        text.setStyle("-fx-font-size: 12px; -fx-font-weight: " + (current ? "700" : "500") +
                "; -fx-text-fill: " + (completed ? "#111827" : "#9ca3af") + ";");

        step.getChildren().addAll(dot, text);
        container.getChildren().add(step);
    }

    private void addTimelineLine(VBox container, boolean active) {
        Region line = new Region();
        line.setMinHeight(24);
        line.setMaxWidth(3);
        line.setPrefWidth(3);
        line.setStyle("-fx-background-color: " + (active ? "#22c55e" : "#e5e7eb") + "; -fx-background-radius: 2;");

        HBox lineBox = new HBox(line);
        lineBox.setAlignment(Pos.CENTER_LEFT);
        lineBox.setPadding(new Insets(0, 0, 0, 5));
        container.getChildren().add(lineBox);
    }

    private VBox buildResponseCard(Application app) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);

        if (app.hasResponse()) {
            String status = app.getStatus() != null ? app.getStatus().toLowerCase() : "";
            boolean accepted = status.contains("accept");

            if (accepted) {
                card.setStyle("-fx-background-color: linear-gradient(to right, #f0fdf4, #dcfce7); " +
                        "-fx-background-radius: 16; -fx-padding: 28; -fx-border-color: #86efac; -fx-border-radius: 16;");

                Label icon = new Label("🎉");
                icon.setStyle("-fx-font-size: 40px;");

                Label title = new Label("Candidature acceptée !");
                title.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #16a34a;");

                Label response = new Label(app.getRecruiterResponse());
                response.setWrapText(true);
                response.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-padding: 8 0;");

                Label date = new Label("Répondu le "
                        + (app.getResponseDate() != null ? app.getResponseDate().format(dateFormatter) : "—"));
                date.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

                card.getChildren().addAll(icon, title, response, date);
            } else {
                card.setStyle("-fx-background-color: linear-gradient(to right, #fef2f2, #fee2e2); " +
                        "-fx-background-radius: 16; -fx-padding: 28; -fx-border-color: #fca5a5; -fx-border-radius: 16;");

                Label icon = new Label("📋");
                icon.setStyle("-fx-font-size: 40px;");

                Label title = new Label("Réponse du recruteur");
                title.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #dc2626;");

                Label response = new Label(app.getRecruiterResponse());
                response.setWrapText(true);
                response.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-padding: 8 0;");

                Label date = new Label("Répondu le "
                        + (app.getResponseDate() != null ? app.getResponseDate().format(dateFormatter) : "—"));
                date.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

                Label encourage = new Label("Ne vous découragez pas — chaque candidature est une expérience ! 💪");
                encourage.setWrapText(true);
                encourage.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-font-style: italic;");

                card.getChildren().addAll(icon, title, response, date, encourage);
            }
        } else {
            // Pending
            card.setStyle("-fx-background-color: linear-gradient(to right, #fffbeb, #fef3c7); " +
                    "-fx-background-radius: 16; -fx-padding: 28; -fx-border-color: #fde68a; -fx-border-radius: 16;");

            Label icon = new Label("⏳");
            icon.setStyle("-fx-font-size: 40px;");

            Label title = new Label("En attente de réponse");
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #d97706;");

            Label subtitle = new Label("Le recruteur n'a pas encore répondu à votre candidature.");
            subtitle.setWrapText(true);
            subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #92400e;");

            long daysWaiting = ChronoUnit.DAYS.between(app.getApplicationDate(), java.time.LocalDate.now());
            Label waitInfo = new Label("En attente depuis " + daysWaiting + " jour" + (daysWaiting > 1 ? "s" : ""));
            waitInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #b45309; -fx-font-weight: 600;");

            card.getChildren().addAll(icon, title, subtitle, waitInfo);
        }

        return card;
    }

    private void addInfoRow(GridPane grid, String label, String value, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-font-weight: 700;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private void showError(String msg) {
        Label err = new Label("⚠ " + msg);
        err.setStyle("-fx-font-size: 16px; -fx-text-fill: #ef4444; -fx-padding: 40;");
        resultContainer.getChildren().add(err);
    }

    // === Sidebar Navigation ===
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
        new Alert(Alert.AlertType.INFORMATION, "Fonctionnalité bientôt disponible !").showAndWait();
    }

    @FXML
    private void handleSettings() {
        SceneUtil.switchScene("settings.fxml");
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
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
