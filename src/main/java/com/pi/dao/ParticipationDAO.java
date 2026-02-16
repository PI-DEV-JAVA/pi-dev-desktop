package com.pi.dao;

import com.pi.models.Participation;
import com.pi.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationDAO {

    private Connection connection;

    public ParticipationDAO() {
        this.connection = DatabaseConnection.connect();
    }

    public void ajouter(Participation participation) throws SQLException {
        String query = "INSERT INTO participation (id_event, id_user, statut) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, participation.getIdEvent());
            pstmt.setInt(2, participation.getIdUser());
            pstmt.setString(3, participation.getStatut());
            pstmt.executeUpdate();
            System.out.println(" Participation ajoutée");
        }
    }

    public List<Participation> afficherToutes() throws SQLException {
        List<Participation> participations = new ArrayList<>();
        String query = "SELECT * FROM participation ORDER BY id_participation";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Participation p = new Participation();
                p.setIdParticipation(rs.getInt("id_participation"));
                p.setIdEvent(rs.getInt("id_event"));
                p.setIdUser(rs.getInt("id_user"));
                p.setStatut(rs.getString("statut"));
                participations.add(p);
            }
        }
        return participations;
    }

    public List<Participation> getByEvent(int idEvent) throws SQLException {
        List<Participation> participations = new ArrayList<>();
        String query = "SELECT * FROM participation WHERE id_event = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Participation p = new Participation();
                p.setIdParticipation(rs.getInt("id_participation"));
                p.setIdEvent(rs.getInt("id_event"));
                p.setIdUser(rs.getInt("id_user"));
                p.setStatut(rs.getString("statut"));
                participations.add(p);
            }
        }
        return participations;
    }

    public List<Participation> getByUser(int idUser) throws SQLException {
        List<Participation> participations = new ArrayList<>();
        String query = "SELECT * FROM participation WHERE id_user = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Participation p = new Participation();
                p.setIdParticipation(rs.getInt("id_participation"));
                p.setIdEvent(rs.getInt("id_event"));
                p.setIdUser(rs.getInt("id_user"));
                p.setStatut(rs.getString("statut"));
                participations.add(p);
            }
        }
        return participations;
    }

    public void modifierStatut(int idParticipation, String nouveauStatut) throws SQLException {
        String query = "UPDATE participation SET statut = ? WHERE id_participation = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, idParticipation);
            pstmt.executeUpdate();
            System.out.println(" Statut modifié");
        }
    }

    public void supprimer(int idParticipation) throws SQLException {
        String query = "DELETE FROM participation WHERE id_participation = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idParticipation);
            pstmt.executeUpdate();
            System.out.println(" Participation supprimée");
        }
    }

    public void supprimerParEvent(int idEvent) throws SQLException {
        String query = "DELETE FROM participation WHERE id_event = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            pstmt.executeUpdate();
            System.out.println(" Participations de l'événement " + idEvent + " supprimées");
        }
    }

    public int compterParticipants(int idEvent) throws SQLException {
        String query = "SELECT COUNT(*) FROM participation WHERE id_event = ? AND statut = 'Inscrit'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean estInscrit(int idEvent, int idUser) throws SQLException {
        String query = "SELECT COUNT(*) FROM participation WHERE id_event = ? AND id_user = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            pstmt.setInt(2, idUser);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}