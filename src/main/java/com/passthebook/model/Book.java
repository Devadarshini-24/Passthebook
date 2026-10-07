package com.passthebook.model;

import com.passthebook.exception.InvalidBookException;
import com.passthebook.exception.InvalidPriceException;

/**
 * Represents a book in the PassTheBook system.
 * Implements Cloneable to allow duplicating a book listing when an owner has multiple copies.
 */
public class Book implements Cloneable {
    private int id;
    private int ownerId;
    private String ownerUsername;
    private String name;
    private String author;
    private String isbn;
    private double price;
    private String condition;
    private String type; // "Sell", "Lend", "Free"
    private boolean available;

    public Book() {}

    public Book(int id, int ownerId, String ownerUsername, String name, String author, 
                String isbn, double price, String condition, String type, boolean available) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.name = name;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.condition = condition;
        this.type = type;
        this.available = available;
    }

    /**
     * Validates book input fields.
     * Throws InvalidBookException if name, author, or isbn are empty.
     * Throws InvalidPriceException if price is negative or invalid.
     */
    public static void validate(String name, String author, String isbn, String type, double price) 
            throws InvalidBookException, InvalidPriceException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidBookException("Book name cannot be empty.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new InvalidBookException("Author name cannot be empty.");
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new InvalidBookException("ISBN cannot be empty.");
        }
        if ("Free".equalsIgnoreCase(type)) {
            if (price != 0) {
                throw new InvalidPriceException("Free books must have a price of 0.0.");
            }
        } else {
            if (price < 0) {
                throw new InvalidPriceException("Price cannot be negative.");
            }
        }
    }

    /**
     * Java Cloneable implementation: Creates a duplicate of this book listing.
     * Resets the ID to 0 so it can be saved as a new listing in the database.
     */
    @Override
    public Book clone() {
        try {
            Book copy = (Book) super.clone();
            copy.id = 0; // reset ID for new DB record insertion
            copy.available = true; // duplicated copy starts as available
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloning not supported for Book", e);
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
