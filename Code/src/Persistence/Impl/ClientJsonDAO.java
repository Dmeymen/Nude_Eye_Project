package Persistence.Impl;

import Business.Customer;
import Business.Entities.PhoneNumber;
import Persistence.ClientDAO;
import Persistence.Exceptions.PersistenceException;
import Persistence.Exceptions.PersistenceFileNotFoundException;
import Persistence.Exceptions.PersistenceWriteException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Local JSON implementation of {@link ClientDAO}.
 * Reads and writes {@code clients.json} from the working directory.
 * Uses {@link ClientDeserializer} to route polymorphic client JSON to the correct
 * concrete type ({@link Business.Entities.IndividualClient}, {@link Business.Entities.OnlineClient},
 * or {@link Business.Entities.CorporateClient}) and {@link AddressDeserializer} to handle
 * both structured and plain-string address formats.
 */
public class ClientJsonDAO implements ClientDAO {

    private static final String FILE_PATH = "clients.json";

    private final Gson readGson;
    private final Gson writeGson;

    /**
     * Constructs a {@code ClientJsonDAO} and registers the custom deserializers
     * required for polymorphic client and address deserialization.
     */
    public ClientJsonDAO() {
        this.readGson  = new GsonBuilder()
                .registerTypeAdapter(Customer.class, new ClientDeserializer())
                .registerTypeAdapter(geolocationAPI.Address.class, new AddressDeserializer())
                .create();
        this.writeGson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public boolean isFileCreated() {
        return new File(FILE_PATH).exists();
    }

    @Override
    public void createIfNotExists() throws PersistenceException {
        if (!isFileCreated()) {
            try (FileWriter writer = new FileWriter(FILE_PATH)) {
                writer.write("[]");
            } catch (IOException e) {
                throw new PersistenceWriteException("Error creating " + FILE_PATH, e);
            }
        }
    }

    @Override
    public Customer findById(String id) throws PersistenceException {
        for (Customer client : findAll(id)) {
            if (client.getClientId().equals(id)) return client;
        }
        return null;
    }

    @Override
    public List<Customer> findAll(String id) throws PersistenceException {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<List<Customer>>() {}.getType();
            List<Customer> clients = readGson.fromJson(reader, listType);
            return clients == null ? new ArrayList<>() : clients;
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(FILE_PATH, e);
        } catch (RuntimeException e) {
            throw new PersistenceFileNotFoundException(FILE_PATH, e);
        }
    }

    @Override
    public List<Customer> findByFields(String id, String field, String value) throws PersistenceException {
        List<Customer> matches = new ArrayList<>();
        for (Customer client : findAll(id)) {
            if (matchesField(client, field, value)) matches.add(client);
        }
        return matches;
    }

    @Override
    public void save(Customer client) throws PersistenceException {
        JsonArray array;
        try (FileReader reader = new FileReader(FILE_PATH)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            array = parsed.isJsonArray() ? parsed.getAsJsonArray() : new JsonArray();
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(FILE_PATH, e);
        }

        array.add(writeGson.toJsonTree(client));

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writeGson.toJson(array, writer);
        } catch (IOException e) {
            throw new PersistenceWriteException("Error writing to " + FILE_PATH, e);
        }
    }

    @Override
    public boolean removeClients(String id, String field, String value) throws PersistenceException {
        List<Customer> clients = findAll(id);
        boolean removed = clients.removeIf(client -> matchesField(client, field, value));
        if (!removed) return false;
        writeAll(clients);
        return true;
    }

    @Override
    public boolean removeClient(String id, String pos) throws PersistenceException {
        List<Customer> clients = findAll(id);
        boolean removed = clients.removeIf(client -> client.getClientId().equals(pos));
        if (!removed) return false;
        writeAll(clients);
        return true;
    }

    /**
     * Serialises the full client list to {@code clients.json}, replacing its contents.
     *
     * @param clients the list of clients to persist
     * @throws PersistenceException if the file cannot be written
     */
    private void writeAll(List<Customer> clients) throws PersistenceException {
        JsonArray array = new JsonArray();
        for (Customer c : clients) {
            array.add(writeGson.toJsonTree(c));
        }
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writeGson.toJson(array, writer);
        } catch (IOException e) {
            throw new PersistenceWriteException("Error writing to " + FILE_PATH, e);
        }
    }

    /**
     * Returns {@code true} if the given client's {@code field} equals {@code value}.
     * Supports both camelCase and snake_case variants of common field names.
     *
     * @param client the client to test
     * @param field  the field name to match
     * @param value  the expected value
     * @return {@code true} if the field matches
     */
    private boolean matchesField(Customer client, String field, String value) {
        if (client == null || field == null || value == null) return false;
        switch (field) {
            case "clientId":
            case "client_id":
            case "id":
                return value.equals(client.getClientId());
            case "clientName":
            case "full_name":
            case "name":
                return value.equals(client.getName());
            case "countryPrefix":
            case "country_prefix":
                return hasPhoneValue(client, "countryPrefix", value);
            case "number":
            case "phoneNumber":
                return hasPhoneValue(client, "number", value);
            default:
                return false;
        }
    }

    /**
     * Returns {@code true} if any of the client's phone numbers has a field equal to the given value.
     *
     * @param client the client whose phone numbers are checked
     * @param field  {@code "countryPrefix"} or {@code "number"}
     * @param value  the value to match
     * @return {@code true} if at least one phone number matches
     */
    private boolean hasPhoneValue(Customer client, String field, String value) {
        List<PhoneNumber> phones = client.getPhoneNumber();
        if (phones == null) return false;
        for (PhoneNumber p : phones) {
            if ("countryPrefix".equals(field) && value.equals(p.getCountryPrefix())) return true;
            if ("number".equals(field) && value.equals(p.getPhoneNumber())) return true;
        }
        return false;
    }
}
