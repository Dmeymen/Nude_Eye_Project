package Business.Entities;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a completed sale transaction.
 * Records which client bought which product, the price paid, and when the purchase occurred.
 * The purchase date is stored as a UNIX timestamp.
 */
public class Sale {

    @SerializedName(value = "client_id", alternate = "clientId")
    private String clientId;

    @SerializedName(value = "product_id", alternate = "productId")
    private String productId;

    @SerializedName(value = "price_paid", alternate = "pricePaid")
    private double pricePaid;

    @SerializedName(value = "purchase_date", alternate = "purchaseDate")
    private long purchaseDate;

    /**
     * Constructs a {@code Sale} with all required fields.
     *
     * @param clientId     the ID of the client who made the purchase
     * @param productId    the ID of the product that was purchased
     * @param pricePaid    the final price paid by the client
     * @param purchaseDate the date and time of the purchase as a UNIX timestamp
     */
    public Sale(String clientId, String productId, double pricePaid, long purchaseDate) {
        this.clientId = clientId;
        this.productId = productId;
        this.pricePaid = pricePaid;
        this.purchaseDate = purchaseDate;
    }

    /**
     * Returns the ID of the client who made this purchase.
     *
     * @return the client ID
     */
    public String getClientId() { return this.clientId; }

    /**
     * Returns the ID of the product that was purchased.
     *
     * @return the product ID
     */
    public String getProductId() { return this.productId; }

    /**
     * Returns the final price paid by the client for this sale, including all applicable taxes.
     *
     * @return the price paid
     */
    public double getPricePaid() { return this.pricePaid; }

    /**
     * Returns the date and time of this purchase as a UNIX timestamp.
     *
     * @return the purchase timestamp
     */
    public long getPurchaseDate() { return this.purchaseDate; }
}
