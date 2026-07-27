package Persistence.Exceptions;

/**
 * Thrown when a write operation to the persistence layer fails.
 * Typically wraps an {@link java.io.IOException} from file I/O or an API error.
 */
public class PersistenceWriteException extends PersistenceException {

    /**
     * Constructs a {@code PersistenceWriteException} with the given message.
     *
     * @param message a human-readable description of the write error
     */
    public PersistenceWriteException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code PersistenceWriteException} that wraps a lower-level cause.
     *
     * @param message a human-readable description of the write error
     * @param cause   the underlying exception that triggered this error
     */
    public PersistenceWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}