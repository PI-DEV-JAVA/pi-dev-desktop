package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentos.pidev.dao.QuizDAO;
import talentos.pidev.models.Quiz;
import talentos.pidev.services.QuizService;

import java.sql.SQLException;
import java.time.LocalDate;

public class QuizFormController {

    @FXML private Label titleLabel;
    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private Spinner<Integer> spDuree;
    @FXML private DatePicker dpDate;
    @FXML private CheckBox cbActif;

    private Quiz quizToEdit;
    private int seanceId;
    private Runnable onDone;

    private Object parentController;

    private final QuizService quizService = new QuizService();
    private final QuizDAO quizDAO = new QuizDAO(); // for link seance_quiz

    @FXML
    public void initialize() {
        spDuree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, 30));
        dpDate.setValue(LocalDate.now());
        cbActif.setSelected(true);

        if (titleLabel != null) titleLabel.setText("Ajouter Quiz");
    }
    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    public void setSeanceId(int seanceId) {
        this.seanceId = seanceId;
    }

    public void setOnDone(Runnable r) {
        this.onDone = r;
    }

    public void setQuizToEdit(Quiz q) {
        this.quizToEdit = q;

        if (titleLabel != null) titleLabel.setText(q == null ? "Ajouter Quiz" : "Modifier Quiz");

        if (q != null) {
            tfTitre.setText(q.getTitre());
            taDescription.setText(q.getDescription());
            spDuree.getValueFactory().setValue(q.getDureeMinutes());
            dpDate.setValue(q.getDateCreation() != null ? q.getDateCreation() : LocalDate.now());
            cbActif.setSelected(q.isActif());
        } else {
            tfTitre.clear();
            taDescription.clear();
            spDuree.getValueFactory().setValue(30);
            dpDate.setValue(LocalDate.now());
            cbActif.setSelected(true);
        }
    }

    @FXML
    public void onSave() {
        String titre = tfTitre.getText() == null ? "" : tfTitre.getText().trim();
        if (titre.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Titre obligatoire").show();
            return;
        }

        Quiz q = (quizToEdit == null) ? new Quiz() : quizToEdit;
        q.setTitre(titre);
        q.setDescription(taDescription.getText());
        q.setDureeMinutes(spDuree.getValue());
        q.setDateCreation(dpDate.getValue() != null ? dpDate.getValue() : LocalDate.now());
        q.setActif(cbActif.isSelected());

        try {
            if (quizToEdit == null) quizService.add(q);
            else quizService.update(q);

            // ✅ Link quiz to seance if provided
            if (seanceId > 0 && q.getId() > 0) {
                quizDAO.linkQuizToSeance(seanceId, q.getId());
            }

            if (onDone != null) onDone.run();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur sauvegarde: " + e.getMessage()).show();
        }
    }

    @FXML
    public void onCancel() {
        if (onDone != null) onDone.run();
    }
}