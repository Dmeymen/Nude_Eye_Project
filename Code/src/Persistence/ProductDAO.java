package Persistence;

import Business.Entities.Product;
import Persistence.Exceptions.PersistenceException;

import java.util.List;

/**
 * Data Access Object interface for {@link Product} entities.
 * Defines the contract for all product persistence operations.
 */
public interface ProductDAO {

    /**
     * Returns whether the backing persistence file exists on disk.
     *
     * @return {@code true} if the file exists, {@code false} otherwise
     */
    boolean isFileCreated();

    /**
     * Returns all products whose name contains the given keyword (case-insensitive).
     * Returns an empty list if no matches are found.
     *
     * @param keyword the search term to match against product names
     * @return a list of matching {@link Product} objects; never {@code null}
     */
    List<Product> findByName(String keyword) throws PersistenceException;

    /**
     * Returns the product matching the given ID, or {@code null} if not found.
     *
     * @param id the product ID to search for
     * @return the matching {@link Product}, or {@code null}
     */
    Product findById(String id) throws PersistenceException;
}