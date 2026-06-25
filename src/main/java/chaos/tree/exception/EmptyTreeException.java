package chaos.tree.exception;

import java.util.NoSuchElementException;

/**
 * Thrown when an operation requires at least one tree element, but the tree is empty.
 *
 * @author Chaos
 * @since 1.0.0
 */
public class EmptyTreeException extends NoSuchElementException {
    /**
     * Constructs an empty tree exception with the specified detail message.
     *
     * @param message the detail message
     */
    public EmptyTreeException(String message) {
        super(message);
    }
}
