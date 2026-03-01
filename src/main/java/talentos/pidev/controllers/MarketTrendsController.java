package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import talentos.pidev.models.MarketTrend;
import talentos.pidev.models.NewsArticle;
import talentos.pidev.services.MarketTrendService;

import java.io.IOException;
import java.util.List;

public class MarketTrendsController {

    @FXML private ComboBox<String> industrySelector;
    @FXML private Label trendTitle;
    @FXML private Label growthRateLabel;
    @FXML private VBox skillsContainer;
    @FXML private VBox keywordsContainer;
    @FXML private VBox newsContainer;
    @FXML private ProgressIndicator loadingIndicator;

    private final MarketTrendService trendService;

    public MarketTrendsController() {
        this.trendService = new MarketTrendService();
    }

    @FXML
    public void initialize() {
        setupIndustrySelector();
        loadTrendsForIndustry("IT"); // Charger par défaut
    }

    private void setupIndustrySelector() {
        industrySelector.getItems().addAll(
                "IT", "RH", "Finance", "Marketing", "Production", "Logistique", "Commerce"
        );
        industrySelector.setValue("IT");
        industrySelector.setOnAction(e -> loadTrendsForIndustry(industrySelector.getValue()));
    }

    private void loadTrendsForIndustry(String industry) {
        // Afficher le chargement
        loadingIndicator.setVisible(true);
        trendTitle.setText("Analyse des tendances " + industry + "...");

        new Thread(() -> {
            try {
                MarketTrend trend = trendService.analyzeTrendsForIndustry(industry);

                javafx.application.Platform.runLater(() -> {
                    displayTrends(trend);
                    loadingIndicator.setVisible(false);
                });

            } catch (IOException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showError("Erreur lors de l'analyse: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void displayTrends(MarketTrend trend) {
        trendTitle.setText("📊 Tendances du secteur " + trend.getIndustry());

        // Taux de croissance
        double growth = trend.getGrowthRate();
        String growthText = String.format("%+.1f%%", growth);
        String growthColor = growth >= 0 ? "#10B981" : "#EF4444";
        growthRateLabel.setText(growthText);
        growthRateLabel.setStyle("-fx-text-fill: " + growthColor + "; -fx-font-weight: bold;");

        // Compétences tendance
        skillsContainer.getChildren().clear();
        List<String> hotSkills = trend.getHotSkills();
        for (int i = 0; i < hotSkills.size(); i++) {
            HBox skillBox = createSkillCard(hotSkills.get(i), i + 1);
            skillsContainer.getChildren().add(skillBox);
        }

        // Mots-clés tendance
        keywordsContainer.getChildren().clear();
        List<String> keywords = trend.getTrendingKeywords();
        FlowPane keywordFlow = new FlowPane(10, 10);
        keywordFlow.setPadding(new Insets(10, 0, 0, 0));

        for (String keyword : keywords) {
            Label keywordLabel = new Label("#" + keyword);
            keywordLabel.setStyle(
                    "-fx-background-color: #334155;" +
                            "-fx-text-fill: #94A3B8;" +
                            "-fx-padding: 5 12;" +
                            "-fx-background-radius: 20;" +
                            "-fx-font-size: 12px;"
            );
            keywordFlow.getChildren().add(keywordLabel);
        }
        keywordsContainer.getChildren().add(keywordFlow);

        // Articles récents
        newsContainer.getChildren().clear();
        List<NewsArticle> articles = trend.getRelevantArticles();
        for (int i = 0; i < Math.min(5, articles.size()); i++) {
            VBox articleCard = createArticleCard(articles.get(i));
            newsContainer.getChildren().add(articleCard);
        }
    }

    private HBox createSkillCard(String skill, int rank) {
        HBox card = new HBox(15);
        card.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 12;"
        );
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);

        Label rankLabel = new Label("#" + rank);
        rankLabel.setStyle(
                "-fx-background-color: #6366F1;" +
                        "-fx-text-fill: white;" +
                        "-fx-min-width: 30;" +
                        "-fx-min-height: 30;" +
                        "-fx-background-radius: 15;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-alignment: center;"
        );

        Label skillLabel = new Label(skill);
        skillLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label trendLabel = new Label("📈 +25%");
        trendLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: bold;");

        card.getChildren().addAll(rankLabel, skillLabel, spacer, trendLabel);

        return card;
    }

    private VBox createArticleCard(NewsArticle article) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 12;"
        );
        card.setMaxWidth(Double.MAX_VALUE);

        Label titleLabel = new Label(article.getTitle());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");
        titleLabel.setWrapText(true);

        Label sourceLabel = new Label("📰 " + article.getSource() + " • " + article.getPublishedAt());
        sourceLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

        Button readBtn = new Button("Lire l'article");
        readBtn.setStyle(
                "-fx-background-color: #334155;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        readBtn.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(article.getUrl()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(readBtn);

        card.getChildren().addAll(titleLabel, sourceLabel, buttonBox);

        return card;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}