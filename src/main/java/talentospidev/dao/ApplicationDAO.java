package talentospidev.dao;

import talentospidev.models.Application;
import talentospidev.utils.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    private final Connection connection;

    public ApplicationDAO() {
        this.connection = DB.getConnection();
    }

    // CREATE
    public int addApplication(Application application) throws SQLException {
        String sql = "INSERT INTO applications (user_id, offer_id, cv_file_path, motivation_letter, " +
                "status, application_date, score, notes, interviewer, interview_date, interview_result, " +
                "recruiter_response, response_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setApplicationParameters(pstmt, application);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int appId = rs.getInt(1);
                        updateApplicationsCount(application.getOfferId());
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

        try (Statement stmt = connection.createStatement();
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

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToApplication(rs);
                }
            }
        }
        return null;
    }

    // READ BY USER ID — Smart order: responded first, then by date
    public List<Application> getApplicationsByUserId(int userId) throws SQLException {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT * FROM applications WHERE user_id = ? " +
                "ORDER BY (recruiter_response IS NOT NULL AND recruiter_response != '') DESC, " +
                "application_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapResultSetToApplication(rs));
                }
            }
        }
        return applications;
    }

    // CHECK DUPLICATE — prevents double-apply
    public boolean hasUserApplied(int userId, int offerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE user_id = ? AND offer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, offerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // RECRUITER RESPONSE
    public boolean respondToApplication(int appId, String response, String newStatus) throws SQLException {
        String sql = "UPDATE applications SET recruiter_response = ?, response_date = ?, status = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, response);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            pstmt.setString(3, newStatus);
            pstmt.setInt(4, appId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // UPDATE
    public boolean updateApplication(Application application) throws SQLException {
        String sql = "UPDATE applications SET status = ?, score = ?, notes = ?, " +
                "interviewer = ?, interview_date = ?, interview_result = ?, " +
                "recruiter_response = ?, response_date = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, application.getStatus());
            pstmt.setDouble(2, application.getScore());
            pstmt.setString(3, application.getNotes());
            pstmt.setString(4, application.getInterviewer());
            pstmt.setDate(5,
                    application.getInterviewDate() != null ? Date.valueOf(application.getInterviewDate()) : null);
            pstmt.setString(6, application.getInterviewResult());
            pstmt.setString(7, application.getRecruiterResponse());
            pstmt.setDate(8,
                    application.getResponseDate() != null ? Date.valueOf(application.getResponseDate()) : null);
            pstmt.setInt(9, application.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean deleteApplication(int id) throws SQLException {
        String sql = "DELETE FROM applications WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // SEARCH
    public List<Application> searchApplications(String keyword, String status,
            LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Application> applications = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM applications WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (motivation_letter LIKE ?)");
            params.add("%" + keyword + "%");
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

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
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

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getApplicationsByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE status = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
    private void updateApplicationsCount(int offerId) throws SQLException {
        String sql = "UPDATE offers SET applications_received = applications_received + 1 WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, offerId);
            pstmt.executeUpdate();
        }
    }

    private void setApplicationParameters(PreparedStatement pstmt, Application app) throws SQLException {
        pstmt.setInt(1, app.getUserId());
        pstmt.setInt(2, app.getOfferId());
        pstmt.setString(3, app.getCvFilePath());
        pstmt.setString(4, app.getMotivationLetter());
        pstmt.setString(5, app.getStatus());
        pstmt.setDate(6, Date.valueOf(app.getApplicationDate()));
        pstmt.setDouble(7, app.getScore());
        pstmt.setString(8, app.getNotes());
        pstmt.setString(9, app.getInterviewer());
        pstmt.setDate(10, app.getInterviewDate() != null ? Date.valueOf(app.getInterviewDate()) : null);
        pstmt.setString(11, app.getInterviewResult());
        pstmt.setString(12, app.getRecruiterResponse());
        pstmt.setDate(13, app.getResponseDate() != null ? Date.valueOf(app.getResponseDate()) : null);
    }

    private Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setId(rs.getInt("id"));
        app.setUserId(rs.getInt("user_id"));
        app.setOfferId(rs.getInt("offer_id"));
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
        app.setRecruiterResponse(rs.getString("recruiter_response"));
        Date responseDate = rs.getDate("response_date");
        app.setResponseDate(responseDate != null ? responseDate.toLocalDate() : null);
        return app;
    }
}
