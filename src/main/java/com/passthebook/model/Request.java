package com.passthebook.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a seeker's request for a book.
 * Implements Comparable so PriorityQueue orders requests by earliest requestTime first.
 */
public class Request implements Comparable<Request> {
    private int id;
    private int bookId;
    private String bookTitle;
    private int seekerId;
    private String seekerUsername;
    private int ownerId;
    private String type; // Sell, Lend, Free
    private double price;
    private long requestTime; // Timestamp in milliseconds
    private String status; // PENDING, ACCEPTED, REJECTED

    public Request() {}

    public Request(int id, int bookId, String bookTitle, int seekerId, String seekerUsername, 
                   int ownerId, String type, double price, long requestTime, String status) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.seekerId = seekerId;
        this.seekerUsername = seekerUsername;
        this.ownerId = ownerId;
        this.type = type;
        this.price = price;
        this.requestTime = requestTime;
        this.status = status;
    }

    /**
     * PriorityQueue requirement: Earliest request time gets priority (natural order ascending).
     */
    @Override
    public int compareTo(Request other) {
        return Long.compare(this.requestTime, other.requestTime);
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(requestTime));
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public int getSeekerId() { return seekerId; }
    public void setSeekerId(int seekerId) { this.seekerId = seekerId; }

    public String getSeekerUsername() { return seekerUsername; }
    public void setSeekerUsername(String seekerUsername) { this.seekerUsername = seekerUsername; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public long getRequestTime() { return requestTime; }
    public void setRequestTime(long requestTime) { this.requestTime = requestTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
