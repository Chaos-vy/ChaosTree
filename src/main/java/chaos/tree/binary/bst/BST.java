package chaos.tree.binary.bst;
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
public class BST<T extends Comparable<T>> extends AbstractBiTree<T, BSTNode<T>> {
    @Override
    protected BSTNode<T> createNode(T value) {
        return new BSTNode<>(value);
    }
}
