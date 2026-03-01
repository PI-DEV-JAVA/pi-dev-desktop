package talentos.pidev.services;

import talentos.pidev.dao.BookmarkDAO;
import talentos.pidev.models.Offer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookmarkService {

    private final BookmarkDAO bookmarkDAO;
    private final OfferService offerService;

    // ID du candidat connecté (à remplacer par système d'authentification)
    private static final int CURRENT_CANDIDATE_ID = 1;

    public BookmarkService() {
        this.bookmarkDAO = new BookmarkDAO();
        this.offerService = new OfferService();
    }

    public boolean toggleBookmark(int offerId) {
        try {
            if (bookmarkDAO.isBookmarked(CURRENT_CANDIDATE_ID, offerId)) {
                return bookmarkDAO.removeBookmark(CURRENT_CANDIDATE_ID, offerId);
            } else {
                return bookmarkDAO.addBookmark(CURRENT_CANDIDATE_ID, offerId);
            }
        } catch (SQLException e) {
            System.err.println("Erreur bookmark: " + e.getMessage());
            return false;
        }
    }

    public boolean isBookmarked(int offerId) {
        try {
            return bookmarkDAO.isBookmarked(CURRENT_CANDIDATE_ID, offerId);
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Offer> getBookmarkedOffers() {
        List<Offer> bookmarked = new ArrayList<>();
        try {
            List<Integer> offerIds = bookmarkDAO.getBookmarkedOfferIds(CURRENT_CANDIDATE_ID);
            for (int offerId : offerIds) {
                Offer offer = offerService.getOfferById(offerId);
                if (offer != null) {
                    bookmarked.add(offer);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération: " + e.getMessage());
        }
        return bookmarked;
    }

    public boolean addNote(int offerId, String note) {
        try {
            return bookmarkDAO.updateNotes(CURRENT_CANDIDATE_ID, offerId, note);
        } catch (SQLException e) {
            return false;
        }
    }
}