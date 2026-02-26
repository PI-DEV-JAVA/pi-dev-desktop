package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MainLayoutController {

    @FXML private StackPane contentPane;

    // ✅ JavaFX injecte automatiquement le controller de l'include dans sidebarController
    // à condition que l'include ait fx:id="sidebar"
    @FXML private SidebarController sidebarController;

    @FXML
    public void initialize() {

        // ✅ inject mainLayout into sidebar
        if (sidebarController != null) {
            sidebarController.setMainLayout(this);
            System.out.println("✅ MainLayout injected into SidebarController");
        } else {
            System.out.println("❌ sidebarController is null. Check <fx:include fx:id=\"sidebar\" .../>");
        }

        setContent("/fxml/formations/FormationsRH.fxml");
    }

    public void setContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            Object controller = loader.getController();
            System.out.println("Loaded controller = " + (controller == null ? "null" : controller.getClass().getName()));

            if (controller instanceof talentos.pidev.controllers.formations.FormationsRHController rh) {
                rh.setMainLayout(this);
            }
            if (controller instanceof talentos.pidev.controllers.formations.FormationsCandidatController cand) {
                cand.setMainLayout(this);
            }
            if (controller instanceof talentos.pidev.controllers.quiz.QuizRHController qrh) {
                qrh.setMainLayout(this);
            }

            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setView(Node view) {
        contentPane.getChildren().setAll(view);
    }

    public void goFormationsRH() {
        setContent("/fxml/formations/FormationsRH.fxml");
    }

    public void goFormationsCandidat() {
        setContent("/fxml/formations/FormationsCandidat.fxml");
    }

    public StackPane getContentPane() {
        return contentPane;
    }
}