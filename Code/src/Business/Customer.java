package Business;

import Business.Entities.PhoneNumber;
import Business.Entities.Product;

import java.util.List;

/**
 * Represents a customer in the system.
 * Defines the common contract for all customer types, including
 * identity, contact information, pricing strategy, and purchase eligibility.
 */
public interface Customer {

    /**
     * Returns the unique identifier for this customer.
     *
     * @return the client ID
     */
    String getClientId();

    /**
     * Returns the full name of this customer.
     *
     * @return the customer's name
     */
    String getName();

    /**
     * Returns the list of phone numbers registered for this customer.
     *
     * @return the phone number list; may be empty but never {@code null}
     */
    List<PhoneNumber> getPhoneNumber();

    /**
     * Returns the per-item price multiplier for this customer.
     * A value of {@code 1.0} means full product price; values below {@code 1.0} represent
     * loyalty discounts (e.g. {@link Business.Entities.IndividualClient}).
     * Corporate clients return the VAT factor ({@code 1.0} or {@code 1.21}), which
     * {@link Business.Entities.CorporateClient#calculateFinalPrice} applies directly to the
     * provider's selling price — not on top of a product VAT calculation.
     * Online clients return {@code 1.0}; their order-level shipping surcharge is
     * returned separately by {@link #getShippingCost()}.
     *
     * @return a positive price multiplier
     */
    double getPricingStrategy();

    /**
     * Returns whether this customer is allowed to purchase the given product.
     * <p>
     * This method encapsulates customer-type-specific purchase restrictions without
     * requiring the caller to perform {@code instanceof} checks. For example,
     * {@link Business.Entities.Service} products can only be sold to in-store
     * ({@link Business.Entities.IndividualClient}) customers.
     * </p>
     *
     * @param product the product the customer wishes to purchase
     * @return {@code true} if this customer type may purchase the given product
     */
    boolean canPurchase(Product product);

    /**
     * Calculates the final price this customer pays for the given product at the given
     * provider selling price, applying any VAT rules, discounts, or special pricing
     * specific to this customer type.
     * <p>
     * The default implementation applies the product's own {@link Product#calculatePrice(double)}
     * (which includes the product-type-specific VAT) and then multiplies by
     * {@link #getPricingStrategy()} (e.g. a loyalty discount for individual clients).
     * Client types that bypass product-level VAT entirely (e.g. corporate clients)
     * must override this method.
     * </p>
     *
     * @param product      the product being purchased
     * @param sellingPrice the provider's base selling price before any tax
     * @return the final price this customer will be charged
     */
    default double calculateFinalPrice(Product product, double sellingPrice) {
        return product.calculatePrice(sellingPrice) * getPricingStrategy();
    }

    /**
     * Returns the one-time shipping surcharge added to the cart total for this customer.
     * The default implementation returns {@code 0.0} (no shipping cost).
     * {@link Business.Entities.OnlineClient} overrides this to compute the distance-based fee.
     *
     * @return the shipping surcharge in euros
     */
    default double getShippingCost() {
        return 0.0;
    }
}