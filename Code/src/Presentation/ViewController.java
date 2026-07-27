package Presentation;

import Business.ClientManager;
import Business.Customer;
import Business.ProductManager;
import Business.ProviderManager;
import Business.SaleManager;
import Business.ShoppingManager;
import Business.Entities.CorporateClient;
import Business.Entities.Item;
import Business.Entities.OnlineClient;
import Business.Entities.PhoneNumber;
import Business.Entities.Product;
import Business.Entities.ProductStock;
import Business.Entities.Provider;
import Business.Entities.Sale;
import Business.Entities.ShoppingCart;
import Business.Exceptions.BusinessException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller that mediates between the {@link UserInterface} and the Business layer managers.
 * It owns the session state (logged-in client and active cart) and drives all menu flows.
 * No Persistence classes are imported here; the controller never touches the DAO layer directly.
 */
public class ViewController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final UserInterface ui;
    private final ClientManager clientManager;
    private final ProductManager productManager;
    private final ProviderManager providerManager;
    private final SaleManager saleManager;
    private final ShoppingManager shoppingManager;

    /** The currently logged-in client, or {@code null} when no session is active. */
    private Customer currentClient;

    /** The active shopping cart, or {@code null} when no session is active. */
    private ShoppingCart currentCart;

    /**
     * Constructs a {@code ViewController} with all required business-layer managers.
     * No Persistence objects are passed; the controller stays in the Presentation layer.
     *
     * @param clientManager   manages client login and registration
     * @param productManager  manages product search operations
     * @param providerManager manages provider lookup operations
     * @param saleManager     manages sale recording and history retrieval
     * @param shoppingManager manages cart and checkout operations
     */
    public ViewController(ClientManager clientManager, ProductManager productManager,
                          ProviderManager providerManager, SaleManager saleManager,
                          ShoppingManager shoppingManager) {
        this.clientManager   = clientManager;
        this.productManager  = productManager;
        this.providerManager = providerManager;
        this.saleManager     = saleManager;
        this.shoppingManager = shoppingManager;
        this.ui = new UserInterface();
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    /**
     * Main entry point of the application.
     * Runs the authentication menu in a loop until the user chooses to exit.
     */
    public void start() {
        boolean running = true;
        while (running) {
            AutoMenuOption option = ui.showAuthMenu();
            switch (option) {
                case LOGIN:    handleLogin();    break;
                case REGISTER: handleRegister(); break;
                case EXIT:
                    ui.showMessage("Goodbye!");
                    running = false;
                    break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Authentication handlers
    // -------------------------------------------------------------------------

    /**
     * Asks the user for their client ID and attempts to log them in.
     * If found, initialises a fresh shopping cart and enters the main menu.
     * If not found, shows an error message and returns to the authentication menu.
     */
    private void handleLogin() {
        String clientId = ui.askClientId();
        try {
            Customer client = clientManager.login(clientId);
            if (client == null) {
                ui.showMessage("No account found with that ID. Please try again or register.");
                return;
            }
            currentClient = client;
            currentCart   = new ShoppingCart(new ArrayList<>());
            ui.showMessage("Welcome back, " + currentClient.getName() + "!");
            handleMainMenu();
        } catch (BusinessException e) {
            ui.showMessage("Error: " + e.getMessage());
        }
    }

    /**
     * Collects name and phone numbers from the user, registers a new account,
     * and immediately enters the main menu as the newly created client.
     * Raw {@code String[]} pairs returned by the UI are converted to {@link PhoneNumber}
     * objects here, keeping Business types out of {@link UserInterface} entirely.
     */
    private void handleRegister() {
        String fullName = ui.askFullName();

        // UI returns plain [prefix, number] pairs; controller converts to Business entities
        List<String[]> rawPhones = ui.askPhoneNumbers();
        List<PhoneNumber> phoneNumbers = rawPhones.stream()
                .map(pair -> new PhoneNumber(pair[1], pair[0]))
                .collect(Collectors.toList());

        try {
            Customer newClient = clientManager.register(fullName, phoneNumbers);
            currentClient = newClient;
            currentCart   = new ShoppingCart(new ArrayList<>());
            ui.showMessage("Account created! Your client ID is: " + currentClient.getClientId());
            ui.showMessage("Please save your ID — you will need it to log in next time.");
            handleMainMenu();
        } catch (BusinessException e) {
            ui.showMessage("Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Main menu handler
    // -------------------------------------------------------------------------

    /**
     * Displays the main menu in a loop until the user chooses to log out.
     */
    private void handleMainMenu() {
        boolean loggedIn = true;
        while (loggedIn) {
            MainMenuOption option = ui.showMainMenu();
            switch (option) {
                case SHOW_PROFILE:       handleUserProfile();    break;
                case FIND_PROD_NAME:     handleFindByName();     break;
                case FIND_PROD_SUPPLIER: handleFindByProvider(); break;
                case SEE_SHOPPING_CART:  handleShoppingCart();   break;
                case LOGOUT:
                    handleLogout();
                    loggedIn = false;
                    break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Main menu sub-handlers
    // -------------------------------------------------------------------------

    /**
     * Fetches the current client's last 10 purchases (sorted most-recent first),
     * resolves each product's full details from the product manager,
     * formats all data into plain strings, and passes them to the UI for display.
     * Extra client-type-specific fields (shipping address, email, NIF, billing address)
     * are collected and forwarded to the UI when the client is an
     * {@link OnlineClient} or {@link CorporateClient}.
     */
    private void handleUserProfile() {
        try {
            List<Sale> history = saleManager.getSalesByClient(currentClient.getClientId());

            // Format phone numbers as "+<prefix> <number>"
            List<String> phoneLines = currentClient.getPhoneNumber().stream()
                    .map(p -> "+" + p.getCountryPrefix() + " " + p.getPhoneNumber())
                    .collect(Collectors.toList());

            // Collect client-type-specific extra fields
            List<String> extraDetails = new ArrayList<>();
            if (currentClient instanceof OnlineClient) {
                OnlineClient oc = (OnlineClient) currentClient;
                if (oc.getEmail() != null) {
                    extraDetails.add("Email   : " + oc.getEmail());
                }
                if (oc.getShippingAddress() != null) {
                    extraDetails.add("Address : " + formatAddress(oc.getShippingAddress()));
                }
            } else if (currentClient instanceof CorporateClient) {
                CorporateClient cc = (CorporateClient) currentClient;
                if (cc.getNif() != null) {
                    extraDetails.add("NIF     : " + cc.getNif());
                }
                if (cc.getBillingAddress() != null) {
                    extraDetails.add("Billing : " + formatAddress(cc.getBillingAddress()));
                }
                if (cc.getShippingAddress() != null) {
                    extraDetails.add("Mailing : " + formatAddress(cc.getShippingAddress()));
                }
            }

            // Format each sale: show product name, and brand/model only when present
            List<String> historyLines = new ArrayList<>();
            if (history != null) {
                for (Sale sale : history) {
                    String date = DATE_FORMAT.format(Instant.ofEpochSecond(sale.getPurchaseDate()));

                    Product product = productManager.findProductById(sale.getProductId());
                    String productInfo;
                    if (product != null) {
                        String brand = product.getProductBrand();
                        String model = product.getProductModel();
                        productInfo = (brand != null && model != null)
                                ? String.format("%s - %s %s", product.getProductName(), brand, model)
                                : product.getProductName();
                    } else {
                        productInfo = "ID: " + sale.getProductId();
                    }

                    historyLines.add(String.format("[%s]  %-35s  %.2f€",
                            date, productInfo, sale.getPricePaid()));
                }
            }

            ui.showUserProfile(
                    currentClient.getClientId(),
                    currentClient.getName(),
                    phoneLines,
                    extraDetails,
                    historyLines
            );
        } catch (BusinessException e) {
            ui.showMessage("Error: " + e.getMessage());
        }
    }

    /**
     * Asks for a search keyword, shows the matching product list, then lets the user
     * pick a product to see its full details and available providers (in-stock only).
     * The user can then select a provider to add the product to the cart.
     */
    private void handleFindByName() {
        String keyword = ui.askSearchKeyword();
        List<Product> products;
        try {
            products = productManager.findProductsByName(keyword);
        } catch (BusinessException e) {
            ui.showMessage("Error: " + e.getMessage());
            return;
        }

        // Format each product as "[id] name - brand model"
        List<String> productLines = products == null ? new ArrayList<>() : products.stream()
                .map(p -> String.format("[%s] %s - %s %s",
                        p.getProductId(), p.getProductName(),
                        p.getProductBrand(), p.getProductModel()))
                .collect(Collectors.toList());

        ui.showProductList(productLines);

        if (products == null || products.isEmpty()) return;

        ui.showMessage("Enter product number to see details (or 0 to go back):");
        int productIndex = parseIndex(ui.readInput(), products.size());
        if (productIndex < 0) return;

        Product selectedProduct = products.get(productIndex);

        // Fetch providers that sell this product, then keep only those with stock > 0
        List<Provider> allProviders;
        try {
            allProviders = providerManager.findProvidersByProduct(selectedProduct.getProductId());
        } catch (BusinessException e) {
            ui.showMessage("Error: " + e.getMessage());
            return;
        }
        List<Provider> providers = allProviders == null ? new ArrayList<>() : allProviders.stream()
                .filter(prov -> {
                    ProductStock s = prov.getProviderProductById(selectedProduct.getProductId());
                    return s != null && s.getUnitsInStock() > 0;
                })
                .collect(Collectors.toList());

        // Format product detail header lines
        List<String> productDetailLines = new ArrayList<>();
        productDetailLines.add("ID    : " + selectedProduct.getProductId());
        productDetailLines.add("Name  : " + selectedProduct.getProductName());
        productDetailLines.add("Brand : " + selectedProduct.getProductBrand());
        productDetailLines.add("Model : " + selectedProduct.getProductModel());

        // Format each in-stock provider row with name, price and stock
        List<String> providerLines = providers.stream()
                .map(prov -> {
                    ProductStock stock = prov.getProviderProductById(selectedProduct.getProductId());
                    return String.format("%-25s  %.2f€  (stock: %d)",
                            prov.getProviderCompanyName(),
                            stock.getSellingPrice(),
                            stock.getUnitsInStock());
                })
                .collect(Collectors.toList());

        ui.showProductDetail(productDetailLines, providerLines);

        if (providers.isEmpty()) return;

        ui.showMessage("Enter supplier number to add to cart (or 0 to go back):");
        int providerIndex = parseIndex(ui.readInput(), providers.size());
        if (providerIndex < 0) return;

        Provider selectedProvider = providers.get(providerIndex);
        ProductStock stock = selectedProvider.getProviderProductById(selectedProduct.getProductId());

        try {
            shoppingManager.addToCart(currentCart, stock, selectedProvider, currentClient);
            ui.showMessage("Added to cart: " + selectedProduct.getProductName()
                    + " from " + selectedProvider.getProviderCompanyName());
        } catch (BusinessException e) {
            ui.showMessage("Error: " + e.getMessage());
        }
    }

    /**
     * Shows all providers, lets the user pick one to view its details (including
     * all products with price and stock), then lets the user pick a product to add
     * to the cart — but only if it has stock remaining.
     */
    private void handleFindByProvider() {
        List<Provider> providers;
        try {
            providers = providerManager.getAllProviders();
        } catch (BusinessException e) {
            ui.showMessage("Error: " + e.getMessage());
            return;
        }

        // Format each provider as "[id] companyName"
        List<String> providerLines = providers == null ? new ArrayList<>() : providers.stream()
                .map(p -> String.format("[%s] %s", p.getProviderId(), p.getProviderCompanyName()))
                .collect(Collectors.toList());

        ui.showProviderList(providerLines);

        if (providers == null || providers.isEmpty()) return;

        ui.showMessage("Enter supplier number to see details (or 0 to go back):");
        int providerIndex = parseIndex(ui.readInput(), providers.size());
        if (providerIndex < 0) return;

        Provider selectedProvider = providers.get(providerIndex);

        // Format provider detail header lines
        List<String> providerDetailLines = new ArrayList<>();
        providerDetailLines.add("ID      : " + selectedProvider.getProviderId());
        providerDetailLines.add("Company : " + selectedProvider.getProviderCompanyName());
        providerDetailLines.add("CIF     : " + selectedProvider.getProviderCif());
        providerDetailLines.add("Contact : " + selectedProvider.getContactName());
        providerDetailLines.add("Phone   : " + selectedProvider.getProviderPhone());
        providerDetailLines.add("Email   : " + selectedProvider.getProviderEmail());

        // Show all products (including out-of-stock ones, labelled accordingly)
        List<ProductStock> stockList = selectedProvider.getAllProductStock();
        List<String> stockLines = new ArrayList<>();
        if (stockList != null) {
            for (ProductStock s : stockList) {
                String stockLabel = s.getUnitsInStock() > 0
                        ? String.valueOf(s.getUnitsInStock())
                        : "OUT OF STOCK";
                stockLines.add(String.format("Product ID: %-15s  Price: %.2f€  Stock: %s",
                        s.getProductId(), s.getSellingPrice(), stockLabel));
            }
        }

        ui.showProviderDetail(providerDetailLines, stockLines);

        if (stockList == null || stockList.isEmpty()) return;

        ui.showMessage("Enter product number to add to cart (or 0 to go back):");
        int stockIndex = parseIndex(ui.readInput(), stockList.size());
        if (stockIndex < 0) return;

        ProductStock selectedStock = stockList.get(stockIndex);

        if (selectedStock.getUnitsInStock() <= 0) {
            ui.showMessage("Sorry, this product is out of stock with that supplier.");
            return;
        }

        try {
            shoppingManager.addToCart(currentCart, selectedStock, selectedProvider, currentClient);
            ui.showMessage("Added to cart: product ID " + selectedStock.getProductId()
                    + " from " + selectedProvider.getProviderCompanyName());
        } catch (BusinessException e) {
            ui.showMessage("Error: " + e.getMessage());
        }
    }

    /**
     * Formats the cart contents and displays them, then offers the user options
     * to remove an item, proceed to checkout, or go back to the main menu.
     */
    private void handleShoppingCart() {
        List<Item> items = currentCart.getItems();

        // Format each item as "productName  via  providerName  price€"
        List<String> cartLines = items.stream()
                .map(item -> String.format("%-20s  via %-20s  %.2f€",
                        item.getProduct().getProductName(),
                        item.getProvider().getProviderCompanyName(),
                        item.getPriceStrategy()))
                .collect(Collectors.toList());

        ui.showShoppingCart(cartLines, shoppingManager.getCartTotal(currentCart, currentClient));

        if (items.isEmpty()) return;

        ui.showMessage("Options: [C] Checkout  [R] Remove item  [B] Back");
        String input = ui.readInput().toUpperCase();

        switch (input) {
            case "C":
                handleCheckout();
                break;
            case "R":
                ui.showMessage("Enter item number to remove:");
                int index = parseIndex(ui.readInput(), items.size());
                if (index >= 0) {
                    shoppingManager.removeFromCart(currentCart, index);
                    ui.showMessage("Item removed.");
                }
                break;
            case "B":
            default:
                break;
        }
    }

    /**
     * Displays the invoice for the current cart, asks the user to confirm,
     * and processes the checkout if confirmed.
     * On success, provider stock is decremented and sales are persisted.
     */
    private void handleCheckout() {
        List<Item> items = currentCart.getItems();

        // Format each item as "productName  via  providerName  price€"
        List<String> invoiceLines = items.stream()
                .map(item -> String.format("%-20s  via %-20s  %.2f€",
                        item.getProduct().getProductName(),
                        item.getProvider().getProviderCompanyName(),
                        item.getPriceStrategy()))
                .collect(Collectors.toList());

        ui.showInvoice(invoiceLines, shoppingManager.getCartTotal(currentCart, currentClient));
        ui.showMessage("Confirm purchase? [Y] Yes  [N] No");
        String confirm = ui.readInput().toUpperCase();

        if (confirm.equals("Y")) {
            try {
                shoppingManager.checkout(currentCart, currentClient);
                ui.showMessage("Purchase completed successfully!");
            } catch (BusinessException e) {
                ui.showMessage("Error: " + e.getMessage());
            }
        } else {
            ui.showMessage("Purchase cancelled. Your cart has been kept.");
        }
    }

    /**
     * Clears the session state (client and cart) and returns to the authentication menu.
     */
    private void handleLogout() {
        ui.showMessage("Goodbye, " + currentClient.getName() + "!");
        currentClient = null;
        currentCart   = null;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Formats a geolocation API Address object into a human-readable string.
     * This method is used to avoid printing the default Object.toString()
     * representation (like Address@3f200884) and instead returns a
     * properly structured address format including street, number, city,
     * country, and postal code.
     *
     * @param a the Address object returned by the geolocation API
     * @return a formatted address string suitable for display in the UI
     */
    private String formatAddress(geolocationAPI.Address a) {
        return a.getStreet() + " " +
                a.getNumber() + ", " +
                a.getCity() + ", " +
                a.getCountry() + " (" +
                a.getPostalCode() + ")";
    }

    /**
     * Parses a 1-based index string entered by the user into a 0-based list index.
     * Returns {@code -1} if the input is {@code "0"}, out of range, or not a number.
     *
     * @param input    the raw string entered by the user
     * @param listSize the size of the list being indexed
     * @return a valid 0-based index, or {@code -1} for invalid / cancel input
     */
    private int parseIndex(String input, int listSize) {
        try {
            int chosen = Integer.parseInt(input);
            if (chosen == 0) return -1;
            if (chosen < 1 || chosen > listSize) {
                ui.showMessage("Invalid selection.");
                return -1;
            }
            return chosen - 1;
        } catch (NumberFormatException e) {
            ui.showMessage("Please enter a valid number.");
            return -1;
        }
    }
}