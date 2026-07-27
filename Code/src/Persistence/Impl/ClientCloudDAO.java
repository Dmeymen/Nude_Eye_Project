package Persistence.Impl;

import Business.Customer;
import Persistence.ClientDAO;
import Persistence.Exceptions.PersistenceException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.salle.url.api.ApiHelper;
import edu.salle.url.api.exception.ApiException;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Cloud-first implementation of {@link ClientDAO}.
 * Sends and retrieves client data via the group's REST API endpoint.
 * Falls back to the local {@link ClientJsonDAO} if the API is unavailable or
 * returns no results. File lifecycle operations ({@link #createIfNotExists()}) are
 * always delegated to the local fallback.
 */
public class ClientCloudDAO implements ClientDAO {

    private static final String GROUP_ID = "OOP_Project_33";
    private static final String CLIENTS_URL = "https://balandrau.salle.url.edu/dpoo/" + GROUP_ID + "/clients";

    private final ApiHelper helper;
    private final ClientJsonDAO fallbackDAO;
    private final Gson readGson;
    private final Gson writeGson;

    /**
     * Constructs a {@code ClientCloudDAO} using a freshly initialised {@link ApiHelper}
     * and a default {@link ClientJsonDAO} as the local fallback.
     */
    public ClientCloudDAO() {
        this(createApiHelper(), new ClientJsonDAO());
    }

    /**
     * Package-private constructor for unit testing with injected collaborators.
     *
     * @param helper      the API helper to use ({@code null} forces local fallback)
     * @param fallbackDAO the local DAO to use when the API is unavailable
     */
    ClientCloudDAO(ApiHelper helper, ClientJsonDAO fallbackDAO) {
        this.helper    = helper;
        this.fallbackDAO = fallbackDAO;
        this.readGson  = new GsonBuilder()
                .registerTypeAdapter(Customer.class, new ClientDeserializer())
                .registerTypeAdapter(geolocationAPI.Address.class, new AddressDeserializer())
                .create();
        this.writeGson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Attempts to create an {@link ApiHelper}; returns {@code null} if the API is unreachable.
     */
    private static ApiHelper createApiHelper() {
        try {
            return new ApiHelper();
        } catch (ApiException e) {
            return null;
        }
    }

    @Override
    public boolean isFileCreated() {
        return fallbackDAO.isFileCreated();
    }

    @Override
    public void createIfNotExists() throws PersistenceException {
        fallbackDAO.createIfNotExists();
    }

    @Override
    public Customer findById(String id) throws PersistenceException {
        if (helper == null) return fallbackDAO.findById(id);
        try {
            String response = helper.getFromUrl(CLIENTS_URL + "?client_id=" + encode(id));

            Type listType = new TypeToken<List<Customer>>() {}.getType();
            List<Customer> apiClients = readGson.fromJson(response, listType);

            if (apiClients != null) {
                for (Customer client : apiClients) {
                    if (client != null && id.equals(client.getClientId())) return client;
                }
            }
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.findById(id);
    }

    @Override
    public List<Customer> findAll(String id) throws PersistenceException {
        if (helper == null) return fallbackDAO.findAll(id);
        try {
            String response = helper.getFromUrl(CLIENTS_URL);

            Type listType = new TypeToken<List<Customer>>() {}.getType();
            List<Customer> apiClients = readGson.fromJson(response, listType);

            if (apiClients != null) return apiClients;
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.findAll(id);
    }

    @Override
    public List<Customer> findByFields(String id, String field, String value) throws PersistenceException {
        if (helper == null) return fallbackDAO.findByFields(id, field, value);
        try {
            String apiField = toApiField(field);
            String response = helper.getFromUrl(CLIENTS_URL + "?" + apiField + "=" + encode(value));

            Type listType = new TypeToken<List<Customer>>() {}.getType();
            List<Customer> apiClients = readGson.fromJson(response, listType);

            if (apiClients != null) return apiClients;
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.findByFields(id, field, value);
    }

    @Override
    public void save(Customer client) throws PersistenceException {
        if (helper == null) { fallbackDAO.save(client); return; }
        try {
            String body = writeGson.toJson(client);
            helper.postToUrl(CLIENTS_URL, body);
            return;
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        fallbackDAO.save(client);
    }

    @Override
    public boolean removeClients(String id, String field, String value) throws PersistenceException {
        if (helper == null) return fallbackDAO.removeClients(id, field, value);
        try {
            String apiField = toApiField(field);
            helper.deleteFromUrl(CLIENTS_URL + "?" + apiField + "=" + encode(value));
            return true;
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.removeClients(id, field, value);
    }

    @Override
    public boolean removeClient(String id, String pos) throws PersistenceException {
        if (helper == null) return fallbackDAO.removeClient(id, pos);
        try {
            helper.deleteFromUrl(CLIENTS_URL + "/" + encode(pos));
            return true;
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.removeClient(id, pos);
    }

    /**
     * Converts an internal field name to the snake_case name expected by the REST API.
     *
     * @param field the internal field name (camelCase or snake_case)
     * @return the API-compatible field name
     */
    private String toApiField(String field) {
        if ("clientId".equals(field) || "id".equals(field)) return "client_id";
        if ("clientName".equals(field) || "name".equals(field)) return "full_name";
        if ("phoneNumber".equals(field)) return "number";
        return field;
    }

    /**
     * URL-encodes the given value using UTF-8. A {@code null} input is treated as an empty string.
     *
     * @param value the string to encode
     * @return the URL-encoded string
     */
    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
