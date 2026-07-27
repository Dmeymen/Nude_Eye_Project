package Business.Entities;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a product entry in a provider's inventory,
 * including its price and current stock level.
 */
public class ProductStock {

    @SerializedName("product_id")
    private String productId;

    @SerializedName("selling_price")
    private double sellingPrice;

    @SerializedName("units_in_stock")
    private int unitsInStock;

    /**
     * Constructs a {@code ProductStock} entry.
     *
     * @param productId     the ID of the product
     * @param sellingPrice  the price at which this provider sells the product
     * @param unitsInStock  the number of units currently available
     */
    public ProductStock(String productId, double sellingPrice, int unitsInStock) {
        this.productId = productId;
        this.sellingPrice = sellingPrice;
        this.unitsInStock = unitsInStock;
    }

    /**
     * Returns the product ID.
     *
     * @return the product ID
     */
    public String getProductId() { return this.productId; }

    /**
     * Returns the selling price for this product at this provider.
     *
     * @return the selling price
     */
    public double getSellingPrice() { return this.sellingPrice; }

    /**
     * Returns the number of units currently in stock.
     *
     * @return units in stock
     */
    public int getUnitsInStock() { return this.unitsInStock; }

    /**
     * Sets the number of units in stock to an exact value.
     *
     * @param units the new stock level (should be &gt;= 0)
     */
    public void setUnitsInStock(int units) { this.unitsInStock = units; }

    /**
     * Decreases the stock level by the given number of units.
     * The result is stopped at zero to prevent negative stock.
     *
     * @param units the number of units to subtract
     */
    public void decreaseUnits(int units) {
        this.unitsInStock = Math.max(0, this.unitsInStock - units);
    }
}