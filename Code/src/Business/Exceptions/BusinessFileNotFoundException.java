package Business.Exceptions;

/**
 * Thrown when a required data file cannot be found or accessed in the Business layer.
 * Typically wraps a {@link Persistence.Exceptions.PersistenceFileNotFoundException}.
 */
public class BusinessFileNotFoundException extends BusinessException {

    /**
     * Constructs a {@code BusinessFileNotFoundException} with the given message.
     *
     * @param message a human-readable description of the missing resource
     */
    public BusinessFileNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code BusinessFileNotFoundException} that wraps a lower-level cause.
     *
     * @param message a human-readable description of the missing resource
     * @param cause   the underlying exception that triggered this error
     */
    public BusinessFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}