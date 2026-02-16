package talentos.pidev.services;

import talentos.pidev.dao.InscriptionDAO;
import talentos.pidev.models.Inscription;

import java.sql.SQLException;
import java.util.List;

public class InscriptionService {

    private final InscriptionDAO dao = new InscriptionDAO();

    public void inscrire(Inscription ins) throws SQLException {
        dao.addInscription(ins);
    }

    public List<Inscription> getByFormation(int formationId) throws SQLException {
        return dao.getByFormation(formationId);
    }

    public void changerStatut(int inscriptionId, String statut) throws SQLException {
        dao.updateStatut(inscriptionId, statut);
    }

    public void supprimer(int inscriptionId) throws SQLException {
        dao.delete(inscriptionId);
    }
}
