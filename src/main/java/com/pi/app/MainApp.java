package com.pi.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🔍 Chargement du FXML...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pi/views/main.fxml"));
            Parent root = loader.load();
            System.out.println("✅ FXML chargé avec succès");

            primaryStage.setTitle("Gestion des Événements RH");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();

            System.out.println("✅ Fenêtre affichée");

        } catch (Exception e) {
            System.out.println("❌ ERREUR DÉTAILLÉE :");
            System.out.println("Message: " + e.getMessage());
            System.out.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Aucune cause"));
            e.printStackTrace();  // ← Ceci affiche l'erreur complète
        }
    }

    public static void main(String[] args) {
        System.out.println("🚀 Démarrage de l'application...");
        launch(args);
    }
}