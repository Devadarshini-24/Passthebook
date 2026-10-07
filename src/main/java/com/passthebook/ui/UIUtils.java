package com.passthebook.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Utility helper for consistent, clean JavaFX UI components.
 */
public class UIUtils {

    public static final String PRIMARY_COLOR = "#2563EB";   // Royal Blue
    public static final String ACCENT_COLOR = "#1D4ED8";    // Darker Blue hover
    public static final String SUCCESS_COLOR = "#16A34A";   // Green
    public static final String DANGER_COLOR = "#DC2626";    // Red
    public static final String BG_LIGHT = "#F8FAFC";        // Off-white/Slate light
    public static final String CARD_BG = "#FFFFFF";         // White
    public static final String TEXT_DARK = "#1E293B";       // Dark slate
    public static final String TEXT_MUTED = "#64748B";      // Gray text

    public static Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        label.setTextFill(Color.web(TEXT_DARK));
        return label;
    }

    public static Label createSubHeaderLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        label.setTextFill(Color.web(TEXT_MUTED));
        return label;
    }

    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 16 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 16 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 16 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));
        return btn;
    }

    public static Button createSuccessButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + SUCCESS_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12 6 12; -fx-background-radius: 5; -fx-cursor: hand;");
        return btn;
    }

    public static Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12 6 12; -fx-background-radius: 5; -fx-cursor: hand;");
        return btn;
    }

    public static Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 16 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    public static HBox createTopNavigation(Runnable onDashboard, Runnable onOwner, Runnable onSeeker, Runnable onHistory, Runnable onLogout, String activeScreen) {
        HBox navbar = new HBox(15);
        navbar.setPadding(new Insets(12, 20, 12, 20));
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setStyle("-fx-background-color: #1E293B;");

        Label brand = new Label("📖 PassTheBook");
        brand.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        brand.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDash = createNavBtn("Dashboard", "Dashboard".equalsIgnoreCase(activeScreen), onDashboard);
        Button btnOwner = createNavBtn("Owner Mode", "Owner".equalsIgnoreCase(activeScreen), onOwner);
        Button btnSeeker = createNavBtn("Seeker Mode", "Seeker".equalsIgnoreCase(activeScreen), onSeeker);
        Button btnHistory = createNavBtn("My Requests & History", "History".equalsIgnoreCase(activeScreen), onHistory);
        Button btnLogout = createNavBtn("Logout", false, onLogout);
        btnLogout.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");

        navbar.getChildren().addAll(brand, spacer, btnDash, btnOwner, btnSeeker, btnHistory, btnLogout);
        return navbar;
    }

    private static Button createNavBtn(String text, boolean isActive, Runnable action) {
        Button btn = new Button(text);
        if (isActive) {
            btn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-font-size: 13px; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-font-size: 13px; -fx-cursor: hand;"));
        }
        btn.setOnAction(e -> action.run());
        return btn;
    }

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
