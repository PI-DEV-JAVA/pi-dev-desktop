package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MainLayoutController {

    @FXML private StackPane contentPane;

    @FXML
    public void initialize() {
        // ✅ default page
        setContent("/fxml/formations/FormationsRH.fxml");
    }

    public void setContent(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
