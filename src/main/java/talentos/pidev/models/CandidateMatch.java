package talentos.pidev.models;

public class CandidateMatch {
    private Application application;
    private Offer offer;
    private AIScoreResult score;

    public CandidateMatch(Application application, Offer offer, AIScoreResult score) {
        this.application = application;
        this.offer = offer;
        this.score = score;
    }

    public Application getApplication() { return application; }
    public Offer getOffer() { return offer; }
    public AIScoreResult getScore() { return score; }

    public double getOverallScore() { return score.getOverallScore(); }
    public String getCandidateName() { return application.getCandidateName(); }
    public String getCandidateEmail() { return application.getCandidateEmail(); }
}