package chaos.tree.nary;

import chaos.tree.core.searchtree.nary.AbstractNaryTree;

/**
 * A self-balancing N-ary search tree parameterized by its
 * <em>minimum degree</em> {@code t}.
 *
 * <p>Unlike binary search trees, each node stores multiple sorted keys and
 * may have multiple children, resulting in a shallow tree with logarithmic
 * height. B-Trees provide {@code O(log n)} search, insertion, and deletion
 * operations while minimizing structural changes.</p>
 *
 * <p>This implementation follows the minimum degree definition described in
 * <em>Introduction to Algorithms (CLRS)</em>, where:</p>
 *
 * <ul>
 *     <li>Every non-root node contains between {@code t - 1} and
 *     {@code 2t - 1} keys.</li>
 *     <li>Every internal non-root node has between {@code t} and
 *     {@code 2t} children.</li>
 *     <li>The root contains at least one key unless the tree is empty.</li>
 *     <li>All leaf nodes appear at the same depth.</li>
 * </ul>
 *
 * <p>The high branching factor keeps the tree height small, making B-Trees
 * particularly well suited for databases, file systems, and storage systems
 * where reducing page or disk accesses is essential.</p>
 * <p>
 * Node Design:
 *
 * <p>This implementation optimizes memory usage by allocating child arrays
 * only for internal nodes. Leaf nodes do not maintain a children array,
 * reducing the per-node memory footprint since leaf nodes never reference
 * child nodes.</p>
 *
 * @param <T> the type of elements maintained by this tree
 * @see BTreeNode
 * @since 1.0.0
 */
public final class BTree<T extends Comparable<T>> extends AbstractNaryTree<T, BTreeNode<T>> implements NaryTree<T> {

    private static final int DEFAULT_DEGREE = 32;

    /**
     * Creates a B-Tree using the default degree (32),
     * selected as the general-purpose balance between
     * memory density, cache locality, and mutation cost.
     */
    public BTree() {
        super(DEFAULT_DEGREE);
    }

    /**
     * Constructs an empty B-Tree with the specified maximum degree.
     *
     * @param degree the maximum number of children a node can have (must be &ge; 2)
     * @throws IllegalArgumentException if the degree is less than 2
     */
    public BTree(int degree) {
        super(degree);
    }

    /**
     * Constructs an empty B-Tree using the default minimum degree.
     * <p>
     * The default degree is {@value #DEFAULT_DEGREE}, providing a balanced
     * trade-off between memory density, cache locality, and mutation cost
     * for general-purpose workloads.
     *
     * @param collection the collection of elements to insert
     */
    public BTree(Iterable<? extends T> collection) {
        this(DEFAULT_DEGREE, collection);
    }

    /**
     * Constructs a B-Tree with the specified degree and populates it with elements
     * from the provided iterable collection.
     * <p>If the provided collection is another {@code BTree} instance with the exact
     * same degree, this constructor executes an optimized <b>O(N)</b> structural deep clone
     * of the source tree, entirely bypassing sequential re-insertion logic. Otherwise,
     * elements are inserted sequentially yielding <b>O(N log_t(N))</b> time complexity.</p>
     *
     * @param degree     the maximum number of children a node can have (must be &ge; 2)
     * @param collection the collection whose elements are to be placed into this tree
     * @throws IllegalArgumentException if the degree is less than 2
     * @throws NullPointerException     if the collection or any of its elements are {@code null}
     */
    @SuppressWarnings("unchecked")
    public BTree(int degree, Iterable<? extends T> collection) {
        super(degree);
        if (collection == null) return;
        if (collection instanceof BTree) {
            BTree<T> other = (BTree<T>) collection;
            if (this.degree == other.degree) {
                if (other.root != null) {
                    this.root = deepCloneNode(other.root);
                    this.size = other.size;
                }
                return;
            }
        }

        for (T item : collection) {
            this.insert(item);
        }
    }

    /**
     * Constructs a physical deep clone of the provided B-Tree.
     * <p><b>Complexity:</b> O(N) time to physically copy the nodes, and O(log_t(N)) auxiliary space for the recursive call stack.</p>
     *
     * @param other the tree to clone
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public BTree(BTree<T> other) {
        super(other.degree);
        if (other.root != null) {
            this.root = deepCloneNode(other.root);
            this.size = other.size;
            this.cachedHashedCode = other.cachedHashedCode;
        }
    }

    /**
     * Recursively performs a physical deep clone of a node and its entire sub-hierarchy.
     * <p><b>Complexity:</b> O(N) time when N is the number of nodes in the subtree, O(log_t(N)) space for the recursive call stack.</p>
     *
     * @param original the original node to clone
     * @return a completely unlinked, physically distinct clone of the node hierarchy
     */
    private BTreeNode<T> deepCloneNode(BTreeNode<T> original) {
        BTreeNode<T> clone = createNode(this.degree, original.isLeaf());
        clone.setKeyCount(original.getKeyCount());
        System.arraycopy(original.getKeys(), 0, clone.getKeys(), 0, original.getKeyCount());
        if (!original.isLeaf()) {
            for (int i = 0; i <= original.getKeyCount(); i++) {
                BTreeNode<T> childToClone = original.getChild(i);
                if (childToClone != null) {
                    clone.setChild(i, deepCloneNode(childToClone));
                }
            }
        }

        return clone;
    }

    @Override
    protected BTreeNode<T> createNode(int degree, boolean isLeaf) {
        return new BTreeNode<>(degree, isLeaf);
    }
}
