package Persistence.Exceptions;

/**
 * Thrown when a required persistence file cannot be found or accessed.
 * The exception message is automatically formatted as
 * {@code "The <filename> file can't be accessed."}.
 */
public class PersistenceFileNotFoundException extends PersistenceException {

    /**
     * Constructs a {@code PersistenceFileNotFoundException} for the named file.
     *
     * @param filename the name of the file that could not be accessed
     */
    public PersistenceFileNotFoundException(String filename) {
        super("The " + filename + " file can't be accessed.");
    }

    /**
     * Constructs a {@code PersistenceFileNotFoundException} for the named file,
     * wrapping the underlying cause.
     *
     * @param filename the name of the file that could not be accessed
     * @param cause    the underlying exception that triggered this error
     */
    public PersistenceFileNotFoundException(String filename, Throwable cause) {
        super("The " + filename + " file can't be accessed.", cause);
    }
}
