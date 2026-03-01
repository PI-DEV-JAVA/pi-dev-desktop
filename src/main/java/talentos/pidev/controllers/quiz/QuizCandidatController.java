package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import talentos.pidev.controllers.MainLayoutController;
import talentos.pidev.models.Quiz;
import talentos.pidev.services.QuizService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizCandidatController {

    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;

    private final QuizService quizService = new QuizService();

    private MainLayoutController mainLayout;

    private List<Quiz> all = new ArrayList<>();

    // temporaire : plus tard tu vas le récupérer de User/Session
    private String candidatEmail = "test@demo.com";

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
    }

    public void setCandidatEmail(String email) {
        this.candidatEmail = email;
    }

    @FXML
    public void initialize() {
        loadData();
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
            render(all);
            return;
        }

        List<Quiz> filtered = new ArrayList<>();
        for (Quiz quiz : all) {
            String t = quiz.getTitre() == null ? "" : quiz.getTitre().toLowerCase();
            String d = quiz.getDescription() == null ? "" : quiz.getDescription().toLowerCase();
            if (t.contains(q) || d.contains(q)) filtered.add(quiz);
        }
        render(filtered);
    }

    private void loadData() {
        try {
            // Pour candidat: on peut afficher tous les quiz actifs
            all = quizService.getAllQuizzesForCandidat();
            render(all);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur chargement quiz: " + e.getMessage()).show();
        }
    }

    private void render(List<Quiz> list) {
        cardsContainer.getChildren().clear();

        for (Quiz quiz : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizCandidatCard.fxml"));
                Node card = loader.load();

                QuizCandidatCardController c = loader.getController();
                c.setData(quiz, this);

                cardsContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // appelé depuis les cards
    public void openPassage(Quiz quiz) {
        if (quiz == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizPassage.fxml"));
            Node view = loader.load();

            QuizPassageController c = loader.getController();

            // retour vers la page catalogue

            c.setOnExit(() -> {
                if (mainLayout != null) mainLayout.setView(getSelfView());
            });



            c.start(quiz.getId(), quiz.getTitre(), candidatEmail);

            if (mainLayout != null) mainLayout.setView(view);
            else new Alert(Alert.AlertType.ERROR, "MainLayout null").show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture passage quiz: " + e.getMessage()).show();
        }
    }

    // recharger la même page (retour)
    private Node getSelfView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quiz/QuizCandidat.fxml"));
            Node root = loader.load();

            QuizCandidatController c = loader.getController();
            c.setMainLayout(mainLayout);
            c.setCandidatEmail(candidatEmail);

            return root;
        } catch (IOException e) {
            e.printStackTrace();
            return cardsContainer.getScene().getRoot();
        }
    }
}