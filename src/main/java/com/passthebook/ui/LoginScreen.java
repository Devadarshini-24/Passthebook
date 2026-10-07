package com.passthebook.ui;

import com.passthebook.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Screen 1: Login / Register Screen.
 */
public class LoginScreen {
    private VBox view;
    private Runnable onLoginSuccess;

    public LoginScreen(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        createUI();
    }

    private void createUI() {
        view = new VBox();
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(30));
        view.setStyle("-fx-background-color: " + UIUtils.BG_LIGHT + ";");

        // App Card Container
        VBox card = new VBox(20);
        card.setMaxWidth(420);
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");

        Label appTitle = new Label("📖 PassTheBook");
        appTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + UIUtils.PRIMARY_COLOR + ";");

        Label subtitle = new Label("Student Book-Sharing Platform (Java PBL)");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UIUtils.TEXT_MUTED + ";");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 12px;");

        // TabPane for Login and Register
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-tab-min-width: 150px;");

        // --- LOGIN TAB ---
        Tab loginTab = new Tab("Sign In");
        VBox loginBox = new VBox(15);
        loginBox.setPadding(new Insets(20, 10, 10, 10));

        TextField loginUserField = new TextField();
        loginUserField.setPromptText("Username");
        loginUserField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");

        PasswordField loginPassField = new PasswordField();
        loginPassField.setPromptText("Password");
        loginPassField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");

        Button btnLogin = UIUtils.createPrimaryButton("Sign In to PassTheBook");
        btnLogin.setMaxWidth(Double.MAX_VALUE);

        btnLogin.setOnAction(e -> {
            try {
                AuthService.login(loginUserField.getText(), loginPassField.getText());
                statusLabel.setTextFill(Color.web(UIUtils.SUCCESS_COLOR));
                statusLabel.setText("Login successful!");
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
            } catch (Exception ex) {
                statusLabel.setTextFill(Color.web(UIUtils.DANGER_COLOR));
                statusLabel.setText(ex.getMessage());
            }
        });

        loginBox.getChildren().addAll(
            new Label("Username"), loginUserField,
            new Label("Password"), loginPassField,
            btnLogin
        );
        loginTab.setContent(loginBox);

        // --- REGISTER TAB ---
        Tab regTab = new Tab("Create Account");
        VBox regBox = new VBox(15);
        regBox.setPadding(new Insets(20, 10, 10, 10));

        TextField regNameField = new TextField();
        regNameField.setPromptText("Full Name (e.g. Alex Smith)");
        regNameField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");

        TextField regUserField = new TextField();
        regUserField.setPromptText("Choose Username");
        regUserField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");

        PasswordField regPassField = new PasswordField();
        regPassField.setPromptText("Choose Password");
        regPassField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");

        Button btnRegister = UIUtils.createPrimaryButton("Register Account");
        btnRegister.setMaxWidth(Double.MAX_VALUE);

        btnRegister.setOnAction(e -> {
            try {
                AuthService.register(regUserField.getText(), regPassField.getText(), regNameField.getText());
                // Auto login upon registration
                AuthService.login(regUserField.getText(), regPassField.getText());
                statusLabel.setTextFill(Color.web(UIUtils.SUCCESS_COLOR));
                statusLabel.setText("Account registered successfully!");
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
            } catch (Exception ex) {
                statusLabel.setTextFill(Color.web(UIUtils.DANGER_COLOR));
                statusLabel.setText(ex.getMessage());
            }
        });

        regBox.getChildren().addAll(
            new Label("Full Name"), regNameField,
            new Label("Username"), regUserField,
            new Label("Password"), regPassField,
            btnRegister
        );
        regTab.setContent(regBox);

        tabPane.getTabs().addAll(loginTab, regTab);

        card.getChildren().addAll(appTitle, subtitle, tabPane, statusLabel);
        view.getChildren().add(card);
    }

    public VBox getView() {
        return view;
    }
}
