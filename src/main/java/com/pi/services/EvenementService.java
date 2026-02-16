package com.pi.services;

import com.pi.dao.EvenementRhDAO;
import com.pi.models.EvenementRh;
import java.sql.SQLException;
import java.util.List;

public class EvenementService {

    private EvenementRhDAO evenementDAO;

    public EvenementService() {
        this.evenementDAO = new EvenementRhDAO();
    }

    public void ajouterEvenement(EvenementRh event) throws SQLException {
        evenementDAO.ajouter(event);
    }

    public void modifierEvenement(EvenementRh event) throws SQLException {
        evenementDAO.modifier(event);
    }

    public void supprimerEvenement(int id) throws SQLException {
        evenementDAO.supprimer(id);
    }

    public EvenementRh getEvenementById(int id) throws SQLException {
        return evenementDAO.getById(id);
    }

    public List<EvenementRh> getAllEvenements() throws SQLException {
        return evenementDAO.afficherTous();
    }

    public List<EvenementRh> rechercherParType(String type) throws SQLException {
        return evenementDAO.rechercherParType(type);
    }

    public List<EvenementRh> rechercherParStatut(String statut) throws SQLException {
        return evenementDAO.rechercherParStatut(statut);
    }

    public List<EvenementRh> getEvenementsAVenir() throws SQLException {
        return evenementDAO.getEvenementsAVenir();
    }
}