package talentos.pidev.services;

import talentos.pidev.dao.ChoixDAO;
import talentos.pidev.dao.QuestionDAO;
import talentos.pidev.dao.QuizDAO;
import talentos.pidev.models.Choix;
import talentos.pidev.models.Question;
import talentos.pidev.models.Quiz;

import java.sql.SQLException;
import java.util.List;

public class QuizService {

    private final QuizDAO quizDAO = new QuizDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ChoixDAO choixDAO = new ChoixDAO();

    // Quiz
    public void add(Quiz q) throws SQLException { quizDAO.add(q); }
    public void update(Quiz q) throws SQLException { quizDAO.update(q); }
    public void delete(int id) throws SQLException { quizDAO.delete(id); }
    public List<Quiz> getAll() throws SQLException { return quizDAO.getAll(); }

    // Questions
    public void addQuestion(Question q) throws SQLException { questionDAO.add(q); }
    public void updateQuestion(Question q) throws SQLException { questionDAO.update(q); }
    public void deleteQuestion(int id) throws SQLException { questionDAO.delete(id); }
    public void setQuestionPublished(int questionId, boolean published) throws SQLException {
        new QuestionDAO().setPublished(questionId, published);
    }

    public List<Question> getQuestionsByQuiz(int quizId) throws SQLException {
        return questionDAO.getByQuiz(quizId);
    }

    public List<Question> getPublishedQuestionsByQuiz(int quizId) throws SQLException {
        return questionDAO.getPublishedByQuiz(quizId);
    }

    // Choix
    public void addChoice(Choix c) throws SQLException { choixDAO.add(c); }
    public void updateChoice(Choix c) throws SQLException { choixDAO.update(c); }
    public void deleteChoice(int id) throws SQLException { choixDAO.delete(id); }
    public List<Choix> getChoicesByQuestion(int questionId) throws SQLException { return choixDAO.getByQuestion(questionId); }


    public void setChoiceCorrect(int choixId, int questionId) throws SQLException {
        choixDAO.setCorrectUnique(choixId, questionId);
    }
}
