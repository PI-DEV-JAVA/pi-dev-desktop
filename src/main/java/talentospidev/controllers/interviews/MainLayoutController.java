package talentospidev.controllers.interviews;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainLayoutController {

    @FXML
    private StackPane contentPane;

    @FXML
    private Label pageTitle;

    private static MainLayoutController instance;

    public MainLayoutController() {
        instance = this;
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    public void navigate(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + fxml));
            Node view = loader.load();
            contentPane.getChildren().setAll(view);
            // pageTitle.setText(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> T navigatewithDependencieInjection(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlPath));
        Parent root = loader.load();
        contentPane.getChildren().setAll(root);
        return loader.getController();
    }

    @FXML
    public void initialize() {
        navigate("placeholder.fxml", "place holder");
    }
}
