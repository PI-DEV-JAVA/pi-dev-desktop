package talentospidev.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import talentospidev.models.MarketTrend;
import talentospidev.models.NewsArticle;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Fetches market trends, hot skills, and news articles per industry
 * using the NewsAPI.org API.
 */
public class MarketTrendService {

    private static final String NEWS_API_KEY = "9ba9a31ad5ca464e8bf19866cc05160a";
    private static final String NEWS_API_URL = "https://newsapi.org/v2/everything";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public MarketTrendService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public MarketTrend analyzeTrendsForIndustry(String industry) throws IOException {
        MarketTrend trend = new MarketTrend(industry);

        List<NewsArticle> articles = fetchNewsForIndustry(industry);
        trend.setRelevantArticles(articles);
        trend.setHotSkills(extractHotSkills(articles, industry));
        trend.setTrendingKeywords(extractTrendingKeywords(articles));
        trend.setGrowthRate(calculateGrowthRate(articles));

        return trend;
    }

    private List<NewsArticle> fetchNewsForIndustry(String industry) throws IOException {
        String query = buildSearchQuery(industry);
        String url = NEWS_API_URL + "?q=" + query
                + "&language=en"
                + "&sortBy=publishedAt"
                + "&pageSize=50"
                + "&apiKey=" + NEWS_API_KEY;

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("News API error: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            List<NewsArticle> articles = new ArrayList<>();
            JsonNode articlesNode = root.path("articles");

            for (JsonNode node : articlesNode) {
                NewsArticle article = new NewsArticle();
                article.setTitle(node.path("title").asText(""));
                article.setDescription(node.path("description").asText(""));
                article.setUrl(node.path("url").asText(""));
                article.setSource(node.path("source").path("name").asText(""));
                String publishedAt = node.path("publishedAt").asText("");
                if (publishedAt.length() >= 10) {
                    try {
                        article.setPublishedAt(LocalDate.parse(publishedAt.substring(0, 10)));
                    } catch (Exception ignored) {
                    }
                }
                articles.add(article);
            }
            return articles;
        }
    }

    private String buildSearchQuery(String industry) {
        switch (industry.toLowerCase()) {
            case "it":
                return "technology+OR+software+OR+AI+OR+programming+OR+cloud+OR+cybersecurity";
            case "rh":
                return "human+resources+OR+recruitment+OR+talent+management+OR+HR+technology";
            case "finance":
                return "finance+OR+banking+OR+investment+OR+fintech+OR+stock+market";
            case "marketing":
                return "digital+marketing+OR+social+media+OR+SEO+OR+advertising+OR+branding";
            case "commercial":
                return "sales+OR+business+development+OR+CRM+OR+commerce+OR+retail";
            case "logistique":
                return "logistics+OR+supply+chain+OR+warehouse+OR+shipping+OR+automation";
            default:
                return industry + "+jobs+OR+" + industry + "+careers+OR+" + industry + "+trends";
        }
    }

    private List<String> extractHotSkills(List<NewsArticle> articles, String industry) {
        Map<String, List<String>> skillKeywords = new HashMap<>();
        skillKeywords.put("it", Arrays.asList("Python", "JavaScript", "React", "AI", "Machine Learning",
                "Cloud", "AWS", "DevOps", "Kubernetes", "TypeScript", "Rust", "Go", "Cybersecurity", "Blockchain"));
        skillKeywords.put("rh", Arrays.asList("Talent Acquisition", "Employee Experience", "HR Analytics",
                "Diversity", "Remote Work", "Onboarding", "HRIS", "Employer Branding"));
        skillKeywords.put("finance", Arrays.asList("Financial Analysis", "Risk Management", "ESG",
                "Cryptocurrency", "RegTech", "Data Analytics", "Compliance", "FinTech"));
        skillKeywords.put("marketing", Arrays.asList("SEO", "Content Marketing", "Social Media",
                "Analytics", "Influencer Marketing", "Email Marketing", "PPC", "UX Design"));
        skillKeywords.put("commercial", Arrays.asList("CRM", "B2B Sales", "Negotiation",
                "Account Management", "Business Development", "Lead Generation", "E-commerce"));
        skillKeywords.put("logistique", Arrays.asList("Supply Chain", "Automation", "Inventory Management",
                "Warehouse Management", "Last Mile", "IoT", "ERP"));

        List<String> industrySkills = skillKeywords.getOrDefault(industry.toLowerCase(),
                Arrays.asList("Management", "Communication", "Leadership", "Problem Solving", "Teamwork"));

        Map<String, Integer> skillCount = new LinkedHashMap<>();
        for (String skill : industrySkills)
            skillCount.put(skill, 0);

        for (NewsArticle article : articles) {
            String content = ((article.getTitle() != null ? article.getTitle() : "") + " " +
                    (article.getDescription() != null ? article.getDescription() : "")).toLowerCase();
            for (String skill : industrySkills) {
                if (content.contains(skill.toLowerCase())) {
                    skillCount.put(skill, skillCount.get(skill) + 1);
                }
            }
        }

        return skillCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(8)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> extractTrendingKeywords(List<NewsArticle> articles) {
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "and", "for", "that", "this", "with", "from", "are", "was", "were",
                "have", "has", "had", "been", "will", "would", "could", "should", "more",
                "about", "than", "into", "its", "not", "but", "also", "which", "their",
                "what", "when", "where", "who", "how", "all", "can", "each", "other", "new",
                "says", "said", "just", "most", "over", "after", "before", "between", "some",
                "removed", "null"));

        Map<String, Integer> wordCount = new HashMap<>();
        for (NewsArticle article : articles) {
            String text = (article.getTitle() != null ? article.getTitle() : "") + " " +
                    (article.getDescription() != null ? article.getDescription() : "");
            String[] words = text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
            for (String word : words) {
                if (word.length() > 3 && !stopWords.contains(word)) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }

        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(15)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateGrowthRate(List<NewsArticle> articles) {
        if (articles.isEmpty())
            return 0.0;
        LocalDate now = LocalDate.now();
        long recent = articles.stream()
                .filter(a -> a.getPublishedAt() != null && a.getPublishedAt().isAfter(now.minusDays(7)))
                .count();
        long older = articles.stream()
                .filter(a -> a.getPublishedAt() != null &&
                        a.getPublishedAt().isBefore(now.minusDays(7)) &&
                        a.getPublishedAt().isAfter(now.minusDays(30)))
                .count();
        if (older == 0)
            return recent > 0 ? 100.0 : 0.0;
        double olderNorm = older / 3.0;
        if (olderNorm == 0)
            return 100.0;
        return ((recent - olderNorm) / olderNorm) * 100;
    }
}
