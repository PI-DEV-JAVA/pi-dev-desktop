package talentospidev.config;

/**
 * Google OAuth 2.0 credentials and endpoint configuration.
 * Get your credentials from: https://console.cloud.google.com/apis/credentials
 */
public class GoogleOAuthConfig {

    // ── Your credentials ──
    public static final String CLIENT_ID = "xxx";

    public static final String CLIENT_SECRET = "xx";

    // ── OAuth endpoints ──
    public static final String REDIRECT_URI = "http://localhost:8888/callback";

    public static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    public static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    public static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    // ── Scopes ──
    public static final String SCOPES = "openid email profile";
}
