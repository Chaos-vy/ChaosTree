package chaos.tree.exception;

/**
 * When tree is Empty.
 */
public class EmptyTreeException extends RuntimeException {
    public EmptyTreeException(String message) {
        super(message);
    }
}
