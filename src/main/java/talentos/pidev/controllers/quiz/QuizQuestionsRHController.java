package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import talentos.pidev.controllers.MainLayoutController;
import talentos.pidev.models.Question;
import talentos.pidev.services.QuizService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizQuestionsRHController {

    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private ListView<Question> listView;

    private final QuizService quizService = new QuizService();

    private int quizId;
    private String quizTitre;

    private List<Question> allQuestions = new ArrayList<>();

    private Runnable onBack;
    private MainLayoutController mainLayout;

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
    }

    public void set(int quizId, String quizTitre) {
        this.quizId = quizId;
        this.quizTitre = quizTitre;

        if (titleLabel != null) titleLabel.setText("Questions pour " + quizTitre);
        loadData();
    }

    public void setOnBack(Runnable r) { this.onBack = r; }

    @FXML
    public void initialize() {
        if (listView == null) return;

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuestionRow.fxml"));
                    Node row = loader.load();

                    QuestionRowController c = loader.getController();
                    c.setData(item, QuizQuestionsRHController.this);

                    setText(null);
                    setGraphic(row);

                } catch (IOException e) {
                    e.printStackTrace();
                    setText(item.getEnonce());
                    setGraphic(null);
                }
            }
        });
    }

    @FXML
    private void onAdd() {
        openQuestionForm(null);
    }

    @FXML
    private void onRefresh() {
        if (searchField != null) searchField.clear();
        loadData();
    }

    @FXML
    private void onSearch() {
        String q = (searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();

        if (q.isEmpty()) {
            listView.getItems().setAll(allQuestions);
            return;
        }

        List<Question> filtered = new ArrayList<>();
        for (Question qu : allQuestions) {
            String en = qu.getEnonce() == null ? "" : qu.getEnonce().toLowerCase();
            String cor = qu.getCorrectPreview() == null ? "" : qu.getCorrectPreview().toLowerCase();
            if (en.contains(q) || cor.contains(q)) filtered.add(qu);
        }
        listView.getItems().setAll(filtered);
    }

    private void loadData() {
        if (quizId <= 0) return;

        try {
            allQuestions = quizService.getQuestionsByQuiz(quizId);
            listView.getItems().setAll(allQuestions);
        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur chargement questions: " + ex.getMessage()).show();
        }
    }

    public void reload() { loadData(); }

    // ✅ OUVRIR FORMULAIRE DANS LE MAINLAYOUT
    public void openQuestionForm(Question toEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuestionForm.fxml"));
            Node view = loader.load();

            QuestionFormController c = loader.getController();
            c.setParentController(this);
            c.setQuizId(quizId);
            c.setQuestionToEdit(toEdit);
            c.setOnDone(() -> {
                if (mainLayout != null) {
                    mainLayout.setView(getSelfView());
                } else {
                    reload();
                }
            });

            if (mainLayout != null) {
                mainLayout.setView(view);
            } else {
                new Alert(Alert.AlertType.ERROR, "MainLayout null: impossible d'ouvrir QuestionForm dans contentPane.").show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture QuestionForm: " + e.getMessage()).show();
        }
    }

    // ✅ recréer la page Questions actuelle (pour retour)
    private Node getSelfView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizQuestionsRH.fxml"));
            Node root = loader.load();

            QuizQuestionsRHController c = loader.getController();
            c.setMainLayout(mainLayout);
            c.setOnBack(onBack);
            c.set(quizId, quizTitre);

            return root;
        } catch (IOException e) {
            e.printStackTrace();
            // fallback
            reload();
            return listView.getScene().getRoot();
        }
    }

    public void openChoices(int questionId, String questionEnonce) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/ChoicesRH.fxml"));
            Node view = loader.load();

            ChoicesRHController c = loader.getController();
            c.setMainLayout(mainLayout);
            c.setQuestionId(questionId);
            c.setTitle("Choix — " + (questionEnonce == null ? "" : questionEnonce));

            // ✅ back vers Questions du quiz
            c.setOnBack(() -> {
                if (mainLayout != null) mainLayout.setView(getSelfView());
                else reload();
            });

            if (mainLayout != null) mainLayout.setView(view);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture choix: " + e.getMessage()).show();
        }
    }

    @FXML
    private void onBack() {
        if (onBack != null) onBack.run();
    }
}