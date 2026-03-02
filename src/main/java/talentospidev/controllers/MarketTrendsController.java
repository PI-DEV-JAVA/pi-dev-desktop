package talentospidev.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import talentospidev.models.MarketTrend;
import talentospidev.models.NewsArticle;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.services.MarketTrendService;
import talentospidev.utils.SceneUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Market Trends controller — shows hot skills, trending keywords, career news,
 * and growth metrics.
 */
public class MarketTrendsController implements Initializable {

    @FXML
    private ComboBox<String> industrySelector;
    @FXML
    private VBox contentArea;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final MarketTrendService trendService = new MarketTrendService();

    private static final String INDIGO = "#6366f1";
    private static final String GREEN = "#22c55e";
    private static final String RED = "#ef4444";
    private static final String AMBER = "#f59e0b";
    private static final String CYAN = "#06b6d4";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        setupIndustrySelector();
        loadTrends("IT");
    }

    private void setupIndustrySelector() {
        industrySelector.getItems().addAll("IT", "Finance", "Marketing", "RH", "Commercial", "Logistique");
        industrySelector.setValue("IT");
        industrySelector.setOnAction(e -> loadTrends(industrySelector.getValue()));
    }

    private void loadTrends(String industry) {
        contentArea.getChildren().clear();

        // Loading state
        VBox loadingBox = new VBox(16);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(80));
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(48, 48);
        Label loadLabel = new Label("Analyzing " + industry + " market trends...");
        loadLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: " + INDIGO + ";");
        Label subLabel = new Label("Fetching latest news and extracting insights");
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        loadingBox.getChildren().addAll(spinner, loadLabel, subLabel);
        contentArea.getChildren().add(loadingBox);

        Task<MarketTrend> task = new Task<>() {
            @Override
            protected MarketTrend call() throws Exception {
                return trendService.analyzeTrendsForIndustry(industry);
            }
        };

        task.setOnSucceeded(e -> {
            contentArea.getChildren().clear();
            displayTrends(task.getValue());
        });

        task.setOnFailed(e -> {
            contentArea.getChildren().clear();
            VBox errBox = new VBox(12);
            errBox.setAlignment(Pos.CENTER);
            errBox.setPadding(new Insets(60));
            Label errIcon = new Label("⚠");
            errIcon.setStyle("-fx-font-size: 40px;");
            Label errLabel = new Label("Failed to load trends: " + task.getException().getMessage());
            errLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + RED + ";");
            errLabel.setWrapText(true);
            Button retryBtn = new Button("Retry");
            retryBtn.setStyle("-fx-background-color: " + INDIGO + "; -fx-text-fill: white; -fx-padding: 10 24; " +
                    "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: 700;");
            retryBtn.setOnAction(ev -> loadTrends(industry));
            errBox.getChildren().addAll(errIcon, errLabel, retryBtn);
            contentArea.getChildren().add(errBox);
        });

        new Thread(task).start();
    }

    private void displayTrends(MarketTrend trend) {
        // ──── Hero stats bar ────
        HBox statsBar = new HBox(20);
        statsBar.setPadding(new Insets(20, 24, 20, 24));
        statsBar.setAlignment(Pos.CENTER);
        statsBar.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); -fx-background-radius: 16;");

        int articleCount = trend.getRelevantArticles() != null ? trend.getRelevantArticles().size() : 0;
        int skillCount = trend.getHotSkills() != null ? trend.getHotSkills().size() : 0;
        int kwCount = trend.getTrendingKeywords() != null ? trend.getTrendingKeywords().size() : 0;

        statsBar.getChildren().addAll(
                createStatCard("📊", "Growth", String.format("%+.0f%%", trend.getGrowthRate()),
                        trend.getGrowthRate() >= 0 ? "#4ade80" : "#f87171"),
                createStatCard("📰", "Articles", String.valueOf(articleCount), "white"),
                createStatCard("🔥", "Hot Skills", String.valueOf(skillCount), "#fbbf24"),
                createStatCard("🏷", "Keywords", String.valueOf(kwCount), "#67e8f9"));

        contentArea.getChildren().add(statsBar);

        // ──── Hot Skills Section ────
        VBox skillsSection = createSection("🔥 Most Demanded Skills — " + trend.getIndustry());
        if (trend.getHotSkills() != null && !trend.getHotSkills().isEmpty()) {
            VBox skillsList = new VBox(8);
            List<String> skills = trend.getHotSkills();
            for (int i = 0; i < skills.size(); i++) {
                skillsList.getChildren().add(createSkillRow(skills.get(i), i + 1, skills.size()));
            }
            skillsSection.getChildren().add(skillsList);
        } else {
            skillsSection.getChildren().add(createEmptyState("No skills data available"));
        }
        contentArea.getChildren().add(skillsSection);

        // ──── Trending Keywords Section ────
        VBox kwSection = createSection("🏷 Trending Keywords");
        if (trend.getTrendingKeywords() != null && !trend.getTrendingKeywords().isEmpty()) {
            FlowPane kwFlow = new FlowPane(10, 10);
            kwFlow.setPadding(new Insets(4, 0, 0, 0));
            String[] colors = { INDIGO, "#8b5cf6", CYAN, "#3b82f6", GREEN, AMBER, "#ec4899", "#f97316" };
            List<String> keywords = trend.getTrendingKeywords();
            for (int i = 0; i < keywords.size(); i++) {
                String color = colors[i % colors.length];
                Label tag = new Label("#" + keywords.get(i));
                tag.setStyle("-fx-background-color: derive(" + color + ", 85%); -fx-text-fill: " + color + "; " +
                        "-fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
                tag.setOnMouseEntered(e -> tag.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                        "-fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;"));
                tag.setOnMouseExited(e -> tag.setStyle("-fx-background-color: derive(" + color
                        + ", 85%); -fx-text-fill: " + color + "; " +
                        "-fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;"));
                kwFlow.getChildren().add(tag);
            }
            kwSection.getChildren().add(kwFlow);
        }
        contentArea.getChildren().add(kwSection);

        // ──── News Section ────
        VBox newsSection = createSection("📰 Latest Industry News");
        if (trend.getRelevantArticles() != null && !trend.getRelevantArticles().isEmpty()) {
            VBox newsList = new VBox(12);
            int limit = Math.min(8, trend.getRelevantArticles().size());
            for (int i = 0; i < limit; i++) {
                newsList.getChildren().add(createNewsCard(trend.getRelevantArticles().get(i)));
            }
            newsSection.getChildren().add(newsList);
        } else {
            newsSection.getChildren().add(createEmptyState("No articles found for this industry"));
        }
        contentArea.getChildren().add(newsSection);
    }

    // ===== UI Builders =====

    private VBox createStatCard(String icon, String label, String value, String valueColor) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 24, 12, 24));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 20px;");

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: " + valueColor + ";");

        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: 600;");

        card.getChildren().addAll(iconLbl, valLbl, nameLbl);
        return card;
    }

    private VBox createSection(String title) {
        VBox section = new VBox(12);
        section.setPadding(new Insets(20, 24, 20, 24));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        section.getChildren().add(titleLbl);
        return section;
    }

    private HBox createSkillRow(String skill, int rank, int total) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));

        String bg = rank <= 3 ? "#f5f3ff" : "#f9fafb";
        String border = rank == 1 ? AMBER : rank <= 3 ? INDIGO : "#e5e7eb";
        row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 12; " +
                "-fx-border-color: " + border + "; -fx-border-radius: 12; -fx-border-width: " + (rank <= 3 ? "2" : "1")
                + ";");

        // Rank badge
        String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "";
        Label rankLbl;
        if (!medal.isEmpty()) {
            rankLbl = new Label(medal);
            rankLbl.setStyle("-fx-font-size: 18px;");
        } else {
            rankLbl = new Label("#" + rank);
            rankLbl.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280; -fx-font-size: 11px; " +
                    "-fx-font-weight: 700; -fx-min-width: 28; -fx-min-height: 28; -fx-alignment: center; " +
                    "-fx-background-radius: 50;");
        }
        rankLbl.setMinWidth(32);

        // Skill name
        Label skillLbl = new Label(skill);
        skillLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        HBox.setHgrow(skillLbl, Priority.ALWAYS);

        // Demand bar
        double barWidth = Math.max(0.2, 1.0 - ((rank - 1.0) / total));
        ProgressBar bar = new ProgressBar(barWidth);
        bar.setPrefWidth(120);
        bar.setPrefHeight(8);
        String barColor = rank <= 3 ? INDIGO : "#a5b4fc";
        bar.setStyle("-fx-accent: " + barColor + ";");

        // Demand label
        String demand = rank <= 2 ? "🔥 Very High" : rank <= 4 ? "📈 High" : "📊 Rising";
        Label demandLbl = new Label(demand);
        demandLbl.setStyle(
                "-fx-font-size: 10px; -fx-text-fill: " + (rank <= 2 ? "#f97316" : rank <= 4 ? GREEN : "#6b7280") +
                        "; -fx-font-weight: 700; -fx-min-width: 80;");
        demandLbl.setTextAlignment(TextAlignment.RIGHT);

        row.getChildren().addAll(rankLbl, skillLbl, bar, demandLbl);
        return row;
    }

    private VBox createNewsCard(NewsArticle article) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        String base = "-fx-background-color: #f9fafb; -fx-background-radius: 12; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 12;";
        String hover = "-fx-background-color: #eef2ff; -fx-background-radius: 12; " +
                "-fx-border-color: " + INDIGO + "; -fx-border-radius: 12;";
        card.setStyle(base);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(base));
        card.setCursor(javafx.scene.Cursor.HAND);

        // Title
        Label titleLbl = new Label(article.getTitle() != null ? article.getTitle() : "Untitled");
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        titleLbl.setWrapText(true);

        // Meta row
        HBox meta = new HBox(12);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label sourceLbl = new Label("📰 " + (article.getSource() != null ? article.getSource() : "Unknown"));
        sourceLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        if (article.getPublishedAt() != null) {
            Label dateLbl = new Label("📅 " + article.getPublishedAt().toString());
            dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
            meta.getChildren().addAll(sourceLbl, dateLbl);
        } else {
            meta.getChildren().add(sourceLbl);
        }

        // Description snippet
        String desc = article.getDescription() != null ? article.getDescription() : "";
        if (desc.length() > 150)
            desc = desc.substring(0, 150) + "…";
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        descLbl.setWrapText(true);

        card.getChildren().addAll(titleLbl, meta, descLbl);

        // Open in browser on click
        card.setOnMouseClicked(e -> {
            if (article.getUrl() != null && !article.getUrl().isEmpty()) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(article.getUrl()));
                } catch (Exception ex) {
                    System.err.println("Could not open URL: " + ex.getMessage());
                }
            }
        });

        return card;
    }

    private Label createEmptyState(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af; -fx-padding: 20;");
        return lbl;
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
        /* Already here */ }
    @FXML private void handleInterviews() { talentospidev.utils.SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
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
