package Presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserInterface {

    private final Scanner scanner;

    // Constructor: initialises the shared Scanner for all console I/O
    public UserInterface() {
        this.scanner = new Scanner(System.in);
    }

    // -------------------------------------------------------------------------
    // Menu displays
    // -------------------------------------------------------------------------

    /**
     * Displays the authentication menu and returns the option chosen by the user.
     * Keeps prompting until a valid option is entered.
     */
    public AutoMenuOption showAuthMenu() {
        System.out.println("\n========== WELCOME ==========");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        while (true) {
            String input = readInput();
            switch (input) {
                case "1": return AutoMenuOption.LOGIN;
                case "2": return AutoMenuOption.REGISTER;
                case "3": return AutoMenuOption.EXIT;
                default:
                    showMessage("Invalid option. Please enter 1, 2 or 3.");
                    System.out.print("Choose an option: ");
            }
        }
    }

    /**
     * Displays the main menu and returns the option chosen by the user.
     * Keeps prompting until a valid option is entered.
     */
    public MainMenuOption showMainMenu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. Show my profile");
        System.out.println("2. Find product by name");
        System.out.println("3. Find products by supplier");
        System.out.println("4. See shopping cart");
        System.out.println("5. Logout");
        System.out.print("Choose an option: ");

        while (true) {
            String input = readInput();
            switch (input) {
                case "1": return MainMenuOption.SHOW_PROFILE;
                case "2": return MainMenuOption.FIND_PROD_NAME;
                case "3": return MainMenuOption.FIND_PROD_SUPPLIER;
                case "4": return MainMenuOption.SEE_SHOPPING_CART;
                case "5": return MainMenuOption.LOGOUT;
                default:
                    showMessage("Invalid option. Please enter a number between 1 and 5.");
                    System.out.print("Choose an option: ");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Profile & history
    // -------------------------------------------------------------------------

    /**
     * Displays the client's personal details and their full purchase history.
     *
     * @param clientId      the client's unique ID
     * @param clientName    the client's full name
     * @param phoneLines    each phone formatted as "+<prefix> <number>"
     * @param extraDetails  client-type-specific fields (e.g. email, address, NIF);
     *                      empty for regular clients
     * @param historyLines  each sale formatted as "[date]  product info  price€"
     */
    public void showUserProfile(String clientId, String clientName, List<String> phoneLines,
                                List<String> extraDetails, List<String> historyLines) {
        System.out.println("\n========== MY PROFILE ==========");
        System.out.println("ID     : " + clientId);
        System.out.println("Name   : " + clientName);

        System.out.println("Phones :");
        if (phoneLines.isEmpty()) {
            System.out.println("  (no phone numbers registered)");
        } else {
            for (String line : phoneLines) {
                System.out.println("  " + line);
            }
        }

        if (!extraDetails.isEmpty()) {
            System.out.println("\n--- Account details ---");
            for (String line : extraDetails) {
                System.out.println("  " + line);
            }
        }

        System.out.println("\n--- Purchase history ---");
        if (historyLines.isEmpty()) {
            System.out.println("  No purchases yet.");
        } else {
            for (String line : historyLines) {
                System.out.println("  " + line);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Product views
    // -------------------------------------------------------------------------

    /**
     * Displays a numbered list of products.
     *
     * @param productLines  each product formatted as a display string
     */
    public void showProductList(List<String> productLines) {
        System.out.println("\n--- Products found ---");
        if (productLines == null || productLines.isEmpty()) {
            System.out.println("  No products found.");
            return;
        }
        for (int i = 0; i < productLines.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, productLines.get(i));
        }
    }

    /**
     * Displays the full details of a single product and all providers that sell it.
     *
     * @param productLines   product header lines (id, name, brand, model)
     * @param providerLines  each provider formatted with name, price and stock
     */
    public void showProductDetail(List<String> productLines, List<String> providerLines) {
        System.out.println("\n========== PRODUCT DETAIL ==========");
        for (String line : productLines) {
            System.out.println(line);
        }

        System.out.println("\n--- Available from ---");
        if (providerLines == null || providerLines.isEmpty()) {
            System.out.println("  No suppliers available for this product.");
        } else {
            for (int i = 0; i < providerLines.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, providerLines.get(i));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Provider views
    // -------------------------------------------------------------------------

    /**
     * Displays a numbered list of providers.
     *
     * @param providerLines  each provider formatted as a display string
     */
    public void showProviderList(List<String> providerLines) {
        System.out.println("\n--- Suppliers ---");
        if (providerLines == null || providerLines.isEmpty()) {
            System.out.println("  No suppliers found.");
            return;
        }
        for (int i = 0; i < providerLines.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, providerLines.get(i));
        }
    }

    /**
     * Displays full details of a single provider and all their available products.
     *
     * @param providerLines  provider header lines (id, company, cif, phone, email)
     * @param stockLines     each product-stock entry formatted as a display string
     */
    public void showProviderDetail(List<String> providerLines, List<String> stockLines) {
        System.out.println("\n========== SUPPLIER DETAIL ==========");
        for (String line : providerLines) {
            System.out.println(line);
        }

        System.out.println("\n--- Products offered ---");
        if (stockLines == null || stockLines.isEmpty()) {
            System.out.println("  (no products listed)");
        } else {
            for (int i = 0; i < stockLines.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, stockLines.get(i));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Cart & checkout views
    // -------------------------------------------------------------------------

    /**
     * Displays all items currently in the shopping cart with a running total.
     *
     * @param cartLines  each item formatted as a display string
     * @param total      the VAT-inclusive total, or -1 if the cart is empty
     */
    public void showShoppingCart(List<String> cartLines, double total) {
        System.out.println("\n========== SHOPPING CART ==========");
        if (cartLines == null || cartLines.isEmpty()) {
            System.out.println("  Your cart is empty.");
        } else {
            for (int i = 0; i < cartLines.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, cartLines.get(i));
            }
            System.out.printf("%n  Subtotal (VAT incl.): %.2f€%n", total);
        }
    }

    /**
     * Displays the final invoice after a successful checkout.
     *
     * @param invoiceLines  each item formatted as a display string
     * @param total         the VAT-inclusive total
     */
    public void showInvoice(List<String> invoiceLines, double total) {
        System.out.println("\n========== INVOICE ==========");
        for (String line : invoiceLines) {
            System.out.println("  " + line);
        }
        System.out.printf("%n  TOTAL (taxes incl.): %.2f€%n", total);
        System.out.println("  Thank you for your purchase!");
    }

    // -------------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------------

    /**
     * Asks the user to type a keyword to search products by name.
     * Keeps prompting until a non-blank value is entered.
     */
    public String askSearchKeyword() {
        while (true) {
            System.out.print("Enter product name to search: ");
            String input = readInput();
            if (!input.isBlank()) return input;
            showMessage("Search keyword cannot be empty. Please try again.");
        }
    }

    /**
     * Asks the user to enter their client ID for login.
     * Keeps prompting until a non-blank value is entered.
     */
    public String askClientId() {
        while (true) {
            System.out.print("Enter your client ID: ");
            String input = readInput();
            if (!input.isBlank()) return input;
            showMessage("Client ID cannot be empty. Please try again.");
        }
    }

    /**
     * Asks the user to enter their full name during registration.
     * Keeps prompting until a non-blank value is entered.
     */
    public String askFullName() {
        while (true) {
            System.out.print("Enter your full name: ");
            String input = readInput();
            if (!input.isBlank()) return input;
            showMessage("Name cannot be empty. Please try again.");
        }
    }

    /**
     * Collects one or more phone numbers from the user during registration.
     * Each entry is returned as a String array: [prefix, number].
     * The country prefix must contain only digits; the phone number must be non-empty digits.
     * The user types an empty line at the prefix prompt to finish entering numbers.
     */
    public List<String[]> askPhoneNumbers() {
        List<String[]> phones = new ArrayList<>();
        System.out.println("Enter phone numbers (leave blank to finish):");
        while (true) {
            System.out.print("  Country prefix (e.g. 34, blank to finish): ");
            String prefix = readInput();
            if (prefix.isBlank()) break;
            if (!prefix.matches("\\d+")) {
                showMessage("  Invalid prefix: only digits are allowed. Please try again.");
                continue;
            }
            System.out.print("  Phone number: ");
            String number = readInput();
            if (number.isBlank()) {
                showMessage("  Phone number cannot be empty. Please try again.");
                continue;
            }
            if (!number.matches("\\d+")) {
                showMessage("  Invalid number: only digits are allowed. Please try again.");
                continue;
            }
            phones.add(new String[]{prefix, number});
        }
        return phones;
    }

    /**
     * Displays a generic message to the user.
     */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /**
     * Reads and returns a trimmed line of input from the console.
     */
    public String readInput() {
        return scanner.nextLine().trim();
    }
}