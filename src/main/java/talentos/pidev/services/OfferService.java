
        package talentos.pidev.services;

import talentos.pidev.dao.OfferDAO;
import talentos.pidev.models.Offer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OfferService {
    private final OfferDAO offerDAO;

    public OfferService() {
        this.offerDAO = new OfferDAO();
    }

    public boolean createOffer(Offer offer) {
        try {
            // Validation
            if (offer.getTitle() == null || offer.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Le titre est obligatoire");
            }
            if (offer.getClosingDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La date de clôture doit être dans le futur");
            }
            if (offer.getSalaryMin() > offer.getSalaryMax()) {
                throw new IllegalArgumentException("Le salaire minimum ne peut pas être supérieur au maximum");
            }

            return offerDAO.addOffer(offer) > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'offre: " + e.getMessage());
            return false;
        }
    }

    public List<Offer> getAllOffers() {
        try {
            return offerDAO.getAllOffers();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des offres: " + e.getMessage());
            return List.of();
        }
    }

    public Offer getOfferById(int id) {
        try {
            return offerDAO.getOfferById(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'offre: " + e.getMessage());
            return null;
        }
    }

    public boolean updateOffer(Offer offer) {
        try {
            return offerDAO.updateOffer(offer);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'offre: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteOffer(int id) {
        try {
            return offerDAO.deleteOffer(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'offre: " + e.getMessage());
            return false;
        }
    }

    public List<Offer> searchOffers(String keyword, String department, String status) {
        try {
            return offerDAO.searchOffers(keyword, department, status);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
            return List.of();
        }
    }

    public int getTotalOffers() {
        try {
            return offerDAO.getTotalOffers();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des offres: " + e.getMessage());
            return 0;
        }
    }

    public int getOpenOffersCount() {
        try {
            return offerDAO.getOpenOffersCount();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des offres ouvertes: " + e.getMessage());
            return 0;
        }
    }
}
