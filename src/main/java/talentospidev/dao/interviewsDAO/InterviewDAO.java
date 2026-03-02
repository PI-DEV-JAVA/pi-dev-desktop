package talentospidev.dao.interviewsDAO;



import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import talentospidev.models.interviews.Interview;
import talentospidev.utils.DB;

public class InterviewDAO {

    private Connection conn;

    public InterviewDAO() throws SQLException {
        this.conn = DB.getConnection();
    }

    public void create(Interview i) {
        System.out.println(i.getCandidateId());
        String sql = """
            INSERT INTO interview (title, recruiter_id, candidate_id, status, general_grade)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getTitle());
            ps.setLong(2, i.getRecruiterId());
            ps.setLong(3, i.getCandidateId());
            ps.setString(4, i.getStatus());
            if (i.getGeneralGrade() != null)
                ps.setDouble(5, i.getGeneralGrade());
            else
                ps.setNull(5, Types.DECIMAL);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) i.setId(rs.getLong(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Interview i) {
        System.out.println(i.getCandidateId());

        String sql = """
            UPDATE interview SET title=?, recruiter_id=?, candidate_id=?, status=?, general_grade=?
            WHERE id=?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, i.getTitle());
            ps.setLong(2, i.getRecruiterId());
            ps.setLong(3, i.getCandidateId());
            ps.setString(4, i.getStatus());
            if (i.getGeneralGrade() != null)
                ps.setDouble(5, i.getGeneralGrade());
            else
                ps.setNull(5, Types.DECIMAL);
            ps.setLong(6, i.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(long id) {
        String sql = "DELETE FROM interview WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Interview> list(String keyword, boolean asc) {
        String sql = """
            SELECT * FROM interview
            WHERE title LIKE ? 
            ORDER BY created_at %s
        """.formatted(asc ? "ASC" : "DESC");

        List<Interview> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Interview i = new Interview();
                i.setId(rs.getLong("id"));
                i.setTitle(rs.getString("title"));
                i.setRecruiterId(rs.getLong("recruiter_id"));
                i.setCandidateId(rs.getLong("candidate_id"));
                i.setStatus(rs.getString("status"));
                double grade = rs.getDouble("general_grade");
                if (!rs.wasNull()) i.setGeneralGrade(grade);
                i.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(i);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Interview> listByCandidateId(long candidateId, String keyword, boolean asc) {
        String sql = """
            SELECT * FROM interview
            WHERE candidate_id = ? AND title LIKE ?
            ORDER BY created_at %s
        """.formatted(asc ? "ASC" : "DESC");

        List<Interview> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, candidateId);
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Interview i = new Interview();
                i.setId(rs.getLong("id"));
                i.setTitle(rs.getString("title"));
                i.setRecruiterId(rs.getLong("recruiter_id"));
                i.setCandidateId(rs.getLong("candidate_id"));
                i.setStatus(rs.getString("status"));
                double grade = rs.getDouble("general_grade");
                if (!rs.wasNull()) i.setGeneralGrade(grade);
                i.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(i);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Interview findById(long id) {
        String sql = "SELECT * FROM interview WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Interview i = new Interview();
                i.setId(rs.getLong("id"));
                i.setTitle(rs.getString("title"));
                i.setRecruiterId(rs.getLong("recruiter_id"));
                i.setCandidateId(rs.getLong("candidate_id"));
                i.setStatus(rs.getString("status"));
                double grade = rs.getDouble("general_grade");
                if (!rs.wasNull()) i.setGeneralGrade(grade);
                i.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return i;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
