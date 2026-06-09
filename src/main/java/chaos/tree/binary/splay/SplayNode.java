package chaos.tree.binary.splay;

import chaos.tree.core.searchtree.binary.node.ParentBiNode;

/**
 * Splay tree  node structure
 * @param <T> value type to be stored in node
 */
public class SplayNode<T extends Comparable<T>> extends ParentBiNode<T, SplayNode<T>> {
    /**
     * Constructs a Splay Tree node with the specified value.
     *
     * @param value the value to be stored in this node
     */
    public SplayNode(T value) {
        super(value);
    }
}
