package com.pi.dao;

import com.pi.models.Feedback;
import com.pi.database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {

    private Connection connection;

    public FeedbackDAO() {
        this.connection = DatabaseConnection.connect();
    }

    // CREATE - Ajouter un feedback
    public void ajouter(Feedback feedback) throws SQLException {
        String query = "INSERT INTO feedback (id_participation, note, commentaire, date_feedback, recommanderait) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, feedback.getIdParticipation());
            pstmt.setInt(2, feedback.getNote());
            pstmt.setString(3, feedback.getCommentaire());
            pstmt.setTimestamp(4, Timestamp.valueOf(feedback.getDateFeedback()));
            pstmt.setBoolean(5, feedback.isRecommanderait());
            pstmt.executeUpdate();
        }
    }

    // READ BY PARTICIPATION - Récupérer feedback par participation
    public Feedback getByParticipation(int idParticipation) throws SQLException {
        String query = "SELECT * FROM feedback WHERE id_participation = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idParticipation);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToFeedback(rs);
            }
        }
        return null;
    }

    // READ BY EVENT - Tous les feedbacks d'un événement
    public List<Feedback> getByEvent(int idEvent) throws SQLException {
        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT f.* FROM feedback f " +
                "JOIN participation p ON f.id_participation = p.id_participation " +
                "WHERE p.id_event = ? ORDER BY f.date_feedback DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                feedbacks.add(mapResultSetToFeedback(rs));
            }
        }
        return feedbacks;
    }

    // READ ALL - Tous les feedbacks
    public List<Feedback> getAll() throws SQLException {
        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT * FROM feedback ORDER BY date_feedback DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                feedbacks.add(mapResultSetToFeedback(rs));
            }
        }
        return feedbacks;
    }

    // STATS - Note moyenne d'un événement
    public double getNoteMoyenne(int idEvent) throws SQLException {
        String query = "SELECT AVG(f.note) FROM feedback f " +
                "JOIN participation p ON f.id_participation = p.id_participation " +
                "WHERE p.id_event = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // STATS - Nombre de feedbacks par note
    public int[] getDistributionNotes(int idEvent) throws SQLException {
        int[] distribution = new int[5];
        String query = "SELECT note, COUNT(*) FROM feedback f " +
                "JOIN participation p ON f.id_participation = p.id_participation " +
                "WHERE p.id_event = ? GROUP BY note";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int note = rs.getInt(1);
                int count = rs.getInt(2);
                if (note >= 1 && note <= 5) {
                    distribution[note - 1] = count;
                }
            }
        }
        return distribution;
    }

    // TOP - Meilleurs événements par note
    public List<Integer> getTopEvenements(int limite) throws SQLException {
        List<Integer> topIds = new ArrayList<>();
        String query = "SELECT p.id_event, AVG(f.note) as moyenne " +
                "FROM feedback f JOIN participation p ON f.id_participation = p.id_participation " +
                "GROUP BY p.id_event ORDER BY moyenne DESC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, limite);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                topIds.add(rs.getInt("id_event"));
            }
        }
        return topIds;
    }

    // UPDATE - Modifier feedback
    public void modifier(Feedback feedback) throws SQLException {
        String query = "UPDATE feedback SET note = ?, commentaire = ?, recommanderait = ? WHERE id_feedback = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, feedback.getNote());
            pstmt.setString(2, feedback.getCommentaire());
            pstmt.setBoolean(3, feedback.isRecommanderait());
            pstmt.setInt(4, feedback.getIdFeedback());
            pstmt.executeUpdate();
        }
    }

    // DELETE - Supprimer feedback
    public void supprimer(int idFeedback) throws SQLException {
        String query = "DELETE FROM feedback WHERE id_feedback = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idFeedback);
            pstmt.executeUpdate();
        }
    }

    // Helper - Mapper ResultSet vers Feedback
    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setIdFeedback(rs.getInt("id_feedback"));
        f.setIdParticipation(rs.getInt("id_participation"));
        f.setNote(rs.getInt("note"));
        f.setCommentaire(rs.getString("commentaire"));

        Timestamp ts = rs.getTimestamp("date_feedback");
        if (ts != null) {
            f.setDateFeedback(ts.toLocalDateTime());
        }

        f.setRecommanderait(rs.getBoolean("recommanderait"));
        return f;
    }
}