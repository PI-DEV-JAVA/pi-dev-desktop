package pi_dev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Load activities by default
        loadActivitiesView();
        
        primaryStage.setTitle("RH Recruit - Management System");
        primaryStage.show();
    }
    
    // Method to load Activities view
    public static void loadActivitiesView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/view/activities.fxml")
            );
            
            Scene scene = new Scene(loader.load(), 1200, 800);
            
            // Add CSS if you have it
            try {
                scene.getStylesheets().add(
                    MainApp.class.getResource("/style/style.css").toExternalForm()
                );
            } catch (Exception e) {
                // CSS not found, continue without it
            }
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("Activities Management");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Method to load Projects view
    public static void loadProjectsView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/view/projects.fxml")
            );
            
            Scene scene = new Scene(loader.load(), 1200, 800);
            
            // Add CSS if you have it
            try {
                scene.getStylesheets().add(
                    MainApp.class.getResource("/style/style.css").toExternalForm()
                );
            } catch (Exception e) {
                // CSS not found, continue without it
            }
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("Projects Management");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}