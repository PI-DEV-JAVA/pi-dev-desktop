package com.pi.dao;

import com.pi.models.Presence;
import com.pi.database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PresenceDAO {

    private Connection connection;

    public PresenceDAO() {
        this.connection = DatabaseConnection.connect();
    }

    // CREATE - Ajouter une présence
    public void ajouter(Presence presence) throws SQLException {
        String query = "INSERT INTO presence (id_participation, est_present, date_scan, code_qr) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, presence.getIdParticipation());
            pstmt.setBoolean(2, presence.isEstPresent());

            // Gestion du cas où dateScan est null
            if (presence.getDateScan() != null) {
                pstmt.setTimestamp(3, Timestamp.valueOf(presence.getDateScan()));
            } else {
                pstmt.setNull(3, Types.TIMESTAMP);
            }

            pstmt.setString(4, presence.getCodeQr());
            pstmt.executeUpdate();
        }
    }

    // READ BY PARTICIPATION - Récupérer présence par participation
    public Presence getByParticipation(int idParticipation) throws SQLException {
        String query = "SELECT * FROM presence WHERE id_participation = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idParticipation);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Presence p = new Presence();
                p.setIdPresence(rs.getInt("id_presence"));
                p.setIdParticipation(rs.getInt("id_participation"));
                p.setEstPresent(rs.getBoolean("est_present"));

                Timestamp ts = rs.getTimestamp("date_scan");
                if (ts != null) {
                    p.setDateScan(ts.toLocalDateTime());
                } else {
                    p.setDateScan(null);
                }

                p.setCodeQr(rs.getString("code_qr"));
                return p;
            }
        }
        return null;
    }

    // READ BY EVENT - Toutes les présences d'un événement
    public List<Presence> getByEvent(int idEvent) throws SQLException {
        List<Presence> presences = new ArrayList<>();
        String query = "SELECT p.* FROM presence p " +
                "JOIN participation part ON p.id_participation = part.id_participation " +
                "WHERE part.id_event = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Presence p = new Presence();
                p.setIdPresence(rs.getInt("id_presence"));
                p.setIdParticipation(rs.getInt("id_participation"));
                p.setEstPresent(rs.getBoolean("est_present"));

                Timestamp ts = rs.getTimestamp("date_scan");
                if (ts != null) {
                    p.setDateScan(ts.toLocalDateTime());
                }

                p.setCodeQr(rs.getString("code_qr"));
                presences.add(p);
            }
        }
        return presences;
    }

    // UPDATE - Marquer présence (avec date du scan)
    public void marquerPresent(int idParticipation) throws SQLException {
        String query = "UPDATE presence SET est_present = TRUE, date_scan = ? WHERE id_participation = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, idParticipation);
            pstmt.executeUpdate();
        }
    }

    // COUNT - Compter les présents pour un événement
    public int compterPresents(int idEvent) throws SQLException {
        String query = "SELECT COUNT(*) FROM presence p " +
                "JOIN participation part ON p.id_participation = part.id_participation " +
                "WHERE part.id_event = ? AND p.est_present = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // DELETE - Supprimer présence
    public void supprimer(int idPresence) throws SQLException {
        String query = "DELETE FROM presence WHERE id_presence = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idPresence);
            pstmt.executeUpdate();
        }
    }
}