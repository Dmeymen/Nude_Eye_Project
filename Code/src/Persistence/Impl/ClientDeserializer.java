package Persistence.Impl;

import Business.Customer;
import Business.Entities.CorporateClient;
import Business.Entities.IndividualClient;
import Business.Entities.OnlineClient;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Custom Gson deserializer for the {@link Customer} interface.
 * Reads the {@code client_type} field from the JSON object and routes
 * deserialization to the appropriate concrete class:
 * <ul>
 *   <li>{@code "online"} → {@link Business.Entities.OnlineClient}</li>
 *   <li>{@code "corporate"} → {@link Business.Entities.CorporateClient}</li>
 *   <li>anything else (or absent) → {@link Business.Entities.IndividualClient}</li>
 * </ul>
 */
public class ClientDeserializer implements JsonDeserializer<Customer> {

    /**
     * Deserializes a JSON element into the correct {@link Customer} subtype based on
     * the {@code client_type} discriminator field.
     *
     * @param json    the JSON element being deserialized
     * @param typeOfT the type of the desired object (always {@code Customer.class})
     * @param context the deserialization context used to delegate to concrete types
     * @return the deserialized {@link Customer} instance
     * @throws JsonParseException if the JSON cannot be parsed
     */
    @Override
    public Customer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String type = obj.has("client_type") ? obj.get("client_type").getAsString() : "regular";
        switch (type) {
            case "online":    return context.deserialize(obj, OnlineClient.class);
            case "corporate": return context.deserialize(obj, CorporateClient.class);
            default:          return context.deserialize(obj, IndividualClient.class);
        }
    }
}
