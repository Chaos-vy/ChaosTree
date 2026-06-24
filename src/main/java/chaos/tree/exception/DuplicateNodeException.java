package chaos.tree.exception;

/**
 * Thrown when a tree operation receives a value that already exists in the tree.
 *
 * <p>This exception is used for duplicate node values. Null node values are
 * handled separately by {@link NullPointerException}.</p>
 *
 * @author Chaos
 * @since 1.0.0
 */
public class DuplicateNodeException extends IllegalArgumentException {
    /**
     * Constructs a duplicate node exception with the specified detail message.
     *
     * @param message the detail message
     */
    public DuplicateNodeException(String message) {
        super(message);
    }
}
