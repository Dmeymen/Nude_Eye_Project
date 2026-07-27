package Business.Entities;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a pair of glasses in the product catalogue.
 * Extends {@link Product} with frame type and lens material information.
 * <p>
 * Glasses carry the standard 21% VAT rate. The {@link #calculatePrice(double)}
 * implementation in this class is equivalent to the base {@link Product}
 * implementation; it is overridden here to make the VAT rate explicit and
 * to allow future customisation without touching the base class.
 * </p>
 */
public class Glasses extends Product {

    /** The standard VAT rate applied to glasses products. */
    private static final double VAT_RATE = 0.21;

    @SerializedName("frame_type")
    private String frameType;

    @SerializedName("lens_material")
    private String lensMaterial;

    /**
     * Constructs a {@code Glasses} product with all required fields.
     *
     * @param productId    the unique product identifier
     * @param productName  the display name of the product
     * @param productBrand the brand or manufacturer
     * @param productModel the specific model name or number
     * @param frameType    the type of frame (e.g. "full-rim", "semi-rimless", "rimless")
     * @param lensMaterial the material used for the lenses (e.g. "polycarbonate", "glass")
     */
    public Glasses(String productId, String productName, String productBrand,
                   String productModel, String frameType, String lensMaterial) {
        super(productId, productName, productBrand, productModel);
        this.frameType = frameType;
        this.lensMaterial = lensMaterial;
    }

    /**
     * Calculates the final price for this glasses product.
     * Applies the standard 21% VAT rate to the provider's base selling price.
     *
     * @param sellingPrice the provider's base selling price before VAT
     * @return the final price including 21% VAT
     */
    @Override
    public double calculatePrice(double sellingPrice) {
        return sellingPrice * (1 + VAT_RATE);
    }

    /**
     * Returns the frame type of these glasses (e.g. "full-rim", "rimless").
     *
     * @return the frame type
     */
    public String getFrameType()    { return frameType; }

    /**
     * Returns the lens material used in these glasses (e.g. "polycarbonate").
     *
     * @return the lens material
     */
    public String getLensMaterial() { return lensMaterial; }
}