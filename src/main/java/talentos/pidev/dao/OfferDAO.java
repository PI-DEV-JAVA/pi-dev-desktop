

        package talentos.pidev.dao;

import talentos.pidev.models.Offer;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OfferDAO {

    // CREATE
    public int addOffer(Offer offer) throws SQLException {
        String sql = "INSERT INTO offers (title, description, department, contract_type, " +
                "experience_level, salary_min, salary_max, location, status, " +
                "publish_date, closing_date, positions_available, applications_received) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setOfferParameters(pstmt, offer);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return -1;
        }
    }

    // READ ALL
    public List<Offer> getAllOffers() throws SQLException {
        List<Offer> offers = new ArrayList<>();
        String sql = "SELECT * FROM offers ORDER BY publish_date DESC";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                offers.add(mapResultSetToOffer(rs));
            }
        }
        return offers;
    }

    // READ BY ID
    public Offer getOfferById(int id) throws SQLException {
        String sql = "SELECT * FROM offers WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOffer(rs);
                }
            }
        }
        return null;
    }

    // UPDATE
    public boolean updateOffer(Offer offer) throws SQLException {
        String sql = "UPDATE offers SET title = ?, description = ?, department = ?, " +
                "contract_type = ?, experience_level = ?, salary_min = ?, salary_max = ?, " +
                "location = ?, status = ?, publish_date = ?, closing_date = ?, " +
                "positions_available = ?, applications_received = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setOfferParameters(pstmt, offer);
            pstmt.setInt(14, offer.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean deleteOffer(int id) throws SQLException {
        String sql = "DELETE FROM offers WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // SEARCH
    public List<Offer> searchOffers(String keyword, String department, String status) throws SQLException {
        List<Offer> offers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM offers WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        if (department != null && !department.isEmpty()) {
            sql.append(" AND department = ?");
            params.add(department);
        }

        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY publish_date DESC");

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    offers.add(mapResultSetToOffer(rs));
                }
            }
        }
        return offers;
    }

    // STATISTICS
    public int getTotalOffers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM offers";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getOpenOffersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM offers WHERE status = 'Ouverte'";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // HELPER METHODS
    private void setOfferParameters(PreparedStatement pstmt, Offer offer) throws SQLException {
        pstmt.setString(1, offer.getTitle());
        pstmt.setString(2, offer.getDescription());
        pstmt.setString(3, offer.getDepartment());
        pstmt.setString(4, offer.getContractType());
        pstmt.setString(5, offer.getExperienceLevel());
        pstmt.setDouble(6, offer.getSalaryMin());
        pstmt.setDouble(7, offer.getSalaryMax());
        pstmt.setString(8, offer.getLocation());
        pstmt.setString(9, offer.getStatus());
        pstmt.setDate(10, Date.valueOf(offer.getPublishDate()));
        pstmt.setDate(11, Date.valueOf(offer.getClosingDate()));
        pstmt.setInt(12, offer.getPositionsAvailable());
        pstmt.setInt(13, offer.getApplicationsReceived());
    }

    private Offer mapResultSetToOffer(ResultSet rs) throws SQLException {
        Offer offer = new Offer();
        offer.setId(rs.getInt("id"));
        offer.setTitle(rs.getString("title"));
        offer.setDescription(rs.getString("description"));
        offer.setDepartment(rs.getString("department"));
        offer.setContractType(rs.getString("contract_type"));
        offer.setExperienceLevel(rs.getString("experience_level"));
        offer.setSalaryMin(rs.getDouble("salary_min"));
        offer.setSalaryMax(rs.getDouble("salary_max"));
        offer.setLocation(rs.getString("location"));
        offer.setStatus(rs.getString("status"));
        offer.setPublishDate(rs.getDate("publish_date").toLocalDate());
        offer.setClosingDate(rs.getDate("closing_date").toLocalDate());
        offer.setPositionsAvailable(rs.getInt("positions_available"));
        offer.setApplicationsReceived(rs.getInt("applications_received"));
        return offer;
    }
}
