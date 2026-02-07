package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

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
                    getClass().getResource("/fxml/" + fxml)
            );
            Node view = loader.load();
            contentPane.getChildren().setAll(view);
            // pageTitle.setText(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        navigate("placeholder.fxml", "place holder");
    }
}
