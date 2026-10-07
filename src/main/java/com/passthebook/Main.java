package com.passthebook;

import com.passthebook.db.DatabaseHandler;
import com.passthebook.service.AuthService;
import com.passthebook.service.AvailabilityMonitorThread;
import com.passthebook.ui.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for PassTheBook JavaFX application.
 */
public class Main extends Application {
    private Stage primaryStage;
    private AvailabilityMonitorThread monitorThread;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("PassTheBook - Student Book Sharing Platform");

        // 1. Initialize SQLite Database
        DatabaseHandler.initializeDatabase();

        // 2. Start Background Monitor Thread
        monitorThread = new AvailabilityMonitorThread();
        monitorThread.start();

        // 3. Show Login Screen initially
        showLoginScreen();

        primaryStage.setMinWidth(950);
        primaryStage.setMinHeight(650);
        primaryStage.setOnCloseRequest(e -> {
            if (monitorThread != null) {
                monitorThread.stopMonitoring();
            }
        });

        primaryStage.show();
    }

    public void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(this::showDashboard);
        Scene scene = new Scene(loginScreen.getView(), 950, 650);
        primaryStage.setScene(scene);
    }

    public void showDashboard() {
        DashboardScreen dashboardScreen = new DashboardScreen(
            this::showOwnerMode,
            this::showSeekerMode,
            this::showHistory,
            this::handleLogout
        );
        Scene scene = new Scene(dashboardScreen.getView(), 950, 650);
        primaryStage.setScene(scene);
    }

    public void showOwnerMode() {
        OwnerModeScreen ownerScreen = new OwnerModeScreen(
            this::showDashboard,
            this::showSeekerMode,
            this::showHistory,
            this::handleLogout
        );
        Scene scene = new Scene(ownerScreen.getView(), 950, 650);
        primaryStage.setScene(scene);
    }

    public void showSeekerMode() {
        SeekerModeScreen seekerScreen = new SeekerModeScreen(
            this::showDashboard,
            this::showOwnerMode,
            this::showHistory,
            this::handleLogout
        );
        Scene scene = new Scene(seekerScreen.getView(), 950, 650);
        primaryStage.setScene(scene);
    }

    public void showHistory() {
        HistoryScreen historyScreen = new HistoryScreen(
            this::showDashboard,
            this::showOwnerMode,
            this::showSeekerMode,
            this::handleLogout
        );
        Scene scene = new Scene(historyScreen.getView(), 950, 650);
        primaryStage.setScene(scene);
    }

    public void handleLogout() {
        AuthService.logout();
        showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
