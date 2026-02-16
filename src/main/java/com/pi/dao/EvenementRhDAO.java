package com.pi.dao;

import com.pi.models.EvenementRh;
import com.pi.database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EvenementRhDAO {

    private Connection connection;

    public EvenementRhDAO() {
        this.connection = DatabaseConnection.connect();
    }

    public void ajouter(EvenementRh evenement) throws SQLException {
        String query = "INSERT INTO evenement_rh (titre, type_event, date_event, lieu, statut) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, evenement.getTitre());
            pstmt.setString(2, evenement.getTypeEvent());
            pstmt.setDate(3, Date.valueOf(evenement.getDateEvent()));
            pstmt.setString(4, evenement.getLieu());
            pstmt.setString(5, evenement.getStatut());
            pstmt.executeUpdate();
            System.out.println("✅ Événement ajouté");
        }
    }

    public List<EvenementRh> afficherTous() throws SQLException {
        List<EvenementRh> evenements = new ArrayList<>();
        String query = "SELECT * FROM evenement_rh ORDER BY date_event DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                EvenementRh e = new EvenementRh();
                e.setIdEvent(rs.getInt("id_event"));
                e.setTitre(rs.getString("titre"));
                e.setTypeEvent(rs.getString("type_event"));
                e.setDateEvent(rs.getDate("date_event").toLocalDate());
                e.setLieu(rs.getString("lieu"));
                e.setStatut(rs.getString("statut"));
                evenements.add(e);
            }
        }
        return evenements;
    }

    public EvenementRh getById(int id) throws SQLException {
        String query = "SELECT * FROM evenement_rh WHERE id_event = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                EvenementRh e = new EvenementRh();
                e.setIdEvent(rs.getInt("id_event"));
                e.setTitre(rs.getString("titre"));
                e.setTypeEvent(rs.getString("type_event"));
                e.setDateEvent(rs.getDate("date_event").toLocalDate());
                e.setLieu(rs.getString("lieu"));
                e.setStatut(rs.getString("statut"));
                return e;
            }
        }
        return null;
    }

    public void modifier(EvenementRh evenement) throws SQLException {
        String query = "UPDATE evenement_rh SET titre=?, type_event=?, date_event=?, lieu=?, statut=? WHERE id_event=?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, evenement.getTitre());
            pstmt.setString(2, evenement.getTypeEvent());
            pstmt.setDate(3, Date.valueOf(evenement.getDateEvent()));
            pstmt.setString(4, evenement.getLieu());
            pstmt.setString(5, evenement.getStatut());
            pstmt.setInt(6, evenement.getIdEvent());
            pstmt.executeUpdate();
            System.out.println("✅ Événement modifié");
        }
    }

    public void supprimer(int id) throws SQLException {
        String deleteParticipations = "DELETE FROM participation WHERE id_event = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteParticipations)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
        String query = "DELETE FROM evenement_rh WHERE id_event = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Événement supprimé");
        }
    }

    public List<EvenementRh> rechercherParType(String type) throws SQLException {
        List<EvenementRh> evenements = new ArrayList<>();
        String query = "SELECT * FROM evenement_rh WHERE type_event LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + type + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EvenementRh e = new EvenementRh();
                e.setIdEvent(rs.getInt("id_event"));
                e.setTitre(rs.getString("titre"));
                e.setTypeEvent(rs.getString("type_event"));
                e.setDateEvent(rs.getDate("date_event").toLocalDate());
                e.setLieu(rs.getString("lieu"));
                e.setStatut(rs.getString("statut"));
                evenements.add(e);
            }
        }
        return evenements;
    }

    public List<EvenementRh> rechercherParStatut(String statut) throws SQLException {
        List<EvenementRh> evenements = new ArrayList<>();
        String query = "SELECT * FROM evenement_rh WHERE statut = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EvenementRh e = new EvenementRh();
                e.setIdEvent(rs.getInt("id_event"));
                e.setTitre(rs.getString("titre"));
                e.setTypeEvent(rs.getString("type_event"));
                e.setDateEvent(rs.getDate("date_event").toLocalDate());
                e.setLieu(rs.getString("lieu"));
                e.setStatut(rs.getString("statut"));
                evenements.add(e);
            }
        }
        return evenements;
    }

    public List<EvenementRh> getEvenementsAVenir() throws SQLException {
        List<EvenementRh> evenements = new ArrayList<>();
        String query = "SELECT * FROM evenement_rh WHERE date_event >= CURDATE() AND statut = 'Actif' ORDER BY date_event";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                EvenementRh e = new EvenementRh();
                e.setIdEvent(rs.getInt("id_event"));
                e.setTitre(rs.getString("titre"));
                e.setTypeEvent(rs.getString("type_event"));
                e.setDateEvent(rs.getDate("date_event").toLocalDate());
                e.setLieu(rs.getString("lieu"));
                e.setStatut(rs.getString("statut"));
                evenements.add(e);
            }
        }
        return evenements;
    }
}