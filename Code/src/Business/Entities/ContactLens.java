package Business.Entities;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a contact lens product in the catalogue.
 * Extends {@link Product} with prescription, lens count, and UV filter information.
 * <p>
 * Contact lenses are classified as a healthcare product in Spain and therefore
 * carry a reduced VAT rate of 10% instead of the standard 21%.
 * </p>
 */
public class ContactLens extends Product {

    /** The reduced VAT rate applicable to healthcare products in Spain. */
    private static final double VAT_RATE = 0.10;

    @SerializedName("prescription")
    private double prescription;

    @SerializedName("lens_count")
    private int lensCount;

    @SerializedName("uv_filter")
    private boolean uvFilter;

    /**
     * Constructs a {@code ContactLens} with all required fields.
     *
     * @param productId    the unique product identifier
     * @param productName  the display name of the product
     * @param productBrand the brand or manufacturer
     * @param productModel the model name or number
     * @param prescription the optical prescription value (between -12.00 and +8.00)
     * @param lensCount    the number of lenses included in the package
     * @param uvFilter     {@code true} if the lenses include a UV filter
     */
    public ContactLens(String productId, String productName, String productBrand, String productModel,
                       double prescription, int lensCount, boolean uvFilter) {
        super(productId, productName, productBrand, productModel);
        this.prescription = prescription;
        this.lensCount = lensCount;
        this.uvFilter = uvFilter;
    }

    /**
     * Calculates the final price for this contact lens product.
     * Applies the reduced 10% VAT rate for healthcare products.
     *
     * @param sellingPrice the provider's base selling price before VAT
     * @return the final price including 10% VAT
     */
    @Override
    public double calculatePrice(double sellingPrice) {
        return sellingPrice * (1 + VAT_RATE);
    }

    /**
     * Returns the optical prescription value for these contact lenses.
     * Valid range is between -12.00 and +8.00.
     *
     * @return the prescription value
     */
    public double getPrescription() { return this.prescription; }

    /**
     * Returns the number of lenses included in the package.
     *
     * @return the lens count
     */
    public int getLensCount() { return this.lensCount; }

    /**
     * Returns whether these contact lenses include a UV filter.
     *
     * @return {@code true} if a UV filter is present
     */
    public boolean hasUvFilter() { return this.uvFilter; }
}