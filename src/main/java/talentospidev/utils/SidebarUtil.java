package talentospidev.utils;

import javafx.scene.control.Button;
import talentospidev.models.User;
import talentospidev.services.AuthService;

/**
 * Utility to configure role-based sidebar tab visibility.
 * Call SidebarUtil.configure(toDoTab, activitiesTab, projectsTab) in
 * initialize().
 */
public class SidebarUtil {

    /**
     * Shows/hides sidebar tabs based on the current user's role.
     * Recruiter/Admin → Activities + Projects visible.
     * Candidate → To Do visible.
     */
    public static void configure(Button toDoTab, Button activitiesTab, Button projectsTab) {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;

        if (user.getRole() == User.Role.HR || user.getRole() == User.Role.ADMIN) {
            show(activitiesTab);
            show(projectsTab);
        } else {
            show(toDoTab);
        }
    }

    private static void show(Button btn) {
        if (btn != null) {
            btn.setVisible(true);
            btn.setManaged(true);
        }
    }
}
