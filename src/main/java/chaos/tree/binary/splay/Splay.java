package chaos.tree.binary.splay;

import chaos.tree.core.searchtree.binary.rotation.AbstractParentRotateTree;
import chaos.tree.exception.DuplicateNodeException;
/**
 * Self-adjusting Binary Search Tree implementation utilizing the splaying algorithm.
 *
 * <p>A Splay Tree is a self-balancing binary search tree where recently accessed elements
 * are quick to access again. Upon performing operations like search, insertion, or deletion,
 * the target node (or the last accessed node) is moved to the root of the tree using a
 * sequence of tree rotations called splaying. This property provides excellent support
 * for datasets with highly localized access patterns (locality of reference).</p>
 *
 * <p>By dynamically reshaping the tree structure, Splay Trees guarantee amortized
 * <b>O(log n)</b> time complexity for all basic search, insertion, and deletion operations.</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @see AbstractParentRotateTree
 * @see SplayNode
 * @since 1.0.0
 */
public class Splay<T extends Comparable<T>> extends AbstractParentRotateTree<T, SplayNode<T>> {
    @Override
    protected SplayNode<T> createNode(T value) {
        return new SplayNode<>(value);
    }

    @Override
    public void insert(T value) {
        checkValue(value);
        if (root == null) {
            root = createNode(value);
            size = Math.addExact(size, 1);
            modCount++;
            return;
        }
        SplayNode<T> newNode = bstInsert(root, value);
        splay(newNode);
        size = Math.addExact(size, 1);
        modCount++;
    }

    private SplayNode<T> bstInsert(SplayNode<T> node, T value) {
        int cmp = compare(value, node);
        if (cmp == 0) throw new DuplicateNodeException("Value already present in tree");
        if (cmp > 0) {
            if (node.getRight() == null) {
                SplayNode<T> n = createNode(value);
                node.setRight(n);
                n.setParent(node);
                return n;
            }
            return bstInsert(node.getRight(), value);
        } else {
            if (node.getLeft() == null) {
                SplayNode<T> n = createNode(value);
                node.setLeft(n);
                n.setParent(node);
                return n;
            }
            return bstInsert(node.getLeft(), value);
        }
    }

    private void splay(SplayNode<T> x) {
        if (x == null) return;

        while (x.getParent() != null) {
            SplayNode<T> p = x.getParent();
            SplayNode<T> g = p.getParent();

            if (g == null) {
                if (p.getLeft() == x) {
                    rightRotate(p);
                } else {
                    leftRotate(p);
                }
            } else if (p.getLeft() == x && g.getLeft() == p) {
                rightRotate(g);
                rightRotate(p);
            } else if (p.getRight() == x && g.getRight() == p) {
                leftRotate(g);
                leftRotate(p);
            } else if (p.getLeft() == x && g.getRight() == p) {
                rightRotate(p);
                leftRotate(g);
            } else {
                leftRotate(p);
                rightRotate(g);
            }
        }
    }

    @Override
    public boolean contains(T value) {
        checkValue(value);
        SplayNode<T> current = root;
        SplayNode<T> lastNonNull = null;

        while (current != null) {
            lastNonNull = current;
            int cmp = value.compareTo(current.getValue());
            if (cmp < 0) current = current.getLeft();
            else if (cmp > 0) current = current.getRight();
            else {
                splay(current);
                return true;
            }
        }
        if (lastNonNull != null) {
            splay(lastNonNull);
        }
        return false;
    }

    @Override
    protected DeleteResult<SplayNode<T>> delete(SplayNode<T> node, T value) {
        if (node == null) return deleteResult(null, false);
        if (!this.contains(value)) {
            modCount++;
            return deleteResult(root, false);
        }
        SplayNode<T> target = root;
        SplayNode<T> leftSubtree = target.getLeft();
        SplayNode<T> rightSubtree = target.getRight();
        target.setLeft(null);
        target.setRight(null);
        if (leftSubtree != null) leftSubtree.setParent(null);
        if (rightSubtree != null) rightSubtree.setParent(null);
        if (leftSubtree == null) {
            root = rightSubtree;
        } else if (rightSubtree == null) {
            root = leftSubtree;
        } else {
            root = leftSubtree;
            SplayNode<T> maxLeft = leftSubtree;
            while (maxLeft.getRight() != null) {
                maxLeft = maxLeft.getRight();
            }
            splay(maxLeft);
            root.setRight(rightSubtree);
            rightSubtree.setParent(root);
        }
        return deleteResult(root, true);
    }
}

