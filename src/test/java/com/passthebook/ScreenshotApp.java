package com.passthebook;

import com.passthebook.db.DatabaseHandler;
import com.passthebook.model.Book;
import com.passthebook.model.User;
import com.passthebook.service.AuthService;
import com.passthebook.service.BookService;
import com.passthebook.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 * Captures real PNG screenshots of all JavaFX screens by mounting them on stage.
 */
public class ScreenshotApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        File dir = new File("screenshots");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Initialize DB
        File dbFile = new File("passthebook.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        DatabaseHandler.initializeDatabase();

        // Seed data
        AuthService.register("owner_alice", "pass123", "Alice Smith");
        AuthService.register("seeker_bob", "pass123", "Bob Jones");

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

        // 1. Login Screen
        LoginScreen loginScreen = new LoginScreen(() -> {});
        saveNodeSnapshot(stage, loginScreen.getView(), "screenshots/1_login_screen.png");

        // 2. Dashboard Screen
        DashboardScreen dashboardScreen = new DashboardScreen(() -> {}, () -> {}, () -> {}, () -> {});
        saveNodeSnapshot(stage, dashboardScreen.getView(), "screenshots/2_dashboard_screen.png");

        // 3. Owner Mode Screen
        OwnerModeScreen ownerScreen = new OwnerModeScreen(() -> {}, () -> {}, () -> {}, () -> {});
        saveNodeSnapshot(stage, ownerScreen.getView(), "screenshots/3_owner_mode_screen.png");

        // 4. Seeker Mode Screen
        AuthService.login("seeker_bob", "pass123");
        SeekerModeScreen seekerScreen = new SeekerModeScreen(() -> {}, () -> {}, () -> {}, () -> {});
        saveNodeSnapshot(stage, seekerScreen.getView(), "screenshots/4_seeker_mode_screen.png");

        // 5. History Screen
        HistoryScreen historyScreen = new HistoryScreen(() -> {}, () -> {}, () -> {}, () -> {});
        saveNodeSnapshot(stage, historyScreen.getView(), "screenshots/5_history_screen.png");

        System.out.println("ALL SCREENSHOTS CAPTURED SUCCESSFULLY!");
        Platform.exit();
        System.exit(0);
    }

    private void saveNodeSnapshot(Stage stage, Node node, String path) throws Exception {
        Scene scene = new Scene((javafx.scene.Parent) node, 950, 650);
        stage.setScene(scene);
        stage.show();

        WritableImage image = scene.snapshot(null);
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                bufferedImage.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        ImageIO.write(bufferedImage, "png", new File(path));
        System.out.println("Saved: " + path);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
