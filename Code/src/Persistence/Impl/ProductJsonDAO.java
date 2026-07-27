package Persistence.Impl;

import Business.Entities.Product;
import Persistence.ProductDAO;
import Persistence.Exceptions.PersistenceFileNotFoundException;
import Persistence.Exceptions.PersistenceException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Local JSON implementation of {@link ProductDAO}.
 * Reads {@code products.json} from the working directory using Gson streaming
 * to avoid loading the entire file into memory during search operations.
 * Uses {@link ProductDeserializer} to deserialize each entry to the correct
 * concrete subtype ({@link Business.Entities.Glasses}, {@link Business.Entities.ContactLens},
 * {@link Business.Entities.Consumable}, or {@link Business.Entities.Service}).
 */
public class ProductJsonDAO implements ProductDAO {

    private static final String FILE_PATH = "products.json";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Product.class, new ProductDeserializer())
            .create();

    @Override
    public boolean isFileCreated() {
        return new File(FILE_PATH).exists();
    }

    @Override
    public List<Product> findByName(String keyword) throws PersistenceException {
        List<Product> results = new ArrayList<>();
        String lowerKeyword = keyword == null ? "" : keyword.toLowerCase();

        try (JsonReader jsonReader = new JsonReader(new FileReader(FILE_PATH))) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                Product product = GSON.fromJson(jsonReader, Product.class);
                if (product.getProductName() != null
                        && product.getProductName().toLowerCase().contains(lowerKeyword)) {
                    results.add(product);
                }
            }
            jsonReader.endArray();
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(FILE_PATH, e);
        }
        return results;
    }

    @Override
    public Product findById(String id) throws PersistenceException {
        try (JsonReader jsonReader = new JsonReader(new FileReader(FILE_PATH))) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                Product product = GSON.fromJson(jsonReader, Product.class);
                if (product.getProductId().equals(id)) {
                    return product;
                }
            }
            jsonReader.endArray();
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(FILE_PATH, e);
        }
        return null;
    }
}