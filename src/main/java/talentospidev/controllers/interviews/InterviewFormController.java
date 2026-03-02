package talentospidev.controllers.interviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import talentospidev.dao.interviewsDAO.InterviewDAO;
import talentospidev.models.User;
import talentospidev.models.interviews.Interview;
import talentospidev.services.AuthService;
import talentospidev.utils.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InterviewFormController {

    @FXML private TextField titleField;
    @FXML private ComboBox<String> statusBox;
    @FXML private ComboBox<Candidate> candidateBox;
    @FXML private TextField gradeField;

    private Interview interview;
    private InterviewDAO dao;
    private InterviewController parentController;

    /** Small wrapper — same pattern as ActivityController.Employee */
    public static class Candidate {
        private final int id;
        private final String name;
        private final String email;

        public Candidate(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }

        @Override
        public String toString() { return name + " (" + email + ")"; }
    }

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("PENDING", "IN_PROGRESS", "COMPLETED");

        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null) {
            loadAcceptedCandidates(currentUser.getId());
        }
    }

    /**
     * Same SQL as ActivityController.loadAcceptedCandidates —
     * fetches only candidates the recruiter has accepted.
     */
    private void loadAcceptedCandidates(int recruiterId) {
        ObservableList<Candidate> candidates = FXCollections.observableArrayList();
        String sql = """
                SELECT DISTINCT u.id, u.email, p.first_name, p.last_name
                FROM users u
                JOIN applications a ON a.user_id = u.id
                JOIN offers o ON a.offer_id = o.id
                LEFT JOIN profiles p ON p.user_id = u.id
                WHERE o.recruiter_id = ? AND LOWER(a.status) LIKE '%accept%'
            """;
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int uid = rs.getInt("id");
                String email = rs.getString("email");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String name = (firstName != null && lastName != null)
                        ? firstName + " " + lastName
                        : email.split("@")[0];
                candidates.add(new Candidate(uid, name, email));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        candidateBox.setItems(candidates);
        if (candidates.isEmpty()) {
            candidateBox.setPromptText("No approved candidates");
        }
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
        if (interview != null) {
            titleField.setText(interview.getTitle());
            statusBox.setValue(interview.getStatus());
            gradeField.setText(interview.getGeneralGrade() != null ? interview.getGeneralGrade().toString() : "");

            // Select the matching candidate in the combo
            for (Candidate cand : candidateBox.getItems()) {
                if (cand.getId() == interview.getCandidateId()) {
                    candidateBox.setValue(cand);
                    break;
                }
            }
        }
    }

    public void setDao(InterviewDAO dao) { this.dao = dao; }
    public void setParentController(InterviewController parentController) { this.parentController = parentController; }

    @FXML
    private void onSave() {
        try {
            String title = titleField.getText().trim();
            if (title.isEmpty()) { showAlert("Title cannot be empty."); return; }

            Candidate selectedCandidate = candidateBox.getValue();
            if (selectedCandidate == null) {
                showAlert("Please select a candidate."); return;
            }

            String status = statusBox.getValue();
            if (status == null || status.isEmpty()) { showAlert("Please select a status."); return; }

            String gradeText = gradeField.getText().trim();
            Double grade = null;
            if (!gradeText.isEmpty()) {
                try {
                    grade = Double.parseDouble(gradeText);
                    if (grade < 0 || grade > 100) { showAlert("Grade must be between 0 and 100."); return; }
                } catch (NumberFormatException e) { showAlert("Grade must be a valid number."); return; }
            }

            if (interview == null) interview = new Interview();
            interview.setTitle(title);
            interview.setStatus(status);
            interview.setGeneralGrade(grade);
            interview.setCandidateId(selectedCandidate.getId());

            // Set recruiter to current user
            User currentUser = AuthService.getCurrentUser();
            if (currentUser != null) {
                interview.setRecruiterId(currentUser.getId());
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

    @FXML private void onCancel() { close(); }

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
