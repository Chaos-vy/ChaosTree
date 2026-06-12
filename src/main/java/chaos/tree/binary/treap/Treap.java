package chaos.tree.binary.treap;

import chaos.tree.core.searchtree.binary.rotation.AbstractRotateTree;

import java.util.Random;

/**
 * Randomized Binary Search Tree implementation known as a Treap.
 *
 * <p>A Treap combines the structural characteristics of a Binary Search Tree (BST)
 * and a Max-Heap. Node keys maintain strict BST order, while node priorities
 * (randomly generated upon insertion) satisfy max-heap properties.</p>
 *
 * <p>This probabilistic balancing strategy guarantees expected <b>O(log n)</b>
 * time complexity for search, insertion, and deletion operations, completely eliminating
 * the worst-case degradation associated with unbalanced trees without the strict
 * rebalancing overhead of AVL or Red-Black trees.</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @see AbstractRotateTree
 * @see TreapNode
 * @since 1.0.0
 */
public class Treap<T extends Comparable<T>> extends AbstractRotateTree<T, TreapNode<T>> {

    private final Random random;
    private final int priorityBound;

    /**
     * Constructs an empty Treap initializing a new {@link Random} instance
     * with an upper priority bound of {@link Integer#MAX_VALUE}.
     */
    public Treap() {
        this(new Random().nextInt(), Integer.MAX_VALUE);
    }

    /**
     * Constructs an empty Treap initializing a new {@link Random} instance
     * with the specified seed and an upper priority bound of {@link Integer#MAX_VALUE}.
     *
     * @param seed the initial seed value for the internal random number generator
     */
    public Treap(long seed) {
        this(seed, Integer.MAX_VALUE);
    }

    /**
     * Constructs an empty Treap initializing a new {@link Random} instance
     * with the specified seed and exclusive upper priority bound.
     *
     * @param seed          the initial seed value for the internal random number generator
     * @param priorityBound the exclusive upper bound for generated node priorities
     * @throws IllegalArgumentException if {@code priorityBound <= 1}
     */
    public Treap(long seed, int priorityBound) {
        if (priorityBound <= 0) {
            throw new IllegalArgumentException("Priority bound must be greater than 1 to prevent tree degradation.");
        }
        this.random = new Random(seed);
        this.priorityBound = priorityBound;
    }

    /**
     * Constructs an empty Treap utilizing the provided {@link Random} engine
     * and exclusive upper priority bound.
     *
     * @param random        the {@link Random} instance utilized for priority generation
     * @param priorityBound the exclusive upper bound for generated node priorities
     * @throws IllegalArgumentException if {@code priorityBound <= 1}
     * @throws NullPointerException     if {@code random} is {@code null}
     */
    public Treap(Random random, int priorityBound) {
        if (random == null) throw new NullPointerException();
        this.random = random;
        if (priorityBound <= 1) {
            throw new IllegalArgumentException("Priority bound must be positive");
        }
        this.priorityBound = priorityBound;
    }

    @Override
    protected TreapNode<T> createNode(T key) {
        int priority = random.nextInt(priorityBound);
        return new TreapNode<>(key, priority);
    }

    @Override
    protected String nodeText(TreapNode<T> node) {
        return node.getValue() + "(p=" + node.getPriority() + ")";
    }

    @Override
    protected TreapNode<T> afterInsert(TreapNode<T> node) {
        if(node.getLeft()!=null && node.getLeft().getPriority()>node.getPriority()){
            return rightRotate(node);
        }
        if(node.getRight()!=null && node.getRight().getPriority()>node.getPriority()){
            return leftRotate(node);
        }
        return node;
    }

    @Override
    protected DeleteResult<TreapNode<T>> delete(TreapNode<T> node, T value) {
        if(node == null) return deleteResult(null, false);
        int cmp = compare(value, node);

        if(cmp>0){
            DeleteResult<TreapNode<T>> result = delete(node.getRight(), value);
            if (!result.deleted()) return deleteResult(node, false);
            node.setRight(result.root());
        }
        else if(cmp<0){
            DeleteResult<TreapNode<T>> result = delete(node.getLeft(), value);
            if (!result.deleted()) return deleteResult(node, false);
            node.setLeft(result.root());
        }
        else {
            if(node.getRight()==null) return deleteResult(node.getLeft(), true);
            if(node.getLeft()==null) return deleteResult(node.getRight(), true);
            if(node.getRight().getPriority()>node.getLeft().getPriority()){
                node = leftRotate(node);
                node.setLeft(delete(node.getLeft(), value).root());
            }
            else{
                node = rightRotate(node);
                node.setRight(delete(node.getRight(), value).root());
            }
        }
        return deleteResult(node, true);
    }
}
