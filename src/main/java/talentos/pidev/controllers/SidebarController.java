package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;

public class SidebarController {

    @FXML private Parent sidebarRoot;

    // ✅ NEW: reference to MainLayoutController
    private MainLayoutController mainLayout;

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
    }

    private void open(String fxmlPath) {
        try {
            if (mainLayout == null) {
                new Alert(Alert.AlertType.ERROR,
                        "MainLayout non injecté dans SidebarController.\n" +
                                "Fix: injecter SidebarController.setMainLayout(...) depuis MainLayoutController.")
                        .show();
                return;
            }

            mainLayout.setContent(fxmlPath);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur navigation: " + e.getMessage()).show();
        }
    }

    // ===================== FORMATION =====================
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

    // ===================== QUIZ =====================
    @FXML
    private void goQuizRH() {
        open("/fxml/quiz/QuizRH.fxml");
    }

    @FXML
    private void goQuestionsRH() {
        open("/fxml/quiz/QuizQuestionsRH.fxml");
    }
}