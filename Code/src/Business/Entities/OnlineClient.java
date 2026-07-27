package Business.Entities;

import Business.Customer;
import com.google.gson.annotations.SerializedName;
import geolocationAPI.Address;
import geolocationAPI.Geo;
import geolocationAPI.GeolocationApiManager;
import geolocationAPI.InvalidAddressException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a client who shops online and requires home delivery.
 * Extends the {@link Customer} contract with a shipping address and contact email.
 * <p>
 * Each order carries a one-time shipping surcharge of {@code €2 + €0.01 per km} from
 * the La Salle campus (shipment origin) to the client's delivery address, computed by
 * {@link #getShippingCost()}. Per-item prices are not adjusted; the surcharge is added
 * to the cart total at checkout time.
 * </p>
 * <p>
 * Online clients cannot purchase {@link Service} products, as services require
 * the customer to be physically present at the store.
 * </p>
 */
public class OnlineClient implements Customer {

    /** Geolocation coordinates of the La Salle campus (shipment origin). */
    private static final double STORE_LAT = 41.408879;
    private static final double STORE_LON = 2.130103;

    private GeolocationApiManager geoManager;

    @SerializedName("client_type")
    private final String clientType = "online";

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("full_name")
    private String clientName;

    @SerializedName("phone_numbers")
    private List<PhoneNumber> clientPhoneNumbers;

    @SerializedName("address")
    private Address shippingAddress;

    @SerializedName("contact_email")
    private String email;


    /**
     * Constructs an {@code OnlineClient} with all required fields.
     *
     * @param clientId        the unique client identifier
     * @param clientName      the full name of the client
     * @param phoneNumbers    the phone numbers associated with the client
     * @param shippingAddress the delivery address for online orders
     * @param email           the contact email address
     */
    public OnlineClient(String clientId, String clientName, List<PhoneNumber> phoneNumbers,
                        Address shippingAddress, String email) {
        this.geoManager = new GeolocationApiManager();
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientPhoneNumbers = (phoneNumbers == null) ? new ArrayList<>() : new ArrayList<>(phoneNumbers);
        this.shippingAddress = shippingAddress;
        this.email = email;
    }

    /**
     * Returns {@code 1.0}: online clients pay the full VAT-inclusive product price
     * with no per-item discount or surcharge.
     * The shipping surcharge is a one-time order-level addition returned by
     * {@link #getShippingCost()} and added to the cart total at checkout time.
     *
     * @return always {@code 1.0}
     */
    @Override
    public double getPricingStrategy() {
        return 1.0;
    }

    /**
     * Calculates the one-time shipping cost for this client's order.
     * Uses the geolocation API to determine the distance from the La Salle campus
     * (shipment origin) to the client's shipping address, then applies
     * {@code €2 + €0.01 per km}.
     * <p>
     * Returns {@code 0.0} if the address is unset or the geolocation lookup fails.
     * </p>
     *
     * @return the shipping surcharge in euros
     */
    @Override
    public double getShippingCost() {
        if (shippingAddress == null) return 0.0;
        try {
            if (geoManager == null) {
                geoManager = new GeolocationApiManager();
            }
            Geo storeLocation  = new Geo(STORE_LAT, STORE_LON);
            Geo clientLocation = geoManager.getGeolocationFromAddress(shippingAddress);
            double distanceKm  = geoManager.calculateDistance(storeLocation, clientLocation);
            return 2 + 0.01 * distanceKm;
        } catch (IOException | InvalidAddressException e) {
            return 0.0;
        }
    }

    /**
     * Returns {@code false} if the product is a {@link Service}, as services require
     * physical presence in the store. Returns {@code true} for all other product types.
     *
     * @param product the product the customer wishes to purchase
     * @return {@code false} if {@code product} is a {@link Service}, {@code true} otherwise
     */
    @Override
    public boolean canPurchase(Product product) {
        return !(product instanceof Service);
    }

    /**
     * Returns the unique identifier for this client.
     *
     * @return the client ID
     */
    @Override
    public String getClientId() { return clientId; }

    /**
     * Returns the full name of this client.
     *
     * @return the client's name
     */
    @Override
    public String getName() { return clientName; }

    /**
     * Returns an unmodifiable view of the phone numbers registered for this client.
     *
     * @return the phone number list
     */
    @Override
    public List<PhoneNumber> getPhoneNumber() {
        return clientPhoneNumbers == null ? Collections.emptyList() : Collections.unmodifiableList(clientPhoneNumbers);
    }

    /**
     * Returns the delivery address for this client's online orders.
     *
     * @return the shipping address
     */
    public Address getShippingAddress() { return shippingAddress; }

    /**
     * Returns the contact email address for this client.
     *
     * @return the email address
     */
    public String getEmail() { return email; }
}
