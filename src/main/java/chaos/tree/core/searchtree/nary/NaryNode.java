package chaos.tree.core.searchtree.nary;

/**
 * Base node implementation for N-ary search tree structures.
 *
 * <p>This class encapsulates the core structural components of an N-ary node:
 * the array of keys, the array of child pointers, the active key counter,
 * and the leaf status. It uses CRTP to ensure strongly typed child references
 * in concrete implementations.</p>
 *
 * @param <T> the comparable value type stored in the node
 * @param <N> the concrete node type extending this base class
 * @since 1.0.0
 */
public abstract class NaryNode<T extends Comparable<T>, N extends NaryNode<T, N>> {

    private final Object[] keys;
    private final N[] children;
    private int keyCount;
    private final boolean isLeaf;

    /**
     * Internal constructor for initializing an N-ary node with exact capacities.
     *
     * @param maxKeys     the exact maximum number of keys this node can hold
     * @param maxChildren the exact maximum number of children this node can hold
     * @param isLeaf      {@code true} if this node is a leaf (allocates no children array);
     *                    {@code false} if it is an internal routing node
     */
    @SuppressWarnings("unchecked")
    private NaryNode(int maxKeys, int maxChildren, boolean isLeaf) {
        this.keys = new Object[maxKeys];
        if(isLeaf) {
            this.children = null;
        }
        else this.children = (N[]) new NaryNode[maxChildren];
        this.keyCount = 0;
        this.isLeaf = isLeaf;
    }

    /**
     * Constructs a standard B-Tree or B+Tree node using the CLRS minimum degree arithmetic.
     *
     * <p>This automatically calculates the maximum keys ({@code 2t - 1}) and maximum children
     * ({@code 2t}) required for symmetrical splitting. If the node is marked as a leaf,
     * the children array is entirely omitted to save memory.</p>
     *
     * @param minDegree the CLRS minimum degree (t) of the tree; must be &ge; 2
     * @param isLeaf    {@code true} if this node is a data leaf; {@code false} if it is an internal node
     */
    public NaryNode(int minDegree, boolean isLeaf){
        this((minDegree << 1) -1, (minDegree << 1), isLeaf);
    }

    /**
     * Returns the raw array of keys.
     *
     * @return the array of keys
     */
    public Object[] getKeys() {
        return keys;
    }

    /**
     * Returns the raw array of child pointers.
     *
     * @return the array of children
     */
    public N[] getChildren() {
        return children;
    }

    /**
     * Retrieves the key at the specified index.
     *
     * @param index the position of the key
     * @return the key at the given index
     */
    @SuppressWarnings("unchecked")
    public T getKey(int index) {
        return (T) keys[index];
    }

    /**
     * Sets the key at the specified index.
     *
     * @param index the position to store the key
     * @param value the key to store
     */
    public void setKey(int index, T value) {
        this.keys[index] = value;
    }

    /**
     * Retrieves the child node at the specified index.
     *
     * @param index the position of the child
     * @return the child node at the given index
     */
    public N getChild(int index) {
        return children[index];
    }

    /**
     * Sets the child node at the specified index.
     *
     * @param index the position to store the child pointer
     * @param child the child node to store
     */
    public void setChild(int index, N child) {
        this.children[index] = child;
    }

    /**
     * Returns the current number of active keys in this node.
     *
     * @return the number of active keys
     */
    public int getKeyCount() {
        return keyCount;
    }

    /**
     * Updates the number of active keys in this node.
     *
     * @param keyCount the new key count
     */
    public void setKeyCount(int keyCount) {
        this.keyCount = keyCount;
    }

    /**
     * Checks whether this node is a leaf (has no children).
     *
     * @return {@code true} if this node is a leaf, {@code false} otherwise
     */
    public boolean isLeaf() {
        return isLeaf;
    }

}