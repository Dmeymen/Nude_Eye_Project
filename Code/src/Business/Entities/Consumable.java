package Business.Entities;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;

/**
 * Represents a consumable product in the catalogue (e.g. eye drops, cleaning solutions).
 * Extends {@link Product} with volume, expiration date, and preservative information.
 * <p>
 * Consumables carry the standard 21% VAT. However, if the expiration date is less than
 * three months from today, a 40% discount is applied to the VAT-inclusive price to
 * incentivise clearance of near-expiry stock.
 * </p>
 */
public class Consumable extends Product {

    private static final double VAT_RATE = 0.21;
    private static final double NEAR_EXPIRY_DISCOUNT = 0.40;
    private static final int NEAR_EXPIRY_MONTHS = 3;

    @SerializedName("volume_ml")
    private double volumeMl;

    @SerializedName("expiration_date")
    private LocalDate expirationDate;

    @SerializedName("has_preservatives")
    private boolean hasPreservatives;

    /**
     * Constructs a {@code Consumable} with all required fields.
     *
     * @param productId       the unique product identifier
     * @param productName     the display name of the product
     * @param productBrand    the brand or manufacturer
     * @param productModel    the model name or number
     * @param volumeMl        the volume of the product in millilitres
     * @param expirationDate  the date after which the product should not be used
     * @param hasPreservatives {@code true} if the product contains preservatives
     */
    public Consumable(String productId, String productName, String productBrand, String productModel,
                      double volumeMl, LocalDate expirationDate, boolean hasPreservatives) {
        super(productId, productName, productBrand, productModel);
        this.volumeMl = volumeMl;
        this.expirationDate = expirationDate;
        this.hasPreservatives = hasPreservatives;
    }

    /**
     * Returns whether this product's expiration date is less than three months from today.
     * When {@code true}, a 40% discount is applied by {@link #calculatePrice(double)}.
     *
     * @return {@code true} if the product expires within the next three months
     */
    public boolean isNearExpiry() {
        if (expirationDate == null) return false;
        // The product is near expiry if today is after the threshold date (expiry minus 3 months)
        return LocalDate.now().isAfter(expirationDate.minusMonths(NEAR_EXPIRY_MONTHS));
    }

    /**
     * Calculates the final price for this consumable.
     * Applies 21% VAT first; if the product is within three months of its expiration date,
     * a further 40% discount is applied to the VAT-inclusive total.
     *
     * @param sellingPrice the provider's base selling price before VAT
     * @return the final price including VAT and any applicable near-expiry discount
     */
    @Override
    public double calculatePrice(double sellingPrice) {
        double priceWithVat = sellingPrice * (1 + VAT_RATE);
        if (isNearExpiry()) {
            return priceWithVat * (1 - NEAR_EXPIRY_DISCOUNT);
        }
        return priceWithVat;
    }

    /**
     * Returns the volume of the product in millilitres.
     *
     * @return the volume in ml
     */
    public double getVolumeMl() { return this.volumeMl; }

    /**
     * Returns the expiration date of this product.
     *
     * @return the expiration date
     */
    public LocalDate getExpirationDate() { return this.expirationDate; }

    /**
     * Returns whether this product contains preservatives.
     *
     * @return {@code true} if preservatives are present
     */
    public boolean hasPreservatives() { return this.hasPreservatives; }
}