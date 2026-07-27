package Persistence.Exceptions;

/**
 * Base checked exception for the Persistence layer.
 * All persistence-layer errors extend this class so callers in the Business layer
 * can catch a single type without depending on specific I/O or API exceptions.
 */
public class PersistenceException extends Exception {

    /**
     * Constructs a {@code PersistenceException} with the given message.
     *
     * @param message a human-readable description of the error
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code PersistenceException} that wraps a lower-level cause.
     *
     * @param message a human-readable description of the error
     * @param cause   the underlying {@link Throwable} that triggered this error
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}