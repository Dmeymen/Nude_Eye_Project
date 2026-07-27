package Business;

import Business.Entities.IndividualClient;
import Business.Entities.PhoneNumber;
import Business.Exceptions.BusinessException;
import Business.Exceptions.BusinessFileNotFoundException;
import Business.Exceptions.BusinessWriteException;
import Persistence.ClientDAO;
import Persistence.Exceptions.PersistenceFileNotFoundException;
import Persistence.Exceptions.PersistenceException;

import java.util.List;

/**
 * Business-layer facade for client authentication and registration.
 * Delegates to a {@link ClientDAO} implementation and translates
 * {@link Persistence.Exceptions.PersistenceException} into the appropriate
 * {@link BusinessException} subclass.
 */
public class ClientManager {

    private final ClientDAO clientDAO;

    /**
     * Constructs a {@code ClientManager} backed by the given DAO.
     *
     * @param clientDAO the DAO used to load and persist client records
     */
    public ClientManager(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }

    /**
     * Looks up the client with the given ID.
     *
     * @param clientId the client ID entered by the user
     * @return the matching {@link Customer}, or {@code null} if no account is found
     * @throws BusinessException if the underlying data source cannot be accessed
     */
    public Customer login(String clientId) throws BusinessException {
        try {
            return clientDAO.findById(clientId);
        } catch (PersistenceException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Registers a new {@link IndividualClient} with an auto-incremented numeric ID
     * derived from the highest existing numeric client ID.
     *
     * @param fullName     the full name of the new client
     * @param phoneNumbers the phone numbers to associate with the new client
     * @return the newly created and persisted {@link IndividualClient}
     * @throws BusinessException if the client cannot be saved to the data source
     */
    public IndividualClient register(String fullName, List<PhoneNumber> phoneNumbers) throws BusinessException {
        try {
            int nextId = resolveNextId();
            IndividualClient newClient = new IndividualClient(String.valueOf(nextId), fullName, phoneNumbers);
            clientDAO.save(newClient);
            return newClient;
        } catch (PersistenceFileNotFoundException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        } catch (PersistenceException e) {
            throw new BusinessWriteException(e.getMessage(), e);
        }
    }

    /**
     * Scans all existing clients to determine the next available numeric ID.
     * Non-numeric IDs (e.g. UUIDs from earlier registrations) are skipped.
     *
     * @return one greater than the current maximum numeric client ID, or {@code 1} if none exist
     * @throws PersistenceException if the client list cannot be loaded
     */
    private int resolveNextId() throws PersistenceException {
        List<Customer> all = clientDAO.findAll(null);
        int max = 0;
        for (Customer c : all) {
            try {
                int id = Integer.parseInt(c.getClientId());
                if (id > max) max = id;
            } catch (NumberFormatException ignored) {
                // skip UUID based or non numeric IDs from earlier registrations
            }
        }
        return max + 1;
    }
}
