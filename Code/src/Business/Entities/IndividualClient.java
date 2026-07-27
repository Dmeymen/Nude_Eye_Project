package Business.Entities;

import Business.Customer;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents an individual (retail) client who shops in person at the store.
 * Implements the {@link Customer} interface and provides a pricing strategy
 * based on customer loyalty (number of registered phone numbers).
 * <p>
 * Individual clients have no product-type restrictions and may purchase
 * glasses, contact lenses, consumables, and in-store services.
 * </p>
 */
public class IndividualClient implements Customer {

    @SerializedName("client_type")
    private final String clientType = "regular";

    @SerializedName(value = "client_id", alternate = "clientId")
    private String clientId;

    @SerializedName(value = "full_name", alternate = "clientName")
    private String clientName;

    @SerializedName(value = "phone_numbers", alternate = "clientPhoneNumbers")
    private List<PhoneNumber> clientPhoneNumbers;

    /**
     * Constructs an {@code IndividualClient} with the given details.
     *
     * @param clientId     the unique client identifier
     * @param clientName   the full name of the client
     * @param phoneNumbers the list of phone numbers associated with the client
     */
    public IndividualClient(String clientId, String clientName, List<PhoneNumber> phoneNumbers) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientPhoneNumbers = phoneNumbers;
    }

    /**
     * Returns the unique identifier for this client.
     *
     * @return the client ID
     */
    @Override
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Returns the full name of this client.
     *
     * @return the client's name
     */
    @Override
    public String getName() {
        return this.clientName;
    }

    /**
     * Returns the list of phone numbers registered for this client.
     *
     * @return the phone number list
     */
    @Override
    public List<PhoneNumber> getPhoneNumber() {
        return this.clientPhoneNumbers;
    }

    /**
     * Returns the pricing strategy multiplier for this client.
     * A standard client pays the full price (multiplier = 1.0).
     * Each additional registered phone number grants a 5% loyalty discount,
     * with a maximum 20% discount (multiplier ≥ 0.80).
     *
     * @return a price multiplier between 0.80 and 1.0 (inclusive)
     */
    @Override
    public double getPricingStrategy() {
        int phones = (clientPhoneNumbers == null) ? 0 : clientPhoneNumbers.size();
        double discount = Math.min(phones * 0.05, 0.20);
        return 1.0 - discount;
    }

    /**
     * Returns {@code true} for all product types.
     * Individual in-store clients may purchase any product, including services.
     *
     * @param product the product to check
     * @return always {@code true}
     */
    @Override
    public boolean canPurchase(Product product) {
        return true;
    }

    /**
     * Returns the full name of this client.
     * Alias for {@link #getName()} retained for backwards compatibility.
     *
     * @return the client's name
     */
    public String getClientName() {
        return this.clientName;
    }

    /**
     * Returns the pricing strategy multiplier.
     * Alias for {@link #getPricingStrategy()} retained for backwards compatibility.
     *
     * @return the price multiplier
     */
    public double getClientPricingStrategy() {
        return getPricingStrategy();
    }

    /**
     * Returns the list of phone numbers registered for this client.
     * Alias for {@link #getPhoneNumber()} retained for backwards compatibility.
     *
     * @return the phone number list
     */
    public List<PhoneNumber> getClientPhoneNumber() {
        return this.clientPhoneNumbers;
    }
}
