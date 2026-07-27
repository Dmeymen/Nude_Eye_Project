package Business.Exceptions;

/**
 * Thrown when a write operation fails in the Business layer.
 * Typically wraps a {@link Persistence.Exceptions.PersistenceWriteException}.
 */
public class BusinessWriteException extends BusinessException {

    /**
     * Constructs a {@code BusinessWriteException} with the given message.
     *
     * @param message a human-readable description of the write error
     */
    public BusinessWriteException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code BusinessWriteException} that wraps a lower-level cause.
     *
     * @param message a human-readable description of the write error
     * @param cause   the underlying exception that triggered this error
     */
    public BusinessWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}