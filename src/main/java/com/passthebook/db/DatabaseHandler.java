package com.passthebook.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles SQLite Database initialization and connection management using JDBC.
 */
public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:passthebook.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes SQLite database schema with users, books, requests, and transactions tables.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL
                );
            """);

            // 2. Books table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    author TEXT NOT NULL,
                    isbn TEXT NOT NULL,
                    price REAL NOT NULL,
                    condition TEXT NOT NULL,
                    type TEXT NOT NULL,
                    is_available INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY(owner_id) REFERENCES users(id)
                );
            """);

            // 3. Requests table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    seeker_id INTEGER NOT NULL,
                    request_time INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    FOREIGN KEY(book_id) REFERENCES books(id),
                    FOREIGN KEY(seeker_id) REFERENCES users(id)
                );
            """);

            // 4. Transactions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    request_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    seller_id INTEGER NOT NULL,
                    buyer_id INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    price REAL NOT NULL,
                    date TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'COMPLETED',
                    FOREIGN KEY(request_id) REFERENCES requests(id),
                    FOREIGN KEY(book_id) REFERENCES books(id),
                    FOREIGN KEY(seller_id) REFERENCES users(id),
                    FOREIGN KEY(buyer_id) REFERENCES users(id)
                );
            """);

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
