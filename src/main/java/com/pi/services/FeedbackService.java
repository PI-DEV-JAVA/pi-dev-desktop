package com.pi.services;

import com.pi.dao.FeedbackDAO;
import com.pi.dao.ParticipationDAO;
import com.pi.dao.EvenementRhDAO;
import com.pi.models.Feedback;
import com.pi.models.EvenementRh;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeedbackService {

    private FeedbackDAO feedbackDAO;
    private ParticipationDAO participationDAO;
    private EvenementRhDAO evenementDAO;

    public FeedbackService() {
        this.feedbackDAO = new FeedbackDAO();
        this.participationDAO = new ParticipationDAO();
        this.evenementDAO = new EvenementRhDAO();
    }

    // Ajouter un feedback
    public void ajouterFeedback(int idParticipation, int note, String commentaire, boolean recommanderait)
            throws SQLException {
        // Vérifier que la participation existe
        if (participationDAO.getById(idParticipation) == null) {
            throw new SQLException("Participation inexistante");
        }

        // Vérifier que la participation n'a pas déjà un feedback
        if (feedbackDAO.getByParticipation(idParticipation) != null) {
            throw new SQLException("Un feedback existe déjà pour cette participation");
        }

        Feedback feedback = new Feedback(idParticipation, note, commentaire, recommanderait);
        feedbackDAO.ajouter(feedback);
    }

    // Récupérer les feedbacks d'un événement
    public List<Feedback> getFeedbacksByEvent(int idEvent) throws SQLException {
        return feedbackDAO.getByEvent(idEvent);
    }

    // Récupérer les feedbacks avec détails de l'événement
    public List<FeedbackAvecDetails> getFeedbacksAvecDetails(int idEvent) throws SQLException {
        List<Feedback> feedbacks = feedbackDAO.getByEvent(idEvent);
        List<FeedbackAvecDetails> resultats = new ArrayList<>();

        for (Feedback f : feedbacks) {
            FeedbackAvecDetails d = new FeedbackAvecDetails();
            d.setFeedback(f);

            int idParticipation = f.getIdParticipation();
            int idUser = participationDAO.getById(idParticipation).getIdUser();
            d.setIdUser(idUser);

            resultats.add(d);
        }
        return resultats;
    }

    // Note moyenne d'un événement
    public double getNoteMoyenne(int idEvent) throws SQLException {
        return feedbackDAO.getNoteMoyenne(idEvent);
    }

    // Distribution des notes
    public int[] getDistributionNotes(int idEvent) throws SQLException {
        return feedbackDAO.getDistributionNotes(idEvent);
    }

    // Top événements
    public List<EvenementRh> getTopEvenements(int limite) throws SQLException {
        List<Integer> topIds = feedbackDAO.getTopEvenements(limite);
        List<EvenementRh> topEvenements = new ArrayList<>();
        for (int id : topIds) {
            topEvenements.add(evenementDAO.getById(id));
        }
        return topEvenements;
    }

    // Classe interne pour détails
    public static class FeedbackAvecDetails {
        private Feedback feedback;
        private int idUser;

        public Feedback getFeedback() { return feedback; }
        public void setFeedback(Feedback feedback) { this.feedback = feedback; }
        public int getIdUser() { return idUser; }
        public void setIdUser(int idUser) { this.idUser = idUser; }
    }
}