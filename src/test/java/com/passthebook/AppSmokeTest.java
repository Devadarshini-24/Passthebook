package com.passthebook;

import com.passthebook.db.DatabaseHandler;
import com.passthebook.exception.InvalidBookException;
import com.passthebook.exception.InvalidPriceException;
import com.passthebook.model.Book;
import com.passthebook.model.Request;
import com.passthebook.model.Transaction;
import com.passthebook.model.User;
import com.passthebook.service.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.ArrayList;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppSmokeTest {

    @BeforeAll
    public static void setUpDB() {
        File dbFile = new File("passthebook.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        DatabaseHandler.initializeDatabase();
    }

    @Test
    @Order(1)
    public void testUserRegistrationAndLogin() throws Exception {
        boolean reg1 = AuthService.register("owner_user", "pass123", "Owner Alice");
        assertTrue(reg1);

        boolean reg2 = AuthService.register("seeker_bob", "pass123", "Seeker Bob");
        assertTrue(reg2);

        boolean reg3 = AuthService.register("seeker_charlie", "pass123", "Seeker Charlie");
        assertTrue(reg3);

        User loggedUser = AuthService.login("owner_user", "pass123");
        assertNotNull(loggedUser);
        assertEquals("Owner Alice", loggedUser.getFullName());
    }

    @Test
    @Order(2)
    public void testBufferedReaderCatalog() {
        ArrayList<Book> catalog = CatalogReader.loadCatalog();
        assertNotNull(catalog);
        assertFalse(catalog.isEmpty(), "Catalog loaded via BufferedReader should not be empty");
        System.out.println("BufferedReader loaded " + catalog.size() + " books from catalog.txt");
        assertEquals("Effective Java", catalog.get(0).getName());
    }

    @Test
    @Order(3)
    public void testCustomExceptions() {
        // Invalid book name
        assertThrows(InvalidBookException.class, () -> {
            Book.validate("", "Author", "12345", "Sell", 100.0);
        });

        // Invalid free price
        assertThrows(InvalidPriceException.class, () -> {
            Book.validate("Book", "Author", "12345", "Free", 50.0);
        });

        // Negative price
        assertThrows(InvalidPriceException.class, () -> {
            Book.validate("Book", "Author", "12345", "Sell", -10.0);
        });
    }

    @Test
    @Order(4)
    public void testCloneableBook() throws Exception {
        User owner = AuthService.login("owner_user", "pass123");
        Book original = new Book(0, owner.getId(), owner.getUsername(), "Data Structures", "Mark Allen", "978-0131103627", 300.0, "Good", "Sell", true);
        BookService.addBook(original);

        ArrayList<Book> ownerBooks = BookService.getOwnerBooks(owner.getId());
        assertFalse(ownerBooks.isEmpty());
        Book savedBook = ownerBooks.get(0);

        // Duplicate using Cloneable
        Book clonedBook = savedBook.clone();
        assertEquals(0, clonedBook.getId());
        assertEquals(savedBook.getName(), clonedBook.getName());
        assertEquals(savedBook.getIsbn(), clonedBook.getIsbn());

        BookService.cloneAndAddBook(savedBook.getId());
        ArrayList<Book> updatedOwnerBooks = BookService.getOwnerBooks(owner.getId());
        assertTrue(updatedOwnerBooks.size() >= 2, "Owner should now have duplicated book copies");
    }

    @Test
    @Order(5)
    public void testPriorityQueueAndTransactionFlow() throws Exception {
        User owner = AuthService.login("owner_user", "pass123");
        User bob = AuthService.login("seeker_bob", "pass123");
        User charlie = AuthService.login("seeker_charlie", "pass123");

        // List a new book for testing priority queue
        Book testBook = new Book(0, owner.getId(), owner.getUsername(), "Operating Systems", "Silberschatz", "978-1118063330", 250.0, "Like New", "Sell", true);
        BookService.addBook(testBook);

        ArrayList<Book> available = BookService.searchAvailableBooks("Operating Systems", "All", bob.getId());
        assertFalse(available.isEmpty());
        int bookId = available.get(0).getId();

        // 1. Bob requests at time T1
        BookService.requestBook(bookId, bob.getId());
        Thread.sleep(50); // slight time difference

        // 2. Charlie requests at time T2
        BookService.requestBook(bookId, charlie.getId());

        // Test PriorityQueue ordering
        PriorityQueue<Request> pq = BookService.getPriorityRequestQueueForBook(bookId);
        assertEquals(2, pq.size());

        Request earliestRequest = pq.peek();
        assertNotNull(earliestRequest);
        assertEquals(bob.getId(), earliestRequest.getSeekerId(), "Earliest request by Bob should get priority in PriorityQueue");

        // Owner accepts Bob's request
        BookService.acceptRequest(earliestRequest.getId());

        // Verify book is no longer available
        Book acceptedBook = BookService.getBookById(bookId);
        assertFalse(acceptedBook.isAvailable());

        // Verify transaction was created
        ArrayList<Transaction> bobTx = BookService.getTransactionHistory(bob.getId());
        assertFalse(bobTx.isEmpty());
        assertEquals("Operating Systems", bobTx.get(0).getBookTitle());
        assertEquals("COMPLETED", bobTx.get(0).getStatus());

        // Verify Charlie's request was marked REJECTED
        ArrayList<Request> charlieReqs = BookService.getSeekerRequests(charlie.getId());
        assertFalse(charlieReqs.isEmpty());
        assertEquals("REJECTED", charlieReqs.get(0).getStatus());
    }

    @Test
    @Order(6)
    public void testBackgroundThreadMonitor() throws Exception {
        AvailabilityMonitorThread thread = new AvailabilityMonitorThread();
        thread.start();
        Thread.sleep(500);
        String msg = AvailabilityMonitorThread.getStatusMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("Background"), "Background monitor thread should generate status message");
        thread.stopMonitoring();
    }
}
