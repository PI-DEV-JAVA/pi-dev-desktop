package talentos.pidev.dao;

import talentos.pidev.models.Inscription;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InscriptionDAO {

    private final Connection connection;

    public InscriptionDAO() {
        connection = DB.getInstance().getMyConnection();
    }

    // ADD
    public void addInscription(Inscription i) throws SQLException {
        String sql = "INSERT INTO inscription (formation_id, candidat_nom, candidat_email, statut, created_at) " +
                "VALUES (?, ?, ?, ?, NOW())";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, i.getFormationId());
        ps.setString(2, i.getCandidatNom());
        ps.setString(3, i.getCandidatEmail());
        ps.setString(4, i.getStatut());
        ps.executeUpdate();
    }

    // GET BY FORMATION
    public List<Inscription> getInscriptionsByFormation(int formationId) throws SQLException {
        List<Inscription> list = new ArrayList<>();
        String sql = "SELECT * FROM inscription WHERE formation_id = ? ORDER BY id DESC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, formationId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Inscription i = new Inscription();
            i.setId(rs.getInt("id"));
            i.setFormationId(rs.getInt("formation_id"));
            i.setCandidatNom(rs.getString("candidat_nom"));
            i.setCandidatEmail(rs.getString("candidat_email"));
            i.setStatut(rs.getString("statut"));
            i.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            list.add(i);
        }
        return list;
    }

    // UPDATE STATUT
    public void updateStatutInscription(int inscriptionId, String statut) throws SQLException {
        String sql = "UPDATE inscription SET statut = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, statut);
        ps.setInt(2, inscriptionId);
        ps.executeUpdate();
    }

    // DELETE
    public void deleteInscription(int id) throws SQLException {
        String sql = "DELETE FROM inscription WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
    public List<Inscription> getAllInscriptions() throws SQLException {
        List<Inscription> list = new ArrayList<>();
        String sql = "SELECT * FROM inscription ORDER BY id DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Inscription i = new Inscription();
            i.setId(rs.getInt("id"));
            i.setFormationId(rs.getInt("formation_id"));
            i.setCandidatNom(rs.getString("candidat_nom"));
            i.setCandidatEmail(rs.getString("candidat_email"));
            i.setStatut(rs.getString("statut"));
            i.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            list.add(i);
        }
        return list;
    }

}
