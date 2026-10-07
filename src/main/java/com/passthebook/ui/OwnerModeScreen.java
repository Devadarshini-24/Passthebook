package com.passthebook.ui;

import com.passthebook.exception.InvalidBookException;
import com.passthebook.exception.InvalidPriceException;
import com.passthebook.model.Book;
import com.passthebook.model.Request;
import com.passthebook.model.User;
import com.passthebook.service.AuthService;
import com.passthebook.service.BookService;
import com.passthebook.service.CatalogReader;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Screen 3: Owner Mode Screen.
 * 
 * Features:
 * 1. Add book (Sell, Lend, Free - price auto 0 for Free)
 * 2. Custom exceptions: InvalidBookException, InvalidPriceException
 * 3. Cloneable: Duplicate book listing when owner has multiple copies
 * 4. BufferedReader: Quick import from catalog.txt
 * 5. Manage Requests: Accept (with PriorityQueue order) or Reject incoming requests.
 */
public class OwnerModeScreen {
    private BorderPane view;

    private TextField nameField;
    private TextField authorField;
    private TextField isbnField;
    private ComboBox<String> typeCombo;
    private TextField priceField;
    private ComboBox<String> conditionCombo;

    private TableView<Book> booksTable;
    private TableView<Request> requestsTable;

    public OwnerModeScreen(Runnable onDashboard, Runnable onSeeker, Runnable onHistory, Runnable onLogout) {
        view = new BorderPane();
        view.setStyle("-fx-background-color: " + UIUtils.BG_LIGHT + ";");

        // Top Navigation
        HBox navbar = UIUtils.createTopNavigation(
            onDashboard, () -> {}, onSeeker, onHistory, onLogout, "Owner"
        );
        view.setTop(navbar);

        // Center Split Layout
        HBox centerBox = new HBox(20);
        centerBox.setPadding(new Insets(20));

        VBox formBox = createAddBookForm();
        VBox rightTabsBox = createRightTabs();
        HBox.setHgrow(rightTabsBox, Priority.ALWAYS);

        centerBox.getChildren().addAll(formBox, rightTabsBox);
        view.setCenter(centerBox);

        refreshData();
    }

    private VBox createAddBookForm() {
        VBox formCard = new VBox(14);
        formCard.setPrefWidth(350);
        formCard.setPadding(new Insets(20));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        Label formTitle = UIUtils.createHeaderLabel("➕ List a Book");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameField = new TextField();
        nameField.setPromptText("Book Title (e.g. Effective Java)");

        authorField = new TextField();
        authorField.setPromptText("Author (e.g. Joshua Bloch)");

        isbnField = new TextField();
        isbnField.setPromptText("ISBN Number");

        typeCombo = new ComboBox<>(FXCollections.observableArrayList("Sell", "Lend", "Free"));
        typeCombo.setValue("Sell");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        priceField = new TextField();
        priceField.setPromptText("Price (INR)");

        // Listener for TypeCombo: Automatically set price to 0 and disable when Free is selected
        typeCombo.setOnAction(e -> {
            if ("Free".equalsIgnoreCase(typeCombo.getValue())) {
                priceField.setText("0.0");
                priceField.setDisable(true);
            } else {
                priceField.setDisable(false);
            }
        });

        conditionCombo = new ComboBox<>(FXCollections.observableArrayList("New", "Like New", "Good", "Fair"));
        conditionCombo.setValue("Good");
        conditionCombo.setMaxWidth(Double.MAX_VALUE);

        Button btnAdd = UIUtils.createPrimaryButton("Post Book Listing");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setOnAction(e -> handleAddBook());

        // BufferedReader requirement action: Import preset catalog from catalog.txt
        Button btnCatalog = UIUtils.createSecondaryButton("📖 Import from Catalog (BufferedReader)");
        btnCatalog.setMaxWidth(Double.MAX_VALUE);
        btnCatalog.setOnAction(e -> showCatalogImportDialog());

        formCard.getChildren().addAll(
            formTitle,
            new Label("Book Name *"), nameField,
            new Label("Author *"), authorField,
            new Label("ISBN *"), isbnField,
            new Label("Listing Type *"), typeCombo,
            new Label("Price *"), priceField,
            new Label("Condition *"), conditionCombo,
            btnAdd,
            btnCatalog
        );

        return formCard;
    }

