package chaos.tree.binary.treap;

import chaos.tree.core.searchtree.binary.node.BiNode;
import java.util.Random;

/**
 * Node representation specifically designed for a {@link Treap}.
 * * <p>In addition to standard binary tree elements, each {@code TreapNode}
 * maintains a unique, randomly assigned numeric priority. This priority enforces
 * the max-heap (or min-heap) structural property required to keep the tree
 * probabilistically balanced.</p>
 *
 * @param <T> the type of value stored within this node, must be {@link Comparable}
 * @see BiNode
 * @see Treap
 */
public class TreapNode<T extends Comparable<T>> extends BiNode<T,TreapNode<T>> {


    private static final Random RANDOM = new Random();
    /**
     * Constructs the node with the specified value
     *
     * @param value the value to be stored in current node
     */

    public TreapNode(T value){
        super(value);
        this.priority = RANDOM.nextInt();
    }
    private final int priority;

    protected int getPriority(){
        return priority;
    }
    public static void setSeed(long seed) {
        RANDOM.setSeed(seed);
    }
}
