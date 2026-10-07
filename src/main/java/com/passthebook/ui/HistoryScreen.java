package com.passthebook.ui;

import com.passthebook.model.Request;
import com.passthebook.model.Transaction;
import com.passthebook.model.User;
import com.passthebook.service.AuthService;
import com.passthebook.service.BookService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

/**
 * Screen 5: My Requests / Transaction History Screen.
 * Displays submitted requests status and completed transaction records.
 */
public class HistoryScreen {
    private BorderPane view;

    private TableView<Request> requestsTable;
    private TableView<Transaction> historyTable;

    public HistoryScreen(Runnable onDashboard, Runnable onOwner, Runnable onSeeker, Runnable onLogout) {
        view = new BorderPane();
        view.setStyle("-fx-background-color: " + UIUtils.BG_LIGHT + ";");

        // Top Navigation
        HBox navbar = UIUtils.createTopNavigation(
            onDashboard, onOwner, onSeeker, () -> {}, onLogout, "History"
        );
        view.setTop(navbar);

        // Center Content
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(20, 30, 20, 30));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // --- TAB 1: My Submitted Requests ---
        Tab reqTab = new Tab("My Submitted Requests");
        VBox reqBox = new VBox(10);
        reqBox.setPadding(new Insets(15));
        requestsTable = new TableView<>();
        setupRequestsTable();
        VBox.setVgrow(requestsTable, Priority.ALWAYS);
        reqBox.getChildren().addAll(new Label("Status of requests you have submitted for books:"), requestsTable);
        reqTab.setContent(reqBox);

        // --- TAB 2: Transaction History ---
        Tab historyTab = new Tab("Completed Transaction History");
        VBox historyBox = new VBox(10);
        historyBox.setPadding(new Insets(15));
        historyTable = new TableView<>();
        setupHistoryTable();
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        historyBox.getChildren().addAll(new Label("All completed book exchange transactions (Buy / Borrow / Free):"), historyTable);
        historyTab.setContent(historyBox);

        tabPane.getTabs().addAll(reqTab, historyTab);
        centerBox.getChildren().add(tabPane);
        view.setCenter(centerBox);

        refreshData();
    }

    @SuppressWarnings("unchecked")
    private void setupRequestsTable() {
        TableColumn<Request, Integer> idCol = new TableColumn<>("Request ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        idCol.setPrefWidth(80);

        TableColumn<Request, String> titleCol = new TableColumn<>("Book Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookTitle()));
        titleCol.setPrefWidth(200);

        TableColumn<Request, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        typeCol.setPrefWidth(90);

        TableColumn<Request, Double> priceCol = new TableColumn<>("Price (₹)");
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        priceCol.setPrefWidth(90);

        TableColumn<Request, String> dateCol = new TableColumn<>("Request Time");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDate()));
        dateCol.setPrefWidth(160);

        TableColumn<Request, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(110);

        // Highlight status colors
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ACCEPTED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                    } else if ("REJECTED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;"); // Pending (Amber)
                    }
                }
            }
        });

        requestsTable.getColumns().addAll(idCol, titleCol, typeCol, priceCol, dateCol, statusCol);
    }

    @SuppressWarnings("unchecked")
    private void setupHistoryTable() {
        TableColumn<Transaction, Integer> idCol = new TableColumn<>("Tx ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        idCol.setPrefWidth(70);

        TableColumn<Transaction, String> titleCol = new TableColumn<>("Book");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookTitle()));
        titleCol.setPrefWidth(200);

        TableColumn<Transaction, String> sellerCol = new TableColumn<>("Seller / Owner");
        sellerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSellerUsername()));
        sellerCol.setPrefWidth(120);

        TableColumn<Transaction, String> buyerCol = new TableColumn<>("Buyer / Seeker");
        buyerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBuyerUsername()));
        buyerCol.setPrefWidth(120);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        typeCol.setPrefWidth(90);

        TableColumn<Transaction, Double> priceCol = new TableColumn<>("Price (₹)");
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        priceCol.setPrefWidth(90);

        TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        dateCol.setPrefWidth(160);

        historyTable.getColumns().addAll(idCol, titleCol, sellerCol, buyerCol, typeCol, priceCol, statusCol, dateCol);
    }

    private void refreshData() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        ArrayList<Request> seekerReqs = BookService.getSeekerRequests(user.getId());
        requestsTable.setItems(FXCollections.observableArrayList(seekerReqs));

        ArrayList<Transaction> txList = BookService.getTransactionHistory(user.getId());
        historyTable.setItems(FXCollections.observableArrayList(txList));
    }

    public BorderPane getView() {
        return view;
    }
}
