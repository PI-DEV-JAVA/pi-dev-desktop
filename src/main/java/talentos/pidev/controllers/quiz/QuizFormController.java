package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import talentos.pidev.models.Quiz;
import talentos.pidev.services.QuizService;

import java.sql.SQLException;
import java.time.LocalDate;

public class QuizFormController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private Spinner<Integer> spDuree;
    @FXML private DatePicker dpDate;
    @FXML private CheckBox cbActif;

    private QuizRHController parentController;
    private Quiz quizToEdit;

    private final QuizService quizService = new QuizService();

    @FXML
    public void initialize() {
        spDuree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, 30));
        dpDate.setValue(LocalDate.now());
        cbActif.setSelected(true);
    }

    public void setParentController(QuizRHController parentController) {
        this.parentController = parentController;
    }

    public void setQuizToEdit(Quiz q) {
        this.quizToEdit = q;
        if (q != null) {
            tfTitre.setText(q.getTitre());
            taDescription.setText(q.getDescription());
            spDuree.getValueFactory().setValue(q.getDureeMinutes());
            dpDate.setValue(q.getDateCreation() != null ? q.getDateCreation() : LocalDate.now());
            cbActif.setSelected(q.isActif());
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

            if (parentController != null) parentController.showList();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur sauvegarde: " + e.getMessage()).show();
        }
    }


    @FXML
    public void onCancel() {
        if (parentController != null) parentController.showList();
    }
}
