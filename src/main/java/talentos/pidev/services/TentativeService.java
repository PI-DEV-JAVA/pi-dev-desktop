package talentos.pidev.services;

import talentos.pidev.dao.TentativeDAO;
import talentos.pidev.models.TentativeQuiz;

import java.sql.SQLException;

public class TentativeService {
    private final TentativeDAO dao = new TentativeDAO();

    public void saveTentative(TentativeQuiz t) throws SQLException {
        dao.add(t);
    }
}
