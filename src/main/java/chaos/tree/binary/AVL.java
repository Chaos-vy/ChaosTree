package chaos.tree.binary;

import chaos.tree.core.searchtree.binary.rotation.AbstractRotateTree;
import chaos.tree.core.searchtree.binary.AbstractBiTree;
import chaos.tree.exception.DuplicateNodeException;

/**
 * Height-balanced Binary Search Tree implementation utilizing the AVL invariant.
 * <p>An AVL tree is a strictly self-balancing structure where the height difference
 * (balance factor) between the left and right subtrees of any node is guaranteed
 * to be at most <b>1</b>.</p>
 * <p>By enforcing this strict structural constraint on every insertion and deletion,
 * the tree maintains a worst-case height bound of approximately 1.44 log n.
 * This makes it exceptionally fast, highly deterministic <b>O(log n)</b> lookups,
 * making it an excellent fit for read-heavy datasets.</p>
 *
 * @param <T> the type of elements maintained by this tree, must be {@link Comparable}
 * @see AbstractRotateTree
 * @see AVLNode
 * @see AbstractBiTree
 * @since 1.0.0
 */
public final class AVL<T extends Comparable<? super T>> extends AbstractRotateTree<T, AVLNode<T>> {

    //This value is taken into consideration after 1.44log base 2(Integer.MAX_VALUE) =~ 44
    private static final int PATH_CAPACITY = 48;
    @SuppressWarnings("unchecked")
    private final transient AVLNode<T>[] path = new AVLNode[PATH_CAPACITY];
    /*
    I used here boolean instead of int and byte because int is 4byte and byte will have runtime casting
    false represent -> left node.
    true represent -> right node.
     */
    private final transient boolean[] dirs = new boolean[PATH_CAPACITY];

    /**
     * Constructs an empty AVL tree.
     */
    public AVL() {
    }

    /**
     * Constructs a new AVL tree by inserting all elements from the specified iterable.
     *
     * @param source the iterable collection containing elements to insert
     * @throws NullPointerException if {@code source} is {@code null}
     * @see #insertAll(Iterable)
     */
    public AVL(Iterable<T> source) {
        if (source == null) throw new NullPointerException("Source collection cannot be null.");
        insertAll(source);
    }

    /**
     * Constructs a deep structural copy of the specified source tree.
     *
     * <p>Clones nodes via pre-order traversal in <b>O(n)</b> time and <b>O(h)</b>
     * stack space, bypassing the insertion pipeline entirely.</p>
     *
     * @param source the AVL instance to deep copy
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public AVL(AVL<T> source) {
        if (source == null) throw new NullPointerException("Source tree cannot be null.");
        if (!source.isEmpty()) {
            this.root = cloneStructure(source.root);
            this.size = source.size();
        }
    }

    @Override
    protected AVLNode<T> createNode(T value) {
        return new AVLNode<>(value);
    }

    @Override
    protected AVLNode<T> copyNode(AVLNode<T> source) {
        AVLNode<T> copy = new AVLNode<>(source.getValue());
        copy.setHeight(source.getHeight());
        return copy;
    }

    @Override
    protected AVLNode<T> afterInsert(AVLNode<T> node) {
        updateMetadata(node);
        return rebalanced(node);
    }

    @Override
    protected String nodeText(AVLNode<T> node) {
        return node.getValue() + "(h=" + node.getHeight() + ")";
    }

    /**
     * Book reference: An Introduction to
     * Binary Search Trees and Balanced Trees - Ben Pfaff
     * @param value the value to insert; must not be {@code null}
     */
    @Override
    public void insert(T value) {
        checkValue(value);
        if (root == null) {
            root = createNode(value);
            size++;
            modCount++;
            cachedHashedCode += value.hashCode();
            return;
        }

        AVLNode<T> curr = root;
        int k = 0;
        while (true) {
            path[k] = curr;
            int cmp = value.compareTo(curr.getValue());
            if (cmp == 0) {
                for (int i = 0; i <= k; i++) path[i] = null;
                throw new DuplicateNodeException("Value already present in tree");
            }
            dirs[k] = cmp > 0;
            AVLNode<T> next = dirs[k] ? curr.getRight() : curr.getLeft();
            if (next == null) break;
            curr = next;
            k++;
        }

        AVLNode<T> newNode = createNode(value);
        if (dirs[k]) curr.setRight(newNode);
        else curr.setLeft(newNode);

        path[k + 1] = newNode;
        size = Math.addExact(size, 1);
        modCount++;
        cachedHashedCode += value.hashCode();

        rebalancer(k);
        for (int i = 0; i <= k + 1; i++) path[i] = null;
    }

