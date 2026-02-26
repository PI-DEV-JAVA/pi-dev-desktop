package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentos.pidev.models.Choix;
import talentos.pidev.services.QuizService;

import java.sql.SQLException;

public class ChoiceFormController {

    @FXML private Label titleLabel;
    @FXML private TextArea texteArea;
    @FXML private CheckBox correctCheck;
    @FXML private Label errorLabel;

    private int questionId;
    private Choix choiceToEdit;
    private ChoicesRHController parent;

    private Runnable onBack;   // ✅ important

    private final QuizService quizService = new QuizService();

    public void setParentController(ChoicesRHController parent) {
        this.parent = parent;
    }

    public void setOnBack(Runnable r){
        this.onBack = r;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public void setChoiceToEdit(Choix c) {
        this.choiceToEdit = c;

        if (c == null) {
            titleLabel.setText("Ajouter choix");
            texteArea.clear();
            correctCheck.setSelected(false);
        } else {
            titleLabel.setText("Modifier choix");
            texteArea.setText(c.getTexte());
            correctCheck.setSelected(c.isEstCorrect());
        }
    }

    @FXML
    private void onSave() {

        errorLabel.setText("");

        String txt = texteArea.getText() == null ? "" : texteArea.getText().trim();

        if (txt.isEmpty()) {
            errorLabel.setText("Texte obligatoire.");
            return;
        }

        try {

            if (choiceToEdit == null) {
                Choix c = new Choix();
                c.setQuestionId(questionId);
                c.setTexte(txt);
                c.setEstCorrect(correctCheck.isSelected());
                quizService.addChoice(c);
            } else {
                choiceToEdit.setTexte(txt);
                choiceToEdit.setEstCorrect(correctCheck.isSelected());
                quizService.updateChoice(choiceToEdit);
            }

            // ✅ retourner vers la page précédente
            if (onBack != null) {
                onBack.run();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {

        // ✅ juste revenir
        if (onBack != null) {
            onBack.run();
        }
    }
}