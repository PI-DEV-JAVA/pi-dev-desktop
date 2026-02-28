package talentospidev.utils;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.prefs.Preferences;

/**
 * Manages light/dark theme switching across the entire application.
 * Uses AtlantaFX PrimerLight / PrimerDark as the base UA stylesheet,
 * then overlays our custom app.css and (when dark) app-dark.css.
 *
 * Key: stylesheets are applied at the ROOT NODE level (not scene level)
 * so they override any stylesheets set by FXML files.
 */
public class ThemeManager {

    private static final String PREF_KEY = "dark_mode";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    private static boolean darkMode = false;

    private static final String APP_CSS = ThemeManager.class.getResource("/style/app.css").toExternalForm();
    private static final String DARK_CSS = ThemeManager.class.getResource("/style/app-dark.css").toExternalForm();

    /** Called once at startup to restore the saved preference. */
    public static void init() {
        darkMode = prefs.getBoolean(PREF_KEY, false);
        applyUA();
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    /** Toggles between light and dark and persists the choice. */
    public static void toggle() {
        darkMode = !darkMode;
        prefs.putBoolean(PREF_KEY, darkMode);
        applyUA();
        Scene scene = SceneUtil.getStage().getScene();
        if (scene != null) {
            applyToScene(scene);
        }
    }

    /** Sets the global UA stylesheet (AtlantaFX). */
    private static void applyUA() {
        if (darkMode) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }
    }

    /**
     * Applies custom stylesheets to a scene.
     * We set stylesheets on the ROOT NODE (not scene) so they have
     * higher priority than any FXML-embedded stylesheets attribute.
     * We also fix inline styles that hardcode light-mode colors.
     */
    public static void applyToScene(Scene scene) {
        Parent root = scene.getRoot();

        // Clear any scene-level stylesheets
        scene.getStylesheets().clear();

        // Clear FXML-injected stylesheets on the root node
        root.getStylesheets().clear();

        // Add our stylesheets at root-node level (highest CSS priority after inline)
        root.getStylesheets().add(APP_CSS);
        if (darkMode) {
            root.getStylesheets().add(DARK_CSS);
            // Override inline background styles that hardcode light colors
            fixInlineStyles(root);
        }
    }

    /**
     * Recursively walks the scene graph and replaces inline light-mode
     * background colors with dark-mode equivalents.
     */
    private static void fixInlineStyles(Node node) {
        String style = node.getStyle();
        if (style != null && !style.isEmpty()) {
            // Replace common light background colors with dark equivalents
            String fixed = style;
            fixed = fixed.replaceAll("-fx-background-color:\\s*#f0f2f5", "-fx-background-color: #1a1a2e");
            fixed = fixed.replaceAll("-fx-background-color:\\s*white", "-fx-background-color: #16213e");
            fixed = fixed.replaceAll("-fx-background-color:\\s*#ffffff", "-fx-background-color: #16213e");
            fixed = fixed.replaceAll("-fx-background-color:\\s*#f9fafb", "-fx-background-color: #1e293b");
            fixed = fixed.replaceAll("-fx-background-color:\\s*#f8fafc", "-fx-background-color: #1a1a2e");
            fixed = fixed.replaceAll("-fx-background-color:\\s*transparent;\\s*-fx-background:\\s*transparent",
                    "-fx-background-color: transparent; -fx-background: transparent");

            // Fix text colors
            fixed = fixed.replaceAll("-fx-text-fill:\\s*#111827", "-fx-text-fill: #e2e8f0");
            fixed = fixed.replaceAll("-fx-text-fill:\\s*#1f2937", "-fx-text-fill: #e2e8f0");
            fixed = fixed.replaceAll("-fx-text-fill:\\s*#374151", "-fx-text-fill: #cbd5e1");
            fixed = fixed.replaceAll("-fx-text-fill:\\s*#4b5563", "-fx-text-fill: #94a3b8");
            fixed = fixed.replaceAll("-fx-text-fill:\\s*#6b7280", "-fx-text-fill: #94a3b8");
            fixed = fixed.replaceAll("-fx-text-fill:\\s*#9ca3af", "-fx-text-fill: #64748b");

            // Fix border colors
            fixed = fixed.replaceAll("-fx-border-color:\\s*#e5e7eb", "-fx-border-color: #334155");
            fixed = fixed.replaceAll("-fx-border-color:\\s*#f3f4f6", "-fx-border-color: #1e293b");

            if (!fixed.equals(style)) {
                node.setStyle(fixed);
            }
        }

        // Recurse into children
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                fixInlineStyles(child);
            }
        }
    }
}
