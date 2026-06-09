package chaos.tree.binary.treap;

import chaos.tree.core.searchtree.binary.rotation.AbstractRotateTree;

import java.util.Random;

/**
 * Randomized Binary Search Tree implementation known as a Treap.
 * * <p>A Treap combines the structural characteristics of a Binary Search Tree (BST)
 * and a Heap. Node keys maintain strict BST order, while node priorities
 * (randomly generated upon insertion) satisfy max-heap properties.</p>
 * * <p>This probabilistic balancing strategy guarantees expected <b>O(log n)</b>
 * time complexity for search, insert, and delete operations, completely eliminating
 * the worst-case degradation associated with un-balanced trees without the strict
 * rebalancing overhead of AVL or Red-Black trees.</p>
 *
 * @param <T> the type of elements maintained by this tree, must be {@link Comparable}
 * @see AbstractRotateTree
 */
public class Treap<T extends Comparable<T>> extends AbstractRotateTree<T, TreapNode<T>> {

    private static final Random RANDOM = new Random();
    private final Integer priorityBound;

    /**
     * Constructs a treap whose priorities use the full {@code int} range.
     */
    public Treap() {
        this.priorityBound = null;
    }

    /**
     * Constructs a treap whose priorities range from {@code 0} inclusive to the
     * specified bound exclusive.
     *
     * @param priorityBound the exclusive upper bound for generated priorities
     * @throws IllegalArgumentException if {@code priorityBound} is not positive
     */
    public Treap(int priorityBound) {
        if (priorityBound <= 0) {
            throw new IllegalArgumentException("Priority bound must be positive");
        }
        this.priorityBound = priorityBound;
    }

    /**
     * Sets the seed of the shared random-priority generator.
     *
     * <p>This affects priorities generated for subsequently inserted values in
     * every treap instance.</p>
     *
     * @param seed the new random seed
     */
    public static void setSeed(long seed) {
        RANDOM.setSeed(seed);
    }

    @Override
    protected TreapNode<T> createNode(T key) {
        int priority = priorityBound == null ? RANDOM.nextInt() : RANDOM.nextInt(priorityBound);
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
