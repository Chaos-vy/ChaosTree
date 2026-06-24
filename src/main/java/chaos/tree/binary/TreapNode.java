package chaos.tree.binary;

import chaos.tree.binary.Treap;
import chaos.tree.core.searchtree.binary.node.BiNode;

/**
 * Represents a node in a Treap ({@link Treap}).
 *
 * <p>Alongside the value and child references inherited from {@link BiNode}, this
 * node stores a randomly assigned numeric priority. This priority enforces
 * the max-heap structural property required to keep the Treap
 * probabilistically balanced.</p>
 *
 * @param <T> the type of value stored within this node; must implement {@link Comparable}
 * @see BiNode
 * @see Treap
 * @since 1.0.0
 */
class TreapNode<T extends Comparable<T>> extends BiNode<T,TreapNode<T>> {


    /** The priority of this node used to maintain the heap invariant. */
    private final int priority;

    /**
     * Constructs a new Treap node with the specified value and priority.
     *
     * @param value the value to store in this node
     * @param priority the priority assigned to this node for heap-ordering
     */
    TreapNode(T value, int priority){
        super(value);
        this.priority = priority;
    }

    /**
     * Returns the priority of this node.
     *
     * @return the node priority
     */
    public int getPriority(){
        return priority;
    }

}
