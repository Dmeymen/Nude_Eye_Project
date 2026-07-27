package Business.Exceptions;

/**
 * Base checked exception for the Business layer.
 * All business-layer errors extend this class so callers in the Presentation
 * layer can catch a single type without depending on Persistence exceptions.
 */
public class BusinessException extends Exception {

    /**
     * Constructs a {@code BusinessException} with the given message.
     *
     * @param message a human-readable description of the error
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code BusinessException} that wraps a lower-level cause.
     *
     * @param message a human-readable description of the error
     * @param cause   the underlying exception that triggered this error
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}