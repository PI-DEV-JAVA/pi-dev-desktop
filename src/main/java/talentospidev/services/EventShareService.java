package talentospidev.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Opens social share dialogs in the default browser.
 */
public class EventShareService {

    public static void shareOnFacebook(String title, String url) {
        String shareUrl = "https://www.facebook.com/sharer/sharer.php?u=" + enc(url) + "&quote=" + enc(title);
        openBrowser(shareUrl);
    }

    public static void shareOnTwitter(String title, String url) {
        String shareUrl = "https://twitter.com/intent/tweet?text=" + enc(title) + "&url=" + enc(url);
        openBrowser(shareUrl);
    }

    public static void shareOnLinkedIn(String title, String url) {
        String shareUrl = "https://www.linkedin.com/sharing/share-offsite/?url=" + enc(url);
        openBrowser(shareUrl);
    }

    public static String buildEventUrl(int eventId) {
        return "https://talentos.app/events/" + eventId;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", url);
            } else {
                pb = new ProcessBuilder("xdg-open", url);
            }
            pb.start();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
