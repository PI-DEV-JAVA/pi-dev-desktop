package talentos.pidev.models;

import java.time.LocalDate;
import java.util.List;

public class MarketTrend {
    private String industry;
    private List<String> hotSkills;
    private List<String> trendingKeywords;
    private List<NewsArticle> relevantArticles;
    private LocalDate analysisDate;
    private double growthRate;

    // Constructeur
    public MarketTrend(String industry) {
        this.industry = industry;
        this.analysisDate = LocalDate.now();
    }

    // Getters et Setters
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public List<String> getHotSkills() { return hotSkills; }
    public void setHotSkills(List<String> hotSkills) { this.hotSkills = hotSkills; }

    public List<String> getTrendingKeywords() { return trendingKeywords; }
    public void setTrendingKeywords(List<String> trendingKeywords) { this.trendingKeywords = trendingKeywords; }

    public List<NewsArticle> getRelevantArticles() { return relevantArticles; }
    public void setRelevantArticles(List<NewsArticle> relevantArticles) { this.relevantArticles = relevantArticles; }

    public LocalDate getAnalysisDate() { return analysisDate; }

    public double getGrowthRate() { return growthRate; }
    public void setGrowthRate(double growthRate) { this.growthRate = growthRate; }
}