package com.passthebook.ui;

import com.passthebook.model.Book;
import com.passthebook.model.User;
import com.passthebook.service.AuthService;
import com.passthebook.service.BookService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

/**
 * Screen 4: Seeker Mode Screen.
 * Allows users to search available books, filter by type (Sell/Lend/Free), and request books.
 */
public class SeekerModeScreen {
    private BorderPane view;

    private TextField searchField;
    private ComboBox<String> filterCombo;
    private TableView<Book> availableBooksTable;

    public SeekerModeScreen(Runnable onDashboard, Runnable onOwner, Runnable onHistory, Runnable onLogout) {
        view = new BorderPane();
        view.setStyle("-fx-background-color: " + UIUtils.BG_LIGHT + ";");

        // Top Navigation
        HBox navbar = UIUtils.createTopNavigation(
            onDashboard, onOwner, () -> {}, onHistory, onLogout, "Seeker"
        );
        view.setTop(navbar);

        // Center Box
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20, 30, 20, 30));

        // Search & Filter Header Card
        VBox searchCard = createSearchCard();
        
        // Available Books Table
        VBox tableCard = createTableCard();
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        centerBox.getChildren().addAll(searchCard, tableCard);
        view.setCenter(centerBox);

        refreshAvailableBooks();
    }

    private VBox createSearchCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 6, 0, 0, 2);");

        Label title = UIUtils.createHeaderLabel("🔍 Find & Request Books");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox controlsBox = new HBox(12);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by book title, author, or ISBN...");
        searchField.setPrefWidth(320);
        searchField.setStyle("-fx-font-size: 13px; -fx-padding: 7 10 7 10;");
        searchField.setOnKeyReleased(e -> refreshAvailableBooks());

        filterCombo = new ComboBox<>(FXCollections.observableArrayList("All", "Sell", "Lend", "Free"));
        filterCombo.setValue("All");
        filterCombo.setStyle("-fx-font-size: 13px;");
        filterCombo.setOnAction(e -> refreshAvailableBooks());

        Button btnSearch = UIUtils.createPrimaryButton("Search");
        btnSearch.setOnAction(e -> refreshAvailableBooks());

        Button btnReset = UIUtils.createSecondaryButton("Reset Filters");
        btnReset.setOnAction(e -> {
            searchField.clear();
            filterCombo.setValue("All");
            refreshAvailableBooks();
        });

        controlsBox.getChildren().addAll(
            new Label("Search:"), searchField,
            new Label("Type Filter:"), filterCombo,
            btnSearch, btnReset
        );

        card.getChildren().addAll(title, controlsBox);
        return card;
    }

    private VBox createTableCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 6, 0, 0, 2);");

        availableBooksTable = new TableView<>();
        setupTableColumns();
        VBox.setVgrow(availableBooksTable, Priority.ALWAYS);

        card.getChildren().addAll(new Label("Available Books for Request:"), availableBooksTable);
        return card;
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        TableColumn<Book, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        idCol.setPrefWidth(40);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        titleCol.setPrefWidth(180);

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        authorCol.setPrefWidth(140);

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIsbn()));
        isbnCol.setPrefWidth(120);

        TableColumn<Book, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        typeCol.setPrefWidth(80);

        TableColumn<Book, Double> priceCol = new TableColumn<>("Price (₹)");
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        priceCol.setPrefWidth(80);

        TableColumn<Book, String> condCol = new TableColumn<>("Condition");
        condCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCondition()));
        condCol.setPrefWidth(90);

        TableColumn<Book, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOwnerUsername()));
        ownerCol.setPrefWidth(110);

        TableColumn<Book, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(130);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnRequest = UIUtils.createPrimaryButton("Request Book");
            {
                btnRequest.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
                btnRequest.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    handleRequestBook(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnRequest);
                }
            }
        });

        availableBooksTable.getColumns().addAll(idCol, titleCol, authorCol, isbnCol, typeCol, priceCol, condCol, ownerCol, actionCol);
    }

    private void handleRequestBook(Book book) {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        try {
            BookService.requestBook(book.getId(), user.getId());
            UIUtils.showAlert(Alert.AlertType.INFORMATION, "Request Submitted", 
                "Your request for '" + book.getName() + "' has been submitted to owner @" + book.getOwnerUsername() + "!\n" +
                "Earliest requests get priority via PriorityQueue.");
            refreshAvailableBooks();
        } catch (Exception e) {
            UIUtils.showAlert(Alert.AlertType.WARNING, "Request Notice", e.getMessage());
        }
    }

    private void refreshAvailableBooks() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        String query = searchField.getText();
        String typeFilter = filterCombo.getValue();

        ArrayList<Book> list = BookService.searchAvailableBooks(query, typeFilter, user.getId());
        availableBooksTable.setItems(FXCollections.observableArrayList(list));
    }

    public BorderPane getView() {
        return view;
    }
}
