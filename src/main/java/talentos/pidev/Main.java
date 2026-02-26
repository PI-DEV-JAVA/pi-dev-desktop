
package talentos.pidev;

import java.io.IOException;

import org.freedesktop.gstreamer.Gst;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void init() {
        Gst.init("JavaFX-webRTC", new String[]{});
    }

    @Override
    public void stop(){
        Gst.deinit();
        Platform.exit();
        System.exit(0);
    }

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
