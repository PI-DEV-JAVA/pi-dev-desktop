package talentospidev.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneUtil {

    private static Stage stage;

    /** Standard app window size — consistent across all views. */
    public static final double APP_WIDTH = 1050;
    public static final double APP_HEIGHT = 700;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void switchScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneUtil.class.getResource("/fxml/" + fxml));
            Scene scene = new Scene(loader.load(), APP_WIDTH, APP_HEIGHT);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
