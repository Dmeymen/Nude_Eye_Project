package Persistence.Impl;

import Business.Entities.Product;
import Persistence.ProductDAO;
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
 * Cloud-first implementation of {@link ProductDAO}.
 * Queries the shared REST API for product data and falls back to the local
 * {@link ProductJsonDAO} if the API is unavailable or returns no results.
 */
public class ProductCloudDAO implements ProductDAO {

    private static final String PRODUCTS_URL = "https://balandrau.salle.url.edu/dpoo/shared/products";

    private final ApiHelper helper;
    private final ProductJsonDAO fallbackDAO;
    private final Gson gson;

    /**
     * Constructs a {@code ProductCloudDAO} using a freshly initialised {@link ApiHelper}
     * and a default {@link ProductJsonDAO} as the local fallback.
     */
    public ProductCloudDAO() {
        this(createApiHelper(), new ProductJsonDAO());
    }

    /**
     * Package-private constructor for unit testing with injected collaborators.
     *
     * @param helper      the API helper to use ({@code null} forces local fallback)
     * @param fallbackDAO the local DAO to use when the API is unavailable
     */
    ProductCloudDAO(ApiHelper helper, ProductJsonDAO fallbackDAO) {
        this.helper = helper;
        this.fallbackDAO = fallbackDAO;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Product.class, new ProductDeserializer())
                .setPrettyPrinting()
                .create();
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
    public List<Product> findByName(String keyword) throws PersistenceException {
        if (helper == null) return fallbackDAO.findByName(keyword);
        try {
            String response = helper.getFromUrl(PRODUCTS_URL + "?product_name=" + encode(keyword));

            Type productListType = new TypeToken<List<Product>>() {}.getType();
            List<Product> apiProducts = gson.fromJson(response, productListType);

            if (apiProducts != null) {
                return apiProducts;
            }
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.findByName(keyword);
    }

    @Override
    public Product findById(String id) throws PersistenceException {
        if (helper == null) return fallbackDAO.findById(id);
        try {
            String response = helper.getFromUrl(PRODUCTS_URL + "?product_id=" + encode(id));

            Type productListType = new TypeToken<List<Product>>() {}.getType();
            List<Product> apiProducts = gson.fromJson(response, productListType);

            if (apiProducts != null) {
                for (Product product : apiProducts) {
                    if (product != null && id.equals(product.getProductId())) {
                        return product;
                    }
                }
            }
        } catch (ApiException | RuntimeException e) {
            // API failed so continue with local JSON
        }

        return fallbackDAO.findById(id);
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
