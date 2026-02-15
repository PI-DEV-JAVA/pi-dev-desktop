package talentos.pidev.controllers;

import javafx.fxml.FXML;

public class SidebarController {

    @FXML
    private void handleDashboard() {
        System.out.println("Navigation vers Offres (par défaut)");
        // Si tu n'as pas de Dashboard, redirige vers OffersCardView
        MainLayoutController.getInstance()
                .navigate("OffersCardView.fxml", "Offres d'emploi");
    }

    @FXML
    private void handleOffers() {
        System.out.println("Navigation vers Offres");
        MainLayoutController.getInstance()
                .navigate("OffersCardView.fxml", "Offres d'emploi");
    }

    @FXML
    private void handleApplications() {
        System.out.println("Navigation vers Candidatures");
        MainLayoutController.getInstance()
                .navigate("ApplicationCardView.fxml", "Candidatures");
    }

    @FXML
    private void handleInterviews() {
        System.out.println("Navigation vers Interviews");
        MainLayoutController.getInstance()
                .navigate("placeholder.fxml", "Interviews");
    }

    @FXML
    private void handleActivities() {
        System.out.println("Navigation vers Activités");
        MainLayoutController.getInstance()
                .navigate("placeholder.fxml", "Activités");
    }

    @FXML
    private void handleProjects() {
        System.out.println("Navigation vers Projets");
        MainLayoutController.getInstance()
                .navigate("placeholder.fxml", "Projets");
    }

    @FXML
    private void handleReports() {
        System.out.println("Navigation vers Rapports");
        MainLayoutController.getInstance()
                .navigate("placeholder.fxml", "Rapports");
    }

    @FXML
    private void handleSettings() {
        System.out.println("Navigation vers Paramètres");
        MainLayoutController.getInstance()
                .navigate("placeholder.fxml", "Paramètres");
    }
    @FXML
    private void handleCandidateView() {
        System.out.println("Navigation vers espace candidat");
        MainLayoutController.getInstance()
                .navigate("CandidateApplicationCardView.fxml", "Postuler à une offre");
    }
}
