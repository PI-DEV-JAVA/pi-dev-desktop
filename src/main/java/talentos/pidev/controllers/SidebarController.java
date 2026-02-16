package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class SidebarController {

    private MainLayoutController mainLayoutController;

    @FXML
    public void initialize() {
        // rien
    }

    private MainLayoutController getMainLayoutController() {
        if (mainLayoutController != null) return mainLayoutController;

        // Sidebar est inclus dans MainLayout -> on remonte au parent BorderPane
        Node node = (Node) (Object) this; // (Astuce: pas fiable)
        return mainLayoutController;
    }

    // ✅ Méthode robuste: chercher BorderPane puis controller
    private MainLayoutController findMainLayoutController() {
        try {
            // récupérer root scene -> BorderPane
            BorderPane root = (BorderPane) javafx.stage.Stage.getWindows().stream()
                    .filter(w -> w.isShowing())
                    .findFirst()
                    .map(w -> ((javafx.stage.Stage) w).getScene().getRoot())
                    .orElse(null);

            if (root == null) return null;

            Object controller = root.getProperties().get("controller");
            if (controller instanceof MainLayoutController mlc) return mlc;

        } catch (Exception ignored) {}
        return null;
    }

    private void open(String path) {
        MainLayoutController mlc = findMainLayoutController();
        if (mlc != null) mlc.setContent(path);
    }

    @FXML
    private void goFormationsRH() {
        open("/fxml/formations/FormationsRH.fxml");
    }

    @FXML
    private void goFormationsCandidat() {
        open("/fxml/formations/FormationsCandidat.fxml");
    }

    @FXML
    private void goInscriptionsRH() {
        open("/fxml/formations/InscriptionsRH.fxml");
    }
}
