package chaos.tree.binary;

import chaos.tree.core.searchtree.binary.rotation.AbstractRotateTree;

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
 * <p>
 * Splay Tree Rotation Cases:
 *
 * <ul>
 *     <li><b>Zig</b> - Single right rotation (left child of root).</li>
 *     <li><b>Zag</b> - Single left rotation (right child of root).</li>
 *     <li><b>Zig-Zig</b> - Two consecutive right rotations (Left-Left).</li>
 *     <li><b>Zag-Zag</b> - Two consecutive left rotations (Right-Right).</li>
 *     <li><b>Zig-Zag</b> - Left rotation followed by a right rotation (Left-Right).</li>
 *     <li><b>Zag-Zig</b> - Right rotation followed by a left rotation (Right-Left).</li>
 * </ul>
 * <p><b>Thread-safety:</b> Concurrent use of this class is architecturally impossible
 * without sacrificing its core guarantee. {@link #contains(Comparable)} is a structural
 * write — it splays the accessed node to root — meaning there are no read-only operations
 * in this tree. Every call, including search, requires exclusive access. A
 * {@link java.util.concurrent.locks.ReadWriteLock} provides zero benefit; all operations
 * must contend on a single write lock, making concurrent throughput strictly worse than
 * sequential access with no gain. For concurrent workloads, the recommended pattern is
 * <b>clone-per-thread</b> — each thread owns an independent {@code Splay} instance.</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @see AbstractRotateTree
 * @see SplayNode
 * @since 1.0.0
 */
public final class Splay<T extends Comparable<? super T>> extends AbstractRotateTree<T, SplayNode<T>> {

    /**
     * Constructs an empty Splay Tree.
     */
    public Splay() {
    }

    /**
     * Constructs a new Splay Tree by inserting all elements from the specified iterable.
     *
     * @param source the iterable collection containing elements to insert
     * @throws NullPointerException if {@code source} is {@code null}
     * @see #insertAll(Iterable)
     */
    public Splay(Iterable<T> source) {
        if (source == null) throw new NullPointerException("Source collection cannot be null.");
        insertAll(source);
    }

    /**
     * Constructs a deep structural copy of the specified source tree.
     *
     * <p>Clones nodes via pre-order traversal in <b>O(n)</b> time and <b>O(h)</b>
     * stack space, bypassing the insertion pipeline entirely.</p>
     *
     * @param source the Splay instance to deep copy
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public Splay(Splay<T> source) {
        if (source == null) throw new NullPointerException("Source tree cannot be null.");
        if (!source.isEmpty()) {
            this.root = cloneStructure(source.root);
            this.size = source.size();
            this.cachedHashedCode = source.hashCode();
        }
    }

    @Override
    protected SplayNode<T> createNode(T value) {
        return new SplayNode<>(value);
    }

    @Override
    protected SplayNode<T> copyNode(SplayNode<T> source) {
        return new SplayNode<>(source.getValue());
    }

    @Override
    public void insert(T value) {
        if (value == null) return;
        if (root == null) {
            root = createNode(value);
            size = Math.addExact(size, 1);
            modCount++;
            cachedHashedCode += value.hashCode();
            return;
        }
        SplayNode<T> newNode = bstInsert(value);
        if(newNode == null) return;
        splay(newNode);
        size = Math.addExact(size, 1);
        modCount++;
        cachedHashedCode += value.hashCode();
    }

    /**
     * Iterative BST insertion. Descends to the correct leaf position
     * without recursive stack frames — safe under adversarial sorted input.
     *
     * @param value the value to insert
     * @return the newly created node, ready to be splayed to root
     * @throws DuplicateNodeException if value already exists in the tree
     */
    private SplayNode<T> bstInsert(T value) {
        SplayNode<T> current = root;

        while (true) {
            int cmp = value.compareTo(current.getValue());
            if (cmp == 0) return null;

            if (cmp > 0) {
                if (current.getRight() == null) {
                    SplayNode<T> n = createNode(value);
                    current.setRight(n);
                    n.setParent(current);
                    return n;
                }
                current = current.getRight();
            } else {
                if (current.getLeft() == null) {
                    SplayNode<T> n = createNode(value);
                    current.setLeft(n);
                    n.setParent(current);
                    return n;
                }
                current = current.getLeft();
            }
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
        if(value == null) return false;
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
        if (lastNonNull != null) splay(lastNonNull);
        return false;
    }

    /**
     * Deletes the specified value from the tree using an iterative search.
     *
     * <p>Descends iteratively to locate the target node, splays it to root,
     * then merges the left and right subtrees by splaying the in-order
     * predecessor of the right subtree to its root.</p>
     */
    @Override
    public void delete(T value) {
        if(value == null) return;
        if (root == null) return;

        SplayNode<T> current = root;
        SplayNode<T> lastNonNull = null;

        while (current != null) {
            int cmp = value.compareTo(current.getValue());
            if (cmp == 0) break;
            lastNonNull = current;
            current = cmp < 0 ? current.getLeft() : current.getRight();
        }

        if (current == null) {
            if (lastNonNull != null) splay(lastNonNull);
            return;
        }

        splay(current);

        SplayNode<T> leftSubtree = root.getLeft();
        SplayNode<T> rightSubtree = root.getRight();

        if (leftSubtree == null) {
            root = rightSubtree;
            if (root != null) {
                root.setParent(null);
            }
        } else {
            root = leftSubtree;
            root.setParent(null);
            SplayNode<T> maxNode = leftSubtree;
            while (maxNode.getRight() != null) {
                maxNode = maxNode.getRight();
            }
            splay(maxNode);
            root.setRight(rightSubtree);
            if (rightSubtree != null) {
                rightSubtree.setParent(root);
            }
        }

        size--;
        modCount++;
        cachedHashedCode -= value.hashCode();
    }
}
