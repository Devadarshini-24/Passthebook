package com.passthebook.service;

import com.passthebook.db.DatabaseHandler;
import com.passthebook.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles User registration, login, and current session tracking.
 */
public class AuthService {
    private static User currentUser = null;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    /**
     * Registers a new user in SQLite database.
     */
    public static boolean register(String username, String password, String fullName) throws Exception {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty()) {
            throw new Exception("All registration fields are required.");
        }

        String sql = "INSERT INTO users (username, password, full_name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim().toLowerCase());
            pstmt.setString(2, password.trim());
            pstmt.setString(3, fullName.trim());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                throw new Exception("Username '" + username + "' is already taken.");
            }
            throw new Exception("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user against SQLite database.
     */
    public static User login(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            throw new Exception("Username and password cannot be empty.");
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim().toLowerCase());
            pstmt.setString(2, password.trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name")
                );
                currentUser = user;
                return user;
            } else {
                throw new Exception("Invalid username or password.");
            }
        }
    }
}
