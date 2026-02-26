package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import talentos.pidev.controllers.MainLayoutController;
import talentos.pidev.models.Quiz;
import talentos.pidev.services.QuizService;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizRHController {

    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;

    private final QuizService quizService = new QuizService();
    private List<Quiz> all = new ArrayList<>();

    private MainLayoutController mainLayout;

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
    }

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> filterAndRender());
        }
        loadData();
    }

    @FXML
    private void onRefresh() {
        loadData();
    }

    @FXML
    private void onAdd() {
        openForm(null);
    }

    public void reload() { loadData(); }

    // ===================== navigation =====================

    public void openForm(Quiz toEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizForm.fxml"));
            Node root = loader.load();

            QuizFormController c = loader.getController();
            c.setParentController(this);
            c.setQuizToEdit(toEdit);

            if (mainLayout != null) mainLayout.setView(root);
            else System.out.println("❌ mainLayout null in QuizRHController.openForm()");

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture formulaire quiz: " + e.getMessage()).show();
        }
    }

    public void showList() {
        if (mainLayout != null) {
            mainLayout.setContent("/fxml/quiz/QuizRH.fxml"); // reload with injection
        } else {
            System.out.println("❌ mainLayout null in QuizRHController.showList()");
        }
    }

    public void openQuestionsForQuiz(Quiz quiz) {
        if (quiz == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizQuestionsRH.fxml"));
            Node root = loader.load();

            QuizQuestionsRHController c = loader.getController();
            c.setMainLayout(mainLayout);
            c.set(quiz.getId(), quiz.getTitre());
            c.setOnBack(this::showList);

            if (mainLayout != null) mainLayout.setView(root);
            else System.out.println("❌ mainLayout null in QuizRHController.openQuestionsForQuiz()");

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture questions quiz: " + e.getMessage()).show();
        }
    }

    // ===================== Data =====================

    private void loadData() {
        try {
            all = quizService.getAll();
            filterAndRender();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur chargement quiz: " + e.getMessage()).show();
        }
    }

    private void filterAndRender() {
        String q = (searchField == null || searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();

        List<Quiz> filtered;
        if (q.isEmpty()) filtered = all;
        else {
            filtered = new ArrayList<>();
            for (Quiz z : all) {
                String titre = z.getTitre() == null ? "" : z.getTitre().toLowerCase();
                String desc = z.getDescription() == null ? "" : z.getDescription().toLowerCase();
                if (titre.contains(q) || desc.contains(q)) filtered.add(z);
            }
        }
        renderCards(filtered);
    }

    private void renderCards(List<Quiz> list) {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();

        for (Quiz q : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizCard.fxml"));
                Node card = loader.load();

                QuizCardController c = loader.getController();
                c.setData(q, this);

                if (card instanceof Region r) r.setMinWidth(0);
                cardsContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur chargement carte quiz: " + e.getMessage()).show();
            }
        }
    }
}