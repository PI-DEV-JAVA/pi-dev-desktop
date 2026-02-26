package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import talentos.pidev.controllers.MainLayoutController;
import talentos.pidev.models.Choix;
import talentos.pidev.services.QuizService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChoicesRHController {

    @FXML private Label titleLabel;
    @FXML private FlowPane cardsContainer;

    private final QuizService quizService = new QuizService();

    private int questionId;
    private List<Choix> allChoices = new ArrayList<>();
    private Runnable onBack;
    private MainLayoutController mainLayout;

    // ===================== setters =====================

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
        loadData();
    }

    public void setTitle(String title) {
        if (titleLabel != null) titleLabel.setText(title);
    }

    @FXML
    public void initialize() {
        // rien, car questionId est injecté après via setQuestionId()
    }

    // ===================== UI actions =====================

    @FXML
    private void onAdd() {
        openChoiceForm(null);
    }

    @FXML
    private void onRefresh() {
        loadData();
    }
    public void refresh() { reload(); }

    public void openForm(talentos.pidev.models.Choix c) { openChoiceForm(c); }


    // ===================== Data & render =====================

    private void loadData() {
        if (questionId <= 0) return;

        try {
            allChoices = quizService.getChoicesByQuestion(questionId);
            renderCards(allChoices);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur chargement choix: " + e.getMessage()).show();
        }
    }

    private void renderCards(List<Choix> list) {
        cardsContainer.getChildren().clear();

        for (Choix c : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/ChoiceCard.fxml"));
                Node card = loader.load();

                // IMPORTANT: ChoiceCardController doit avoir setData(Choix, ChoicesRHController)
                ChoiceCardController cc = loader.getController();
                cc.setData(c, this);

                cardsContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ===================== navigation helpers =====================

    public int getQuestionId() {
        return questionId;
    }

    public void reload() {
        loadData();
    }
    private Node getSelfView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/ChoicesRH.fxml"));
            Node root = loader.load();

            ChoicesRHController c = loader.getController();
            c.setMainLayout(mainLayout);
            c.setOnBack(onBack);
            c.setQuestionId(questionId);
            c.setTitle(titleLabel != null ? titleLabel.getText() : "Choix");

            return root;

        } catch (IOException e) {
            e.printStackTrace();
            reload();
            return (mainLayout != null) ? null : cardsContainer.getScene().getRoot();
        }
    }

    public void openChoiceForm(Choix toEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/ChoiceForm.fxml"));
            Node view = loader.load();

            ChoiceFormController c = loader.getController();
            c.setParentController(this);
            c.setQuestionId(questionId);
            c.setChoiceToEdit(toEdit);

            // ✅ Save/Cancel -> retour vers la page choices
            c.setOnBack(() -> {
                if (mainLayout != null) {
                    mainLayout.setView(getSelfView());
                } else {
                    reload();
                }
            });

            if (mainLayout != null) {
                mainLayout.setView(view);
            } else {
                new Alert(Alert.AlertType.ERROR, "MainLayout null (ChoicesRHController)").show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture ChoiceForm: " + e.getMessage()).show();
        }
    }

    // ===================== generic loader =====================

    private <T> void loadIntoMainContent(String fxmlPath, ControllerInit<T> init) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            T controller = loader.getController();
            if (init != null && controller != null) init.init(controller);

            StackPane contentPane = (StackPane) cardsContainer.getScene().lookup("#contentPane");
            if (contentPane == null) {
                new Alert(Alert.AlertType.ERROR,
                        "contentPane introuvable. Vérifie fx:id=\"contentPane\" dans MainLayout.fxml").show();
                return;
            }

            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur navigation: " + e.getMessage()).show();
        }
    }
    public void setMainLayout(MainLayoutController mainLayout) { this.mainLayout = mainLayout; }
    public void setOnBack(Runnable r){ this.onBack = r; }

    @FXML
    private void onBack(){
        if(onBack != null) onBack.run();
    }


    @FunctionalInterface
    private interface ControllerInit<T> {
        void init(T controller);
    }
}
