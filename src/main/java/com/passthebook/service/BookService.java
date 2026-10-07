package com.passthebook.service;

import com.passthebook.db.DatabaseHandler;
import com.passthebook.exception.InvalidBookException;
import com.passthebook.exception.InvalidPriceException;
import com.passthebook.model.Book;
import com.passthebook.model.Request;
import com.passthebook.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.PriorityQueue;

/**
 * Service class for Book operations, Request handling, and Transactions.
 * 
 * Uses Java Syllabus Concepts:
 * - ArrayList: For storing collections of books, requests, and transactions.
 * - PriorityQueue: For ordering pending book requests by request time (earliest first).
 * - Cloneable: Uses book.clone() when owner duplicates a book listing.
 * - Custom Checked Exceptions: Validates book inputs throwing InvalidBookException and InvalidPriceException.
 * - JDBC: Manages SQLite CRUD queries.
 */
public class BookService {

    /**
     * Requirement: Custom Checked Exceptions (InvalidBookException & InvalidPriceException)
     * Validates and adds a new book listing.
     */
    public static void addBook(Book book) throws InvalidBookException, InvalidPriceException, Exception {
        // Validate inputs using custom checked exceptions
        Book.validate(book.getName(), book.getAuthor(), book.getIsbn(), book.getType(), book.getPrice());

        if ("Free".equalsIgnoreCase(book.getType())) {
            book.setPrice(0.0);
        }

        String sql = """
            INSERT INTO books (owner_id, name, author, isbn, price, condition, type, is_available)
            VALUES (?, ?, ?, ?, ?, ?, ?, 1)
        """;

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, book.getOwnerId());
            pstmt.setString(2, book.getName().trim());
            pstmt.setString(3, book.getAuthor().trim());
            pstmt.setString(4, book.getIsbn().trim());
            pstmt.setDouble(5, book.getPrice());
            pstmt.setString(6, book.getCondition().trim());
            pstmt.setString(7, book.getType().trim());
            pstmt.executeUpdate();
        }
    }

    /**
     * Requirement: Cloneable concept.
     * Owner duplicates an existing book listing using book.clone().
     */
    public static void cloneAndAddBook(int bookId) throws Exception {
        Book original = getBookById(bookId);
        if (original == null) {
            throw new Exception("Original book not found.");
        }

        // Clone the book object (resets ID to 0 and sets available to true)
        Book cloned = original.clone();
        
        // Save the cloned book to DB
        addBook(cloned);
    }

    /**
     * Fetches a single book by ID.
     */
    public static Book getBookById(int bookId) throws Exception {
        String sql = """
            SELECT b.*, u.username as owner_username 
            FROM books b 
            JOIN users u ON b.owner_id = u.id 
            WHERE b.id = ?
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Book(
                    rs.getInt("id"),
                    rs.getInt("owner_id"),
                    rs.getString("owner_username"),
                    rs.getString("name"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getDouble("price"),
                    rs.getString("condition"),
                    rs.getString("type"),
                    rs.getInt("is_available") == 1
                );
            }
        }
        return null;
    }

    /**
     * Requirement: ArrayList concept.
     * Retrieves books listed by a specific owner.
     */
    public static ArrayList<Book> getOwnerBooks(int ownerId) {
        ArrayList<Book> list = new ArrayList<>();
        String sql = """
            SELECT b.*, u.username as owner_username 
            FROM books b 
            JOIN users u ON b.owner_id = u.id 
            WHERE b.owner_id = ? 
            ORDER BY b.id DESC
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Book(
                    rs.getInt("id"),
                    rs.getInt("owner_id"),
                    rs.getString("owner_username"),
                    rs.getString("name"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getDouble("price"),
                    rs.getString("condition"),
                    rs.getString("type"),
                    rs.getInt("is_available") == 1
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Requirement: ArrayList & Search/Filter logic for Seeker Mode.
     */
    public static ArrayList<Book> searchAvailableBooks(String nameOrAuthorQuery, String typeFilter, int currentUserId) {
        ArrayList<Book> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT b.*, u.username as owner_username 
            FROM books b 
            JOIN users u ON b.owner_id = u.id 
            WHERE b.is_available = 1 AND b.owner_id != ?
        """);

        ArrayList<Object> params = new ArrayList<>();
        params.add(currentUserId);

        if (nameOrAuthorQuery != null && !nameOrAuthorQuery.trim().isEmpty()) {
            sql.append(" AND (LOWER(b.name) LIKE ? OR LOWER(b.author) LIKE ? OR LOWER(b.isbn) LIKE ?)");
            String q = "%" + nameOrAuthorQuery.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (typeFilter != null && !typeFilter.equalsIgnoreCase("All") && !typeFilter.trim().isEmpty()) {
            sql.append(" AND b.type = ?");
            params.add(typeFilter.trim());
        }

        sql.append(" ORDER BY b.id DESC");

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Book(
                    rs.getInt("id"),
                    rs.getInt("owner_id"),
                    rs.getString("owner_username"),
                    rs.getString("name"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getDouble("price"),
                    rs.getString("condition"),
                    rs.getString("type"),
                    rs.getInt("is_available") == 1
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Requirement: Seeker requests an available book.
     */
    public static void requestBook(int bookId, int seekerId) throws Exception {
        // Check if seeker already requested this book
        String checkSql = "SELECT COUNT(*) FROM requests WHERE book_id = ? AND seeker_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, bookId);
            checkStmt.setInt(2, seekerId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new Exception("You have already submitted a pending request for this book.");
            }
        }

        String insertSql = "INSERT INTO requests (book_id, seeker_id, request_time, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, seekerId);
            pstmt.setLong(3, System.currentTimeMillis()); // Store request time in milliseconds
            pstmt.executeUpdate();
        }
    }

    /**
     * Requirement: PriorityQueue based on request time.
     * Loads all pending requests for a specific book into a PriorityQueue.
     * The request with the earliest requestTime will be at the head of the PriorityQueue.
     */
    public static PriorityQueue<Request> getPriorityRequestQueueForBook(int bookId) {
        PriorityQueue<Request> pq = new PriorityQueue<>();
        String sql = """
            SELECT r.*, b.name as book_title, b.owner_id, b.type, b.price, u.username as seeker_username
            FROM requests r
            JOIN books b ON r.book_id = b.id
            JOIN users u ON r.seeker_id = u.id
            WHERE r.book_id = ? AND r.status = 'PENDING'
        """;

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Request req = new Request(
                    rs.getInt("id"),
                    rs.getInt("book_id"),
                    rs.getString("book_title"),
                    rs.getInt("seeker_id"),
                    rs.getString("seeker_username"),
                    rs.getInt("owner_id"),
                    rs.getString("type"),
                    rs.getDouble("price"),
                    rs.getLong("request_time"),
                    rs.getString("status")
                );
                pq.add(req); // PriorityQueue orders by earliest request time automatically
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pq;
    }

    /**
     * Requirement: ArrayList of pending requests for an owner across all their books.
     */
    public static ArrayList<Request> getPendingRequestsForOwner(int ownerId) {
        ArrayList<Request> list = new ArrayList<>();
        String sql = """
            SELECT r.*, b.name as book_title, b.owner_id, b.type, b.price, u.username as seeker_username
            FROM requests r
            JOIN books b ON r.book_id = b.id
            JOIN users u ON r.seeker_id = u.id
            WHERE b.owner_id = ? AND r.status = 'PENDING'
            ORDER BY r.request_time ASC
        """;

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Request(
                    rs.getInt("id"),
                    rs.getInt("book_id"),
                    rs.getString("book_title"),
                    rs.getInt("seeker_id"),
                    rs.getString("seeker_username"),
                    rs.getInt("owner_id"),
                    rs.getString("type"),
                    rs.getDouble("price"),
                    rs.getLong("request_time"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Requirement: Accept request logic.
     * When owner accepts a request:
     * - Uses PriorityQueue to grant priority to earliest request if multiple exist.
     * - Request status becomes ACCEPTED.
     * - Book becomes unavailable (is_available = 0).
     * - Other pending requests for this book are REJECTED.
     * - A Transaction record is created.
     */
    public static void acceptRequest(int requestId) throws Exception {
        // Fetch request details
        String reqSql = """
            SELECT r.*, b.id as book_id, b.name as book_title, b.owner_id, b.type, b.price, u.username as seeker_username
            FROM requests r
            JOIN books b ON r.book_id = b.id
            JOIN users u ON r.seeker_id = u.id
            WHERE r.id = ?
        """;

        Request targetReq = null;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(reqSql)) {
            pstmt.setInt(1, requestId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                targetReq = new Request(
                    rs.getInt("id"),
                    rs.getInt("book_id"),
                    rs.getString("book_title"),
                    rs.getInt("seeker_id"),
                    rs.getString("seeker_username"),
                    rs.getInt("owner_id"),
                    rs.getString("type"),
                    rs.getDouble("price"),
                    rs.getLong("request_time"),
                    rs.getString("status")
                );
            }
        }

        if (targetReq == null) {
            throw new Exception("Request not found.");
        }

        int bookId = targetReq.getBookId();

        // Load into PriorityQueue to verify priority
        PriorityQueue<Request> pq = getPriorityRequestQueueForBook(bookId);
        if (!pq.isEmpty()) {
            Request highestPriority = pq.peek();
            if (highestPriority.getId() != requestId) {
                System.out.println("Note: Accepting request (Earliest request was from " + highestPriority.getSeekerUsername() + ").");
            }
        }

        try (Connection conn = DatabaseHandler.getConnection()) {
            conn.setAutoCommit(false); // Transaction block

            // 1. Accept target request
            try (PreparedStatement updateReq = conn.prepareStatement("UPDATE requests SET status = 'ACCEPTED' WHERE id = ?")) {
                updateReq.setInt(1, requestId);
                updateReq.executeUpdate();
            }

            // 2. Reject all other pending requests for the same book
            try (PreparedStatement rejectOthers = conn.prepareStatement("UPDATE requests SET status = 'REJECTED' WHERE book_id = ? AND id != ? AND status = 'PENDING'")) {
                rejectOthers.setInt(1, bookId);
                rejectOthers.setInt(2, requestId);
                rejectOthers.executeUpdate();
            }

            // 3. Mark book as unavailable
            try (PreparedStatement updateBook = conn.prepareStatement("UPDATE books SET is_available = 0 WHERE id = ?")) {
                updateBook.setInt(1, bookId);
                updateBook.executeUpdate();
            }

            // 4. Create Transaction record
            String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String txSql = """
                INSERT INTO transactions (request_id, book_id, seller_id, buyer_id, type, price, date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'COMPLETED')
            """;
            try (PreparedStatement txStmt = conn.prepareStatement(txSql)) {
                txStmt.setInt(1, targetReq.getId());
                txStmt.setInt(2, targetReq.getBookId());
                txStmt.setInt(3, targetReq.getOwnerId());
                txStmt.setInt(4, targetReq.getSeekerId());
                txStmt.setString(5, targetReq.getType());
                txStmt.setDouble(6, targetReq.getPrice());
                txStmt.setString(7, dateStr);
                txStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
        }
    }

    /**
     * Owner rejects a request.
     */
    public static void rejectRequest(int requestId) throws Exception {
        String sql = "UPDATE requests SET status = 'REJECTED' WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Requirement: ArrayList of seeker's requests.
     */
    public static ArrayList<Request> getSeekerRequests(int seekerId) {
        ArrayList<Request> list = new ArrayList<>();
        String sql = """
            SELECT r.*, b.name as book_title, b.owner_id, b.type, b.price, u.username as seeker_username
            FROM requests r
            JOIN books b ON r.book_id = b.id
            JOIN users u ON r.seeker_id = u.id
            WHERE r.seeker_id = ?
            ORDER BY r.request_time DESC
        """;

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seekerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Request(
                    rs.getInt("id"),
                    rs.getInt("book_id"),
                    rs.getString("book_title"),
                    rs.getInt("seeker_id"),
                    rs.getString("seeker_username"),
                    rs.getInt("owner_id"),
                    rs.getString("type"),
                    rs.getDouble("price"),
                    rs.getLong("request_time"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Requirement: ArrayList of Transaction History.
     */
    public static ArrayList<Transaction> getTransactionHistory(int userId) {
        ArrayList<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.*, b.name as book_title, u_seller.username as seller_name, u_buyer.username as buyer_name
            FROM transactions t
            JOIN books b ON t.book_id = b.id
            JOIN users u_seller ON t.seller_id = u_seller.id
            JOIN users u_buyer ON t.buyer_id = u_buyer.id
            WHERE t.seller_id = ? OR t.buyer_id = ?
            ORDER BY t.id DESC
        """;

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Transaction(
                    rs.getInt("id"),
                    rs.getInt("request_id"),
                    rs.getInt("book_id"),
                    rs.getString("book_title"),
                    rs.getInt("seller_id"),
                    rs.getString("seller_name"),
                    rs.getInt("buyer_id"),
                    rs.getString("buyer_name"),
                    rs.getString("type"),
                    rs.getDouble("price"),
                    rs.getString("date"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
