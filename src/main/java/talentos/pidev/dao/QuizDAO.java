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
        // created_at existe dans la base -> on le met avec NOW()
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
        // created_at on ne le touche pas
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

                // created_at peut être DATETIME/TIMESTAMP -> on lit en Timestamp
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) q.setDateCreation(ts.toLocalDateTime().toLocalDate());
                else q.setDateCreation(LocalDate.now());

                list.add(q);
            }
        }
        return list;
    }
}
