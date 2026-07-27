package Business.Entities;

import java.util.List;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a product provider (supplier) in the system.
 * Each provider has company information, a contact person, and a list of
 * products they offer for sale along with their prices and stock levels.
 */
public class Provider {

    @SerializedName("provider_id")
    private String providerId;

    @SerializedName("company_name")
    private String providerCompanyName;

    @SerializedName("cif")
    private String providerCif;

    @SerializedName("contact_name")
    private String contactName;

    @SerializedName("phone")
    private String providerPhone;

    @SerializedName("email")
    private String providerEmail;

    @SerializedName("products_for_sale")
    private List<ProductStock> providerProductsForSale;

    /**
     * Constructs a {@code Provider} with all required fields.
     *
     * @param providerId              the unique provider identifier
     * @param providerCompanyName     the company's trading name
     * @param providerCif             the company's tax identification number (CIF)
     * @param contactName             the name of the contact person at this provider
     * @param providerPhone           the provider's phone number
     * @param providerEmail           the provider's email address
     * @param providerProductsForSale the list of products this provider sells
     */
    public Provider(String providerId, String providerCompanyName, String providerCif,
                    String contactName, String providerPhone, String providerEmail,
                    List<ProductStock> providerProductsForSale) {
        this.providerId = providerId;
        this.providerCompanyName = providerCompanyName;
        this.providerCif = providerCif;
        this.contactName = contactName;
        this.providerPhone = providerPhone;
        this.providerEmail = providerEmail;
        this.providerProductsForSale = providerProductsForSale;
    }

    /**
     * Returns the unique identifier for this provider.
     *
     * @return the provider ID
     */
    public String getProviderId() { return this.providerId; }

    /**
     * Returns the company name of this provider.
     *
     * @return the company name
     */
    public String getProviderCompanyName() { return this.providerCompanyName; }

    /**
     * Returns the tax identification number (CIF) of this provider.
     *
     * @return the CIF
     */
    public String getProviderCif() { return this.providerCif; }

    /**
     * Returns the name of the contact person at this provider.
     *
     * @return the contact name
     */
    public String getContactName() { return this.contactName; }

    /**
     * Returns the phone number of this provider.
     *
     * @return the phone number
     */
    public String getProviderPhone() { return this.providerPhone; }

    /**
     * Returns the email address of this provider.
     *
     * @return the email address
     */
    public String getProviderEmail() { return this.providerEmail; }

    /**
     * Returns the {@link ProductStock} entry for the given product ID,
     * or {@code null} if this provider does not sell that product.
     *
     * @param productId the ID of the product to look up
     * @return the matching {@link ProductStock}, or {@code null}
     */
    public ProductStock getProviderProductById(String productId) {
        if (providerProductsForSale == null) return null;
        return providerProductsForSale.stream()
                .filter(s -> s.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the full list of products this provider has for sale,
     * including their prices and stock levels.
     *
     * @return the list of {@link ProductStock} entries; may be {@code null} if not set
     */
    public List<ProductStock> getAllProductStock() { return this.providerProductsForSale; }
}