package Persistence;

import Business.Customer;
import Persistence.Exceptions.PersistenceException;

import java.util.List;

/**
 * Data Access Object interface for {@link Customer} entities.
 * Defines the contract for all client persistence operations.
 */
public interface ClientDAO {

    /**
     * Returns whether the backing persistence file exists on disk.
     *
     * @return {@code true} if the file exists, {@code false} otherwise
     */
    boolean isFileCreated();

    /**
     * Creates the backing persistence file if it does not already exist.
     * Implementations should initialise the file with an empty structure (e.g. {@code []}).
     *
     * @throws PersistenceException if the file cannot be created
     */
    void createIfNotExists() throws PersistenceException;

    /**
     * Returns the client with the given ID, or {@code null} if not found.
     *
     * @param id the client ID to look up
     * @return the matching {@link Customer}, or {@code null}
     * @throws PersistenceException if the data source cannot be accessed
     */
    Customer findById(String id) throws PersistenceException;

    /**
     * Persists a new client record to the backing storage.
     *
     * @param client the {@link Customer} to save
     * @throws PersistenceException if the client cannot be written
     */
    void save(Customer client) throws PersistenceException;

    /**
     * Returns all client records from the backing storage.
     * The {@code id} parameter is reserved for implementations that scope results
     * to a group or session; pass {@code null} to retrieve all clients.
     *
     * @param id optional scoping identifier (may be {@code null})
     * @return a list of all {@link Customer} objects; never {@code null}
     * @throws PersistenceException if the data source cannot be accessed
     */
    List<Customer> findAll(String id) throws PersistenceException;

    /**
     * Returns all clients whose given field matches the given value.
     *
     * @param id    optional scoping identifier (may be {@code null})
     * @param field the field name to filter on (e.g. {@code "clientId"}, {@code "full_name"})
     * @param value the value the field must equal
     * @return a list of matching {@link Customer} objects; never {@code null}
     * @throws PersistenceException if the data source cannot be accessed
     */
    List<Customer> findByFields(String id, String field, String value) throws PersistenceException;

    /**
     * Removes all clients whose given field matches the given value.
     *
     * @param id    optional scoping identifier (may be {@code null})
     * @param field the field name to match
     * @param value the value the field must equal
     * @return {@code true} if at least one client was removed, {@code false} otherwise
     * @throws PersistenceException if the data source cannot be accessed or written
     */
    boolean removeClients(String id, String field, String value) throws PersistenceException;

    /**
     * Removes the single client identified by the given position or ID.
     *
     * @param id  optional scoping identifier (may be {@code null})
     * @param pos the client ID (or positional key) of the record to remove
     * @return {@code true} if the client was found and removed, {@code false} otherwise
     * @throws PersistenceException if the data source cannot be accessed or written
     */
    boolean removeClient(String id, String pos) throws PersistenceException;
}