
        package talentos.pidev;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import talentos.pidev.utils.DB;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. D'abord tester la connexion à la base de données
            System.out.println("🔍 Test de connexion à la base de données...");
            if (!testDatabaseConnection()) {
                showDatabaseError();
                return;
            }

            // 2. Charger l'interface principale
            System.out.println("🔄 Chargement de l'interface...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainLayout.fxml"));
            Scene scene = new Scene(loader.load());

            // 3. Appliquer le style CSS (si disponible)
            try {
                scene.getStylesheets().add(getClass().getResource("/style/app.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("ℹ️ CSS non trouvé, continuation sans style...");
            }

            // 4. Configurer la fenêtre principale
            primaryStage.setTitle("TalentOS - Gestion des Offres");

            // Essayer de charger une icône (optionnel)
            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            } catch (Exception e) {
                System.out.println("ℹ️ Logo non trouvé, continuation sans icône...");
            }

            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);

            // 5. Gérer la fermeture de l'application
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("👋 Fermeture de l'application...");
                DB.closeConnection();
                Platform.exit();
                System.exit(0);
            });

            // 6. Afficher la fenêtre
            primaryStage.show();
            System.out.println("✅ Application TalentOS démarrée avec succès !");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de l'interface : " + e.getMessage());
            showFatalError("Erreur d'interface",
                    "Impossible de charger l'interface principale.\n" +
                            "Vérifiez que les fichiers FXML existent dans le dossier /resources/fxml/");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue : " + e.getMessage());
            showFatalError("Erreur inattendue",
                    "Une erreur s'est produite au démarrage :\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Teste la connexion à la base de données
     */
    private boolean testDatabaseConnection() {
        try {
            // Tenter d'obtenir une connexion
            Connection conn = DB.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connexion à la base de données réussie !");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Échec de la connexion à la base de données : " + e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Affiche un message d'erreur de connexion base de données
     */
    private void showDatabaseError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de Connexion");
        alert.setHeaderText("Impossible de se connecter à la base de données");
        alert.setContentText(
                "Veuillez vérifier :\n\n" +
                        "1. ✅ Que MySQL est démarré\n" +
                        "2. ✅ Que la base 'main' existe\n" +
                        "3. ✅ Les identifiants dans DB.java\n" +
                        "4. ✅ Le port MySQL (3306 par défaut)\n\n" +
                        "Message d'erreur : " + getLastDatabaseError()
        );

        alert.showAndWait();

        // Fermer l'application après l'erreur
        Platform.exit();
        System.exit(1);
    }

    /**
     * Récupère le dernier message d'erreur SQL
     */
    private String getLastDatabaseError() {
        try {
            // Tenter une connexion pour obtenir l'erreur précise
            Connection conn = DB.getConnection();
            return "Aucune erreur (connexion réussie)";
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /**
     * Affiche une erreur fatale
     */
    private void showFatalError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Erreur Critique");
            alert.setContentText(message);
            alert.showAndWait();

            Platform.exit();
            System.exit(1);
        });
    }

    /**
     * Méthode main - Point d'entrée de l'application
     */
    public static void main(String[] args) {
        System.out.println("🚀 Démarrage de TalentOS...");
        System.out.println("========================");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("JavaFX Version: " + System.getProperty("javafx.version"));
        System.out.println("========================");

        // Lancer l'application JavaFX
        launch(args);
    }
}
