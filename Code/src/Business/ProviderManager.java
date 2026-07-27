package Business;

import Business.Entities.Provider;
import Business.Exceptions.BusinessException;
import Business.Exceptions.BusinessFileNotFoundException;
import Persistence.Exceptions.PersistenceException;
import Persistence.ProviderDAO;

import java.util.List;

/**
 * Business-layer facade for provider lookup operations.
 * Delegates to a {@link ProviderDAO} implementation and translates
 * {@link Persistence.Exceptions.PersistenceException} into {@link BusinessException}
 * so the Presentation layer has no dependency on the Persistence layer.
 */
public class ProviderManager {

    private final ProviderDAO providerDAO;

    /**
     * Constructs a {@code ProviderManager} backed by the given DAO.
     *
     * @param providerDAO the DAO used to retrieve provider data
     */
    public ProviderManager(ProviderDAO providerDAO) {
        this.providerDAO = providerDAO;
    }

    /**
     * Returns all providers stored in the persistence layer.
     *
     * @return a list of all {@link Provider} objects; never {@code null}
     * @throws BusinessException if the underlying data source cannot be accessed
     */
    public List<Provider> getAllProviders() throws BusinessException {
        try {
            return providerDAO.findAll();
        } catch (PersistenceException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Returns the provider with the given ID, or {@code null} if not found.
     *
     * @param providerId the provider ID to search for
     * @return the matching {@link Provider}, or {@code null}
     * @throws BusinessException if the underlying data source cannot be accessed
     */
    public Provider findProviderById(String providerId) throws BusinessException {
        try {
            return providerDAO.findById(providerId);
        } catch (PersistenceException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Returns all providers that sell the product with the given ID.
     *
     * @param productId the product ID to filter by
     * @return a list of matching {@link Provider} objects; never {@code null}
     * @throws BusinessException if the underlying data source cannot be accessed
     */
    public List<Provider> findProvidersByProduct(String productId) throws BusinessException {
        try {
            return providerDAO.findByProductId(productId);
        } catch (PersistenceException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        }
    }
}
