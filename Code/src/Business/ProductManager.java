package Business;

import Business.Entities.Product;
import Business.Exceptions.BusinessException;
import Business.Exceptions.BusinessFileNotFoundException;
import Persistence.Exceptions.PersistenceException;
import Persistence.ProductDAO;

import java.util.List;

/**
 * Business-layer facade for product search operations.
 * Delegates to a {@link ProductDAO} implementation and translates
 * {@link Persistence.Exceptions.PersistenceException} into {@link BusinessException}
 * so the Presentation layer has no dependency on the Persistence layer.
 */
public class ProductManager {

    private final ProductDAO productDAO;

    /**
     * Constructs a {@code ProductManager} backed by the given DAO.
     *
     * @param productDAO the DAO used to retrieve product data
     */
    public ProductManager(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * Returns all products whose name contains the given keyword (case-insensitive).
     *
     * @param keyword the search term to match against product names
     * @return a list of matching products; never {@code null}
     * @throws BusinessException if the underlying data source cannot be accessed
     */
    public List<Product> findProductsByName(String keyword) throws BusinessException {
        try {
            return productDAO.findByName(keyword);
        } catch (PersistenceException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Returns the product with the given ID, or {@code null} if not found.
     *
     * @param productId the product ID to search for
     * @return the matching {@link Product}, or {@code null}
     * @throws BusinessException if the underlying data source cannot be accessed
     */
    public Product findProductById(String productId) throws BusinessException {
        try {
            return productDAO.findById(productId);
        } catch (PersistenceException e) {
            throw new BusinessFileNotFoundException(e.getMessage(), e);
        }
    }
}
