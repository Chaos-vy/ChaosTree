package chaos.tree.binary.rbt;
import static chaos.tree.binary.rbt.Color.*;
import chaos.tree.core.searchtree.binary.rotation.AbstractParentRotateTree;
import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.core.searchtree.binary.AbstractBiTree;

/**
 * Self-balancing Binary Search Tree implementation utilizing the Red-Black Tree invariant.
 *
 * <p>A Red-Black Tree is a balanced binary search tree where each node is colored either
 * {@link Color#RED} or {@link Color#BLACK}. By enforcing strict properties regarding node
 * coloring (e.g., the root is black, red nodes cannot have red children, and every path
 * from a node to any of its descendant leaves contains the same number of black nodes), the
 * tree guarantees that no path is more than twice as long as any other path.</p>
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
public class RBT<T extends Comparable<T>> extends AbstractParentRotateTree<T, RBTNode<T>> {



    @Override
    protected RBTNode<T> createNode(T value) {
        return new RBTNode<>(value);
    }

    @Override
    protected String nodeText(RBTNode<T> node) {
        return node.getValue() + (node.getColor()==BLACK ? "(B)" : "(R)");
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
        root = insert(root, value);
        setColor(root, BLACK);
        size = Math.addExact(size, 1);
        modCount++;
    }

    @Override
    protected RBTNode<T> insert(RBTNode<T> node, T value) {
        if (node == null) {
            return createNode(value);
        }
        int cmp = compare(value, node);

        if (cmp == 0) {
            throw new DuplicateNodeException("Value already present in tree");
        }
        if (cmp > 0) {
            node.setRight(insert(node.getRight(), value));
            node.getRight().setParent(node);
        } else {
            node.setLeft(insert(node.getLeft(), value));
            node.getLeft().setParent(node);
        }
        return afterInsert(node);
    }

    private void refactorUncleRed(RBTNode<T> node) {
        setColor(node, RED);
        setColor(node.getLeft(), BLACK);
        setColor(node.getRight(), BLACK);
    }

    private void refactorUncleBlack(RBTNode<T> node, RBTNode<T> child) {
        setColor(node, BLACK);
        setColor(child, RED);
    }

    @Override
    protected RBTNode<T> afterInsert(RBTNode<T> node) {

        RBTNode<T> left = node.getLeft();
        RBTNode<T> right = node.getRight();

        if (isRed(left) && isRed(left.getLeft())) {
            if (!isRed(right)) {
                node = rightRotate(node);
                refactorUncleBlack(node, node.getRight());
            } else {
                refactorUncleRed(node);
            }
        } else if (isRed(left) && isRed(left.getRight())) {
            if (!isRed(right)) {
                node.setLeft(leftRotate(left));
                node = rightRotate(node);
                refactorUncleBlack(node, node.getRight());
            } else {
                refactorUncleRed(node);
            }
        } else if (isRed(right) && isRed(right.getRight())) {
            if (!isRed(left)) {
                node = leftRotate(node);
                refactorUncleBlack(node, node.getLeft());
            } else {
                refactorUncleRed(node);
            }
        } else if (isRed(right) && isRed(right.getLeft())) {
            if (!isRed(left)) {
                node.setRight(rightRotate(right));
                node = leftRotate(node);
                refactorUncleBlack(node, node.getLeft());
            } else {
                refactorUncleRed(node);
            }
        }
        return node;
    }

    @Override
    public void delete(T value) {
        checkValue(value);
        RBTNode<T> target = findNode(root, value);
        if (target == null) {
            return;
        }
        deleteNode(target);
        size--;
        modCount++;
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

        if (node == root) { root = null; return; }

        fixDoubleBlack(node);
        rewireParent(node.getParent(), node, null);
    }

    /**
     * Replaces {@code node} with {@code replacement} in the tree structure,
     * updating all parent references accordingly.
     *
     * @param parent      the parent of {@code node}; {@code null} if {@code node} is root
     * @param node        the node being removed
     * @param replacement the node taking {@code node}'s position; {@code null} for leaf removal
     */
    private void rewireParent(RBTNode<T> parent, RBTNode<T> node, RBTNode<T> replacement) {
        if (parent == null){
            root = replacement;
            if (replacement != null) replacement.setParent(null);
            return;
        }
        boolean isLeftNode = parent.getLeft()==node;
        if(replacement != null){
            replacement.setParent(parent);
        }
        if(isLeftNode){
            parent.setLeft(replacement);
        }
        else {
            parent.setRight(replacement);
        }
    }

    /**
     * Restores Red-Black properties after removal of a black node
     * by propagating the double-black condition up toward the root.
     *
     * @param node the node holding the double-black condition
     */
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
        boolean siblingHasRedChild = isRed(sibling.getLeft()) ||isRed(sibling.getRight());
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
            }
            else {
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
            }
            else {
                sibling.getRight().setColor(BLACK);
                leftRotate(sibling);
                sibling = parent.getLeft();
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                rightRotate(parent);
            }
        }
    }

    /** For internal testing only. Will be removed in a stable release. */
    public boolean validateRBT() {
        return validateRootBlack() && validateNoRedRed(root) && validateBlackHeight(root) != -1;
    }
    private boolean validateRootBlack() {
        return root == null || root.getColor()==BLACK;
    }
    private boolean validateNoRedRed(RBTNode<T> node) {
        if (node == null) return true;
        if (node.getColor()==RED) {
            if ((node.getLeft() != null && node.getLeft().getColor()==RED) || (node.getRight() != null && node.getRight().getColor()==RED)) {
                return false;
            }
        }
        return validateNoRedRed(node.getLeft()) && validateNoRedRed(node.getRight());
    }
    private int validateBlackHeight(RBTNode<T> node){
        if (node == null) return 1;
        int left = validateBlackHeight(node.getLeft());
        int right = validateBlackHeight(node.getRight());
        if (left == -1 || right == -1) return -1;
        if (left != right) return -1;
        return left + (node.getColor()==BLACK ? 1 : 0);
    }
}
