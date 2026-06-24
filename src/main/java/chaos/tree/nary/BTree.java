package chaos.tree.nary;

import chaos.tree.core.searchtree.nary.AbstractNaryTree;

/**
 * Self-balancing N-ary Search Tree implementation utilizing the standard B-Tree invariant.
 *
 * <p>A B-Tree is a massively-wide balanced search tree where nodes contain multiple keys
 * and child pointers. By enforcing strict constraints on the minimum and maximum degree
 * (the number of children), the tree ensures that every leaf node remains at the exact
 * same depth. Keys are distributed across both internal and leaf nodes.</p>
 *
 * <p>By maintaining these invariants through proactive node splitting, merging, and
 * borrowing during mutations, the tree guarantees <b>O(log_t(N))</b> time for search, insertion,
 * and deletion operations. Its high branching factor significantly reduces the overall height
 * of the tree, making it highly efficient for block-storage architectures and disk I/O.</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @see BTreeNode
 * @since 1.0.0
 */
public final class BTree<T extends Comparable<T>> extends AbstractNaryTree<T, BTreeNode<T>> implements NaryTree<T> {

    /**
     * Constructs an empty B-Tree with the specified maximum degree.
     *
     * @param degree the maximum number of children a node can have (must be &ge; 2)
     * @throws IllegalArgumentException if the degree is less than 2
     */
    public BTree(int degree){
        super(degree);
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
    public BTree(int degree, Iterable<? extends T> collection){
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
