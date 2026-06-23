package chaos.tree.binary;

import chaos.tree.binary.node.TreapNode;
import chaos.tree.core.searchtree.binary.rotation.AbstractRotateTree;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
public final class Treap<T extends Comparable<T>> extends AbstractRotateTree<T, TreapNode<T>> {

    private final Random random;
    private final int priorityBound;

    /**
     * Constructs an empty Treap using {@link ThreadLocalRandom} for priority
     * generation with an upper bound of {@link Integer#MAX_VALUE}.
     *
     * <p>This constructor is thread-safe for concurrent {@code insert()} calls.</p>
     */
    public Treap() {
        this.random = null;
        this.priorityBound = Integer.MAX_VALUE;
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
        if (priorityBound <= 1) {
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
        if (random == null) throw new NullPointerException("random must not be null");
        this.random = random;
        if (priorityBound <= 1) {
            throw new IllegalArgumentException("Priority bound must be greater than 1 to prevent tree degradation.");
        }
        this.priorityBound = priorityBound;
    }

    /**
     * Constructs a new Treap by inserting all elements from the specified iterable
     * using a default random engine and priority bound of {@link Integer#MAX_VALUE}.
     *
     * @param source the iterable collection containing elements to insert
     * @throws NullPointerException if {@code source} is {@code null}
     * @see #insertAll(Iterable)
     */
    public Treap(Iterable<T> source) {
        this();
        if (source == null) throw new NullPointerException("Source collection cannot be null.");
        insertAll(source);
    }

    /**
     * Constructs a deep structural copy of the specified source tree.
     *
     * <p>Clones nodes via pre-order traversal in <b>O(n)</b> time and <b>O(h)</b>
     * stack space, bypassing the insertion pipeline entirely. The cloned tree
     * uses {@link ThreadLocalRandom} for future inserts while preserving
     * the original priority bound.</p>
     *
     * @param source the Treap instance to deep copy
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public Treap(Treap<T> source) {
        if (source == null) throw new NullPointerException("Source tree cannot be null.");
        this.random = null;
        this.priorityBound = source.priorityBound;
        if (!source.isEmpty()) {
            this.root = cloneStructure(source.root);
            this.size = source.size();
        }
    }

    @Override
    protected TreapNode<T> createNode(T key) {
        int priority = (random != null) ? random.nextInt(priorityBound) : ThreadLocalRandom.current().nextInt(priorityBound);
        return new TreapNode<>(key, priority);
    }

    @Override
    protected TreapNode<T> copyNode(TreapNode<T> source) {
        return new TreapNode<>(source.getValue(), source.getPriority());
    }

    @Override
    protected String nodeText(TreapNode<T> node) {
        return node.getValue() + "(p=" + node.getPriority() + ")";
    }

    @Override
    protected TreapNode<T> afterInsert(TreapNode<T> node) {
        if (node.getLeft() != null && node.getLeft().getPriority() > node.getPriority()) {
            return rightRotate(node);
        }
        if (node.getRight() != null && node.getRight().getPriority() > node.getPriority()) {
            return leftRotate(node);
        }
        return node;
    }

    @Override
    protected DeleteResult<TreapNode<T>> delete(TreapNode<T> node, T value) {
        if (node == null) return deleteResult(null, false);
        int cmp = compare(value, node);

        if (cmp > 0) {
            DeleteResult<TreapNode<T>> result = delete(node.getRight(), value);
            if (!result.deleted()) return deleteResult(node, false);
            node.setRight(result.root());
        } else if (cmp < 0) {
            DeleteResult<TreapNode<T>> result = delete(node.getLeft(), value);
            if (!result.deleted()) return deleteResult(node, false);
            node.setLeft(result.root());
        } else {
            if (node.getRight() == null) return deleteResult(node.getLeft(), true);
            if (node.getLeft() == null) return deleteResult(node.getRight(), true);
            if (node.getRight().getPriority() > node.getLeft().getPriority()) {
                node = leftRotate(node);
                node.setLeft(delete(node.getLeft(), value).root());
            } else {
                node = rightRotate(node);
                node.setRight(delete(node.getRight(), value).root());
            }
        }
        return deleteResult(node, true);
    }
}
