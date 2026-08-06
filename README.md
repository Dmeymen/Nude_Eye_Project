# Nude Eye Project

Nude Eye Project is a Java console application for an optical store. It lets clients log in or register, browse optical products and suppliers, manage a shopping cart, complete purchases, and view profile and purchase history information.

The project follows a layered Object-Oriented Programming structure:

- `Presentation`: console menus, input handling, and flow control.
- `Business`: application rules, customer/product entities, pricing, cart, sales, and manager classes.
- `Persistence`: DAO interfaces and implementations for cloud API access and local JSON/CSV fallback storage.

## Features

- Client authentication by client ID.
- Registration for new regular clients.
- Product search by product name.
- Supplier browsing with provider details, stock, and prices.
- Shopping cart with add, remove, and checkout actions.
- Purchase history shown in the client profile.
- Cloud-first persistence using La Salle API endpoints.
- Local fallback using `products.json`, `providers.json`, `clients.json`, and `sales.csv`.
- Product-specific pricing rules:
  - Glasses: 21% VAT.
  - Contact lenses: 10% VAT.
  - Consumables: 21% VAT, with a 40% near-expiry discount.
  - Services: hourly price with 21% VAT.
- Customer-specific rules:
  - Regular clients can buy all product types and may receive phone-number loyalty discounts.
  - Online clients pay a distance-based shipping surcharge and cannot buy services.
  - Corporate clients use VAT rules based on billing country and cannot buy services.

## Project Structure

```text
.
|-- Code/
|   |-- lib/                         # Bundled third-party and course API JARs
|   `-- src/
|       |-- Main.java                 # Application entry point
|       |-- Business/                 # Business logic and domain entities
|       |-- Persistence/              # DAO interfaces, API DAOs, local file DAOs
|       `-- Presentation/             # Console UI and menu controller
|-- Documents + UML + JavaDoc/
|   |-- JavaDoc/                      # Generated JavaDoc site
|   |-- UML_Diagram_G3.pdf            # UML diagram
|-- clients.json                      # Local client fallback data
|-- products.json                     # Local product fallback data
|-- providers.json                    # Local provider fallback data
|-- sales.csv                         # Local sales fallback data
`-- Phase2_G3.iml                     # IntelliJ IDEA module file
```

## Requirements

- Java JDK installed.
- IntelliJ IDEA is recommended because the repository already includes an IDEA module configuration.
- Internet access is optional. When the shared API is unavailable, the app falls back to the local data files.

The required JAR dependencies are already included in `Code/lib`:

- Gson
- OpenCSV
- Apache Commons Lang
- La Salle `ApiHelper`
- La Salle `CoordinatesAPI`

## Running in IntelliJ IDEA

1. Open the project root folder in IntelliJ IDEA.
2. Make sure `Code/src` is marked as a source root.
3. Make sure the JAR files in `Code/lib` are configured as project libraries.
4. Run `Main.java`.

The included `Phase2_G3.iml` and `.idea/libraries/lib.xml` files already point IntelliJ to `Code/src` and `Code/lib`.

## Running from the Command Line

From the project root, compile all Java files into `out`:

```powershell
New-Item -ItemType Directory -Force out
$sources = Get-ChildItem -Recurse Code\src -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "Code\lib\*" -d out $sources
```

Then run the application:

```powershell
java -cp "out;Code\lib\*" Main
```

On macOS/Linux, use `:` instead of `;` in the runtime classpath:

```bash
mkdir -p out
javac -cp "Code/lib/*" -d out $(find Code/src -name "*.java")
java -cp "out:Code/lib/*" Main
```

## Data Files

The application uses cloud DAOs by default. If the API cannot be reached or returns no data, these local files are used:

- `products.json`: product catalog.
- `providers.json`: supplier catalog and stock data.
- `clients.json`: registered clients.
- `sales.csv`: purchase records.

At startup, the app checks API availability. If the API is unavailable, `products.json` and `providers.json` must exist. Client and sales fallback files are created when needed.

## Application Flow

1. The app starts from `Main.java`.
2. It checks whether the shared API is available.
3. It creates cloud-first DAO implementations with local fallback DAOs.
4. It creates business managers for clients, products, providers, sales, and shopping.
5. `ViewController` starts the console menu loop.

Main menu options after login:

- Show profile and purchase history.
- Find product by name.
- Find products by supplier.
- See shopping cart.
- Logout.

## Documentation

Generated JavaDoc is available at:

```text
Documents + UML + JavaDoc/JavaDoc/index.html
```

The UML diagram is available at:

```text
Documents + UML + JavaDoc/UML_Diagram_G3.pdf
```

## Authors

Duna Meya i Mendoza (duna.meya)

Júlia Escoriza Pons (julia.escoriza)
## License

This project is submitted as coursework in LaSalle Campus / Ramon Llull University.

## Notes

- The current repository does not use Maven or Gradle; dependencies are managed through local JAR files.
- `javac` must be available on your PATH to run the manual command-line instructions.
- The app is designed for console interaction and stores fallback data in the project root.
