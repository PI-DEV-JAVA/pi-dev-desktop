package talentos.pidev.dao;

import talentos.pidev.models.Formation;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FormationDAO {

        private Connection connection;

        public FormationDAO() {
                connection = DB.getInstance().getMyConnection();
        }

        // ADD
        public void addFormation(Formation f) throws SQLException {
                String sql = "INSERT INTO formation (nom, description, date_debut, date_fin, contenu, difficulte, categorie, mode, lieu, formateur, prerequis, capacite_max, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(sql);

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

                ps.executeUpdate();
        }

        // GET ALL
        public List<Formation> getAllFormations() throws SQLException {
                List<Formation> list = new ArrayList<>();
                String sql = "SELECT * FROM formation";

                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql);

                while (rs.next()) {
                        Formation f = new Formation();
                        f.setId(rs.getInt("id"));
                        f.setNom(rs.getString("nom"));
                        f.setDescription(rs.getString("description"));
                        f.setDateDebut(rs.getDate("date_debut").toLocalDate());
                        f.setDateFin(rs.getDate("date_fin").toLocalDate());
                        f.setContenu(rs.getString("contenu"));
                        f.setDifficulte(rs.getString("difficulte"));
                        f.setCategorie(rs.getString("categorie"));
                        f.setMode(rs.getString("mode"));
                        f.setLieu(rs.getString("lieu"));
                        f.setFormateur(rs.getString("formateur"));
                        f.setPrerequis(rs.getString("prerequis"));
                        f.setCapaciteMax(rs.getInt("capacite_max"));
                        f.setStatut(rs.getString("statut"));

                        list.add(f);
                }

                return list;
        }

        // DELETE
        public void deleteFormation(int id) throws SQLException {
                String sql = "DELETE FROM formation WHERE id = ?";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, id);
                ps.executeUpdate();
        }

        // UPDATE
        public void updateFormation(Formation f) throws SQLException {
                String sql = "UPDATE formation SET nom=?, description=?, date_debut=?, date_fin=?, contenu=?, difficulte=?, categorie=?, mode=?, lieu=?, formateur=?, prerequis=?, capacite_max=?, statut=? WHERE id=?";
                PreparedStatement ps = connection.prepareStatement(sql);

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

        // SEARCH
        public List<Formation> searchByNom(String keyword) throws SQLException {
                List<Formation> list = new ArrayList<>();
                String sql = "SELECT * FROM formation WHERE nom LIKE ?";

                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, "%" + keyword + "%");

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                        Formation f = new Formation();
                        f.setId(rs.getInt("id"));
                        f.setNom(rs.getString("nom"));
                        f.setDescription(rs.getString("description"));
                        f.setDateDebut(rs.getDate("date_debut").toLocalDate());
                        f.setDateFin(rs.getDate("date_fin").toLocalDate());
                        f.setContenu(rs.getString("contenu"));
                        f.setDifficulte(rs.getString("difficulte"));
                        f.setCategorie(rs.getString("categorie"));
                        f.setMode(rs.getString("mode"));
                        f.setLieu(rs.getString("lieu"));
                        f.setFormateur(rs.getString("formateur"));
                        f.setPrerequis(rs.getString("prerequis"));
                        f.setCapaciteMax(rs.getInt("capacite_max"));
                        f.setStatut(rs.getString("statut"));

                        list.add(f);
                }

                return list;
        }
}
