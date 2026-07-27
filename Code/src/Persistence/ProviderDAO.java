package Persistence;

import Business.Entities.Provider;
import Persistence.Exceptions.PersistenceException;

import java.util.List;

/**
 * Data Access Object interface for {@link Provider} entities.
 * Defines the contract for all provider persistence operations.
 */
public interface ProviderDAO {

    /**
     * Finds and returns the provider with the given ID, or {@code null} if not found.
     *
     * @param id the provider ID to search for
     * @return the matching {@link Provider}, or {@code null}
     */
    Provider findById(String id) throws PersistenceException;

    /**
     * Returns all providers stored in the persistence layer.
     *
     * @return a list of all {@link Provider} objects; never {@code null}
     */
    List<Provider> findAll() throws PersistenceException;

    /**
     * Returns all providers that sell the product with the given ID.
     *
     * @param productId the product ID to filter by
     * @return a list of matching {@link Provider} objects; never {@code null}
     */
    List<Provider> findByProductId(String productId) throws PersistenceException;

    /**
     * Decreases the stock of the given product at the given provider by the specified quantity.
     * If the provider or product is not found, this method does nothing.
     *
     * @param providerId the provider whose stock should be updated
     * @param productId  the product whose stock should be decreased
     * @param quantity   the number of units to subtract
     */
    void updateStock(String providerId, String productId, int quantity) throws PersistenceException;
}