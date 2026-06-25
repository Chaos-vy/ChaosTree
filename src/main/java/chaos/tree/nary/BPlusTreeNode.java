package chaos.tree.nary;

import chaos.tree.core.searchtree.nary.NaryNode;
import chaos.tree.nary.BPlusTree;
/**
 * Represents a node in a B+ Tree ({@link BPlusTree}).
 *
 * <p>Inheriting from {@link NaryNode}, this structure implements the physical memory
 * layout required for a B+ Tree index. If this node is an internal node, it acts purely
 * as a "Ghost Routing" index directing traversals down the tree. If this node is a leaf,
 * it contains actual data values and additionally maintains a {@code next} pointer, creating
 * a continuous, sequentially-linked list for extremely fast range queries.</p>
 *
 * @param <T> the type of value stored in this node; must be {@link Comparable}
 * @see BPlusTree
 * @see NaryNode
 * @since 1.0.0
 */
public class BPlusTreeNode<T extends Comparable<T>> extends NaryNode<T, BPlusTreeNode<T>> {
    /**
     * Constructs an N-ary node with the specified maximum capacities.
     *
     * @param degree the degree of this tree.
     * @param isLeaf determine leaf node of this tree.
     */
    BPlusTreeNode(int degree, boolean isLeaf) {
        super(degree, isLeaf);
    }

    private BPlusTreeNode<T> next;

    BPlusTreeNode<T> getNext(){
        return next;
    }
    /**
     * Sets the next node in the horizontal leaf chain.
     *
     * @param next the next BPlusTreeNode
     */
    public void setNext(BPlusTreeNode<T> next){
        this.next = next;
    }
}
