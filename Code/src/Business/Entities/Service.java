package Business.Entities;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a service offered by an optical store (e.g. eye test, glasses repair).
 * Extends {@link Product} with a completion time in hours.
 * <p>
 * Services do not have a brand or model — those fields are always {@code null}.
 * The provider's selling price represents the cost <em>per hour</em>, so the final price
 * is calculated as: {@code sellingPrice × completionHours × 1.21} (21% VAT included).
 * </p>
 * <p>
 * Services can only be purchased by customers physically present in the store
 * ({@link Business.Entities.IndividualClient}). They cannot be sold to
 * {@link OnlineClient} or {@link CorporateClient} instances. This rule is enforced
 * via {@link Business.Customer#canPurchase(Product)}.
 * </p>
 */
public class Service extends Product {

    private static final double VAT_RATE = 0.21;

    @SerializedName("completion_hours")
    private double completionHours;

    /**
     * Constructs a {@code Service} with all required fields.
     * Brand and model are not applicable to services and are passed as {@code null}.
     *
     * @param productId       the unique service identifier
     * @param productName     the display name of the service
     * @param completionHours the estimated time the service will take, in hours
     */
    public Service(String productId, String productName, double completionHours) {
        super(productId, productName, null, null);
        this.completionHours = completionHours;
    }

    /**
     * Calculates the final price for this service.
     * The provider's selling price is a cost per hour, so it is first multiplied by
     * the number of hours the service requires, then 21% VAT is added.
     *
     * @param sellingPrice the provider's hourly rate before VAT
     * @return the total service price including 21% VAT
     */
    @Override
    public double calculatePrice(double sellingPrice) {
        return sellingPrice * completionHours * (1 + VAT_RATE);
    }

    /**
     * Returns the estimated completion time for this service in hours.
     *
     * @return the completion time in hours
     */
    public double getCompletionHours() { return this.completionHours; }
}