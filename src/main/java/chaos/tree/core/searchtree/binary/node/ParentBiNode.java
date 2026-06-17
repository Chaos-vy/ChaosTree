package chaos.tree.core.searchtree.binary.node;

/**
 * Base node implementation for binary search tree structures that maintain
 * a permanent reference to their parent node.
 *
 * <p>Extends {@link BiNode} to add bidirectional relationship capabilities,
 * which are heavily used in self-adjusting trees like Red-Black Trees or Splay Trees.</p>
 *
 * @param <T> the type of value stored in the node, must implement {@link Comparable}
 * @param <N> the concrete parent-tracking node type
 * @since 1.0.0
 *
 */
public abstract class ParentBiNode<T extends Comparable<T>, N extends ParentBiNode<T,N>> extends BiNode<T,N> {

    private N parent;

    /**
     * Constructs the node with the specified value
     *
     * @param value the value to be stored in current node
     */
    public ParentBiNode(T value) {
        super(value);
    }

    /**
     * Updates the parent reference of this node.
     *
     * @param parent the new parent node
     */
    public void setParent(N parent){
        this.parent=parent;
    }
    /**
     * Return the parent of node
     *
     * @return the parent node
     */
    public N getParent() {
        return parent;
    }
}
