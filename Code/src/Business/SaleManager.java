package Business;

import Business.Entities.Sale;
import Business.Exceptions.BusinessException;
import Business.Exceptions.BusinessFileNotFoundException;
import Business.Exceptions.BusinessWriteException;
import Persistence.Exceptions.PersistenceFileNotFoundException;
import Persistence.Exceptions.PersistenceException;
import Persistence.SalesDAO;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business-layer facade for sale history retrieval and recording.
 * Delegates to a {@link SalesDAO} implementation and translates
 * {@link Persistence.Exceptions.PersistenceException} into the appropriate
 * {@link BusinessException} subclass. The history returned to callers is
 * limited to the {@value #MAX_HISTORY_ENTRIES} most recent entries, sorted
 * newest first.
 */
public class SaleManager {

    /** Maximum number of sale history entries returned per client. */
    private static final int MAX_HISTORY_ENTRIES = 10;

    private final SalesDAO salesDAO;

    /**
     * Constructs a {@code SaleManager} backed by the given DAO.
     *
     * @param salesDAO the DAO used to persist and retrieve sale records
     */
    public SaleManager(SalesDAO salesDAO) {
        this.salesDAO = salesDAO;
    }

    /**
     * Returns the purchase history for the given client, limited to the
     * {@value #MAX_HISTORY_ENTRIES} most recent sales sorted newest first.
     *
     * @param clientId the ID of the client whose history is requested
     * @return a list of up to 10 {@link Sale} records; never {@code null}
     * @throws BusinessException if the underlying data source cannot be accessed
     */
    public List<Sale> getSalesByClient(String clientId) throws BusinessException {
        try {
            return salesDAO.getClientHistory(clientId)
                    .stream()
                    .sorted(Comparator.comparingLong(Sale::getPurchaseDate).reversed())
                    .limit(MAX_HISTORY_ENTRIES)
                    .collect(Collectors.toList());
        } catch (PersistenceFileNotFoundException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        } catch (PersistenceException e) {
            throw new BusinessWriteException(e.getMessage(), e);
        }
    }

    /**
     * Persists a new sale record with the current timestamp.
     *
     * @param clientId  the ID of the client who made the purchase
     * @param productId the ID of the product that was purchased
     * @param pricePaid the final price paid by the client
     * @throws BusinessException if the sale cannot be written to the data source
     */
    public void recordSale(String clientId, String productId, double pricePaid) throws BusinessException {
        try {
            long timestamp = Instant.now().getEpochSecond();
            Sale sale = new Sale(clientId, productId, pricePaid, timestamp);
            salesDAO.addSale(sale);
        } catch (PersistenceFileNotFoundException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        } catch (PersistenceException e) {
            throw new BusinessWriteException(e.getMessage(), e);
        }
    }
}
