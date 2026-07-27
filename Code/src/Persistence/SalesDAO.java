package Persistence;

import Business.Entities.Sale;
import Persistence.Exceptions.PersistenceException;

import java.util.List;

/**
 * Data Access Object interface for {@link Sale} entities.
 * Defines the contract for all sale persistence operations.
 */
public interface SalesDAO {

    /**
     * Loads and returns every sale record from the backing storage.
     * Use with caution on large datasets; prefer {@link #getClientHistory(String)}
     * when only a specific client's records are needed.
     *
     * @return a list of all {@link Sale} records; never {@code null}
     */
    List<Sale> loadAllSales() throws PersistenceException;

    /**
     * Returns whether the backing persistence file exists on disk.
     *
     * @return {@code true} if the file exists, {@code false} otherwise
     */
    boolean isFileCreated();

    /**
     * Creates the backing persistence file if it does not already exist.
     * Implementations should initialise the file with the required structure (e.g. a CSV header).
     */
    void createIfNotExists() throws PersistenceException;

    /**
     * Appends a new sale record to the backing storage.
     * No existing data is loaded into memory during this operation.
     *
     * @param sale the {@link Sale} to persist
     */
    void addSale(Sale sale) throws PersistenceException;

    /**
     * Returns all sale records associated with the given client ID.
     *
     * @param clientId the ID of the client whose history is requested
     * @return a list of matching {@link Sale} records; never {@code null}
     */
    List<Sale> getClientHistory(String clientId) throws PersistenceException;
}