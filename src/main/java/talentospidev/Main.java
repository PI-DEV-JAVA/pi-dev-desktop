package talentospidev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import talentospidev.utils.SceneUtil;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Register primary stage globally
        SceneUtil.setStage(stage);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml"));

        Scene scene = new Scene(loader.load(), SceneUtil.APP_WIDTH, SceneUtil.APP_HEIGHT);

        stage.setTitle("TalentOs");
        stage.setMinWidth(SceneUtil.APP_WIDTH);
        stage.setMinHeight(SceneUtil.APP_HEIGHT);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
