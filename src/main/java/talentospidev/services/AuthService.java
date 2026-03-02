package talentospidev.services;

import talentospidev.dao.ProfileDao;
import talentospidev.dao.UserDao;
import talentospidev.models.User;
import talentospidev.utils.PasswordUtil;

public class AuthService {

    private static final UserDao userDao = new UserDao();
    private static final ProfileDao profileDao = new ProfileDao();
    private static User currentUser;

    public static final int MAX_FAILED_ATTEMPTS = 5;

    /** Result of a login attempt — carries state for the UI to display. */
    public static class LoginResult {
        public enum Status { SUCCESS, INVALID, LOCKED, WRONG_PASSWORD }
        public final Status status;
        public final User user;
        public final int attemptsRemaining;

        private LoginResult(Status status, User user, int attemptsRemaining) {
            this.status = status;
            this.user = user;
            this.attemptsRemaining = attemptsRemaining;
        }

        public static LoginResult success(User u)            { return new LoginResult(Status.SUCCESS, u, 0); }
        public static LoginResult invalid()                   { return new LoginResult(Status.INVALID, null, 0); }
        public static LoginResult locked()                    { return new LoginResult(Status.LOCKED, null, 0); }
        public static LoginResult wrongPassword(int remaining){ return new LoginResult(Status.WRONG_PASSWORD, null, remaining); }
    }

    /**
     * Local login with lockout: deactivates after 5 wrong passwords.
     */
    public static LoginResult loginLocalSafe(String email, String password) {
        User user = userDao.findByEmail(email);
        if (user == null) return LoginResult.invalid();

        if (!user.isActive()) return LoginResult.locked();

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            userDao.incrementFailedAttempts(user.getId());
            int newCount = user.getFailedAttempts() + 1;
            int remaining = MAX_FAILED_ATTEMPTS - newCount;

            if (newCount >= MAX_FAILED_ATTEMPTS) {
                userDao.setActive(user.getId(), false);
                return LoginResult.locked();
            }
            return LoginResult.wrongPassword(remaining);
        }

        // Success — reset counter
        if (user.getFailedAttempts() > 0) {
            userDao.resetFailedAttempts(user.getId());
        }
        currentUser = user;
        return LoginResult.success(user);
    }

    /**
     * Legacy local login (kept for backward compat).
     */
    public static User loginLocal(String email, String password) {
        LoginResult r = loginLocalSafe(email, password);
        return r.status == LoginResult.Status.SUCCESS ? r.user : null;
    }

    /**
     * OAuth login/register:
     * 1. If user exists by providerId → log in (ensure email_verified)
     * 2. If user exists by email → link provider, mark verified, log in
     * 3. If new → create account as CANDIDATE + initial profile with Google name
     */
    public static User loginOAuth(String email, String providerId, String givenName, String familyName) {
        // 1) Check by Google ID
        User user = userDao.findByProviderId(providerId);
        if (user != null) {
            // Ensure email is marked verified for Google users
            if (!user.isEmailVerified()) {
                userDao.setEmailVerified(user.getId(), true);
                user.setEmailVerified(true);
            }
            currentUser = user;
            return user;
        }

        // 2) Check by email (maybe registered locally before)
        user = userDao.findByEmail(email);
        if (user != null) {
            // Mark email as verified since Google already verified it
            if (!user.isEmailVerified()) {
                userDao.setEmailVerified(user.getId(), true);
                user.setEmailVerified(true);
            }
            currentUser = user;
            return user;
        }

        // 3) New user — create with CANDIDATE role
        User newUser = new User(email, User.Role.CANDIDATE, User.AuthProvider.GOOGLE, providerId);
        userDao.saveOAuth(newUser);
        // Mark email verified (Google already verified it)
        userDao.setEmailVerified(newUser.getId(), true);
        newUser.setEmailVerified(true);

        // Create initial profile with Google name pre-filled
        profileDao.createInitialProfile(newUser.getId());
        if (givenName != null && !givenName.isEmpty()) {
            talentospidev.models.Profile profile = profileDao.findByUserId(newUser.getId());
            if (profile != null) {
                profile.setFirstName(givenName);
                profile.setLastName(familyName != null ? familyName : "");
                profileDao.save(profile, newUser);
            }
        }

        currentUser = newUser;
        return newUser;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}
