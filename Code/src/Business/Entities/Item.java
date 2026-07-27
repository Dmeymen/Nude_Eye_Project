package Business.Entities;

/**
 * Represents a single line item in a {@link ShoppingCart}.
 * Combines a product, the specific provider it is being purchased from,
 * and the strategy-adjusted price applicable to the current client.
 */
public class Item {

    private Product shoppingProduct;
    private Provider shoppingProvider;
    private double priceStrategy;

    /**
     * Constructs an {@code Item} with the given product, provider, and adjusted price.
     *
     * @param shoppingProduct  the product being purchased
     * @param shoppingProvider the provider supplying the product
     * @param priceStrategy    the base selling price after applying the client's pricing multiplier
     */
    public Item(Product shoppingProduct, Provider shoppingProvider, double priceStrategy) {
        this.shoppingProduct = shoppingProduct;
        this.shoppingProvider = shoppingProvider;
        this.priceStrategy = priceStrategy;
    }

    /**
     * Returns the product associated with this item.
     *
     * @return the {@link Product}
     */
    public Product getProduct() { return this.shoppingProduct; }

    /**
     * Returns the provider supplying this item.
     *
     * @return the {@link Provider}
     */
    public Provider getProvider() { return this.shoppingProvider; }

    /**
     * Returns the strategy-adjusted price for this item.
     *
     * @return the adjusted price
     */
    public double getPriceStrategy() { return this.priceStrategy; }
}