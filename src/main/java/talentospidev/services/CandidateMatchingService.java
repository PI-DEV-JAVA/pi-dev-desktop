package talentospidev.services;

import talentospidev.models.Application;
import talentospidev.models.Offer;
import talentospidev.models.CandidateMatch;
import talentospidev.models.AIScoreResult;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Scores all applicants of an offer via AI and returns ranked matches.
 */
public class CandidateMatchingService {

    private final ApplicationService applicationService;
    private final AIScoringService aiScoringService;

    public CandidateMatchingService() {
        this.applicationService = new ApplicationService();
        this.aiScoringService = new AIScoringService();
    }

    /**
     * Score all applicants for a given offer and return sorted matches.
     */
    public List<CandidateMatch> findBestCandidatesForOffer(Offer offer, int limit) throws Exception {
        List<Application> applications = applicationService.getApplicationsByOffer(offer.getId());
        List<CandidateMatch> matches = new ArrayList<>();

        int count = Math.min(limit, applications.size());
        System.out.println("🚀 Analyzing " + count + " candidates...");

        // Store submitted jobs
        List<JobInfo> jobs = new ArrayList<>();

        // Step 1: Submit all jobs with rate-limit delay
        for (int i = 0; i < count; i++) {
            Application app = applications.get(i);
            if (app.getCvFilePath() == null || app.getCvFilePath().isEmpty())
                continue;

            File cvFile = new File(app.getCvFilePath());
            if (!cvFile.exists())
                continue;

            try {
                if (i > 0)
                    Thread.sleep(1000);
                String jobId = aiScoringService.submitScoringJob(cvFile, offer.getDescription(), "English");
                jobs.add(new JobInfo(jobId, app));
                System.out.println("✅ Job submitted for application #" + app.getId() + " (ID: " + jobId + ")");
            } catch (Exception e) {
                System.err.println("❌ Submit error for app #" + app.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("⏳ Waiting for results (" + jobs.size() + " jobs)...");

        // Step 2: Wait then retrieve results
        Thread.sleep(15000);

        for (JobInfo job : jobs) {
            try {
                JsonNode result = aiScoringService.getScoringResult(job.jobId);
                if (result != null) {
                    AIScoreResult score = parseResult(result);
                    matches.add(new CandidateMatch(job.application, offer, score));
                    System.out.println("✅ Result received for app #" + job.application.getId());
                } else {
                    System.out.println("⚠️ No result yet for app #" + job.application.getId());
                }
                Thread.sleep(500);
            } catch (Exception e) {
                System.err.println("❌ Retrieval error for app #" + job.application.getId() + ": " + e.getMessage());
            }
        }

        // Sort by overall score descending
        matches.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        System.out.println("🎯 Analysis complete — " + matches.size() + " candidates scored");
        return matches;
    }

    private AIScoreResult parseResult(JsonNode responseNode) {
        AIScoreResult score = new AIScoreResult();
        JsonNode resultNode = responseNode.path("data").path("attributes").path("result");
        JsonNode matchScores = resultNode.path("match_scores");

        score.setOverallScore(matchScores.path("overall_match").asDouble(0));
        score.setSkillsScore(matchScores.path("skills_match").asDouble(0));
        score.setExperienceScore(matchScores.path("experience_match").asDouble(0));
        score.setEducationScore(matchScores.path("education_match").asDouble(0));
        return score;
    }

    private static class JobInfo {
        String jobId;
        Application application;

        JobInfo(String jobId, Application application) {
            this.jobId = jobId;
            this.application = application;
        }
    }
}
