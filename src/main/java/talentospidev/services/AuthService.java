package talentospidev.services;

import talentospidev.dao.ProfileDao;
import talentospidev.dao.UserDao;
import talentospidev.models.User;
import talentospidev.utils.PasswordUtil;

public class AuthService {

    private static final UserDao userDao = new UserDao();
    private static final ProfileDao profileDao = new ProfileDao();
    private static User currentUser;

    /**
     * Local login: checks email + password.
     */
    public static User loginLocal(String email, String password) {
        User user = userDao.findByEmail(email);

        if (user == null)
            return null;
        if (!user.isActive())
            return null;

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return null;
        }

        currentUser = user;
        return user;
    }

    /**
     * OAuth login/register:
     * 1. If user exists by providerId → log in
     * 2. If user exists by email → link provider and log in
     * 3. If new → create account as CANDIDATE + initial profile
     */
    public static User loginOAuth(String email, String providerId) {
        // 1) Check by Google ID
        User user = userDao.findByProviderId(providerId);
        if (user != null) {
            currentUser = user;
            return user;
        }

        // 2) Check by email (maybe registered locally before)
        user = userDao.findByEmail(email);
        if (user != null) {
            currentUser = user;
            return user;
        }

        // 3) New user — create with CANDIDATE role
        User newUser = new User(email, User.Role.CANDIDATE, User.AuthProvider.GOOGLE, providerId);
        userDao.saveOAuth(newUser);
        profileDao.createInitialProfile(newUser.getId());

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
