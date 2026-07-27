package Persistence.Impl;

import Business.Entities.*;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;

/**
 * Custom Gson deserializer for the {@link Business.Entities.Product} hierarchy.
 * Inspects JSON field presence to route deserialization to the correct concrete type:
 * <ul>
 *   <li>{@code "completion_hours"} present → {@link Service}</li>
 *   <li>{@code "prescription"} present → {@link ContactLens}</li>
 *   <li>{@code "volume_ml"} present → {@link Consumable}</li>
 *   <li>anything else → {@link Glasses}</li>
 * </ul>
 */
public class ProductDeserializer implements JsonDeserializer<Product> {

    private static final Gson DELEGATE = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()))
            .create();

    @Override
    public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        if (obj.has("completion_hours")) {
            return DELEGATE.fromJson(obj, Service.class);
        }
        if (obj.has("prescription")) {
            return DELEGATE.fromJson(obj, ContactLens.class);
        }
        if (obj.has("volume_ml")) {
            return DELEGATE.fromJson(obj, Consumable.class);
        }
        return DELEGATE.fromJson(obj, Glasses.class);
    }
}
