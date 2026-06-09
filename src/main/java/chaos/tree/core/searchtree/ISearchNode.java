package chaos.tree.core.searchtree;

/**
 * Defines the value-access operations shared by nodes in search-tree
 * implementations.
 *
 * @param <T> the type of value stored in the node
 * @since 1.0.0
 */
public interface ISearchNode<T> {

    /**
     * Returns the value stored in this node.
     *
     * @return the stored value
     */
    T getValue();

    /**
     * Replaces the value stored in this node.
     *
     * @param value the new value
     */
    void setValue(T value);
}
