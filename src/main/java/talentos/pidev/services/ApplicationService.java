
        package talentos.pidev.services;

import talentos.pidev.dao.ApplicationDAO;
import talentos.pidev.models.Application;

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
            // Validation
            if (application.getCandidateName() == null || application.getCandidateName().trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom du candidat est obligatoire");
            }
            if (application.getCandidateEmail() == null || application.getCandidateEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("L'email du candidat est obligatoire");
            }
            if (!application.getCandidateEmail().contains("@")) {
                throw new IllegalArgumentException("Email invalide");
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
            System.err.println("Erreur lors de la récupération des candidatures: " + e.getMessage());
            return List.of();
        }
    }

    public Application getApplicationById(int id) {
        try {
            return applicationDAO.getApplicationById(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la candidature: " + e.getMessage());
            return null;
        }
    }

    public boolean updateApplication(Application application) {
        try {
            return applicationDAO.updateApplication(application);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la candidature: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteApplication(int id) {
        try {
            return applicationDAO.deleteApplication(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la candidature: " + e.getMessage());
            return false;
        }
    }

    public List<Application> searchApplications(String candidateName, String status,
                                                LocalDate fromDate, LocalDate toDate) {
        try {
            return applicationDAO.searchApplications(candidateName, status, fromDate, toDate);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des candidatures: " + e.getMessage());
            return List.of();
        }
    }

    public int getTotalApplications() {
        try {
            return applicationDAO.getTotalApplications();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des candidatures: " + e.getMessage());
            return 0;
        }
    }

    public int getApplicationsByStatus(String status) {
        try {
            return applicationDAO.getApplicationsByStatus(status);
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage par statut: " + e.getMessage());
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
