package Persistence.Impl;

import Business.Entities.ProductStock;
import Business.Entities.Provider;
import Persistence.ProviderDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import Persistence.Exceptions.PersistenceException;
import edu.salle.url.api.ApiHelper;
import edu.salle.url.api.exception.ApiException;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cloud-first implementation of {@link ProviderDAO}.
 * Fetches provider data from the shared REST API and overlays local stock overrides
 * from {@link ProviderJsonDAO} so that checkout-driven decrements are reflected even
 * when reading from the API. Falls back entirely to the local DAO if the API is
 * unavailable.
 */
public class ProviderCloudDAO implements ProviderDAO {

    private static final String PROVIDERS_URL = "https://balandrau.salle.url.edu/dpoo/shared/providers";

    private final ApiHelper helper;
    private final ProviderJsonDAO fallbackDAO;
    private final Gson gson;

    /**
     * Constructs a {@code ProviderCloudDAO} using a freshly initialised {@link ApiHelper}
     * and a default {@link ProviderJsonDAO} as the local fallback.
     */
    public ProviderCloudDAO() {
        this(createApiHelper(), new ProviderJsonDAO());
    }

    /**
     * Package-private constructor for unit testing with injected collaborators.
     *
     * @param helper      the API helper to use ({@code null} forces local fallback)
     * @param fallbackDAO the local DAO to use when the API is unavailable
     */
    ProviderCloudDAO(ApiHelper helper, ProviderJsonDAO fallbackDAO) {
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
    public Provider findById(String id) throws PersistenceException {
        if (helper == null) return fallbackDAO.findById(id);
        try {
            String response = helper.getFromUrl(PROVIDERS_URL + "?provider_id=" + encode(id));

            Type providerListType = new TypeToken<List<Provider>>() {}.getType();
            List<Provider> apiProviders = gson.fromJson(response, providerListType);

            if (apiProviders != null) {
                applyLocalStockOverrides(apiProviders);
                for (Provider provider : apiProviders) {
                    if (provider != null && id.equals(provider.getProviderId())) {
                        return provider;
                    }
                }
            }
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.findById(id);
    }

    @Override
    public List<Provider> findAll() throws PersistenceException {
        if (helper == null) return fallbackDAO.findAll();
        try {
            String response = helper.getFromUrl(PROVIDERS_URL);

            Type providerListType = new TypeToken<List<Provider>>() {}.getType();
            List<Provider> apiProviders = gson.fromJson(response, providerListType);

            if (apiProviders != null) {
                applyLocalStockOverrides(apiProviders);
                return apiProviders;
            }
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.findAll();
    }

    @Override
    public List<Provider> findByProductId(String productId) throws PersistenceException {
        if (helper == null) return fallbackDAO.findByProductId(productId);
        try {
            String response = helper.getFromUrl(PROVIDERS_URL + "?product_id=" + encode(productId));

            Type providerListType = new TypeToken<List<Provider>>() {}.getType();
            List<Provider> apiProviders = gson.fromJson(response, providerListType);

            if (apiProviders != null) {
                applyLocalStockOverrides(apiProviders);
                return apiProviders;
            }
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.findByProductId(productId);
    }

    @Override
    public void updateStock(String providerId, String productId, int quantity) throws PersistenceException {
        fallbackDAO.updateStock(providerId, productId, quantity);
    }

    /**
     * Overlays the stock levels from the local {@link ProviderJsonDAO} onto the cloud-sourced
     * provider list. This ensures checkout-driven stock decrements (written locally) are
     * reflected when reading provider data from the API.
     *
     * @param cloudProviders the list of providers returned by the API
     */
    private void applyLocalStockOverrides(List<Provider> cloudProviders) {
        if (cloudProviders == null) return;
        List<Provider> localProviders;
        try {
            localProviders = fallbackDAO.findAll();
        } catch (PersistenceException e) {
            return;
        }
        if (localProviders == null || localProviders.isEmpty()) return;

        Map<String, Map<String, Integer>> localStock = new HashMap<>();
        for (Provider local : localProviders) {
            List<ProductStock> stockList = local.getAllProductStock();
            if (stockList == null) continue;
            Map<String, Integer> map = new HashMap<>();
            for (ProductStock s : stockList) {
                map.put(s.getProductId(), s.getUnitsInStock());
            }
            localStock.put(local.getProviderId(), map);
        }

        for (Provider cloud : cloudProviders) {
            Map<String, Integer> stockMap = localStock.get(cloud.getProviderId());
            if (stockMap == null) continue;
            List<ProductStock> stockList = cloud.getAllProductStock();
            if (stockList == null) continue;
            for (ProductStock stock : stockList) {
                Integer localUnits = stockMap.get(stock.getProductId());
                if (localUnits != null) {
                    stock.setUnitsInStock(localUnits);
                }
            }
        }
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
