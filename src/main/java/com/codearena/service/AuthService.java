package com.codearena.service;

import com.codearena.dao.UserDAO;
import com.codearena.model.User;
import com.codearena.util.AuthException;
import com.codearena.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this(new UserDAO());
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String username, String password) {
        try {
            if (isBlank(username) || isBlank(password)) {
                throw new AuthException("Username and password are required.");
            }

            User user = userDAO.findByUsername(username);
            if (user == null || password == null || !BCrypt.checkpw(password, user.getPassword())) {
                throw new AuthException("Invalid username or password.");
            }

            return user;
        } catch (AuthException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AuthException("Unable to complete login.", exception);
        }
    }

    public boolean register(String username, String email, String password) {
        try {
            if (isBlank(username) || isBlank(email) || isBlank(password)) {
                throw new AuthException("Username, email, and password are required.");
            }

            if (isUsernameTaken(username)) {
                throw new AuthException("Username is already taken.");
            }

            User user = new User();
            user.setUsername(username.trim());
            user.setEmail(email.trim());
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            user.setRole("CODER");

            boolean registered = userDAO.register(user);
            if (!registered) {
                throw new AuthException("Unable to register user.");
            }

            return true;
        } catch (AuthException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AuthException("Unable to complete registration.", exception);
        }
    }

    public boolean isUsernameTaken(String username) {
        try {
            return userDAO.findByUsername(username) != null;
        } catch (Exception exception) {
            throw new AuthException("Unable to verify username availability.", exception);
        }
    }

    public void logout() {
        SessionManager.clearSession();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
