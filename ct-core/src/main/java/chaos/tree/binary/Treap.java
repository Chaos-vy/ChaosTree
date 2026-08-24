package chaos.tree.binary;

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
public final class Treap<T extends Comparable<? super T>> extends AbstractRotateTree<T, TreapNode<T>> {

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
            this.cachedHashedCode = source.hashCode();
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
    public void insert(T value) {
        if(value == null) return;
        TreapNode<T> newNode = createNode(value);
        if (root == null) {
            root = newNode;
            size = Math.addExact(size, 1);
            modCount++;
            cachedHashedCode += value.hashCode();
            return;
        }

        TreapNode<T> curr = root;
        TreapNode<T> parent = null;
        int cmp = 0;

        while (curr != null) {
            parent = curr;
            cmp = compare(value, curr);
            if (cmp < 0) {
                curr = curr.getLeft();
            } else if (cmp > 0) {
                curr = curr.getRight();
            } else {
                return;
            }
        }

        newNode.setParent(parent);
        if (cmp < 0) {
            parent.setLeft(newNode);
        } else {
            parent.setRight(newNode);
        }

        size = Math.addExact(size, 1);
        modCount++;
        cachedHashedCode += value.hashCode();
        curr = newNode;
        while (curr.getParent() != null && curr.getPriority() > curr.getParent().getPriority()) {
            TreapNode<T> p = curr.getParent();
            if (curr == p.getLeft()) {
                rightRotate(p);
            } else {
                leftRotate(p);
            }
        }
    }

    @Override
    public void delete(T value) {
        if (root == null) return;
        if(value == null) return;

        TreapNode<T> curr = root;
        while (curr != null) {
            int cmp = compare(value, curr);
            if (cmp < 0) {
                curr = curr.getLeft();
            } else if (cmp > 0) {
                curr = curr.getRight();
            } else {
                break;
            }
        }

        if (curr == null) return;
        while (curr.getLeft() != null && curr.getRight() != null) {
            if (curr.getLeft().getPriority() > curr.getRight().getPriority()) {
                rightRotate(curr);
            } else {
                leftRotate(curr);
            }
        }
        TreapNode<T> child = (curr.getLeft() != null) ? curr.getLeft() : curr.getRight();
        TreapNode<T> parent = curr.getParent();

        if (parent == null) {
            root = child;
        } else if (curr == parent.getLeft()) {
            parent.setLeft(child);
        } else {
            parent.setRight(child);
        }

        if (child != null) {
            child.setParent(parent);
        }

        size--;
        modCount++;
        cachedHashedCode -= value.hashCode();
    }
}
