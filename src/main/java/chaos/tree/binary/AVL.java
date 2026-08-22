package chaos.tree.binary;

import chaos.tree.core.searchtree.binary.rotation.AbstractParentRotateTree;
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
 * @see AbstractParentRotateTree
 * @see AVLNode
 * @see AbstractBiTree
 * @since 1.0.0
 */
public final class AVL<T extends Comparable<? super T>> extends AbstractParentRotateTree<T, AVLNode<T>> {

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
            this.cachedHashedCode = source.hashCode();
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
    protected String nodeText(AVLNode<T> node) {
        return node.getValue() + "(h=" + node.getHeight() + ")";
    }

    @Override
    public int height() {
        return nodeHeight(root);
    }


    private void updateHeight(AVLNode<T> root) {
        root.setHeight(1 + Math.max(nodeHeight(root.getLeft()), nodeHeight(root.getRight())));
    }

    private int nodeHeight(AVLNode<T> node) {
        return node == null ? -1 : node.getHeight();
    }

    @Override
    protected AVLNode<T> leftRotate(AVLNode<T> node) {
        AVLNode<T> x = super.leftRotate(node);
        updateHeight(node);
        updateHeight(x);
        return x;
    }

    @Override
    protected AVLNode<T> rightRotate(AVLNode<T> node) {
        AVLNode<T> x = super.rightRotate(node);
        updateHeight(node);
        updateHeight(x);
        return x;
    }

    @Override
    public void insert(T value) {
        checkValue(value);
        if (root == null) {
            root = createNode(value);
            size = 1;
            modCount++;
            cachedHashedCode += value.hashCode();
            return;
        }

        AVLNode<T> parent = null;
        AVLNode<T> curr = root;
        int cmp = 0;
        while (curr != null) {
            parent = curr;
            cmp = compare(value, curr);
            if (cmp == 0) {
                throw new DuplicateNodeException("Value already present in tree");
            } else if (cmp < 0) {
                curr = curr.getLeft();
            } else {
                curr = curr.getRight();
            }
        }
        AVLNode<T> newNode = createNode(value);
        newNode.setParent(parent);
        if (cmp < 0) {
            parent.setLeft(newNode);
        } else {
            parent.setRight(newNode);
        }
        fixAfterModification(parent);

        size = Math.addExact(size, 1);
        modCount++;
        cachedHashedCode += value.hashCode();
    }
    private void fixAfterModification(AVLNode<T> node) {
        AVLNode<T> curr = node;
        while (curr != null) {
            int oldHeight = curr.getHeight();
            int leftH = nodeHeight(curr.getLeft());
            int rightH = nodeHeight(curr.getRight());
            curr.setHeight(1 + Math.max(leftH, rightH));

            int balance = leftH - rightH;
            if (balance > 1) {
                if (nodeHeight(curr.getLeft().getLeft()) - nodeHeight(curr.getLeft().getRight()) < 0) {
                    leftRotate(curr.getLeft());
                }
                curr = rightRotate(curr);
            }
            else if (balance < -1) {
                if (nodeHeight(curr.getRight().getLeft()) - nodeHeight(curr.getRight().getRight()) > 0) {
                    rightRotate(curr.getRight());
                }
                curr = leftRotate(curr);
            }
            if (oldHeight == curr.getHeight()) {
                break;
            }
            curr = curr.getParent();
        }
    }
    @Override
    public void delete(T value) {
        checkValue(value);
        AVLNode<T> target = findNode(root, value);
        if (target == null) {
            return;
        }
        if (target.getLeft() != null && target.getRight() != null) {
            AVLNode<T> successor = getMinNode(target.getRight());
            target.setValue(successor.getValue());
            target = successor;
        }

        AVLNode<T> child = (target.getLeft() != null) ? target.getLeft() : target.getRight();
        AVLNode<T> parent = target.getParent();

        if (child != null) {
            child.setParent(parent);
        }

        if (parent == null) {
            root = child;
        } else if (target == parent.getLeft()) {
            parent.setLeft(child);
        } else {
            parent.setRight(child);
        }
        fixAfterModification(parent);

        size--;
        modCount++;
        cachedHashedCode -= value.hashCode();
        /*
        The insert and delete of AVL tree is easy with addition of parent pointer do read the ADR-009 for more detail.
         */
    }
}
