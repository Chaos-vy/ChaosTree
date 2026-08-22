package chaos.tree.binary;

import static chaos.tree.binary.Color.*;

import chaos.tree.core.searchtree.binary.rotation.AbstractParentRotateTree;
import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.core.searchtree.binary.AbstractBiTree;

/**
 * Self-balancing Binary Search Tree implementation utilizing the Red-Black Tree invariant.
 *
 * <p>A Red-Black Tree is a balanced binary search tree where each node is colored either
 * {@link Color#RED} or {@link Color#BLACK}. By enforcing strict properties regarding node
 * coloring.
 * <p>The properties of RBT trees:</p>
 * <ol>
 *     <li>The root is BLACK</li>
 *     <li>Node can be either BLACK or RED</li>
 *     <li>All leaf node must be null/Black</li>
 *     <li>A RED node cannot have a red children</li>
 *     <li>Every path from a node to any descendant leaves contains the same no of BLACK node</li>
 * </ol>
 * <p>Tree guarantees that no path is more than twice as long as any other path.</p>
 *
 * <p>By maintaining these invariants through color flips and rotations on insertion and
 * deletion, the tree guarantees <b>O(log n)</b> search, insertion, and deletion times. This makes
 * it highly efficient and suitable for general-purpose applications with frequent inserts and deletes.</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @see RBTNode
 * @see Color
 * @see AbstractParentRotateTree
 * @see AbstractBiTree
 * @since 1.0.0
 */
public final class RBT<T extends Comparable<? super T>> extends AbstractParentRotateTree<T, RBTNode<T>> {

    /**
     * Constructs an empty Red-Black Tree.
     */
    public RBT() {
    }

    /**
     * Constructs a new Red-Black Tree by inserting all elements from the specified iterable.
     *
     * @param source the iterable collection containing elements to insert
     * @throws NullPointerException if {@code source} is {@code null}
     * @see #insertAll(Iterable)
     */
    public RBT(Iterable<T> source) {
        if (source == null) throw new NullPointerException("Source collection cannot be null.");
        insertAll(source);
    }

