package talentos.pidev.dao;

import talentos.pidev.models.TentativeQuiz;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDateTime;

public class TentativeDAO {

    private final Connection connection;

    public TentativeDAO() {
        connection = DB.getInstance().getMyConnection();
    }

    public void add(TentativeQuiz t) throws SQLException {
        String sql = "INSERT INTO tentative_quiz(quiz_id, candidat_nom, candidat_email, score, total) VALUES(?,?,?,?,?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, t.getQuizId());
            ps.setString(2, t.getCandidatNom() == null ? "" : t.getCandidatNom());
            ps.setString(3, t.getCandidatEmail() == null ? "" : t.getCandidatEmail());
            ps.setInt(4, t.getScore());
            ps.setInt(5, t.getTotal());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }
        }
    }
}
