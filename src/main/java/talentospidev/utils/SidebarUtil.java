package talentospidev.utils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import talentospidev.models.User;
import talentospidev.services.AuthService;

/**
 * Utility to configure role-based sidebar tab visibility
 * and replace emoji text with proper Ikonli vector icons.
 */
public class SidebarUtil {

    // Bright icons for dark sidebar (#0D203B background)
    private static final Color ICON_COLOR = Color.web("#e2e8f0"); // bright slate-200
    private static final Color ICON_COLOR_ACTIVE = Color.WHITE; // pure white for active tab
    private static final Color ICON_COLOR_DANGER = Color.web("#fca5a5"); // soft red for danger/logout

    /**
     * Shows/hides sidebar tabs based on the current user's role.
     * Also auto-applies icons to ALL sibling sidebar buttons.
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

        // Auto-apply icons to the entire sidebar by walking up from a known button
        Button reference = toDoTab != null ? toDoTab : activitiesTab;
        if (reference != null && reference.getParent() instanceof VBox sidebar) {
            applySidebarIcons(sidebar);
        }
    }

    /**
     * Scans all Buttons in a sidebar VBox and replaces emoji text with Ikonli
     * icons.
     */
    public static void applySidebarIcons(VBox sidebar) {
        if (sidebar == null)
            return;
        for (Node node : sidebar.getChildren()) {
            if (node instanceof Button btn) {
                String text = btn.getText();
                if (text == null)
                    continue;

                boolean isActive = btn.getStyleClass().contains("sidebar-button-active");
                boolean isDanger = btn.getStyleClass().contains("sidebar-button-danger");
                Color color = isDanger ? ICON_COLOR_DANGER : (isActive ? ICON_COLOR_ACTIVE : ICON_COLOR);

                if (text.contains("Dashboard")) {
                    setIcon(btn, FontAwesomeSolid.CHART_BAR, "Dashboard", color);
                } else if (text.contains("Job Offers")) {
                    setIcon(btn, FontAwesomeSolid.BRIEFCASE, "Job Offers", color);
                } else if (text.contains("To Do")) {
                    setIcon(btn, FontAwesomeSolid.CHECK_CIRCLE, "To Do", color);
                } else if (text.contains("Activities")) {
                    setIcon(btn, FontAwesomeSolid.TASKS, "Activities", color);
                } else if (text.contains("Projects")) {
                    setIcon(btn, FontAwesomeSolid.FOLDER_OPEN, "Projects", color);
                } else if (text.contains("My Profile")) {
                    setIcon(btn, FontAwesomeSolid.USER_CIRCLE, "My Profile", color);
                } else if (text.contains("Logout")) {
                    setIcon(btn, FontAwesomeSolid.SIGN_OUT_ALT, "Logout", color);
                } else if (text.contains("Users")) {
                    setIcon(btn, FontAwesomeSolid.USERS, "Users", color);
                } else if (text.contains("Analytics") || text.contains("Stats")) {
                    setIcon(btn, FontAwesomeSolid.CHART_LINE, "Analytics", color);
                } else if (text.contains("Applications")) {
                    setIcon(btn, FontAwesomeSolid.FILE_ALT, "Applications", color);
                } else if (text.contains("Add Offer")) {
                    setIcon(btn, FontAwesomeSolid.PLUS_CIRCLE, "Add Offer", color);
                } else if (text.contains("Settings")) {
                    setIcon(btn, FontAwesomeSolid.COG, "Settings", color);
                }
            }
        }
    }

    private static void setIcon(Button btn, org.kordamp.ikonli.Ikon ikon, String label, Color color) {
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(16);
        icon.setIconColor(color);
        btn.setGraphic(icon);
        btn.setText(label);
        btn.setGraphicTextGap(8);
    }

    private static void show(Button btn) {
        if (btn != null) {
            btn.setVisible(true);
            btn.setManaged(true);
        }
    }
}
