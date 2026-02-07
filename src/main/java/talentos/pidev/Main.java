
package talentos.pidev;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/MainLayout.fxml"));

        Scene scene;
        try {
            scene = new Scene(loader.load());
            scene.getStylesheets().add(
                getClass().getResource("/style/app.css").toExternalForm()
        );
            stage.setTitle("TalentOS");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
