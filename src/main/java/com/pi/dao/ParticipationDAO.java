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
        if (this.connection == null) {
            System.out.println("⚠️ Attention: Connexion à la base non établie!");
        }
    }

    // CREATE - Ajouter une participation
    public void ajouter(Participation participation) throws SQLException {
        String query = "INSERT INTO participation (id_event, id_user, statut) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, participation.getIdEvent());
            pstmt.setInt(2, participation.getIdUser());
            pstmt.setString(3, participation.getStatut());

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Participation ajoutée! (ID événement: " + participation.getIdEvent() +
                    ", ID utilisateur: " + participation.getIdUser() + ")");
        }
    }

    // READ ALL - Toutes les participations
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

    // READ BY ID - Participation par ID
    public Participation getById(int id) throws SQLException {
        String query = "SELECT * FROM participation WHERE id_participation = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Participation p = new Participation();
                p.setIdParticipation(rs.getInt("id_participation"));
                p.setIdEvent(rs.getInt("id_event"));
                p.setIdUser(rs.getInt("id_user"));
                p.setStatut(rs.getString("statut"));
                return p;
            }
        }
        return null;
    }

    // READ BY EVENT - Participations d'un événement spécifique
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

    // READ BY USER - Participations d'un utilisateur spécifique
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

    // UPDATE - Modifier le statut d'une participation
    public void modifierStatut(int idParticipation, String nouveauStatut) throws SQLException {
        String query = "UPDATE participation SET statut = ? WHERE id_participation = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, idParticipation);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Statut modifié en '" + nouveauStatut + "' pour la participation ID " + idParticipation);
            } else {
                System.out.println("⚠️ Aucune participation trouvée avec l'ID: " + idParticipation);
            }
        }
    }

    // DELETE - Supprimer une participation
    public void supprimer(int idParticipation) throws SQLException {
        String query = "DELETE FROM participation WHERE id_participation = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idParticipation);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Participation ID " + idParticipation + " supprimée!");
            } else {
                System.out.println("⚠️ Aucune participation trouvée avec l'ID: " + idParticipation);
            }
        }
    }

    // DELETE BY EVENT - Supprimer toutes les participations d'un événement
    public void supprimerParEvent(int idEvent) throws SQLException {
        String query = "DELETE FROM participation WHERE id_event = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idEvent);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ " + rowsAffected + " participation(s) supprimée(s) pour l'événement ID " + idEvent);
        }
    }

    // COUNT - Compter les participants d'un événement
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

    // CHECK - Vérifier si un utilisateur est déjà inscrit
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