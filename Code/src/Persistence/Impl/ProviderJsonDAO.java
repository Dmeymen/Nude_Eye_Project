package Persistence.Impl;

import Business.Entities.Provider;
import Business.Entities.ProductStock;
import Persistence.ProviderDAO;
import Persistence.Exceptions.PersistenceException;
import Persistence.Exceptions.PersistenceFileNotFoundException;
import Persistence.Exceptions.PersistenceWriteException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Local JSON implementation of {@link ProviderDAO}.
 * Reads and writes {@code providers.json} from the working directory.
 * Stock updates reload the entire file, apply the change, and rewrite it in full.
 */
public class ProviderJsonDAO implements ProviderDAO {

    private static final String FILE_PATH = "providers.json";

    public boolean isFileCreated() {
        return new File(FILE_PATH).exists();
    }

    @Override
    public Provider findById(String id) throws PersistenceException {
        Gson gson = new Gson();
        try (JsonReader jsonReader = new JsonReader(new FileReader(FILE_PATH))) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                Provider provider = gson.fromJson(jsonReader, Provider.class);
                if (provider.getProviderId().equals(id)) {
                    return provider;
                }
            }
            jsonReader.endArray();
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(FILE_PATH, e);
        }
        return null;
    }

    @Override
    public List<Provider> findAll() throws PersistenceException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type providerListType = new TypeToken<List<Provider>>() {}.getType();
            List<Provider> providers = gson.fromJson(reader, providerListType);
            return providers == null ? new ArrayList<>() : providers;
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(FILE_PATH, e);
        }
    }

    @Override
    public List<Provider> findByProductId(String productId) throws PersistenceException {
        Gson gson = new Gson();
        List<Provider> result = new ArrayList<>();
        try (JsonReader jsonReader = new JsonReader(new FileReader(FILE_PATH))) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                Provider provider = gson.fromJson(jsonReader, Provider.class);
                List<ProductStock> stock = provider.getAllProductStock();
                if (stock != null) {
                    boolean sells = stock.stream()
                            .anyMatch(s -> s.getProductId().equals(productId));
                    if (sells) result.add(provider);
                }
            }
            jsonReader.endArray();
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(FILE_PATH, e);
        }
        return result;
    }

    @Override
    public void updateStock(String providerId, String productId, int quantity) throws PersistenceException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Provider> providers = findAll();

        boolean changed = false;
        for (Provider provider : providers) {
            if (provider.getProviderId().equals(providerId)) {
                List<ProductStock> stockList = provider.getAllProductStock();
                if (stockList != null) {
                    for (ProductStock stock : stockList) {
                        if (stock.getProductId().equals(productId)) {
                            stock.decreaseUnits(quantity);
                            changed = true;
                            break;
                        }
                    }
                }
                break;
            }
        }

        if (!changed) return;

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(providers, writer);
        } catch (IOException e) {
            throw new PersistenceWriteException("Error writing " + FILE_PATH, e);
        }
    }
}