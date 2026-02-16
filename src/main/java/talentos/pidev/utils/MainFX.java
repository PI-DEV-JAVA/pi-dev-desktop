package talentos.pidev.utils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainLayout.fxml"));
        Parent root = loader.load();
        Object controller = loader.getController();

        root.getProperties().put("controller", controller);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style/app.css").toExternalForm());

        stage.setTitle("PIDEV - RH");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
