import Business.ClientManager;
import Business.ProductManager;
import Business.ProviderManager;
import Business.SaleManager;
import Business.ShoppingManager;
import Persistence.Exceptions.PersistenceException;
import Persistence.Impl.ClientCloudDAO;
import Persistence.Impl.ProductCloudDAO;
import Persistence.Impl.ProductJsonDAO;
import Persistence.Impl.ProviderCloudDAO;
import Persistence.Impl.ProviderJsonDAO;
import Persistence.Impl.SaleCloudDAO;
import Presentation.ViewController;
import edu.salle.url.api.ApiHelper;
import edu.salle.url.api.exception.ApiException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point of the Nude Eye Project application.
 * <p>
 * Checks API availability at startup; if the API is unreachable, verifies
 * that the required local JSON files exist before continuing.
 * Loads all DAO implementations, business layer managers, and the
 * {@link Presentation.ViewController}, then hands control to the controller.
 * </p>
 */
public class Main {

    /**
     * Application entry point.
     * Initialises persistence and business objects, creates the file-based data
     * stores if they do not already exist, and starts the user-interface loop.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        suppressGeolocationInfoLogs();

        System.out.println("Welcome to Nude Eye Project.");
        System.out.println("\"by glass wearers for glass wearers\"");
        System.out.println();

        boolean apiAvailable = checkApiStatus();

        if (!apiAvailable) {
            System.out.println("Verifying local files...");
            if (!verifyLocalFiles()) {
                System.out.println("Shutting down...");
                System.exit(1);
            }
        }

        System.out.println("Starting program...");

        try {
            ClientCloudDAO clientDAO = new ClientCloudDAO();
            ProductCloudDAO productDAO = new ProductCloudDAO();
            ProviderCloudDAO providerDAO = new ProviderCloudDAO();
            SaleCloudDAO saleDAO = new SaleCloudDAO();

            // Optional files are created on first run if they don't exist yet
            clientDAO.createIfNotExists();
            saleDAO.createIfNotExists();

            ClientManager clientManager = new ClientManager(clientDAO);
            ProductManager productManager = new ProductManager(productDAO);
            ProviderManager providerManager = new ProviderManager(providerDAO);
            SaleManager saleManager = new SaleManager(saleDAO);
            ShoppingManager shoppingManager = new ShoppingManager(saleManager, productDAO, providerDAO);

            ViewController controller = new ViewController(clientManager, productManager, providerManager, saleManager, shoppingManager);
            controller.start();

        } catch (PersistenceException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Hides verbose geolocation API response logs from the console UI.
     */
    private static void suppressGeolocationInfoLogs() {
        Logger.getLogger("GeoAPI").setLevel(Level.WARNING);
        Logger.getLogger("geolocationAPI").setLevel(Level.WARNING);
        Logger.getLogger("geolocationAPI.GeolocationApiManager").setLevel(Level.WARNING);
    }

    /**
     * Attempts to instantiate {@link ApiHelper} to verify that the API is reachable.
     *
     * @return {@code true} if the API is available, {@code false} otherwise
     */
    private static boolean checkApiStatus() {
        System.out.println("Checking API status...");
        try {
            new ApiHelper();
            System.out.println("API OK");
            return true;
        } catch (ApiException e) {
            System.out.println("Error: The API isn't available.");
            return false;
        }
    }

    /**
     * Checks that the mandatory local data files ({@code products.json} and
     * {@code providers.json}) exist on disk. Prints an error message for each
     * missing file.
     *
     * @return {@code true} if both files are present, {@code false} if either is missing
     */
    private static boolean verifyLocalFiles() {
        if (!new ProductJsonDAO().isFileCreated()) {
            System.out.println("Error: The products.json file can't be accessed.");
            return false;
        }
        if (!new ProviderJsonDAO().isFileCreated()) {
            System.out.println("Error: The providers.json file can't be accessed.");
            return false;
        }
        return true;
    }
}
