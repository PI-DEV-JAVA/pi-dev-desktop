package talentos.pidev.models;

public class AIScoreResult {
    private double overallScore;
    private double skillsScore;
    private double experienceScore;
    private double educationScore;
    private String explanation;
    private String recommendations;

    // Getters et setters
    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public double getSkillsScore() { return skillsScore; }
    public void setSkillsScore(double skillsScore) { this.skillsScore = skillsScore; }

    public double getExperienceScore() { return experienceScore; }
    public void setExperienceScore(double experienceScore) { this.experienceScore = experienceScore; }

    public double getEducationScore() { return educationScore; }
    public void setEducationScore(double educationScore) { this.educationScore = educationScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
}