package Business;

import Business.Customer;
import Business.Entities.Item;
import Business.Entities.Product;
import Business.Entities.ProductStock;
import Business.Entities.Provider;
import Business.Entities.ShoppingCart;
import Business.Exceptions.BusinessException;
import Business.Exceptions.BusinessFileNotFoundException;
import Business.Exceptions.BusinessWriteException;
import Persistence.Exceptions.PersistenceFileNotFoundException;
import Persistence.Exceptions.PersistenceException;
import Persistence.ProductDAO;
import Persistence.ProviderDAO;

/**
 * Orchestrates shopping cart operations and checkout.
 * Resolves the full {@link Product} from a {@link ProductStock} entry, applies the
 * client's pricing strategy multiplier, and manages stock decrements and sale
 * recording during checkout.
 */
public class ShoppingManager {

    private final SaleManager saleManager;
    private final ProductDAO productDAO;
    private final ProviderDAO providerDAO;

    /**
     * Constructs a {@code ShoppingManager} with the required collaborators.
     *
     * @param saleManager  records completed sales
     * @param productDAO   resolves full product details from a product ID
     * @param providerDAO  decrements provider stock on checkout
     */
    public ShoppingManager(SaleManager saleManager, ProductDAO productDAO, ProviderDAO providerDAO) {
        this.saleManager = saleManager;
        this.productDAO  = productDAO;
        this.providerDAO = providerDAO;
    }

    /**
     * Resolves the full product from the given stock entry, verifies the client is
     * allowed to purchase it, calculates the client-adjusted final price via
     * {@link Customer#calculateFinalPrice(Product, double)}, and adds the resulting
     * {@link Item} to the cart.
     *
     * @param cart         the shopping cart to add the item to
     * @param productStock the provider's stock entry identifying the product and base price
     * @param provider     the provider supplying the product
     * @param client       the currently logged-in client
     * @throws BusinessException if the client cannot purchase this product type,
     *                           or if the product cannot be loaded from the data source
     */
    public void addToCart(ShoppingCart cart, ProductStock productStock, Provider provider, Customer client) throws BusinessException {
        try {
            Product product = productDAO.findById(productStock.getProductId());
            if (product == null) {
                throw new BusinessException("Product not found: " + productStock.getProductId());
            }
            if (!client.canPurchase(product)) {
                throw new BusinessException("This type of client cannot purchase services.");
            }
            double finalPrice = client.calculateFinalPrice(product, productStock.getSellingPrice());
            Item item = new Item(product, provider, finalPrice);
            cart.addItem(item);
        } catch (PersistenceException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Returns the total price the client will pay for the cart contents, including any
     * one-time shipping surcharge for {@link Business.Entities.OnlineClient} customers.
     *
     * @param cart   the shopping cart
     * @param client the currently logged-in client
     * @return the sum of all item prices plus the client's shipping cost
     */
    public double getCartTotal(ShoppingCart cart, Customer client) {
        return cart.getTotalPrice() + client.getShippingCost();
    }

    /**
     * Removes the item at the given zero-based index from the cart.
     *
     * @param cart  the shopping cart to modify
     * @param index the zero-based index of the item to remove
     */
    public void removeFromCart(ShoppingCart cart, int index) {
        cart.removeItem(index);
    }

    /**
     * Processes the checkout for all items in the cart.
     * For each item: records a sale via {@link SaleManager} and decrements the
     * provider's stock by 1 unit. Clears the cart on success.
     *
     * @param cart   the cart containing the items to purchase
     * @param client the client completing the purchase
     * @throws BusinessException if a sale cannot be recorded or stock cannot be updated
     */
    public void checkout(ShoppingCart cart, Customer client) throws BusinessException {
        try {
            for (Item item : cart.getItems()) {
                String productId  = item.getProduct().getProductId();
                String providerId = item.getProvider().getProviderId();

                saleManager.recordSale(client.getClientId(), productId, item.getPriceStrategy());
                providerDAO.updateStock(providerId, productId, 1);
            }
            cart.clearShoppingCart();
        } catch (PersistenceFileNotFoundException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        } catch (PersistenceException e) {
            throw new BusinessWriteException(e.getMessage(), e);
        }
    }
}
