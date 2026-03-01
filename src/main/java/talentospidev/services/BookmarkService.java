package talentospidev.services;

import talentospidev.dao.BookmarkDAO;
import talentospidev.models.Offer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Bookmark service — uses the authenticated user's ID (from caller).
 */
public class BookmarkService {

    private final BookmarkDAO bookmarkDAO;
    private final OfferService offerService;

    public BookmarkService() {
        this.bookmarkDAO = new BookmarkDAO();
        this.offerService = new OfferService();
    }

    /**
     * Toggle bookmark state: if bookmarked → remove, else → add.
     * 
     * @return true if now bookmarked, false if removed
     */
    public boolean toggleBookmark(int candidateId, int offerId) {
        try {
            if (bookmarkDAO.isBookmarked(candidateId, offerId)) {
                bookmarkDAO.removeBookmark(candidateId, offerId);
                return false; // removed
            } else {
                bookmarkDAO.addBookmark(candidateId, offerId);
                return true; // added
            }
        } catch (SQLException e) {
            System.err.println("Bookmark error: " + e.getMessage());
            return false;
        }
    }

    public boolean isBookmarked(int candidateId, int offerId) {
        try {
            return bookmarkDAO.isBookmarked(candidateId, offerId);
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Offer> getBookmarkedOffers(int candidateId) {
        List<Offer> bookmarked = new ArrayList<>();
        try {
            List<Integer> offerIds = bookmarkDAO.getBookmarkedOfferIds(candidateId);
            for (int offerId : offerIds) {
                Offer offer = offerService.getOfferById(offerId);
                if (offer != null) {
                    bookmarked.add(offer);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookmarks: " + e.getMessage());
        }
        return bookmarked;
    }

    public boolean addNote(int candidateId, int offerId, String note) {
        try {
            return bookmarkDAO.updateNotes(candidateId, offerId, note);
        } catch (SQLException e) {
            return false;
        }
    }
}
