package chaos.tree.binary;
import chaos.tree.core.searchtree.binary.AbstractBiTree;
import chaos.tree.exception.DuplicateNodeException;

/**
 * Standard, unbalanced Binary Search Tree (BST) implementation.
 *
 * <p>This tree places elements according to binary search tree invariants:
 * strictly smaller values in the left subtree, and strictly greater values
 * in the right subtree. Because it lacks any self-balancing mechanism,
 * insertions of already-sorted or reverse-sorted data can degrade the
 * tree performance to O(n).</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @see AbstractBiTree
 * @see BSTNode
 * @since 1.0.0
 */
public final class BST<T extends Comparable<? super T>> extends AbstractBiTree<T, BSTNode<T>> {

    /**
     * Constructs an empty BST.
     */
    public BST() {}

    @Override
    protected BSTNode<T> copyNode(BSTNode<T> source) {
        return new BSTNode<>(source.getValue());
    }

    /**
     * Constructs a new BST by inserting all elements from the specified iterable.
     *
     * <p>Elements are inserted in iteration order. Already-sorted input will
     * degrade the tree to O(n) height.</p>
     *
     * @param source the iterable collection containing elements to insert
     * @throws NullPointerException if {@code source} is {@code null}
     * @see #insertAll(Iterable)
     */
    public BST(Iterable<T> source) {
        super();
        if (source == null) {
            throw new NullPointerException("Source collection cannot be null.");
        }
        insertAll(source);
    }

    /**
     * Constructs a deep structural copy of the specified source tree.
     *
     * <p>Clones nodes via pre-order traversal in <b>O(n)</b> time and <b>O(h)</b>
     * stack space, bypassing the insertion pipeline entirely.</p>
     *
     * @param source the BST instance to deep copy
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public BST(BST<T> source) {
        if (source == null) {
            throw new NullPointerException("Source tree cannot be null.");
        }
        if (!source.isEmpty()) {
            this.root = cloneStructure(source.root);
            this.size = source.size();
        }
    }

    @Override
    protected BSTNode<T> createNode(T value) {
        return new BSTNode<>(value);
    }

    @Override
    public void insert(T value) {
        checkValue(value);
        if(root == null){
            root = new BSTNode<>(value);
            size += 1;
            modCount += 1;
            return;
        }
        BSTNode<T> curr = root;
        while (curr!=null){
            int cmp = compare(value, curr);
            if(cmp == 0) throw new DuplicateNodeException("Value already exist in tree");
            if(cmp > 0) {
                if(curr.getRight() == null){
                    curr.setRight(new BSTNode<>(value));
                    size += 1;
                    modCount += 1;
                    return;
                }
                curr = curr.getRight();
            }
            else {
                if(curr.getLeft() == null){
                    curr.setLeft(new BSTNode<>(value));
                    size += 1;
                    modCount += 1;
                    return;
                }
                curr = curr.getLeft();
            }
        }
    }

    @Override
    public void delete(T value) {
        if (root == null) return;
        checkValue(value);

        BSTNode<T> curr = root;
        BSTNode<T> prev = null;

        while (curr != null && compare(value, curr) != 0) {
            prev = curr;
            if (compare(value, curr) > 0) {
                curr = curr.getRight();
            } else {
                curr = curr.getLeft();
            }
        }

        if (curr == null) return;
        if (curr.getLeft() != null && curr.getRight() != null) {

            BSTNode<T> succParent = curr;
            BSTNode<T> succ = curr.getRight();

            while (succ.getLeft() != null) {
                succParent = succ;
                succ = succ.getLeft();
            }

            curr.setValue(succ.getValue());

            curr = succ;
            prev = succParent;
        }

        BSTNode<T> child = (curr.getLeft() != null) ? curr.getLeft() : curr.getRight();

        if (prev == null) {
            root = child;
        } else if (prev.getLeft() == curr) {
            prev.setLeft(child);
        } else {
            prev.setRight(child);
        }

        size--;
        modCount++;
    }

}
