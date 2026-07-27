package Business.Entities;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a product in the catalogue.
 * Products are identified by a unique ID and described by their name, brand, and model.
 * <p>
 * Subclasses override {@link #calculatePrice(double)} to apply their own VAT rate,
 * hourly multiplier, or near-expiry discount. The base implementation applies the
 * standard 21% VAT used for regular glasses products.
 * </p>
 */
public class Product {

    @SerializedName("product_id")
    private String productId;

    @SerializedName("product_name")
    private String productName;

    @SerializedName("brand")
    private String productBrand;

    @SerializedName("model")
    private String productModel;

    /**
     * Constructs a {@code Product} with all required fields.
     *
     * @param productId    the unique product identifier
     * @param productName  the display name of the product
     * @param productBrand the brand or manufacturer of the product
     * @param productModel the specific model name or number
     */
    public Product(String productId, String productName, String productBrand, String productModel) {
        this.productId    = productId;
        this.productName = productName;
        this.productBrand = productBrand;
        this.productModel = productModel;
    }

    /**
     * Calculates the final product price from the provider's base selling price,
     * applying the VAT rate and any other product-specific pricing rules.
     * <p>
     * The base implementation applies 21% VAT, which is correct for standard glasses.
     * Subclasses override this to apply a different VAT rate (e.g. 10% for contact lenses)
     * or additional rules (e.g. near-expiry discount for consumables, hourly rate for services).
     * </p>
     *
     * @param sellingPrice the provider's base selling price before VAT
     * @return the final price the customer will be charged, including VAT
     */
    public double calculatePrice(double sellingPrice) {
        return sellingPrice * 1.21;
    }

    /**
     * Returns the unique identifier for this product.
     *
     * @return the product ID
     */
    public String getProductId() { return this.productId; }

    /**
     * Returns the display name of this product.
     *
     * @return the product name
     */
    public String getProductName() { return this.productName; }

    /**
     * Returns the brand or manufacturer of this product.
     *
     * @return the product brand
     */
    public String getProductBrand() { return this.productBrand; }

    /**
     * Returns the model name or number of this product.
     *
     * @return the product model
     */
    public String getProductModel() { return this.productModel; }
}