
        package talentos.pidev.dao;

import talentos.pidev.models.Application;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    // CREATE
    public int addApplication(Application application) throws SQLException {
        String sql = "INSERT INTO applications (offer_id, candidate_name, candidate_email, " +
                "candidate_phone, cv_file_path, motivation_letter, status, " +
                "application_date, score, notes, interviewer, interview_date, interview_result) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setApplicationParameters(pstmt, application);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int appId = rs.getInt(1);
                        // Update applications count in offers table
                        updateApplicationsCount(application.getOfferId(), conn);
                        return appId;
                    }
                }
            }
            return -1;
        }
    }

    // READ ALL
    public List<Application> getAllApplications() throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT * FROM applications ORDER BY application_date DESC";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }
        }
        return applications;
    }

    // READ BY OFFER ID
    public List<Application> getApplicationsByOffer(int offerId) throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT * FROM applications WHERE offer_id = ? ORDER BY application_date DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, offerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToApplication(rs));
                }
            }
        }
        return applications;
    }

    // READ BY ID
    public Application getApplicationById(int id) throws SQLException {
        String sql = "SELECT * FROM applications WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToApplication(rs);
                }
            }
        }
        return null;
    }

    // UPDATE
    public boolean updateApplication(Application application) throws SQLException {
        String sql = "UPDATE applications SET status = ?, score = ?, notes = ?, " +
                "interviewer = ?, interview_date = ?, interview_result = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, application.getStatus());
            pstmt.setDouble(2, application.getScore());
            pstmt.setString(3, application.getNotes());
            pstmt.setString(4, application.getInterviewer());
            pstmt.setDate(5, application.getInterviewDate() != null ?
                    Date.valueOf(application.getInterviewDate()) : null);
            pstmt.setString(6, application.getInterviewResult());
            pstmt.setInt(7, application.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean deleteApplication(int id) throws SQLException {
        String sql = "DELETE FROM applications WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // SEARCH
    public List<Application> searchApplications(String candidateName, String status,
                                                LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Application> applications = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM applications WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (candidateName != null && !candidateName.isEmpty()) {
            sql.append(" AND candidate_name LIKE ?");
            params.add("%" + candidateName + "%");
        }

        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        if (fromDate != null) {
            sql.append(" AND application_date >= ?");
            params.add(Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append(" AND application_date <= ?");
            params.add(Date.valueOf(toDate));
        }

        sql.append(" ORDER BY application_date DESC");

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToApplication(rs));
                }
            }
        }
        return applications;
    }

    // STATISTICS
    public int getTotalApplications() throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getApplicationsByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE status = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // HELPER METHODS
    private void updateApplicationsCount(int offerId, Connection conn) throws SQLException {
        String sql = "UPDATE offers SET applications_received = applications_received + 1 WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, offerId);
            pstmt.executeUpdate();
        }
    }

    private void setApplicationParameters(PreparedStatement pstmt, Application app) throws SQLException {
        pstmt.setInt(1, app.getOfferId());
        pstmt.setString(2, app.getCandidateName());
        pstmt.setString(3, app.getCandidateEmail());
        pstmt.setString(4, app.getCandidatePhone());
        pstmt.setString(5, app.getCvFilePath());
        pstmt.setString(6, app.getMotivationLetter());
        pstmt.setString(7, app.getStatus());
        pstmt.setDate(8, Date.valueOf(app.getApplicationDate()));
        pstmt.setDouble(9, app.getScore());
        pstmt.setString(10, app.getNotes());
        pstmt.setString(11, app.getInterviewer());
        pstmt.setDate(12, app.getInterviewDate() != null ? Date.valueOf(app.getInterviewDate()) : null);
        pstmt.setString(13, app.getInterviewResult());
    }

    private Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setId(rs.getInt("id"));
        app.setOfferId(rs.getInt("offer_id"));
        app.setCandidateName(rs.getString("candidate_name"));
        app.setCandidateEmail(rs.getString("candidate_email"));
        app.setCandidatePhone(rs.getString("candidate_phone"));
        app.setCvFilePath(rs.getString("cv_file_path"));
        app.setMotivationLetter(rs.getString("motivation_letter"));
        app.setStatus(rs.getString("status"));
        app.setApplicationDate(rs.getDate("application_date").toLocalDate());
        app.setScore(rs.getDouble("score"));
        app.setNotes(rs.getString("notes"));
        app.setInterviewer(rs.getString("interviewer"));
        Date interviewDate = rs.getDate("interview_date");
        app.setInterviewDate(interviewDate != null ? interviewDate.toLocalDate() : null);
        app.setInterviewResult(rs.getString("interview_result"));
        return app;
    }
}
