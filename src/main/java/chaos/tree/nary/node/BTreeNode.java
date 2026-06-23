package chaos.tree.nary.node;

import chaos.tree.core.searchtree.nary.NaryNode;
import chaos.tree.nary.BTree;

/**
 * Represents a node in a standard B-Tree ({@link BTree}).
 *
 * <p>Inheriting from {@link NaryNode}, this structure encapsulates arrays of keys
 * and child pointers bounded by the strict degree constraints of the B-Tree invariant.
 * Unlike B+ Tree nodes, a standard B-Tree node stores actual data payloads at both
 * internal and leaf levels.</p>
 *
 * @param <T> the type of value stored in this node; must be {@link Comparable}
 * @see BTree
 * @see NaryNode
 * @since 1.0.0
 */
public class BTreeNode<T extends Comparable<T>> extends NaryNode<T, BTreeNode<T>> {
    /**
     * Constructs an N-ary node with the specified maximum capacities.
     *
     * @param degree the degree of the tree.
     * @param isLeaf the type of structure.
     */
    public BTreeNode(int degree, boolean isLeaf) {
        super(degree, isLeaf);
    }
}