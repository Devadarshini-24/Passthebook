package com.passthebook.exception;

/**
 * Custom checked exception thrown when book details (name, author, ISBN, condition) are invalid.
 */
public class InvalidBookException extends Exception {
    public InvalidBookException(String message) {
        super(message);
    }
}
