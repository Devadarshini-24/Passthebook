package com.passthebook.service;

import com.passthebook.model.Book;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Requirement: Uses BufferedReader to read a small ISBN/book catalogue from a text file.
 */
public class CatalogReader {

    /**
     * Reads catalog.txt using BufferedReader and returns an ArrayList of sample Book listings.
     * Format per line: ISBN|Title|Author|Type|Price|Condition
     */
    public static ArrayList<Book> loadCatalog() {
        ArrayList<Book> catalogList = new ArrayList<>();
        BufferedReader reader = null;

        try {
            // Try loading from working directory first
            File catalogFile = new File("catalog.txt");
            if (catalogFile.exists()) {
                reader = new BufferedReader(new FileReader(catalogFile));
            } else {
                // Fallback to classpath resource stream
                InputStream is = CatalogReader.class.getResourceAsStream("/catalog.txt");
                if (is != null) {
                    reader = new BufferedReader(new InputStreamReader(is));
                }
            }

            if (reader != null) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    String[] parts = line.split("\\|");
                    if (parts.length >= 6) {
                        String isbn = parts[0].trim();
                        String name = parts[1].trim();
                        String author = parts[2].trim();
                        String type = parts[3].trim();
                        double price = Double.parseDouble(parts[4].trim());
                        String condition = parts[5].trim();

                        Book book = new Book(0, 0, "Catalogue", name, author, isbn, price, condition, type, true);
                        catalogList.add(book);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading catalogue with BufferedReader: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return catalogList;
    }
}
