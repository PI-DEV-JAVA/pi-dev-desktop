package talentospidev.utils;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

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

    private static String appCss;
    private static String darkCss;

    private static String getAppCss() {
        if (appCss == null) {
            var r = ThemeManager.class.getResource("/style/app.css");
            appCss = r != null ? r.toExternalForm() : "";
        }
        return appCss;
    }

    private static String getDarkCss() {
        if (darkCss == null) {
            var r = ThemeManager.class.getResource("/style/app-dark.css");
            darkCss = r != null ? r.toExternalForm() : "";
        }
        return darkCss;
    }

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
        String css = getAppCss();
        if (!css.isEmpty())
            root.getStylesheets().add(css);
        if (darkMode) {
            String dark = getDarkCss();
            if (!dark.isEmpty())
                root.getStylesheets().add(dark);
            // Override inline background styles that hardcode light colors
            fixInlineStyles(root);
        }
    }

    /**
     * Recursively walks the scene graph and replaces inline light-mode
     * background colors with dark-mode equivalents.
     * SKIPS sidebar nodes to preserve their icon/button contrast.
     */
    private static void fixInlineStyles(Node node) {
        // Skip sidebar — it has its own dark styling already
        if (node instanceof VBox vbox && vbox.getStyleClass().contains("sidebar")) {
            return;
        }

        String style = node.getStyle();
        if (style != null && !style.isEmpty()) {
            String fixed = style;

            // ── Background colors ──────────────────────────────
            fixed = replaceProp(fixed, "-fx-background-color", "white", "#16213e");
            fixed = replaceProp(fixed, "-fx-background-color", "#ffffff", "#16213e");
            fixed = replaceProp(fixed, "-fx-background-color", "#f0f2f5", "#1a1a2e");
            fixed = replaceProp(fixed, "-fx-background-color", "#f9fafb", "#1e293b");
            fixed = replaceProp(fixed, "-fx-background-color", "#f8fafc", "#1a1a2e");
            fixed = replaceProp(fixed, "-fx-background-color", "#f3f4f6", "#283548");
            fixed = replaceProp(fixed, "-fx-background-color", "#e5e7eb", "#334155");
            fixed = replaceProp(fixed, "-fx-background-color", "#eff6ff", "#1e3a5f");
            fixed = replaceProp(fixed, "-fx-background-color", "#fafbfc", "#1e293b");
            fixed = replaceProp(fixed, "-fx-background-color", "#eef2ff", "rgba(99,102,241,0.15)");
            fixed = replaceProp(fixed, "-fx-background-color", "#dcfce7", "rgba(34,197,94,0.15)");
            fixed = replaceProp(fixed, "-fx-background-color", "#fef3c7", "rgba(245,158,11,0.15)");
            // Login page gradient
            fixed = fixed.replace(
                    "linear-gradient(to bottom right, #eef2ff, #f0f2f5, #ede9fe)",
                    "linear-gradient(to bottom right, #0f172a, #1a1a2e, #1e1b4b)");

            // ── Text colors ────────────────────────────────────
            fixed = replaceProp(fixed, "-fx-text-fill", "#111827", "#e2e8f0");
            fixed = replaceProp(fixed, "-fx-text-fill", "#1f2937", "#e2e8f0");
            fixed = replaceProp(fixed, "-fx-text-fill", "#374151", "#cbd5e1");
            fixed = replaceProp(fixed, "-fx-text-fill", "#4b5563", "#94a3b8");
            fixed = replaceProp(fixed, "-fx-text-fill", "#6b7280", "#94a3b8");
            fixed = replaceProp(fixed, "-fx-text-fill", "#9ca3af", "#94a3b8");

            // ── Fill (Text nodes use -fx-fill) ─────────────────
            fixed = replaceProp(fixed, "-fx-fill", "#111827", "#e2e8f0");
            fixed = replaceProp(fixed, "-fx-fill", "#4b5563", "#94a3b8");
            fixed = replaceProp(fixed, "-fx-fill", "#6b7280", "#94a3b8");
            fixed = replaceProp(fixed, "-fx-fill", "#1f2937", "#e2e8f0");

            // ── Border colors ──────────────────────────────────
            fixed = replaceProp(fixed, "-fx-border-color", "#e5e7eb", "#334155");
            fixed = replaceProp(fixed, "-fx-border-color", "#f3f4f6", "#1e293b");
            fixed = replaceProp(fixed, "-fx-border-color", "#d1d5db", "#475569");

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

    /**
     * Safely replaces a CSS property value, handling both mid-string (followed by
     * ;)
     * and end-of-string occurrences.
     */
    private static String replaceProp(String style, String prop, String oldVal, String newVal) {
        // Case-insensitive to catch "White", "WHITE", etc.
        String pattern = "(?i)" + prop.replace("-", "\\-") + ":\\s*" + oldVal.replace("#", "\\#") + "(?=[;\\s]|$)";
        return style.replaceAll(pattern, prop + ": " + newVal);
    }
}
