package chaos.tree.binary;

import chaos.tree.core.searchtree.binary.node.ParentBiNode;

/**
 * Represents a node in a Splay Tree ({@link Splay}).
 *
 * <p>Alongside the value, parent, and child references inherited from {@link ParentBiNode},
 * this node serves as the fundamental block of the splaying operations. It does not
 * require any additional metadata (like height or color) to perform balancing, relying
 * solely on dynamic path restructuring.</p>
 *
 * @param <T> the type of value stored in this node; must implement {@link Comparable}
 * @see Splay
 * @see ParentBiNode
 * @since 1.0.0
 */
public class SplayNode<T extends Comparable<T>> extends ParentBiNode<T, SplayNode<T>> {
    /**
     * Constructs a Splay Tree node with the specified value.
     *
     * @param value the value to store in this node
     */
    SplayNode(T value) {
        super(value);
    }
}
