package talentos.pidev.dao;

import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDateTime;

public class TentativeSeanceDAO {

    private final Connection cnx = DB.getInstance().getMyConnection();

    public static class TentativeSeanceInfo {
        public int id;
        public int seanceId;
        public int quizId;
        public String email;
        public int score;
        public int total;
        public String statut;
        public LocalDateTime startedAt;
        public LocalDateTime endedAt;
    }

    public TentativeSeanceInfo findByEmailAndSeance(String email, int seanceId) throws SQLException {
        String sql = "SELECT * FROM tentative_seance WHERE candidat_email=? AND seance_id=? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, seanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** crée ou met à jour le score (statut=SOUMISE) */
    public void upsertScore(String email, int seanceId, int quizId, int score, int total) throws SQLException {
        TentativeSeanceInfo exist = findByEmailAndSeance(email, seanceId);
        if (exist == null) {
            String ins = "INSERT INTO tentative_seance (seance_id, quiz_id, candidat_email, score, total, statut, started_at, ended_at) " +
                    "VALUES (?,?,?,?,?,'SOUMISE',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            try (PreparedStatement ps = cnx.prepareStatement(ins)) {
                ps.setInt(1, seanceId);
                ps.setInt(2, quizId);
                ps.setString(3, email);
                ps.setInt(4, score);
                ps.setInt(5, total);
                ps.executeUpdate();
            }
        } else {
            String upd = "UPDATE tentative_seance SET quiz_id=?, score=?, total=?, statut='SOUMISE', ended_at=CURRENT_TIMESTAMP WHERE id=?";
            try (PreparedStatement ps = cnx.prepareStatement(upd)) {
                ps.setInt(1, quizId);
                ps.setInt(2, score);
                ps.setInt(3, total);
                ps.setInt(4, exist.id);
                ps.executeUpdate();
            }
        }
    }

    private TentativeSeanceInfo map(ResultSet rs) throws SQLException {
        TentativeSeanceInfo t = new TentativeSeanceInfo();
        t.id = rs.getInt("id");
        t.seanceId = rs.getInt("seance_id");
        t.quizId = rs.getInt("quiz_id");
        t.email = rs.getString("candidat_email");
        t.score = rs.getInt("score");
        t.total = rs.getInt("total");
        t.statut = rs.getString("statut");

        Timestamp st = rs.getTimestamp("started_at");
        if (st != null) t.startedAt = st.toLocalDateTime();

        Timestamp en = rs.getTimestamp("ended_at");
        if (en != null) t.endedAt = en.toLocalDateTime();

        return t;
    }
}