package talentos.pidev.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import talentos.pidev.models.dao.CandidateDAO;
import talentos.pidev.models.dao.InterviewDAO;
import talentos.pidev.models.schema.Interview;

import java.util.Map;

public class InterviewFormController {

    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<String> statusBox;
    @FXML
    private ComboBox<String> candidateBox; 
    @FXML
    private TextField gradeField;

    private Interview interview;
    private InterviewDAO dao;
    private InterviewController parentController;

    private Map<Long, String> candidatesMap; 

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("PENDING", "IN_PROGRESS", "COMPLETED");

        CandidateDAO candidateDAO = new CandidateDAO();
        candidatesMap = candidateDAO.getAllCandidates();
        candidateBox.setItems(FXCollections.observableArrayList(candidatesMap.values()));
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
        if (interview != null) {
            titleField.setText(interview.getTitle());
            statusBox.setValue(interview.getStatus());
            gradeField.setText(interview.getGeneralGrade() != null ? interview.getGeneralGrade().toString() : "");

            String candidateName = candidatesMap.get(interview.getCandidateId());
            if (candidateName != null) candidateBox.setValue(candidateName);
        }
    }

    public void setDao(InterviewDAO dao) {
        this.dao = dao;
    }

    public void setParentController(InterviewController parentController) {
        this.parentController = parentController;
    }

    @FXML
private void onSave() {
    try {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert("Title cannot be empty.");
            return;
        }

        String selectedCandidate = candidateBox.getValue();
        if (selectedCandidate == null || selectedCandidate.isEmpty()) {
            showAlert("Please select a candidate.");
            return;
        }

        String status = statusBox.getValue();
        if (status == null || status.isEmpty()) {
            showAlert("Please select a status.");
            return;
        }

        String gradeText = gradeField.getText().trim();
        Double grade = null;
        if (!gradeText.isEmpty()) {
            try {
                grade = Double.parseDouble(gradeText);
                if (grade < 0 || grade > 100) { 
                    showAlert("Grade must be between 0 and 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Grade must be a valid number.");
                return;
            }
        }

        if (interview == null) interview = new Interview();

        interview.setTitle(title);
        interview.setStatus(status);
        interview.setGeneralGrade(grade);

        for (Map.Entry<Long, String> entry : candidatesMap.entrySet()) {
            if (entry.getValue().equals(selectedCandidate)) {
                interview.setCandidateId(entry.getKey());
                break;
            }
        }

        if (interview.getId() == 0) dao.create(interview);
        else dao.update(interview);

        parentController.refresh();
        close();

    } catch (Exception e) {
        e.printStackTrace();
        showAlert("An unexpected error occurred: " + e.getMessage());
    }
}


    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
