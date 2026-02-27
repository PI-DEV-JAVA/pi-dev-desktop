package talentospidev.services;

import talentospidev.dao.ApplicationDAO;
import talentospidev.models.Application;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ApplicationService {
    private final ApplicationDAO applicationDAO;

    public ApplicationService() {
        this.applicationDAO = new ApplicationDAO();
    }

    public boolean createApplication(Application application) {
        try {
            // Prevent duplicate applications
            if (applicationDAO.hasUserApplied(application.getUserId(), application.getOfferId())) {
                System.err.println("L'utilisateur a déjà postulé à cette offre.");
                return false;
            }
            return applicationDAO.addApplication(application) > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la candidature: " + e.getMessage());
            return false;
        }
    }

    public List<Application> getAllApplications() {
        try {
            return applicationDAO.getAllApplications();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des candidatures: " + e.getMessage());
            return List.of();
        }
    }

    public List<Application> getApplicationsByOffer(int offerId) {
        try {
            return applicationDAO.getApplicationsByOffer(offerId);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
            return List.of();
        }
    }

    public Application getApplicationById(int id) {
        try {
            return applicationDAO.getApplicationById(id);
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
            return null;
        }
    }

    public List<Application> getApplicationsByUserId(int userId) {
        try {
            return applicationDAO.getApplicationsByUserId(userId);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des candidatures: " + e.getMessage());
            return List.of();
        }
    }

    public boolean hasUserApplied(int userId, int offerId) {
        try {
            return applicationDAO.hasUserApplied(userId, offerId);
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
            return false;
        }
    }

    public boolean respondToApplication(int appId, String response, String newStatus) {
        try {
            return applicationDAO.respondToApplication(appId, response, newStatus);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réponse: " + e.getMessage());
            return false;
        }
    }

    public boolean updateApplication(Application application) {
        try {
            return applicationDAO.updateApplication(application);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteApplication(int id) {
        try {
            return applicationDAO.deleteApplication(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    public List<Application> searchApplications(String keyword, String status,
            LocalDate fromDate, LocalDate toDate) {
        try {
            return applicationDAO.searchApplications(keyword, status, fromDate, toDate);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
            return List.of();
        }
    }

    public int getTotalApplications() {
        try {
            return applicationDAO.getTotalApplications();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getApplicationsCountByOffer(int offerId) {
        try {
            return applicationDAO.getApplicationsByOffer(offerId).size();
        } catch (Exception e) {
            return 0;
        }
    }
}
