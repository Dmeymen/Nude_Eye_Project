package Business.Entities;

import java.util.List;

/**
 * Represents a shopping cart holding a collection of {@link Item} objects
 * selected by a client during a session.
 * The cart exists only for the duration of a login session
 * and is not persisted to disk.
 * <p>
 * The final price of each item — including the correct VAT rate, hourly multiplier,
 * near-expiry discount, and client-specific surcharge — is computed once at
 * {@code addToCart} time and stored inside the {@link Item}. Therefore
 * {@link #getTotalPrice()} simply sums the already-final per-item prices with
 * no additional multiplier.
 * </p>
 */
public class ShoppingCart {

    private List<Item> items;

    /**
     * Constructs a {@code ShoppingCart} with the given initial item list.
     *
     * @param items the initial list of items; should be a mutable list
     */
    public ShoppingCart(List<Item> items) {
        this.items = items;
    }

    /**
     * Adds an item to the cart.
     *
     * @param item the {@link Item} to add
     */
    public void addItem(Item item) {
        this.items.add(item);
    }

    /**
     * Returns the list of items currently in the cart.
     *
     * @return the list of {@link Item} objects; never {@code null}
     */
    public List<Item> getItems() { return this.items; }

    /**
     * Returns the total price of all items in the cart.
     * Each item's price already includes the applicable VAT, product-specific adjustments
     * (e.g. near-expiry discount, hourly rate for services), and client-specific surcharges
     * (e.g. shipping cost for online clients). No further multiplier is applied here.
     *
     * @return the total price of the cart
     */
    public double getTotalPrice() {
        return this.items.stream()
                .mapToDouble(Item::getPriceStrategy)
                .sum();
    }

    /**
     * Removes the item at the given zero-based index from the cart.
     *
     * @param index the zero-based index of the item to remove
     */
    public void removeItem(int index) { this.items.remove(index); }

    /**
     * Removes all items from the cart.
     */
    public void clearShoppingCart() { this.items.clear(); }
}