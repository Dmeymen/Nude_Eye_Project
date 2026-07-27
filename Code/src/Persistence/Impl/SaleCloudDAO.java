package Persistence.Impl;

import Business.Entities.Sale;
import Persistence.SalesDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import Persistence.Exceptions.PersistenceException;
import edu.salle.url.api.ApiHelper;
import edu.salle.url.api.exception.ApiException;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Cloud-first implementation of {@link SalesDAO}.
 * POSTs new sale records to the group's REST API endpoint and GETs history from it.
 * Falls back to the local {@link SaleCSVDAO} if the API is unavailable or returns
 * no data. File lifecycle operations ({@link #createIfNotExists()}) are always
 * delegated to the local fallback.
 */
public class SaleCloudDAO implements SalesDAO {

    private static final String GROUP_ID = "OOP_Project_33";
    private static final String SALES_URL = "https://balandrau.salle.url.edu/dpoo/" + GROUP_ID + "/sales";

    private final ApiHelper helper;
    private final SaleCSVDAO fallbackDAO;
    private final Gson gson;

    /**
     * Constructs a {@code SaleCloudDAO} using a freshly initialised {@link ApiHelper}
     * and a default {@link SaleCSVDAO} as the local fallback.
     */
    public SaleCloudDAO() {
        this(createApiHelper(), new SaleCSVDAO());
    }

    /**
     * Package-private constructor for unit testing with injected collaborators.
     *
     * @param helper      the API helper to use ({@code null} forces local fallback)
     * @param fallbackDAO the local DAO to use when the API is unavailable
     */
    SaleCloudDAO(ApiHelper helper, SaleCSVDAO fallbackDAO) {
        this.helper = helper;
        this.fallbackDAO = fallbackDAO;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
    public List<Sale> loadAllSales() throws PersistenceException {
        if (helper == null) return fallbackDAO.loadAllSales();
        try {
            String response = helper.getFromUrl(SALES_URL);

            Type saleListType = new TypeToken<List<Sale>>() {}.getType();
            List<Sale> apiSales = gson.fromJson(response, saleListType);

            if (apiSales != null) {
                return apiSales;
            }
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local CSV
        }

        return fallbackDAO.loadAllSales();
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
    public void addSale(Sale sale) throws PersistenceException {
        if (helper == null) { fallbackDAO.addSale(sale); return; }
        try {
            String body = gson.toJson(sale);
            helper.postToUrl(SALES_URL, body);
            return;
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local CSV
        }

        fallbackDAO.addSale(sale);
    }

    @Override
    public List<Sale> getClientHistory(String clientId) throws PersistenceException {
        if (helper == null) return fallbackDAO.getClientHistory(clientId);
        try {
            String response = helper.getFromUrl(SALES_URL + "?client_id=" + encode(clientId));

            Type saleListType = new TypeToken<List<Sale>>() {}.getType();
            List<Sale> apiSales = gson.fromJson(response, saleListType);

            if (apiSales != null) {
                return apiSales;
            }
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local CSV
        }

        return fallbackDAO.getClientHistory(clientId);
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
