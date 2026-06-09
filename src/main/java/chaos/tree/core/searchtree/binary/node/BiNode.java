package chaos.tree.core.searchtree.binary.node;

import chaos.tree.core.searchtree.ISearchNode;

/**
 * Base node implementation for binary search tree structures.
 *
 * <p>This class stores a node value together with references to its left and
 * right children. It is the common node abstraction used by the binary search
 * tree hierarchy in ChaosTree.</p>
 *
 * @param <T> the value type stored in the node
 * @param <N> the concrete node type
 * @since 1.0.0
 */
public abstract class BiNode<T,N extends BiNode<T,N>> implements ISearchNode<T> {

    /**
     * Value stored in the current node.
     */
    private T value;

    /**
     * Reference to the left child node.
     */
    private N left;

    /**
     * Reference to the right child node.
     */
    private N right;

    /**
     * Constructs the node with the specified value.
     *
     * @param value the value to be stored in current node
     */
    public BiNode(T value) {
        this.value = value;
    }

    /**
     * Returns the value stored in this node.
     *
     * @return the node value
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * Updates the value stored in this node.
     *
     * @param value the new value
     */
    @Override
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Returns the right child of this node.
     *
     * @return the right child node, or {@code null} if absent
     */
    public N getRight() {
        return right;
    }

    /**
     * Returns the left child of this node.
     *
     * @return the left child node, or {@code null} if absent
     */
    public N getLeft() {
        return left;
    }

    /**
     * Sets the left child node.
     *
     * @param left the left node to be set
     */
    public void setLeft(N left) {
        this.left = left;
    }

    /**
     * Sets the right child node.
     *
     * @param right the right node to be set
     */
    public void setRight(N right) {
        this.right = right;
    }
}
