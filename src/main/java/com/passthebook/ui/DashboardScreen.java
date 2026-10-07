package com.passthebook.ui;

import com.passthebook.model.User;
import com.passthebook.service.AuthService;
import com.passthebook.service.AvailabilityMonitorThread;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Screen 2: Dashboard Screen.
 * Allows user to choose between Owner Mode, Seeker Mode, or History.
 */
public class DashboardScreen {
    private BorderPane view;

    public DashboardScreen(Runnable onOwner, Runnable onSeeker, Runnable onHistory, Runnable onLogout) {
        view = new BorderPane();
        view.setStyle("-fx-background-color: " + UIUtils.BG_LIGHT + ";");

        User user = AuthService.getCurrentUser();
        String username = (user != null) ? user.getFullName() + " (@" + user.getUsername() + ")" : "User";

        // Top Navbar
        HBox navbar = UIUtils.createTopNavigation(
            () -> {}, onOwner, onSeeker, onHistory, onLogout, "Dashboard"
        );
        view.setTop(navbar);

        // Center Content
        VBox centerBox = new VBox(25);
        centerBox.setPadding(new Insets(30, 40, 30, 40));
        centerBox.setAlignment(Pos.TOP_CENTER);

        // Welcome Title
        VBox headerBox = new VBox(6);
        headerBox.setAlignment(Pos.CENTER);
        Label welcomeLabel = new Label("Welcome to PassTheBook, " + username + "!");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        welcomeLabel.setTextFill(Color.web(UIUtils.TEXT_DARK));

        Label modeHint = new Label("Please select a mode to continue:");
        modeHint.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        modeHint.setTextFill(Color.web(UIUtils.TEXT_MUTED));
        headerBox.getChildren().addAll(welcomeLabel, modeHint);

        // Mode Choice Cards Grid (HBox)
        HBox cardsGrid = new HBox(25);
        cardsGrid.setAlignment(Pos.CENTER);

        // Card 1: Owner Mode
        VBox ownerCard = createDashboardCard(
            "📚 OWNER MODE",
            "Offer & List Books",
            "Add books you own to sell, lend, or give for free. Accept or reject incoming requests from seekers.",
            "Enter Owner Mode",
            UIUtils.PRIMARY_COLOR,
            onOwner
        );

        // Card 2: Seeker Mode
        VBox seekerCard = createDashboardCard(
            "🔍 SEEKER MODE",
            "Find & Request Books",
            "Search available books by title or author. Choose Buy, Borrow, or Free and request books instantly.",
            "Enter Seeker Mode",
            "#059669", // Emerald Green
            onSeeker
        );

        // Card 3: History & Requests
        VBox historyCard = createDashboardCard(
            "📋 MY REQUESTS & HISTORY",
            "Transactions & Requests",
            "Track your pending book requests, approved transactions, and past activity.",
            "View History",
            "#7C3AED", // Purple
            onHistory
        );

        cardsGrid.getChildren().addAll(ownerCard, seekerCard, historyCard);

        centerBox.getChildren().addAll(headerBox, cardsGrid);
        view.setCenter(centerBox);

        // Bottom Status Bar (Requirement: Background Thread status display!)
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setStyle("-fx-background-color: #E2E8F0; -fx-border-color: #CBD5E1; -fx-border-width: 1 0 0 0;");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        Label threadIcon = new Label("🔄 Background Monitor Thread:");
        threadIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        threadIcon.setTextFill(Color.web("#334155"));

        Label threadStatus = new Label(AvailabilityMonitorThread.getStatusMessage());
        threadStatus.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        threadStatus.setTextFill(Color.web("#475569"));

        statusBar.getChildren().addAll(threadIcon, threadStatus);
        view.setBottom(statusBar);
    }

    private VBox createDashboardCard(String tag, String title, String description, String btnText, String colorHex, Runnable action) {
        VBox card = new VBox(15);
        card.setPrefSize(260, 240);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 3); -fx-border-color: #E2E8F0; -fx-border-radius: 12;");
        card.setAlignment(Pos.TOP_LEFT);

        Label tagLabel = new Label(tag);
        tagLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        tagLabel.setTextFill(Color.web(colorHex));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web(UIUtils.TEXT_DARK));

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        descLabel.setTextFill(Color.web(UIUtils.TEXT_MUTED));
        VBox.setVgrow(descLabel, Priority.ALWAYS);

        Button btn = new Button(btnText);
        btn.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 16 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> action.run());

        card.getChildren().addAll(tagLabel, titleLabel, descLabel, btn);
        return card;
    }

    public BorderPane getView() {
        return view;
    }
}
