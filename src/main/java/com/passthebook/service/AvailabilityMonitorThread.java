package com.passthebook.service;

import com.passthebook.db.DatabaseHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Requirement: One simple background thread for checking whether a requested book becomes available.
 * Runs periodically in the background as a daemon thread.
 */
public class AvailabilityMonitorThread extends Thread {
    private volatile boolean running = true;
    private static String statusMessage = "Monitor active: Checking book availability...";

    public AvailabilityMonitorThread() {
        setDaemon(true); // Daemon thread so it terminates when app closes
        setName("BookAvailabilityMonitorThread");
    }

    @Override
    public void run() {
        System.out.println("[Background Thread] Book Availability Monitor Thread started.");
        while (running) {
            try {
                checkAvailableBooks();
                Thread.sleep(8000); // Check every 8 seconds
            } catch (InterruptedException e) {
                System.out.println("[Background Thread] Monitor interrupted.");
                break;
            } catch (Exception e) {
                System.err.println("[Background Thread] Error checking availability: " + e.getMessage());
            }
        }
    }

    private void checkAvailableBooks() {
        String sql = "SELECT COUNT(*) FROM books WHERE is_available = 1";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                int availableCount = rs.getInt(1);
                statusMessage = "Background Check: " + availableCount + " book(s) currently available for seekers.";
            }
        } catch (Exception e) {
            statusMessage = "Background Monitor: Database offline or busy.";
        }
    }

    public static String getStatusMessage() {
        return statusMessage;
    }

    public void stopMonitoring() {
        this.running = false;
    }
}
