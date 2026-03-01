package talentos.pidev.dao;

import talentos.pidev.models.Bookmark;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookmarkDAO {

    // Ajouter un bookmark
    public boolean addBookmark(int candidateId, int offerId) throws SQLException {
        String sql = "INSERT INTO bookmarks (candidate_id, offer_id, saved_at) VALUES (?, ?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, candidateId);
            pstmt.setInt(2, offerId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            return pstmt.executeUpdate() > 0;
        }
    }

    // Supprimer un bookmark
    public boolean removeBookmark(int candidateId, int offerId) throws SQLException {
        String sql = "DELETE FROM bookmarks WHERE candidate_id = ? AND offer_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, candidateId);
            pstmt.setInt(2, offerId);

            return pstmt.executeUpdate() > 0;
        }
    }

    // Vérifier si une offre est bookmarkée
    public boolean isBookmarked(int candidateId, int offerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookmarks WHERE candidate_id = ? AND offer_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, candidateId);
            pstmt.setInt(2, offerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Récupérer tous les bookmarks d'un candidat
    public List<Integer> getBookmarkedOfferIds(int candidateId) throws SQLException {
        List<Integer> offerIds = new ArrayList<>();
        String sql = "SELECT offer_id FROM bookmarks WHERE candidate_id = ? ORDER BY saved_at DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, candidateId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    offerIds.add(rs.getInt("offer_id"));
                }
            }
        }
        return offerIds;
    }

    // Ajouter des notes à un bookmark
    public boolean updateNotes(int candidateId, int offerId, String notes) throws SQLException {
        String sql = "UPDATE bookmarks SET notes = ? WHERE candidate_id = ? AND offer_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notes);
            pstmt.setInt(2, candidateId);
            pstmt.setInt(3, offerId);

            return pstmt.executeUpdate() > 0;
        }
    }
}