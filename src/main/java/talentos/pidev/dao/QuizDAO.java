package talentos.pidev.dao;

import talentos.pidev.models.Quiz;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {

    private final Connection connection;

    public QuizDAO() {
        connection = DB.getInstance().getMyConnection();
    }

    public void add(Quiz q) throws SQLException {
        String sql = "INSERT INTO quiz(titre, description, duree_minutes, actif, created_at) VALUES(?,?,?,?,NOW())";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getTitre());
            ps.setString(2, q.getDescription());
            ps.setInt(3, q.getDureeMinutes());
            ps.setBoolean(4, q.isActif());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) q.setId(rs.getInt(1));
            }
        }
    }

    public void update(Quiz q) throws SQLException {
        String sql = "UPDATE quiz SET titre=?, description=?, duree_minutes=?, actif=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, q.getTitre());
            ps.setString(2, q.getDescription());
            ps.setInt(3, q.getDureeMinutes());
            ps.setBoolean(4, q.isActif());
            ps.setInt(5, q.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM quiz WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Quiz> getAll() throws SQLException {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT id, titre, description, duree_minutes, actif, created_at FROM quiz ORDER BY id DESC";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Quiz q = new Quiz();
                q.setId(rs.getInt("id"));
                q.setTitre(rs.getString("titre"));
                q.setDescription(rs.getString("description"));
                q.setDureeMinutes(rs.getInt("duree_minutes"));
                q.setActif(rs.getBoolean("actif"));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) q.setDateCreation(ts.toLocalDateTime().toLocalDate());
                else q.setDateCreation(LocalDate.now());

                list.add(q);
            }
        }
        return list;
    }

    // ======================= NEW (SEANCE ↔ QUIZ) =======================

    public Quiz findBySeanceId(int seanceId) throws SQLException {
        String sql = """
            SELECT id, titre, description, duree_minutes, actif, created_at, seance_id
            FROM quiz
            WHERE seance_id = ?
            LIMIT 1
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, seanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Quiz q = new Quiz();
                    q.setId(rs.getInt("id"));
                    q.setTitre(rs.getString("titre"));
                    q.setDescription(rs.getString("description"));
                    q.setDureeMinutes(rs.getInt("duree_minutes"));
                    q.setActif(rs.getBoolean("actif"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) q.setDateCreation(ts.toLocalDateTime().toLocalDate());
                    else q.setDateCreation(LocalDate.now());

                    // si ton modèle Quiz a seanceId
                     q.setSeanceId(rs.getInt("seance_id"));

                    return q;
                }
            }
        }
        return null;
    }

    public void linkQuizToSeance(int seanceId, int quizId) throws SQLException {
        connection.setAutoCommit(false);
        try {
            // libérer ancien quiz déjà lié à cette séance
            try (PreparedStatement ps1 = connection.prepareStatement(
                    "UPDATE quiz SET seance_id=NULL WHERE seance_id=?")) {
                ps1.setInt(1, seanceId);
                ps1.executeUpdate();
            }

            // lier le nouveau quiz
            try (PreparedStatement ps2 = connection.prepareStatement(
                    "UPDATE quiz SET seance_id=? WHERE id=?")) {
                ps2.setInt(1, seanceId);
                ps2.setInt(2, quizId);
                ps2.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    public Integer findQuizIdBySeance(int seanceId) throws SQLException {
        String sql = "SELECT id FROM quiz WHERE seance_id=? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, seanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return null;
    }

}