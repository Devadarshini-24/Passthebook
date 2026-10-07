package com.passthebook;

import com.passthebook.db.DatabaseHandler;
import com.passthebook.model.Book;
import com.passthebook.model.User;
import com.passthebook.service.AuthService;
import com.passthebook.service.BookService;
import com.passthebook.ui.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Generator script to capture actual PNG screenshots of JavaFX UI screens.
 */
public class GenerateScreenshots {

    public static void main(String[] args) throws Exception {
        File dir = new File("screenshots");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Initialize Database with sample data
        File dbFile = new File("passthebook.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        DatabaseHandler.initializeDatabase();

        // Register sample users and books
        AuthService.register("owner_alice", "pass123", "Alice Smith");
        AuthService.register("seeker_bob", "pass123", "Bob Jones");
        AuthService.register("seeker_charlie", "pass123", "Charlie Brown");

        User alice = AuthService.login("owner_alice", "pass123");
        BookService.addBook(new Book(0, alice.getId(), alice.getUsername(), "Effective Java (3rd Edition)", "Joshua Bloch", "978-0134685991", 450.0, "Like New", "Sell", true));
        BookService.addBook(new Book(0, alice.getId(), alice.getUsername(), "Clean Code", "Robert C. Martin", "978-0132350884", 0.0, "Good", "Free", true));
        BookService.addBook(new Book(0, alice.getId(), alice.getUsername(), "Head First Java", "Kathy Sierra", "978-0596009205", 80.0, "Fair", "Lend", true));

        User bob = AuthService.login("seeker_bob", "pass123");
        ArrayList<Book> available = BookService.searchAvailableBooks("Clean Code", "All", bob.getId());
        if (!available.isEmpty()) {
            BookService.requestBook(available.get(0).getId(), bob.getId());
        }

        AuthService.login("owner_alice", "pass123");

        CountDownLatch latch = new CountDownLatch(1);

        Platform.startup(() -> {
            try {
                // 1. Login Screen
                LoginScreen loginScreen = new LoginScreen(() -> {});
                saveSnapshot(loginScreen.getView(), 950, 650, "screenshots/1_login_screen.png");

                // 2. Dashboard Screen
                DashboardScreen dashboardScreen = new DashboardScreen(() -> {}, () -> {}, () -> {}, () -> {});
                saveSnapshot(dashboardScreen.getView(), 950, 650, "screenshots/2_dashboard_screen.png");

                // 3. Owner Mode Screen
                OwnerModeScreen ownerScreen = new OwnerModeScreen(() -> {}, () -> {}, () -> {}, () -> {});
                saveSnapshot(ownerScreen.getView(), 950, 650, "screenshots/3_owner_mode_screen.png");

                // 4. Seeker Mode Screen
                AuthService.login("seeker_bob", "pass123");
                SeekerModeScreen seekerScreen = new SeekerModeScreen(() -> {}, () -> {}, () -> {}, () -> {});
                saveSnapshot(seekerScreen.getView(), 950, 650, "screenshots/4_seeker_mode_screen.png");

                // 5. History Screen
                HistoryScreen historyScreen = new HistoryScreen(() -> {}, () -> {}, () -> {}, () -> {});
                saveSnapshot(historyScreen.getView(), 950, 650, "screenshots/5_history_screen.png");

                System.out.println("SUCCESS: All screenshots saved to screenshots/ directory!");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        System.exit(0);
    }

    private static void saveSnapshot(Node node, int width, int height, String filePath) throws Exception {
        Scene scene = new Scene((javafx.scene.Parent) node, width, height);
        WritableImage image = scene.snapshot(null);

        int imgWidth = (int) image.getWidth();
        int imgHeight = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < imgHeight; y++) {
            for (int x = 0; x < imgWidth; x++) {
                bufferedImage.setRGB(x, y, pixelReader.getArgb(x, y));
            }
        }

        ImageIO.write(bufferedImage, "png", new File(filePath));
        System.out.println("Captured screenshot: " + filePath);
    }
}
