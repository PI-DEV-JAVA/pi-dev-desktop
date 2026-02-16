package talentospidev.services;

import talentospidev.dao.UserDao;
import talentospidev.models.User;
import talentospidev.utils.PasswordUtil;

public class AuthService {


    private static final UserDao userDao = new UserDao();
    private static User currentUser;

    public static User loginLocal(String email, String password) {

        User user = userDao.findByEmail(email);

        if (user == null) return null;
        if (!user.isActive()) return null;

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return null;
        }

        currentUser = user;
        return user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}


