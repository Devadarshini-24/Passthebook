package com.passthebook.exception;

/**
 * Custom checked exception thrown when book price is invalid for the listing type.
 * (e.g. negative price for Sell/Lend, or non-zero price entered for Free book).
 */
public class InvalidPriceException extends Exception {
    public InvalidPriceException(String message) {
        super(message);
    }
}
