package chaos.tree.exception;

/**
 * When two Node value are same.
 */
public class DuplicateNodeException extends RuntimeException {
    public DuplicateNodeException(String message) {
        super(message);
    }
}