    @Override
    public void delete(T value) {
        checkValue(value);
        if (root == null) return;

        int k = 0;
        AVLNode<T> curr = root;
        while (curr != null) {
            path[k] = curr;
            int cmp = value.compareTo(curr.getValue());
            if (cmp == 0) break;
            dirs[k] = cmp > 0;
            curr = dirs[k]? curr.getRight() : curr.getLeft();
            k++;
        }

        if (curr == null) {
            for (int i = 0; i < k; i++) path[i] = null;
            return;
        }

        if (curr.getLeft() == null || curr.getRight() == null) {
            AVLNode<T> child = curr.getLeft() != null ? curr.getLeft() : curr.getRight();
            if (k == 0) {
                root = child;
            } else {
                if (dirs[k - 1]) path[k - 1].setRight(child);
                else path[k - 1].setLeft(child);
            }
            k--;
        } else {
            int s = k + 1;
            path[s] = curr.getRight();
            dirs[s - 1] = true;
            AVLNode<T> succ = curr.getRight();
            while (succ.getLeft() != null) {
                dirs[s] = false;
                succ = succ.getLeft();
                s++;
                path[s] = succ;
            }

            curr.setValue(succ.getValue());

            AVLNode<T> child = succ.getRight();
            if (s == k + 1) {
                path[k].setRight(child);
            } else {
                path[s - 1].setLeft(child);
            }
            k = s - 1;
        }

        size--;
        modCount++;
        cachedHashedCode -= value.hashCode();

        rebalancer(k);
        for (int i = 0; i < 64; i++) {
            if (path[i] == null) break;
            path[i] = null;
        }
    }

    private void rebalancer(int k) {
        for (int i = k; i >= 0; i--) {
            AVLNode<T> p = path[i];
            updateMetadata(p);
            AVLNode<T> rebalancedP = rebalanced(p);

            if (rebalancedP != p) {
                if (i == 0) {
                    root = rebalancedP;
                } else {
                    if (dirs[i - 1]) path[i - 1].setRight(rebalancedP);
                    else path[i - 1].setLeft(rebalancedP);
                }
            }
        }
    }

    @Override
    public int height() {
        return nodeHeight(root);
    }

    @Override
    protected void updateMetadata(AVLNode<T> root) {
        root.setHeight(1 + Math.max(nodeHeight(root.getLeft()), nodeHeight(root.getRight())));
    }

    private int nodeHeight(AVLNode<T> node) {
        return node == null ? -1 : node.getHeight();
    }

    private int getBalance(AVLNode<T> node) {
        return node == null ? 0 : nodeHeight(node.getLeft()) - nodeHeight(node.getRight());
    }

    private AVLNode<T> rebalanced(AVLNode<T> node) {
        if (getBalance(node) > 1) {
            if (getBalance(node.getLeft()) < 0) {
                node.setLeft(leftRotate(node.getLeft()));
            }
            return rightRotate(node);
        }
        if (getBalance(node) < -1) {
            if (getBalance(node.getRight()) > 0) {
                node.setRight(rightRotate(node.getRight()));
            }
            return leftRotate(node);
        }
        return node;
    }

    @Override
    protected AVLNode<T> afterDelete(AVLNode<T> node) {
        updateMetadata(node);
        return rebalanced(node);
    }
}
