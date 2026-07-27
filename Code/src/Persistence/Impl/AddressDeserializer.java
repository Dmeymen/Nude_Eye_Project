package Persistence.Impl;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import geolocationAPI.Address;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Gson deserializer for {@link Address}.
 * Handles the way address is displayed in two ways:
 * <ul>
 *   <li><strong>Structured object</strong>: a JSON object with {@code street}, {@code number},
 *       {@code city}, {@code country}, and {@code postalCode} fields.</li>
 *   <li><strong>Plain string</strong>: {@code "Street Number, PostalCode City, Country"} —
 *       parsed with a regex and split on commas.</li>
 * </ul>
 */
public class AddressDeserializer implements JsonDeserializer<Address> {

    /** Matches "Street Name 123" or "Street Name 123-1", captures street and number separately. */
    private static final Pattern STREET_PATTERN = Pattern.compile("^(.+)\\s+(\\d+)\\S*$");

    /**
     * Deserializes a JSON element into an {@link Address} object.
     * Dispatches to the structured-object or plain-string parsing path based on the
     * JSON element type.
     *
     * @param json    the JSON element being deserialized
     * @param typeOfT the type of the desired object (always {@code Address.class})
     * @param context the deserialization context (not used directly)
     * @return the parsed {@link Address}
     * @throws JsonParseException if the JSON cannot be parsed as an address
     */
    @Override
    public Address deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (json.isJsonObject()) {
            // Structured object format: read fields directly
            JsonObject obj = json.getAsJsonObject();
            String street = obj.has("street") ? obj.get("street").getAsString() : "";
            int number = obj.has("number") ? obj.get("number").getAsInt() : 0;
            String city = obj.has("city") ? obj.get("city").getAsString() : "";
            String country = obj.has("country") ? obj.get("country").getAsString() : "";
            String postalCode = obj.has("postalCode") ? obj.get("postalCode").getAsString(): "";
            return new Address(street, number, city, country, postalCode);
        }

        // Plain string format: "Street Number, PostalCode City, Country"
        String raw = json.getAsString().trim();
        String[] parts = raw.split(",\\s*", -1);

        String country = parts.length >= 1 ? parts[parts.length - 1].trim() : "";
        String city = "";
        String postalCode = "";
        String street = "";
        int number = 0;

        if (parts.length >= 2) {
            String middle = parts[parts.length - 2].trim();
            // "08008 Barcelona", 5 digit postal code prefix
            if (middle.matches("\\d{5}\\s+.*")) {
                postalCode = middle.substring(0, 5);
                city = middle.substring(5).trim();
            } else {
                city = middle;
            }
        }

        if (parts.length >= 1) {
            Matcher m = STREET_PATTERN.matcher(parts[0].trim());
            if (m.matches()) {
                street = m.group(1).trim();
                try { number = Integer.parseInt(m.group(2)); } catch (NumberFormatException ignored) {}
            } else {
                street = parts[0].trim();
            }
        }

        return new Address(street, number, city, country, postalCode);
    }
}
