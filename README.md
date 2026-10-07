# 📖 PassTheBook - Student Book Sharing Platform (Java PBL)

**PassTheBook** is a lightweight, clean, student-friendly JavaFX application built for a college Java Problem-Based Learning (PBL) curriculum. It provides a local peer-to-peer book-sharing platform where college students can either offer books they own (**Owner Mode**) or search for and request books from other students (**Seeker Mode**).

---

## 📌 Table of Contents
1. [Project Abstract & Overview](#-project-abstract--overview)
2. [Complete System Workflow & Architecture](#-complete-system-workflow--architecture)
3. [📁 Project File Structure](#-project-file-structure)
4. [🖼️ UI Screen Screenshots & Tab Layouts](#️-ui-screen-screenshots--tab-layouts)
5. [📚 Syllabus Java Concepts Mapping](#-syllabus-java-concepts-mapping)
6. [🗄️ Database Architecture & Schema](#-database-architecture--schema)
7. [🚀 How to Build & Run the Application](#-how-to-build--run-the-application)
8. [🧪 Verification & Testing](#-verification--testing)

---

## 🎯 Project Abstract & Overview

College students often buy expensive academic textbooks that sit unused after a single semester. **PassTheBook** solves this problem by connecting students within a campus to exchange books through three simple transaction types:
- **Sell**: List books for purchase at a student-friendly price.
- **Lend**: Borrow books temporarily for a fee or semester duration.
- **Free**: Give away books free of charge (price locked automatically to ₹0.0).

The application strictly prioritizes simplicity, maintainability, and clean software engineering principles. It features custom checked exception handling, Object-Oriented Domain models, thread safety, priority queue-based request dispatching, and persistent SQLite database integration via JDBC.

---

## 🔄 Complete System Workflow & Architecture

### End-to-End User Flow Diagram

```text
                     ┌──────────────────────────┐
                     │     1. User Login /      │
                     │       Registration       │
                     └─────────────┬────────────┘
                                   │
                                   ▼
                     ┌──────────────────────────┐
                     │   2. System Dashboard    │
                     │  (Select Mode / View     │
                     │    Background Status)    │
                     └──────┬────────────┬──────┘
                            │            │
             ┌──────────────┘            └──────────────┐
             ▼                                          ▼
┌──────────────────────────┐               ┌──────────────────────────┐
│     3. OWNER MODE        │               │     4. SEEKER MODE       │
├──────────────────────────┤               ├──────────────────────────┤
│ • Add Book (Sell/Lend/   │               │ • Search by Title/Author │
│   Free - Price Auto 0)   │               │ • Filter Sell/Lend/Free  │
│ • Import via             │               │ • Request Available Book │
│   BufferedReader         │               └────────────┬─────────────┘
│ • Clone Listing          │                            │
│   (Cloneable interface)  │                            │ (Request stored with
│ • View Incoming Requests │                            │  timestamp)
└────────────┬─────────────┘                            │
             │                                          │
             └──────────────────┬───────────────────────┘
                                │
                                ▼
                   ┌──────────────────────────┐
                   │  5. Request Processing   │
                   ├──────────────────────────┤
                   │ • PriorityQueue ranks    │
                   │   earliest request time  │
                   │ • Owner Accepts/Rejects  │
                   └────────────┬─────────────┘
                                │
                                ▼
                   ┌──────────────────────────┐
                   │  6. Transaction Created  │
                   ├──────────────────────────┤
                   │ • Book marked Taken      │
                   │ • Transaction record     │
                   │   saved to SQLite        │
                   │ • Status: COMPLETED      │
                   └────────────┬─────────────┘
                                │
                                ▼
                   ┌──────────────────────────┐
                   │  7. History & Requests   │
                   │   View My Requests & Tx  │
                   └──────────────────────────┘
```

---

## 📁 Project File Structure

```text
PassTheBook/
├── run.bat                          # One-click Windows application launcher
├── pom.xml                          # Maven dependencies & build configuration
├── catalog.txt                      # Book catalogue text file (parsed via BufferedReader)
├── passthebook.db                   # SQLite database (auto-generated at runtime)
├── README.md                        # Project documentation & syllabus mapping
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/passthebook/
    │   │       ├── Main.java                        # JavaFX Application entry point
    │   │       ├── db/
    │   │       │   └── DatabaseHandler.java         # SQLite JDBC connection & tables setup
    │   │       ├── exception/
    │   │       │   ├── InvalidBookException.java    # Custom checked exception for book inputs
    │   │       │   └── InvalidPriceException.java   # Custom checked exception for price inputs
    │   │       ├── model/
    │   │       │   ├── User.java                    # User domain model (OOP)
    │   │       │   ├── Book.java                    # Book domain model (OOP & Cloneable)
    │   │       │   ├── Request.java                 # Request model (OOP & PriorityQueue Comparable)
    │   │       │   └── Transaction.java             # Transaction domain model (OOP)
    │   │       ├── service/
    │   │       │   ├── AuthService.java             # Registration & login service
    │   │       │   ├── BookService.java             # Business logic (ArrayList, PriorityQueue, JDBC)
    │   │       │   ├── CatalogReader.java           # BufferedReader catalog file parser
    │   │       │   └── AvailabilityMonitorThread.java # Background daemon thread for live status
    │   │       └── ui/
    │   │           ├── UIUtils.java                 # Styling helpers & navigation bar
    │   │           ├── LoginScreen.java             # Screen 1: Authentication UI
    │   │           ├── DashboardScreen.java         # Screen 2: Mode Selection & Status Bar
    │   │           ├── OwnerModeScreen.java         # Screen 3: List, Clone, Catalog Import & Requests
    │   │           ├── SeekerModeScreen.java        # Screen 4: Search, Filter & Request Books
    │   │           └── HistoryScreen.java           # Screen 5: Submitted Requests & Transaction Log
    │   └── resources/
    │       └── catalog.txt                          # Classpath fallback copy of book catalog
    └── test/
        └── java/
            └── com/passthebook/
                └── AppSmokeTest.java                # Automated JUnit 5 test suite verifying all concepts
```

---

## 🖼️ UI Screen Screenshots & Tab Layouts

### Screenshot 1: Login & Registration Screen
**Title**: `Screen 1: Authentication View (Login & Registration Tabs)`
**Source File**: [`LoginScreen.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/ui/LoginScreen.java)

```text
+-------------------------------------------------------------------------+
|                                                                         |
|                       📖 PassTheBook                                    |
|              Student Book-Sharing Platform (Java PBL)                    |
|                                                                         |
|         +---------------------------+---------------------------+       |
|         |     [ Sign In ]           |    Create Account         |       |
|         +---------------------------+---------------------------+       |
|         |                                                       |       |
|         |  Username: [ alex_smith                              ] |       |
|         |  Password: [ **********                              ] |       |
|         |                                                       |       |
|         |  [           Sign In to PassTheBook                  ] |       |
|         |                                                       |       |
|         +-------------------------------------------------------+       |
|                                                                         |
+-------------------------------------------------------------------------+
```
- **Description**: Features a TabPane allowing students to either Sign In with existing credentials or register a new account. Includes dynamic feedback label for invalid logins or existing username errors.

---

### Screenshot 2: Main Dashboard & Status Bar
**Title**: `Screen 2: Main Student Dashboard & Live Thread Status Bar`
**Source File**: [`DashboardScreen.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/ui/DashboardScreen.java)

```text
+-------------------------------------------------------------------------+
| 📖 PassTheBook | Dashboard | Owner Mode | Seeker Mode | History | Logout|
+-------------------------------------------------------------------------+
|                                                                         |
|                Welcome to PassTheBook, Alex Smith (@alex)!              |
|                     Please select a mode to continue:                   |
|                                                                         |
|  +---------------------+  +---------------------+  +-----------------+  |
|  | 📚 OWNER MODE       |  | 🔍 SEEKER MODE      |  | 📋 MY HISTORY   |  |
|  | Offer & List Books  |  | Find & Request Books|  | Transactions    |  |
|  | [Enter Owner Mode]  |  | [Enter Seeker Mode] |  | [View History]  |  |
|  +---------------------+  +---------------------+  +-----------------+  |
|                                                                         |
+-------------------------------------------------------------------------+
| 🔄 Background Monitor Thread: 5 book(s) currently available for seekers. |
+-------------------------------------------------------------------------+
```
- **Description**: Central navigation panel providing access to Owner Mode, Seeker Mode, and History. The bottom bar displays real-time updates from `AvailabilityMonitorThread`.

---

### Screenshot 3A: Owner Mode - Book Listing & Clone Tab
**Title**: `Screen 3A: Owner Mode - Listing Form & Active Books Table`
**Source File**: [`OwnerModeScreen.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/ui/OwnerModeScreen.java)

```text
+-------------------------------------------------------------------------+
| 📖 PassTheBook | Dashboard | [Owner Mode] | Seeker Mode | History |Logout|
+-------------------------------------------------------------------------+
|  ➕ List a Book         | [ My Listed Books ] |  Pending Requests       |
|  Title:  [Effective Java]| +----+--------------------+------+-------+----+|
|  Author: [Joshua Bloch ]| | ID | Title              | Type | Price |Act ||
|  ISBN:   [978-013468...]| +----+--------------------+------+-------+----+|
|  Type:   [ Sell   v ]   | | 1  | Effective Java     | Sell | ₹450  |Clone||
|  Price:  [ 450.00      ]| | 2  | Clean Code         | Free | ₹0.0  |Clone||
|  Cond:   [ Good   v ]   | +----+--------------------+------+-------+----+|
|  [ Post Book Listing ]  |                                               |
|  [ Import via Catalog]  |                                               |
+-------------------------------------------------------------------------+
```
- **Description**: Allows book owners to add listings (validating prices automatically for `Free` books), import catalogue entries using `BufferedReader`, and duplicate listings using the `Cloneable` pattern button (`👯 Clone Copy`).

---

### Screenshot 3B: Owner Mode - Incoming Requests Queue Tab
**Title**: `Screen 3B: Owner Mode - Incoming Pending Requests Queue`
**Source File**: [`OwnerModeScreen.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/ui/OwnerModeScreen.java#L110-L135)

```text
+-------------------------------------------------------------------------+
|  💡 Note: Requests are ordered by PriorityQueue (Earliest gets priority)|
+----+---------------------+------------+------+-------+------------------+
| ID | Book Title          | Seeker     | Type | Price | Action           |
+----+---------------------+------------+------+-------+------------------+
| 1  | Effective Java      | @bob_jones | Sell | ₹450  | [Accept] [Reject]|
| 2  | Effective Java      | @charlie_b | Sell | ₹450  | [Accept] [Reject]|
+----+---------------------+------------+------+-------+------------------+
```
- **Description**: Displays incoming seeker requests ranked in chronological order using `PriorityQueue`. Accepting a request automatically creates a transaction and rejects subsequent pending requests.

---

### Screenshot 4: Seeker Mode - Search & Request Screen
**Title**: `Screen 4: Seeker Mode - Available Books Search & Request View`
**Source File**: [`SeekerModeScreen.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/ui/SeekerModeScreen.java)

```text
+-------------------------------------------------------------------------+
| 🔍 Find & Request Books                                                 |
| Search: [ Effective        ] Filter: [ All v ] [ Search ] [ Reset ]     |
+----+------------------------+--------------+------+-------+-------------+
| ID | Title                  | Author       | Type | Price | Action      |
+----+------------------------+--------------+------+-------+-------------+
| 10 | Head First Java        | Kathy Sierra | Lend | ₹80.0 |[Request Book|
| 12 | Design Patterns        | Erich Gamma  | Sell | ₹600  |[Request Book|
+----+------------------------+--------------+------+-------+-------------+
```
- **Description**: Enables seekers to search for books by title or author and filter by `Sell`, `Lend`, or `Free`. Clicking `Request Book` logs a timestamped request in SQLite.

---

### Screenshot 5A: My Requests Tab (Seeker View)
**Title**: `Screen 5A: My Requests - Seeker Request Status Tracking`
**Source File**: [`HistoryScreen.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/ui/HistoryScreen.java#L40-L65)

```text
+-------------------------------------------------------------------------+
| [ My Submitted Requests ]  |   Completed Transaction History            |
+----+------------------------+-----------+------+-------+----------------+
| ID | Book Title             | Owner     | Type | Price | Status         |
+----+------------------------+-----------+------+-------+----------------+
| 1  | Effective Java         | @alice_s  | Sell | ₹450  | ACCEPTED (Green|
| 2  | Clean Code             | @john_d   | Free | ₹0.0  | PENDING  (Amber|
+----+------------------------+-----------+------+-------+----------------+
```
- **Description**: Displays all requests submitted by the logged-in seeker along with real-time status badges (`PENDING`, `ACCEPTED`, `REJECTED`).

---

### Screenshot 5B: Transaction History Tab
**Title**: `Screen 5B: Transaction History - Completed Exchange Records`
**Source File**: [`HistoryScreen.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/ui/HistoryScreen.java#L66-L90)

```text
+-------------------------------------------------------------------------+
|   My Submitted Requests    | [ Completed Transaction History ]          |
+-----+------------------------+----------+---------+------+-----+--------+
| TxID| Book                   | Seller   | Buyer   | Type |Price| Status |
+-----+------------------------+----------+---------+------+-----+--------+
| 101 | Effective Java         | @alice_s | @bob_j  | Sell | ₹450|COMPLETED
+-----+------------------------+----------+---------+------+-----+--------+
```
- **Description**: Displays completed exchange logs, recording book title, seller, buyer, price, transaction date, and completion status.

---

## 📚 Syllabus Java Concepts Mapping

Every key Java syllabus requirement is explicitly implemented and verified:

| Java Concept | Implementation File & Location | Description / Usage in Project |
| :--- | :--- | :--- |
| **Object-Oriented Programming (OOP)** | [`User.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/model/User.java)<br>[`Book.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/model/Book.java)<br>[`Request.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/model/Request.java)<br>[`Transaction.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/model/Transaction.java) | Entity modeling with getters/setters, constructors, and object encapsulation. |
| **`ArrayList`** | [`BookService.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/service/BookService.java) | Storing and manipulating dynamic collections of books, search results, pending requests, and transaction history. |
| **`PriorityQueue`** | [`Request.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/model/Request.java)<br>[`BookService.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/service/BookService.java#L190-L225) | Prioritizing multiple seeker requests for the same book based on `requestTime` timestamp (earliest request gets top priority). |
| **`BufferedReader`** | [`CatalogReader.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/service/CatalogReader.java) | Reading pre-populated book catalog entries line-by-line from [`catalog.txt`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/catalog.txt). |
| **Custom Checked Exceptions** | [`InvalidBookException.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/exception/InvalidBookException.java)<br>[`InvalidPriceException.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/exception/InvalidPriceException.java) | Thrown when book validation fails (empty fields, non-zero price for free books, or negative pricing). |
| **`Cloneable` Interface** | [`Book.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/model/Book.java) | Implements `clone()` to allow owners to duplicate a book listing when offering multiple copies. |
| **Background Thread** | [`AvailabilityMonitorThread.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/service/AvailabilityMonitorThread.java) | A daemon thread running periodically every 8s to monitor available book counts and log activity in real-time. |
| **JDBC with SQLite** | [`DatabaseHandler.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/main/java/com/passthebook/db/DatabaseHandler.java) | Direct JDBC database management using `passthebook.db` with `users`, `books`, `requests`, and `transactions` tables. |

---

## 🗄️ Database Architecture & Schema

The application uses an embedded **SQLite** database stored in `passthebook.db`.

```sql
-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL
);

-- 2. Books Table
CREATE TABLE IF NOT EXISTS books (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    author TEXT NOT NULL,
    isbn TEXT NOT NULL,
    price REAL NOT NULL,
    condition TEXT NOT NULL,
    type TEXT NOT NULL,
    is_available INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY(owner_id) REFERENCES users(id)
);

-- 3. Requests Table
CREATE TABLE IF NOT EXISTS requests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id INTEGER NOT NULL,
    seeker_id INTEGER NOT NULL,
    request_time INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY(book_id) REFERENCES books(id),
    FOREIGN KEY(seeker_id) REFERENCES users(id)
);

-- 4. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    request_id INTEGER NOT NULL,
    book_id INTEGER NOT NULL,
    seller_id INTEGER NOT NULL,
    buyer_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    price REAL NOT NULL,
    date TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'COMPLETED',
    FOREIGN KEY(request_id) REFERENCES requests(id),
    FOREIGN KEY(book_id) REFERENCES books(id),
    FOREIGN KEY(seller_id) REFERENCES users(id),
    FOREIGN KEY(buyer_id) REFERENCES users(id)
);
```

---

## 🚀 How to Build & Run the Application

### Prerequisites
- **JDK 21** or later installed.
- Windows / macOS / Linux OS.

### Quick Start Options

#### Option 1: Double-Click Batch File (Windows)
Double-click `run.bat` in the project root directory.

#### Option 2: Run via Maven Command Line
```bash
mvn javafx:run
```

---

## 🧪 Verification & Testing

The project includes an automated test suite ([`AppSmokeTest.java`](file:///c:/Users/edeva/OneDrive/Desktop/PassTheBook/src/test/java/com/passthebook/AppSmokeTest.java)) powered by **JUnit 5**.

To run all automated syllabus tests:
```bash
mvn test
```

### Verified Scenarios:
1. ✅ **User Authentication**: Registration, duplicate prevention, and successful login.
2. ✅ **BufferedReader Catalog Import**: Correct line parsing from `catalog.txt`.
3. ✅ **Custom Checked Exceptions**: Throwing `InvalidBookException` and `InvalidPriceException`.
4. ✅ **`Cloneable` Interface**: Duplicating book objects while resetting IDs.
5. ✅ **`PriorityQueue` Order**: Ordering multiple seeker requests chronologically by timestamp.
6. ✅ **Transaction Lifecycle**: Marking books unavailable upon accept and logging transactions.
7. ✅ **Background Threading**: Verifying `AvailabilityMonitorThread` thread safety and execution.