    private VBox createRightTabs() {
        VBox container = new VBox();
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // --- TAB 1: My Listed Books ---
        Tab booksTab = new Tab("My Listed Books");
        VBox booksBox = new VBox(10);
        booksBox.setPadding(new Insets(15));

        booksTable = new TableView<>();
        setupBooksTable();
        VBox.setVgrow(booksTable, Priority.ALWAYS);

        booksBox.getChildren().addAll(new Label("Your Active & Past Book Listings:"), booksTable);
        booksTab.setContent(booksBox);

        // --- TAB 2: Incoming Requests ---
        Tab requestsTab = new Tab("Pending Requests on My Books");
        VBox requestsBox = new VBox(10);
        requestsBox.setPadding(new Insets(15));

        requestsTable = new TableView<>();
        setupRequestsTable();
        VBox.setVgrow(requestsTable, Priority.ALWAYS);

        Label pqNotice = new Label("💡 Note: Incoming requests are ordered by PriorityQueue (Earliest request gets priority).");
        pqNotice.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UIUtils.PRIMARY_COLOR + "; -fx-font-style: italic;");

        requestsBox.getChildren().addAll(pqNotice, requestsTable);
        requestsTab.setContent(requestsBox);

        tabPane.getTabs().addAll(booksTab, requestsTab);
        container.getChildren().add(tabPane);
        return container;
    }

    @SuppressWarnings("unchecked")
    private void setupBooksTable() {
        TableColumn<Book, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        idCol.setPrefWidth(40);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        titleCol.setPrefWidth(160);

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        authorCol.setPrefWidth(120);

        TableColumn<Book, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        typeCol.setPrefWidth(70);

        TableColumn<Book, Double> priceCol = new TableColumn<>("Price (₹)");
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        priceCol.setPrefWidth(70);

        TableColumn<Book, String> condCol = new TableColumn<>("Condition");
        condCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCondition()));
        condCol.setPrefWidth(80);

        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isAvailable() ? "Available" : "Taken"));
        statusCol.setPrefWidth(80);

        // Action Column for Cloneable requirement: Duplicate listing button
        TableColumn<Book, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnClone = new Button("👯 Clone Copy");
            {
                btnClone.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnClone.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    handleCloneBook(book.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnClone);
                }
            }
        });

        booksTable.getColumns().addAll(idCol, titleCol, authorCol, typeCol, priceCol, condCol, statusCol, actionCol);
    }

    @SuppressWarnings("unchecked")
    private void setupRequestsTable() {
        TableColumn<Request, String> titleCol = new TableColumn<>("Book Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookTitle()));
        titleCol.setPrefWidth(150);

        TableColumn<Request, String> seekerCol = new TableColumn<>("Seeker");
        seekerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSeekerUsername()));
        seekerCol.setPrefWidth(100);

        TableColumn<Request, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        typeCol.setPrefWidth(70);

        TableColumn<Request, Double> priceCol = new TableColumn<>("Price (₹)");
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        priceCol.setPrefWidth(70);

        TableColumn<Request, String> dateCol = new TableColumn<>("Request Time");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDate()));
        dateCol.setPrefWidth(140);

        TableColumn<Request, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(160);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnAccept = UIUtils.createSuccessButton("Accept");
            private final Button btnReject = UIUtils.createDangerButton("Reject");
            private final HBox box = new HBox(6, btnAccept, btnReject);

            {
                btnAccept.setOnAction(e -> {
                    Request req = getTableView().getItems().get(getIndex());
                    handleAcceptRequest(req.getId());
                });
                btnReject.setOnAction(e -> {
                    Request req = getTableView().getItems().get(getIndex());
                    handleRejectRequest(req.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });

        requestsTable.getColumns().addAll(titleCol, seekerCol, typeCol, priceCol, dateCol, actionCol);
    }

    private void handleAddBook() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        try {
            String name = nameField.getText();
            String author = authorField.getText();
            String isbn = isbnField.getText();
            String type = typeCombo.getValue();
            String condition = conditionCombo.getValue();

            double price = 0.0;
            if (!"Free".equalsIgnoreCase(type)) {
                try {
                    price = Double.parseDouble(priceField.getText().trim());
                } catch (NumberFormatException nfe) {
                    throw new InvalidPriceException("Price must be a valid numeric value.");
                }
            }

            Book book = new Book(0, user.getId(), user.getUsername(), name, author, isbn, price, condition, type, true);

            // Calls BookService.addBook which triggers custom exception validation
            BookService.addBook(book);

            UIUtils.showAlert(Alert.AlertType.INFORMATION, "Success", "Book listed successfully!");
            clearForm();
            refreshData();

        } catch (InvalidBookException | InvalidPriceException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
        } catch (Exception e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to add book: " + e.getMessage());
        }
    }

    /**
     * Demonstrates Cloneable requirement: Copies an existing book listing.
     */
    private void handleCloneBook(int bookId) {
        try {
            BookService.cloneAndAddBook(bookId);
            UIUtils.showAlert(Alert.AlertType.INFORMATION, "Clone Successful", "Book listing duplicated using Java Cloneable interface!");
            refreshData();
        } catch (Exception e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Cloning failed: " + e.getMessage());
        }
    }

    /**
     * Demonstrates PriorityQueue requirement when accepting request.
     */
    private void handleAcceptRequest(int requestId) {
        try {
            BookService.acceptRequest(requestId);
            UIUtils.showAlert(Alert.AlertType.INFORMATION, "Request Accepted", "Request accepted! Priority assigned via PriorityQueue. Transaction record created and book marked unavailable.");
            refreshData();
        } catch (Exception e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to accept request: " + e.getMessage());
        }
    }

    private void handleRejectRequest(int requestId) {
        try {
            BookService.rejectRequest(requestId);
            UIUtils.showAlert(Alert.AlertType.INFORMATION, "Request Rejected", "Request rejected.");
            refreshData();
        } catch (Exception e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject request: " + e.getMessage());
        }
    }

    /**
     * Dialog showing catalogue books loaded via BufferedReader from catalog.txt
     */
    private void showCatalogImportDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Import Book from Catalog (BufferedReader)");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white;");

        Label label = new Label("Select a catalogue book loaded via BufferedReader from catalog.txt:");
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        ListView<String> listView = new ListView<>();
        ArrayList<Book> catalogBooks = CatalogReader.loadCatalog();

        for (Book b : catalogBooks) {
            listView.getItems().add(b.getName() + " by " + b.getAuthor() + " [ISBN: " + b.getIsbn() + "] (" + b.getType() + " - ₹" + b.getPrice() + ")");
        }

        Button btnUse = UIUtils.createPrimaryButton("Auto-Fill Form");
        btnUse.setOnAction(e -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < catalogBooks.size()) {
                Book selected = catalogBooks.get(selectedIndex);
                nameField.setText(selected.getName());
                authorField.setText(selected.getAuthor());
                isbnField.setText(selected.getIsbn());
                typeCombo.setValue(selected.getType());
                priceField.setText(String.valueOf(selected.getPrice()));
                conditionCombo.setValue(selected.getCondition());
                dialog.close();
            }
        });

        layout.getChildren().addAll(label, listView, btnUse);
        Scene scene = new Scene(layout, 500, 350);
        dialog.setScene(scene);
        dialog.show();
    }

    private void clearForm() {
        nameField.clear();
        authorField.clear();
        isbnField.clear();
        priceField.clear();
        typeCombo.setValue("Sell");
        conditionCombo.setValue("Good");
        priceField.setDisable(false);
    }

    private void refreshData() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        // Fetch owner books
        ArrayList<Book> myBooks = BookService.getOwnerBooks(user.getId());
        booksTable.setItems(FXCollections.observableArrayList(myBooks));

        // Fetch pending requests for owner
        ArrayList<Request> pendingReqs = BookService.getPendingRequestsForOwner(user.getId());
        requestsTable.setItems(FXCollections.observableArrayList(pendingReqs));
    }

    public BorderPane getView() {
        return view;
    }
}