    /**
     * Constructs a deep structural copy of the specified source tree.
     *
     * <p>Clones nodes via pre-order traversal in <b>O(n)</b> time and <b>O(h)</b>
     * stack space, bypassing the insertion pipeline entirely.</p>
     *
     * @param source the RBT instance to deep copy
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public RBT(RBT<T> source) {
        if (source == null) throw new NullPointerException("Source tree cannot be null.");
        if (!source.isEmpty()) {
            this.root = cloneStructure(source.root);
            this.size = source.size();
            this.cachedHashedCode = source.hashCode();
        }
    }

    @Override
    protected RBTNode<T> createNode(T value) {
        return new RBTNode<>(value);
    }

    @Override
    protected RBTNode<T> copyNode(RBTNode<T> source) {
        RBTNode<T> copy = new RBTNode<>(source.getValue());
        copy.setColor(source.getColor());
        return copy;
    }

    @Override
    protected String nodeText(RBTNode<T> node) {
        return node.getValue() + (node.getColor() == BLACK ? "(B)" : "(R)");
    }

    private boolean isRed(RBTNode<T> node) {
        return node != null && node.getColor() == RED;
    }

    private void setColor(RBTNode<T> node, Color color) {
        if (node != null) node.setColor(color);
    }

    @Override
    public void insert(T value) {
        checkValue(value);
        if (root == null) {
            root = createNode(value);
            setColor(root, BLACK);
            size = 1;
            modCount++;
            cachedHashedCode += value.hashCode();
            return;
        }

        RBTNode<T> parent = null;
        RBTNode<T> curr = root;
        int cmp = 0;

        while (curr != null) {
            parent = curr;
            cmp = compare(value, curr);
            if (cmp == 0) {
                return;
            } else if (cmp < 0) {
                curr = curr.getLeft();
            } else {
                curr = curr.getRight();
            }
        }

        RBTNode<T> newNode = createNode(value);
        newNode.setParent(parent);
        if (cmp < 0) {
            parent.setLeft(newNode);
        } else {
            parent.setRight(newNode);
        }
        fixAfterInsertion(newNode);

        size = Math.addExact(size, 1);
        modCount++;
        cachedHashedCode += value.hashCode();
    }
    private void fixAfterInsertion(RBTNode<T> x) {
        setColor(x, RED);

        while (x != null && x != root && isRed(x.getParent())) {
            if (x.getParent() == x.getParent().getParent().getLeft()) {
                RBTNode<T> y = x.getParent().getParent().getRight(); // Uncle
                if (isRed(y)) {
                    setColor(x.getParent(), BLACK);
                    setColor(y, BLACK);
                    setColor(x.getParent().getParent(), RED);
                    x = x.getParent().getParent(); // Move up
                } else {
                    if (x == x.getParent().getRight()) {
                        x = x.getParent();
                        leftRotate(x);
                    }
                    setColor(x.getParent(), BLACK);
                    setColor(x.getParent().getParent(), RED);
                    rightRotate(x.getParent().getParent());
                }
            } else {
                RBTNode<T> y = x.getParent().getParent().getLeft(); // Uncle
                if (isRed(y)) {
                    setColor(x.getParent(), BLACK);
                    setColor(y, BLACK);
                    setColor(x.getParent().getParent(), RED);
                    x = x.getParent().getParent(); // Move up
                } else {
                    if (x == x.getParent().getLeft()) {
                        x = x.getParent();
                        rightRotate(x);
                    }
                    setColor(x.getParent(), BLACK);
                    setColor(x.getParent().getParent(), RED);
                    leftRotate(x.getParent().getParent());
                }
            }
        }
        setColor(root, BLACK);
    }

    @Override
    public void delete(T value) {
        if(value == null) return;
        RBTNode<T> target = findNode(root, value);
        if (target == null) {
            return;
        }
        deleteNode(target);
        size--;
        modCount++;
        cachedHashedCode -= value.hashCode();
    }

    private void deleteNode(RBTNode<T> node) {
        if (node.getLeft() != null && node.getRight() != null) {
            RBTNode<T> successor = getMinNode(node.getRight());
            node.setValue(successor.getValue());
            deleteNode(successor);
            return;
        }

        RBTNode<T> child = node.getLeft() != null ? node.getLeft() : node.getRight();

        if (isRed(node)) {
            rewireParent(node.getParent(), node, null);
            return;
        }

        if (isRed(child)) {
            child.setColor(BLACK);
            rewireParent(node.getParent(), node, child);
            return;
        }

        if (node == root) {
            root = null;
            return;
        }

        fixDoubleBlack(node);
        rewireParent(node.getParent(), node, null);
    }

    private void rewireParent(RBTNode<T> parent, RBTNode<T> node, RBTNode<T> replacement) {
        if (parent == null) {
            root = replacement;
            if (replacement != null) replacement.setParent(null);
            return;
        }
        boolean isLeftNode = parent.getLeft() == node;
        if (replacement != null) {
            replacement.setParent(parent);
        }
        if (isLeftNode) {
            parent.setLeft(replacement);
        } else {
            parent.setRight(replacement);
        }
    }

    private void fixDoubleBlack(RBTNode<T> node) {

        if (node == null) return;
        if (node == root) return;

        RBTNode<T> parent = node.getParent();
        boolean isLeftChild = parent.getLeft() == node;
        RBTNode<T> sibling = isLeftChild ? parent.getRight() : parent.getLeft();
        if (sibling == null) {
            fixDoubleBlack(parent);
            return;
        }
        if (isRed(sibling)) {
            sibling.setColor(BLACK);
            parent.setColor(RED);
            if (isLeftChild) {
                leftRotate(parent);
            } else {
                rightRotate(parent);
            }
            fixDoubleBlack(node);
            return;
        }
        boolean siblingHasRedChild = isRed(sibling.getLeft()) || isRed(sibling.getRight());
        if (!siblingHasRedChild) {
            sibling.setColor(RED);
            if (isRed(parent)) {
                parent.setColor(BLACK);
            } else {
                fixDoubleBlack(parent);
            }
            return;
        }
        if (isLeftChild) {
            if (isRed(sibling.getRight())) {
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                sibling.getRight().setColor(BLACK);
                leftRotate(parent);
            } else {
                sibling.getLeft().setColor(BLACK);

                rightRotate(sibling);

                sibling = parent.getRight();
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                leftRotate(parent);
            }
        } else {
            if (isRed(sibling.getLeft())) {
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                sibling.getLeft().setColor(BLACK);
                rightRotate(parent);
            } else {
                sibling.getRight().setColor(BLACK);
                leftRotate(sibling);
                sibling = parent.getLeft();
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                rightRotate(parent);
            }
        }
    }
}
