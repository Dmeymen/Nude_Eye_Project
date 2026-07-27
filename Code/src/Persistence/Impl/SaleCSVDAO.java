package Persistence.Impl;

import Business.Entities.Sale;
import Persistence.SalesDAO;
import Persistence.Exceptions.PersistenceException;
import Persistence.Exceptions.PersistenceFileNotFoundException;
import Persistence.Exceptions.PersistenceWriteException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Local CSV implementation of {@link SalesDAO}.
 * Reads and appends sale records in {@code sales.csv} in the working directory.
 * New records are appended directly without loading the full file into memory.
 * The file uses a four-column header: {@code client_id,product_id,price_paid,purchase_date}.
 */
public class SaleCSVDAO implements SalesDAO {

    private static final String FILE_PATH = "sales.csv";
    private static final String[] CSV_HEADER = {"client_id", "product_id", "price_paid", "purchase_date"};

    /**
     * Constructs a {@code SaleCSVDAO}.
     * The file itself is created on demand via {@link #createIfNotExists()}.
     */
    public SaleCSVDAO() {
        // File is created via createIfNotExists() called at startup
    }

    @Override
    public boolean isFileCreated() {
        return new File(FILE_PATH).exists();
    }

    @Override
    public void createIfNotExists() throws PersistenceException {
        if (!isFileCreated()) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
                writer.writeNext(CSV_HEADER);
            } catch (IOException e) {
                throw new PersistenceWriteException("Error creating " + FILE_PATH, e);
            }
        }
    }

    @Override
    public List<Sale> loadAllSales() throws PersistenceException {
        List<Sale> sales = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            reader.readNext(); // skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                sales.add(new Sale(
                        line[0],
                        line[1],
                        Double.parseDouble(line[2]),
                        Long.parseLong(line[3])
                ));
            }
        } catch (IOException | CsvValidationException e) {
            throw new PersistenceFileNotFoundException(FILE_PATH, e);
        }
        return sales;
    }

    @Override
    public void addSale(Sale sale) throws PersistenceException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH, true))) {
            writer.writeNext(new String[]{
                    sale.getClientId(),
                    sale.getProductId(),
                    String.valueOf(sale.getPricePaid()),
                    String.valueOf(sale.getPurchaseDate())
            });
        } catch (IOException e) {
            throw new PersistenceWriteException("Error writing to " + FILE_PATH, e);
        }
    }

    @Override
    public List<Sale> getClientHistory(String clientId) throws PersistenceException {
        return loadAllSales().stream()
                .filter(s -> s.getClientId().equals(clientId))
                .collect(Collectors.toList());
    }
}