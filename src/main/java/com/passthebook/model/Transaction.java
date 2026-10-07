package com.passthebook.model;

/**
 * Represents a completed transaction when an owner accepts a request for a book.
 */
public class Transaction {
    private int id;
    private int requestId;
    private int bookId;
    private String bookTitle;
    private int sellerId;
    private String sellerUsername;
    private int buyerId;
    private String buyerUsername;
    private String type; // Sell, Lend, Free
    private double price;
    private String date;
    private String status; // COMPLETED

    public Transaction() {}

    public Transaction(int id, int requestId, int bookId, String bookTitle, int sellerId, 
                       String sellerUsername, int buyerId, String buyerUsername, 
                       String type, double price, String date, String status) {
        this.id = id;
        this.requestId = requestId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.sellerId = sellerId;
        this.sellerUsername = sellerUsername;
        this.buyerId = buyerId;
        this.buyerUsername = buyerUsername;
        this.type = type;
        this.price = price;
        this.date = date;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public String getBuyerUsername() { return buyerUsername; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
