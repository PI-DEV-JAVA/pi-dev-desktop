package talentospidev.dao.coursesDAO;

import talentospidev.models.courses.Quiz;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {
    private final Connection conn = DB.getConnection();

    public void add(Quiz q) throws SQLException {
        String sql = "INSERT INTO quiz (titre,description,duree_minutes,actif,seance_id) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getTitre());
            ps.setString(2, q.getDescription());
            ps.setInt(3, q.getDureeMinutes());
            ps.setBoolean(4, q.isActif());
            if (q.getSeanceId() != null) ps.setInt(5, q.getSeanceId()); else ps.setNull(5, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) q.setId(rs.getInt(1)); }
        }
    }

    public void update(Quiz q) throws SQLException {
        String sql = "UPDATE quiz SET titre=?,description=?,duree_minutes=?,actif=?,seance_id=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getTitre());
            ps.setString(2, q.getDescription());
            ps.setInt(3, q.getDureeMinutes());
            ps.setBoolean(4, q.isActif());
            if (q.getSeanceId() != null) ps.setInt(5, q.getSeanceId()); else ps.setNull(5, Types.INTEGER);
            ps.setInt(6, q.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM quiz WHERE id=?")) { ps.setInt(1, id); ps.executeUpdate(); }
    }

    public Quiz getById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM quiz WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    public Quiz getBySeanceId(int seanceId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM quiz WHERE seance_id=?")) {
            ps.setInt(1, seanceId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    public List<Quiz> getAll() throws SQLException {
        List<Quiz> list = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM quiz ORDER BY created_at DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Quiz map(ResultSet rs) throws SQLException {
        Quiz q = new Quiz();
        q.setId(rs.getInt("id"));
        q.setTitre(rs.getString("titre"));
        q.setDescription(rs.getString("description"));
        q.setDureeMinutes(rs.getInt("duree_minutes"));
        q.setActif(rs.getBoolean("actif"));
        Timestamp ts = rs.getTimestamp("created_at"); if (ts != null) q.setCreatedAt(ts.toLocalDateTime());
        int sid = rs.getInt("seance_id"); if (!rs.wasNull()) q.setSeanceId(sid);
        return q;
    }
}
