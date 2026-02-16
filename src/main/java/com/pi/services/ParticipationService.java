package com.pi.services;

import com.pi.dao.ParticipationDAO;
import com.pi.models.Participation;
import java.sql.SQLException;
import java.util.List;

public class ParticipationService {

    private ParticipationDAO participationDAO;

    public ParticipationService() {
        this.participationDAO = new ParticipationDAO();
    }

    public void ajouterParticipation(Participation participation) throws SQLException {
        participationDAO.ajouter(participation);
    }

    public void modifierStatut(int idParticipation, String statut) throws SQLException {
        participationDAO.modifierStatut(idParticipation, statut);
    }

    public void supprimerParticipation(int id) throws SQLException {
        participationDAO.supprimer(id);
    }

    public Participation getParticipationById(int id) throws SQLException {
        return participationDAO.getById(id);
    }

    public List<Participation> getAllParticipations() throws SQLException {
        return participationDAO.afficherToutes();
    }

    public List<Participation> getParticipationsByEvent(int idEvent) throws SQLException {
        return participationDAO.getByEvent(idEvent);
    }

    public List<Participation> getParticipationsByUser(int idUser) throws SQLException {
        return participationDAO.getByUser(idUser);
    }

    public int compterParticipants(int idEvent) throws SQLException {
        return participationDAO.compterParticipants(idEvent);
    }

    public boolean estInscrit(int idEvent, int idUser) throws SQLException {
        return participationDAO.estInscrit(idEvent, idUser);
    }

    public void supprimerParticipationsParEvent(int idEvent) throws SQLException {
        participationDAO.supprimerParEvent(idEvent);
    }
}