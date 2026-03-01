package talentospidev.services;

import talentospidev.dao.SkillDao;
import talentospidev.dao.ProfileDao;
import talentospidev.models.Profile;

import java.util.List;

/**
 * Computes compatibility scores between two users
 * based on shared skills, professional field, and location.
 */
public class SyncService {

    private final SkillDao skillDao = new SkillDao();
    private final ProfileDao profileDao = new ProfileDao();

    /**
     * Calculate compatibility score (0–100) between two users.
     *
     * Weights:
     * - Skills overlap: 60% of score
     * - Same professional field/title: 25%
     * - Same location: 15%
     */
    public int calculateCompatibility(int userId1, int userId2) {
        double score = 0;

        // ── Skills (60%) ──
        List<String> skills1 = skillDao.getSkills(userId1);
        List<String> skills2 = skillDao.getSkills(userId2);
        if (!skills1.isEmpty() && !skills2.isEmpty()) {
            List<String> shared = skillDao.getSharedSkills(userId1, userId2);
            int total = Math.max(skills1.size(), skills2.size());
            score += 60.0 * shared.size() / total;
        } else if (skills1.isEmpty() && skills2.isEmpty()) {
            // Both have no skills → neutral (give partial credit)
            score += 20;
        }

        // ── Professional field (25%) ──
        Profile p1 = profileDao.findByUserId(userId1);
        Profile p2 = profileDao.findByUserId(userId2);
        if (p1 != null && p2 != null) {
            String title1 = p1.getProfessionalTitle() != null ? p1.getProfessionalTitle().toLowerCase() : "";
            String title2 = p2.getProfessionalTitle() != null ? p2.getProfessionalTitle().toLowerCase() : "";
            if (!title1.isEmpty() && !title2.isEmpty()) {
                // Check word overlap in titles
                String[] words1 = title1.split("\\s+");
                String[] words2 = title2.split("\\s+");
                int matching = 0;
                for (String w1 : words1) {
                    for (String w2 : words2) {
                        if (w1.length() > 2 && w1.equalsIgnoreCase(w2)) {
                            matching++;
                            break;
                        }
                    }
                }
                if (words1.length > 0) {
                    score += 25.0 * Math.min(1.0, (double) matching / words1.length);
                }
            }

            // ── Location (15%) ──
            String loc1 = p1.getLocation() != null ? p1.getLocation().toLowerCase() : "";
            String loc2 = p2.getLocation() != null ? p2.getLocation().toLowerCase() : "";
            if (!loc1.isEmpty() && !loc2.isEmpty() && loc1.equalsIgnoreCase(loc2)) {
                score += 15;
            }
        }

        return (int) Math.round(Math.min(100, score));
    }

    /** Get shared skills between two users (for display). */
    public List<String> getSharedSkills(int userId1, int userId2) {
        return skillDao.getSharedSkills(userId1, userId2);
    }
}
