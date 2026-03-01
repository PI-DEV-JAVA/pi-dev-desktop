package talentos.pidev.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import talentos.pidev.models.MarketTrend;
import talentos.pidev.models.NewsArticle;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MarketTrendService {

    private static final String NEWS_API_KEY = "9ba9a31ad5ca464e8bf19866cc05160a"; // À obtenir sur newsapi.org
    private static final String NEWS_API_URL = "https://newsapi.org/v2/everything";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public MarketTrendService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Analyse les tendances pour un secteur d'activité
     */
    public MarketTrend analyzeTrendsForIndustry(String industry) throws IOException {
        MarketTrend trend = new MarketTrend(industry);

        // 1. Récupérer les articles récents
        List<NewsArticle> articles = fetchNewsForIndustry(industry);
        trend.setRelevantArticles(articles);

        // 2. Extraire les compétences tendance
        List<String> hotSkills = extractHotSkills(articles, industry);
        trend.setHotSkills(hotSkills);

        // 3. Analyser les mots-clés fréquents
        List<String> keywords = extractTrendingKeywords(articles);
        trend.setTrendingKeywords(keywords);

        // 4. Calculer le taux de croissance (basé sur le volume d'articles)
        trend.setGrowthRate(calculateGrowthRate(articles));

        return trend;
    }

    /**
     * Récupère les actualités pour un secteur
     */
    private List<NewsArticle> fetchNewsForIndustry(String industry) throws IOException {
        String query = buildSearchQuery(industry);
        String url = NEWS_API_URL + "?q=" + query
                + "&language=fr"
                + "&sortBy=publishedAt"
                + "&pageSize=50"
                + "&apiKey=" + NEWS_API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur API News: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);

            List<NewsArticle> articles = new ArrayList<>();
            JsonNode articlesNode = root.path("articles");

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            for (JsonNode articleNode : articlesNode) {
                NewsArticle article = new NewsArticle();
                article.setTitle(articleNode.path("title").asText());
                article.setDescription(articleNode.path("description").asText());
                article.setUrl(articleNode.path("url").asText());
                article.setSource(articleNode.path("source").path("name").asText());

                String publishedAt = articleNode.path("publishedAt").asText();
                if (!publishedAt.isEmpty()) {
                    article.setPublishedAt(LocalDate.parse(publishedAt.substring(0, 10)));
                }

                articles.add(article);
            }

            return articles;
        }
    }

    /**
     * Construit la requête de recherche optimisée
     */
    private String buildSearchQuery(String industry) {
        switch (industry.toLowerCase()) {
            case "it":
                return "technologie OR informatique OR développement OR logiciel OR IA OR intelligence artificielle";
            case "rh":
                return "ressources humaines OR recrutement OR RH OR management OR talents";
            case "finance":
                return "finance OR banque OR investissement OR bourse OR économie";
            case "marketing":
                return "marketing OR publicité OR digital OR réseaux sociaux OR SEO";
            case "production":
                return "industrie OR production OR usine OR fabrication OR automatisation";
            default:
                return industry + " OR " + industry + " emploi OR " + industry + " recrutement";
        }
    }

    /**
     * Extrait les compétences tendance des articles
     */
    private List<String> extractHotSkills(List<NewsArticle> articles, String industry) {
        // Mots-clés de compétences par secteur
        Map<String, List<String>> skillKeywords = new HashMap<>();
        skillKeywords.put("it", Arrays.asList("Java", "Python", "JavaScript", "React", "Angular",
                "Cloud", "AWS", "DevOps", "IA", "Machine Learning",
                "Cybersécurité", "Blockchain"));
        skillKeywords.put("rh", Arrays.asList("Sourcing", "Onboarding", "Formation", "Évaluation",
                "GPEC", "SIRH", "Mobilité", "Marque employeur"));
        skillKeywords.put("finance", Arrays.asList("Analyse financière", "Comptabilité", "Audit",
                "Gestion de portefeuille", "Fiscalité", "Contrôle de gestion"));
        skillKeywords.put("marketing", Arrays.asList("SEO", "SEM", "Content Marketing", "Social Media",
                "Email Marketing", "Analytics", "Branding"));

        List<String> industrySkills = skillKeywords.getOrDefault(industry.toLowerCase(),
                Arrays.asList("Management", "Communication", "Leadership", "Organisation"));

        // Compter les occurrences
        Map<String, Integer> skillCount = new HashMap<>();
        for (String skill : industrySkills) {
            skillCount.put(skill, 0);
        }

        for (NewsArticle article : articles) {
            String content = (article.getTitle() + " " + article.getDescription()).toLowerCase();

            for (String skill : industrySkills) {
                if (content.contains(skill.toLowerCase())) {
                    skillCount.put(skill, skillCount.get(skill) + 1);
                }
            }
        }

        // Trier par fréquence et retourner les 5 plus fréquents
        return skillCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Extrait les mots-clés tendance
     */
    private List<String> extractTrendingKeywords(List<NewsArticle> articles) {
        // Mots vides à ignorer
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "le", "la", "les", "un", "une", "des", "et", "ou", "mais", "donc",
                "car", "pour", "dans", "avec", "sans", "sur", "sous", "chez"
        ));

        Map<String, Integer> wordCount = new HashMap<>();

        for (NewsArticle article : articles) {
            String text = article.getTitle() + " " + article.getDescription();
            String[] words = text.toLowerCase()
                    .replaceAll("[^a-zA-Z0-9\\s]", "")
                    .split("\\s+");

            for (String word : words) {
                if (word.length() > 3 && !stopWords.contains(word)) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }

        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Calcule le taux de croissance basé sur le volume d'articles
     */
    private double calculateGrowthRate(List<NewsArticle> articles) {
        if (articles.isEmpty()) return 0.0;

        // Séparer les articles en deux périodes
        LocalDate now = LocalDate.now();
        List<NewsArticle> recent = articles.stream()
                .filter(a -> a.getPublishedAt() != null &&
                        a.getPublishedAt().isAfter(now.minusDays(7)))
                .collect(Collectors.toList());

        List<NewsArticle> older = articles.stream()
                .filter(a -> a.getPublishedAt() != null &&
                        a.getPublishedAt().isBefore(now.minusDays(7)) &&
                        a.getPublishedAt().isAfter(now.minusDays(30)))
                .collect(Collectors.toList());

        if (older.isEmpty()) return 100.0; // Nouveau secteur en croissance

        double recentCount = recent.size();
        double olderCount = older.size() / 3.0; // Normalisé sur 7 jours

        if (olderCount == 0) return 100.0;

        return ((recentCount - olderCount) / olderCount) * 100;
    }
}