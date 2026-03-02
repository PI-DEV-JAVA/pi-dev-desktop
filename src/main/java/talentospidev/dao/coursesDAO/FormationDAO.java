package talentospidev.dao.coursesDAO;

import talentospidev.models.courses.Formation;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationDAO {
    private final Connection conn = DB.getConnection();

    public void add(Formation f) throws SQLException {
        String sql = "INSERT INTO formation (nom,description,date_debut,date_fin,contenu,difficulte,categorie,mode,lieu,formateur,prerequis,capacite_max,statut,recruiter_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getDescription());
            ps.setDate(3, Date.valueOf(f.getDateDebut()));
            ps.setDate(4, Date.valueOf(f.getDateFin()));
            ps.setString(5, f.getContenu());
            ps.setString(6, f.getDifficulte());
            ps.setString(7, f.getCategorie());
            ps.setString(8, f.getMode());
            ps.setString(9, f.getLieu());
            ps.setString(10, f.getFormateur());
            ps.setString(11, f.getPrerequis());
            ps.setInt(12, f.getCapaciteMax());
            ps.setString(13, f.getStatut());
            ps.setInt(14, f.getRecruiterId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) f.setId(rs.getInt(1));
            }
        }
    }

    public void update(Formation f) throws SQLException {
        String sql = "UPDATE formation SET nom=?,description=?,date_debut=?,date_fin=?,contenu=?,difficulte=?,categorie=?,mode=?,lieu=?,formateur=?,prerequis=?,capacite_max=?,statut=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getDescription());
            ps.setDate(3, Date.valueOf(f.getDateDebut()));
            ps.setDate(4, Date.valueOf(f.getDateFin()));
            ps.setString(5, f.getContenu());
            ps.setString(6, f.getDifficulte());
            ps.setString(7, f.getCategorie());
            ps.setString(8, f.getMode());
            ps.setString(9, f.getLieu());
            ps.setString(10, f.getFormateur());
            ps.setString(11, f.getPrerequis());
            ps.setInt(12, f.getCapaciteMax());
            ps.setString(13, f.getStatut());
            ps.setInt(14, f.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM formation WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Formation getById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM formation WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Formation> getAll() throws SQLException {
        List<Formation> list = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM formation ORDER BY created_at DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Formation> getByRecruiterId(int recruiterId) throws SQLException {
        List<Formation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM formation WHERE recruiter_id=? ORDER BY created_at DESC")) {
            ps.setInt(1, recruiterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Formation> getOpen() throws SQLException {
        List<Formation> list = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM formation WHERE statut='OUVERTE' ORDER BY date_debut ASC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Formation map(ResultSet rs) throws SQLException {
        Formation f = new Formation();
        f.setId(rs.getInt("id"));
        f.setNom(rs.getString("nom"));
        f.setDescription(rs.getString("description"));
        Date dd = rs.getDate("date_debut"); if (dd != null) f.setDateDebut(dd.toLocalDate());
        Date df = rs.getDate("date_fin"); if (df != null) f.setDateFin(df.toLocalDate());
        f.setContenu(rs.getString("contenu"));
        f.setDifficulte(rs.getString("difficulte"));
        f.setCategorie(rs.getString("categorie"));
        f.setMode(rs.getString("mode"));
        f.setLieu(rs.getString("lieu"));
        f.setFormateur(rs.getString("formateur"));
        f.setPrerequis(rs.getString("prerequis"));
        f.setCapaciteMax(rs.getInt("capacite_max"));
        f.setStatut(rs.getString("statut"));
        f.setRecruiterId(rs.getInt("recruiter_id"));
        return f;
    }
}
