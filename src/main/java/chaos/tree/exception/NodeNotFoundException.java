package chaos.tree.exception;

/**
 * Thrown when a tree operation cannot find the requested node value.
 *
 * @author Chaos
 * @since 1.0.0
 */
import java.util.NoSuchElementException;

public class NodeNotFoundException extends NoSuchElementException {
    /**
     * Constructs a node not found exception with the specified detail message.
     *
     * @param message the detail message
     */
    public NodeNotFoundException(String message) {
        super(message);
    }
}
