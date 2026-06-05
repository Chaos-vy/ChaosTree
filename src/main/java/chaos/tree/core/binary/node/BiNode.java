package chaos.tree.core.binary;


import chaos.tree.core.INode;

public abstract class BiNode<T,N extends BiNode<T,N>> implements INode<T> {

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
     * Constructs the node with the specified value
     *
     * @param value the value to be stored in current node
     */
    public BiNode(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Return the right node child.
     *
     * @return the right node child
     */
    public N getRight() {
        return right;
    }

    /**
     * Return the left node child
     *
     * @return the left node child
     */
    public N getLeft() {
        return left;
    }

    /**
     * Set the left child node.
     *
     * @param left the left node to be set
     */
    public void setLeft(N left) {
        this.left = left;
    }

    /**
     * Set the right child node.
     *
     * @param right the right node to be set
     */
    public void setRight(N right) {
        this.right = right;
    }
}
