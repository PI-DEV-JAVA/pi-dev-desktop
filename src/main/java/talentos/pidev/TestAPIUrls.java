package talentos.pidev;

import talentos.pidev.services.AIScoringService;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.nio.file.Files;

public class TestAPIUrls {
    public static void main(String[] args) {
        try {
            System.out.println("🔍 TEST DE L'API AVEC ENDPOINT CORRECT");
            System.out.println("======================================");

            AIScoringService aiService = new AIScoringService();

            // Créer un fichier de test
            File testFile = new File("test-cv.txt");
            Files.write(testFile.toPath(),
                    "Jean Dupont\nExpérience: 5 ans en Java\nCompétences: Java, Spring, MySQL".getBytes());

            String jobDesc = "Développeur Java avec 5 ans d'expérience, maîtrise de Spring Boot et MySQL";

            System.out.println("📤 Soumission du job...");
            String jobId = aiService.submitScoringJob(testFile, jobDesc, "French");

            if (jobId != null && !jobId.isEmpty()) {
                System.out.println("✅ Job ID reçu: " + jobId);
                System.out.println("\n⏳ Attente du résultat (cela peut prendre 15-30 secondes)...");

                // Récupérer le résultat (max 12 tentatives = 60 secondes)
                JsonNode result = aiService.getScoringResultSync(jobId, 12);

                if (result != null) {
                    System.out.println("\n✅ RÉSULTAT OBTENU:");
                    System.out.println(result.toPrettyString());

                    // Afficher le score si disponible
                    if (result.has("score")) {
                        System.out.println("\n📊 Score global: " + result.path("score").asDouble());
                    }
                } else {
                    System.out.println("\n❌ Pas de résultat après 12 tentatives");
                }
            }

            testFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}