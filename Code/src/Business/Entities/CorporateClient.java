package Business.Entities;

import Business.Customer;
import com.google.gson.annotations.SerializedName;
import geolocationAPI.Address;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a corporate (B2B) client in the system.
 * Extends the {@link Customer} contract with a tax identification number (NIF),
 * a contact person name, a billing address, and a shipping address.
 * <p>
 * VAT handling for corporate clients depends on the billing address country:
 * companies with a billing address <em>outside Spain</em> are exempt from VAT and
 * therefore use a pricing strategy multiplier of {@code 1.0} (no VAT applied).
 * Companies billed within Spain pay the standard 21% VAT, represented by a
 * multiplier of {@code 1.21}.
 * </p>
 * <p>
 * Corporate clients cannot purchase {@link Service} products, as services require
 * the customer to be physically present at the store.
 * </p>
 */
public class CorporateClient implements Customer {

    @SerializedName("client_type")
    private final String clientType = "corporate";

    @SerializedName("client_id")
    private String clientId;

    @SerializedName(value = "cif", alternate = "nif")
    private String nif;

    @SerializedName("contact_name")
    private String contactName;

    @SerializedName("phone_numbers")
    private List<PhoneNumber> clientPhoneNumbers;

    @SerializedName("billing_address")
    private Address billingAddress;

    @SerializedName("mailing_address")
    private Address shippingAddress;

    /**
     * Constructs a {@code CorporateClient} with all required fields.
     *
     * @param clientId       the unique client identifier
     * @param nif            the company's tax identification number (NIF)
     * @param contactName    the name of the contact person at the company
     * @param phoneNumbers   the phone numbers associated with the company
     * @param billingAddress the address used for invoicing (determines VAT exemption)
     * @param shippingAddress the address used for delivery
     */
    public CorporateClient(String clientId, String nif, String contactName,
                           List<PhoneNumber> phoneNumbers,
                           Address billingAddress, Address shippingAddress) {
        this.clientId = clientId;
        this.nif = nif;
        this.contactName = contactName;
        this.clientPhoneNumbers = (phoneNumbers == null) ? new ArrayList<>() : new ArrayList<>(phoneNumbers);
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
    }

    /**
     * Returns the VAT multiplier for this corporate client.
     * <p>
     * Companies with a billing address outside Spain are VAT-exempt: multiplier {@code 1.0}.
     * All other companies pay the standard 21% VAT: multiplier {@code 1.21}.
     * </p>
     * <p>
     * This value is applied directly to the provider's <em>base selling price</em> by
     * {@link #calculateFinalPrice(Product, double)} —
     * it is NOT layered on top of a product VAT calculation.
     * </p>
     *
     * @return {@code 1.0} for VAT-exempt companies, {@code 1.21} for Spanish companies
     */
    @Override
    public double getPricingStrategy() {
        if (billingAddress != null &&
                !"España".equalsIgnoreCase(billingAddress.getCountry())) {
            return 1.0;
        }
        return 1.21;
    }

    /**
     * Calculates the final price for a corporate client by applying the VAT multiplier
     * directly to the provider's base selling price — bypassing the product's own
     * {@link Business.Entities.Product#calculatePrice(double)} so that product-specific
     * VAT rates do not compound with the corporate VAT rule.
     * <ul>
     *   <li>Spain: {@code sellingPrice × 1.21} (standard 21% VAT)</li>
     *   <li>Outside Spain: {@code sellingPrice × 1.0} (VAT-exempt, base price only)</li>
     * </ul>
     *
     * @param product      the product being purchased (not used for price calculation)
     * @param sellingPrice the provider's base selling price before tax
     * @return the final price this corporate client pays
     */
    @Override
    public double calculateFinalPrice(Product product, double sellingPrice) {
        return sellingPrice * getPricingStrategy();
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
     * Returns the name of the contact person at the company.
     *
     * @return the contact name
     */
    @Override
    public String getName() { return contactName; }

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
     * Returns the tax identification number (NIF) for this company.
     *
     * @return the NIF
     */
    public String getNif() { return nif; }

    /**
     * Returns the billing address used for invoicing.
     * This address also determines whether the company is VAT-exempt.
     *
     * @return the billing address
     */
    public Address getBillingAddress() { return billingAddress; }

    /**
     * Returns the delivery address for this company's orders.
     *
     * @return the shipping address
     */
    public Address getShippingAddress() { return shippingAddress; }
}