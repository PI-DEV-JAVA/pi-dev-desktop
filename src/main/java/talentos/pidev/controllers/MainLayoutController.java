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
            if (controller instanceof talentos.pidev.controllers.quiz.QuizCandidatController qc) {
                qc.setMainLayout(this);
            }
            if (controller instanceof talentos.pidev.controllers.formations.FormationFormController ff) {
                ff.setMainLayout(this);
                ff.setSelfView(view);
            }



            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setView(Node view) {
        try {
            // Si le Node vient d'un FXMLLoader, on peut récupérer le controller via userData
            // Mais JavaFX ne le met pas automatiquement, donc on gère le cas classique :
            // -> on préfère une méthode dédiée (ci-dessous) quand on charge nous-mêmes.

            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
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