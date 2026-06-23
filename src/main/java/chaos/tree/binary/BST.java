package chaos.tree.binary;
import chaos.tree.binary.node.BSTNode;
import chaos.tree.core.searchtree.binary.AbstractBiTree;

/**
 * Standard, unbalanced Binary Search Tree (BST) implementation.
 *
 * <p>This tree places elements according to binary search tree invariants:
 * strictly smaller values in the left subtree, and strictly greater values
 * in the right subtree. Because it lacks any self-balancing mechanism,
 * insertions of already-sorted or reverse-sorted data can degrade the
 * tree performance to O(n).</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @see AbstractBiTree
 * @see BSTNode
 * @since 1.0.0
 */
public final class BST<T extends Comparable<T>> extends AbstractBiTree<T, BSTNode<T>> {
    @Override
    protected BSTNode<T> createNode(T value) {
        return new BSTNode<>(value);
    }

    /**
     * Constructs an empty BST.
     */
    public BST() {}

    @Override
    protected BSTNode<T> copyNode(BSTNode<T> source) {
        return new BSTNode<>(source.getValue());
    }

    /**
     * Constructs a new BST by inserting all elements from the specified iterable.
     *
     * <p>Elements are inserted in iteration order. Already-sorted input will
     * degrade the tree to O(n) height.</p>
     *
     * @param source the iterable collection containing elements to insert
     * @throws NullPointerException if {@code source} is {@code null}
     * @see #insertAll(Iterable)
     */
    public BST(Iterable<T> source) {
        super();
        if (source == null) {
            throw new NullPointerException("Source collection cannot be null.");
        }
        insertAll(source);
    }

    /**
     * Constructs a deep structural copy of the specified source tree.
     *
     * <p>Clones nodes via pre-order traversal in <b>O(n)</b> time and <b>O(h)</b>
     * stack space, bypassing the insertion pipeline entirely.</p>
     *
     * @param source the BST instance to deep copy
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public BST(BST<T> source) {
        if (source == null) {
            throw new NullPointerException("Source tree cannot be null.");
        }
        if (!source.isEmpty()) {
            this.root = cloneStructure(source.root);
            this.size = source.size();
        }
    }

}
