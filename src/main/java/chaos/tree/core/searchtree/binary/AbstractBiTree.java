package chaos.tree.core.searchtree.binary;

import chaos.tree.core.searchtree.binary.node.BiNode;
import chaos.tree.core.searchtree.binary.node.ParentBiNode;
import chaos.tree.exception.*;
import chaos.tree.exception.EmptyTreeException;
import chaos.tree.traversal.TraversalType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * Foundation abstract base class implementing the core operations of a Binary Search Tree.
 * <p>This base class coordinates the underlying data-invariant tracking and structural
 * mechanics shared among all specialized variations (e.g., standard BST, AVL, RBT, Splay, Treap).
 * It manages fundamental logic flows including recursive searching, tree metadata inquiries,
 * tree-structure string builders, boundaries, and element replacements.</p>
 * <p><b>Null-handling policy:</b> this implementation does not permit {@code null}
 * element values. Binary-search-tree navigation compares every requested value with
 * existing node values using {@link Comparable#compareTo(Object)}; allowing
 * {@code null} would make those comparisons undefined and would also break the
 * sorted-order contract of the tree. Public value-based operations therefore fail
 * fast with {@link NullPointerException} before traversal begins.</p>
 *
 * <p><b>Concurrency Note:</b> This implementation is not thread-safe. If multiple threads
 * access an instance concurrently, and at least one thread modifies the tree structurally,
 * external synchronization must be provided.</p>
 *
 * @param <T> the type of elements maintained by this tree, must implement {@link Comparable}
 * @param <N> the specific type of {@link BiNode} handled by this tree implementation
 * @see BinaryTree
 * @see BiNode
 * @see ParentBiNode
 * @since 1.0.0
 */
public abstract class AbstractBiTree<T extends Comparable<T>, N extends BiNode<T, N>> implements BinaryTree<T> {

    /**
     * Root of the Binary Search tree
     */
    protected volatile N root;

    /**
     * Total element present in this tree
     */
    protected volatile int size;

    /**
     * Stores the current modification of this tree
     */
    protected volatile long modCount = 0;

    /**
     * Construct an empty Binary tree
     */
    protected AbstractBiTree() {
    }

    /**
     * Creates a new node with the specified value.
     *
     * @param value the value to store in the node
     * @return the newly created node
     */
    protected abstract N createNode(T value);

    /**
     * Creates a shallow copy of the specified node, replicating its value
     * and any subclass-specific metadata (e.g., height, priority, color).
     *
     * <p>Child and parent references are not copied — those are wired
     * by {@link #cloneStructure}.</p>
     *
     * @param source the node to copy
     * @return a new node with the same value and metadata
     */
    protected abstract N copyNode(N source);

    /**
     * Recursively deep-copies the subtree rooted at the specified node
     * in O(n) time and O(h) stack space.
     *
     * @param node the subtree root to clone; may be {@code null}
     * @return the cloned subtree root, or {@code null}
     */
    protected N cloneStructure(N node) {
        if (node == null) return null;
        N clone = copyNode(node);
        clone.setLeft(cloneStructure(node.getLeft()));
        clone.setRight(cloneStructure(node.getRight()));
        return clone;
    }

    /**
     * Verifies that a tree element value is non-null before a value-based operation
     * starts comparing or traversing nodes.
     *
     * <p>This method is the central null guard used by operations such as
     * {@link #insert(Comparable)}, {@link #contains(Comparable)}, {@link #delete(Comparable)},
     * {@link #floor(Comparable)}, {@link #ceil(Comparable)}, {@link #successor(Comparable)},
     * {@link #predecessor(Comparable)}, and {@link #lca(Comparable, Comparable)}.
     * The guard is intentionally executed before calling {@link Comparable#compareTo(Object)}
     * so callers receive a clear failure reason instead of an indirect comparison failure.</p>
     *
     * @param value the value supplied by the caller
     * @throws NullPointerException if {@code value} is {@code null}, because this tree
     *                              stores only comparable, non-null values
     */
    protected void checkValue(T value) {
        Objects.requireNonNull(value, "Value cannot be null");
    }

    @Override
    public final int order() {
        return 2;
    }

    /**
     * Verifies that this tree contains at least one value.
     *
     * @throws EmptyTreeException if this tree has no root node
     */
    protected void treeIsEmpty() {
        if (isEmpty()) {
            throw new EmptyTreeException("Tree is empty");
        }
    }

    /**
     * Compares the values of two nodes.
     *
     * @param value the value to compare
     * @param curr  the second node to compare
     * @return a negative integer, zero, or a positive integer
     * if the first node value is less than, equal to,
     * or greater than the second node value
     * @throws NullPointerException if {@code value} is {@code null}, or if
     *                              {@code curr} is {@code null}
     */
    protected int compare(T value, N curr) {
        return value.compareTo(curr.getValue());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
        modCount++;
    }

    @Override
    public int height() {
        return height(root);
    }

    /**
     * Calculates the height of the subtree rooted at the supplied node.
     *
     * @param root the root of the subtree; may be {@code null}
     * @return {@code -1} when {@code root} is {@code null}; otherwise the number
     * of edges on the longest downward path from {@code root}
     */
    protected int height(N root) {
        if (root == null) return -1;
        int left = 1 + height(root.getLeft());
        int right = 1 + height(root.getRight());
        return Math.max(left, right);
    }

    /**
     * Return the minimum value present in the tree.
     *
     * @return the minimum value
     */
    @Override
    public T min() {
        treeIsEmpty();
        return getMinNode(root).getValue();
    }

    /**
     * Returns the min node from the current node.
     *
     * @param node the node which determines the source
     * @return Min node if present else null
     */
    protected N getMinNode(N node) {
        if (node == null) return null;
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }

    /**
     * Return the maximum value present in the tree.
     *
     * @return the maximum value
     */
    @Override
    public T max() {
        treeIsEmpty();
        return getMaxNode(root).getValue();
    }

    /**
     * Returns the max node from the current node.
     *
     * @param node the node which determines the source
     * @return Max node if present else null
     */
    protected N getMaxNode(N node) {
        if (node == null) return null;
        while (node.getRight() != null) {
            node = node.getRight();
        }
        return node;
    }

    /**
     * Inserts the specified non-null value into this tree.
     *
     * <p>The value is checked with {@link #checkValue(Comparable)} before the recursive
     * insertion starts. This prevents {@code null} from reaching {@link #compare(Comparable, BiNode)},
     * where a null value could not be ordered against existing node values.</p>
     *
     * @param value the value to insert; must not be {@code null}
     * @throws NullPointerException   if {@code value} is {@code null}
     * @throws DuplicateNodeException if {@code value} already exists in this tree
     * @throws ArithmeticException    if the tree has reached {@link Integer#MAX_VALUE} elements
     */
    @Override
    public void insert(T value) {
        checkValue(value);
        root = insert(root, value);
        size = Math.addExact(size, 1);
        modCount++;
    }

    /**
     * Inserts every value provided by the iterable into this tree.
     *
     * <p>The iterable reference itself must be non-null. Each element is then passed
     * through {@link #insert(Comparable)}, so the same non-null element rule applies
     * to every value in the iterable. If a null element appears after earlier values
     * were inserted, those earlier insertions remain in the tree because this method
     * does not perform a pre-validation pass.</p>
     *
     * @param values the values to insert; the iterable and each contained value must
     *               not be {@code null}
     * @throws NullPointerException   if {@code values} is {@code null}, or if any
     *                                element produced by {@code values} is {@code null}
     * @throws DuplicateNodeException if an inserted value already exists in this tree
     * @see #insertAll(Iterable)
     * @see #deleteAll(Iterable)
     * @see #mergeAll(Iterable)
     * @see #retainAll(Iterable)
     * @see #containsAll(Iterable)
     */
    @Override
    public void insertAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);
        for (T value : values) {
            insert(value);
        }
    }

    /**
     * Inserts a value into the subtree rooted at the specified node.
     *
     * @param node  the node of the current subtree
     * @param value the value to insert
     * @return the updated subtree node
     */
    protected N insert(N node, T value) {
        if (node == null) {
            return createNode(value);
        }
        int cmp = compare(value, node);
        if (cmp == 0) {
            throw new DuplicateNodeException("Value already present in tree");
        }

        if (cmp > 0) {
            node.setRight(insert(node.getRight(), value));
        } else {
            node.setLeft(insert(node.getLeft(), value));
        }
        return afterInsert(node);
    }

    /**
     * Update of metadata of respective tree after insert
     *
     * @param node the node associated to tree
     * @return the node as it is.
     */
    protected N afterInsert(N node) {
        return node;
    }

    /**
     * Checks whether this tree contains the specified non-null value.
     *
     * <p>The null check happens before searching because search compares the requested
     * value with node values using {@link Comparable#compareTo(Object)}.</p>
     *
     * @param value the value to search for; must not be {@code null}
     * @return {@code true} if the value exists in this tree; {@code false} otherwise
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @Override
    public boolean contains(T value) {
        checkValue(value);
        return search(value).contains;
    }

    /**
     * Checks whether this tree contains the values provided by the iterable.
     *
     * <p>The null check happens before searching because search compares the requested
     * value with node values using {@link Comparable#compareTo(Object)}.</p>
     *
     * @param values the value to search for; must not be {@code null}
     * @return {@code true} if the all values exists in this tree; {@code false} otherwise
     * @throws NullPointerException if {@code values} is {@code null}, or if any
     *                              element produced by {@code values} is {@code null}
     * @see #insertAll(Iterable)
     * @see #deleteAll(Iterable)
     * @see #mergeAll(Iterable)
     * @see #retainAll(Iterable)
     * @see #containsAll(Iterable)
     */
    @Override
    public boolean containsAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);
        for (T value : values) {
            if (!contains(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Immutable result produced by one binary-search-tree lookup.
     *
     * <p>The lookup tracks three related facts in the same traversal: whether the
     * exact value exists, the greatest candidate less than the requested value, and
     * the smallest candidate greater than the requested value. This lets
     * {@link #contains(Comparable)}, {@link #floor(Comparable)}, and
     * {@link #ceil(Comparable)} reuse the same search result instead of traversing
     * the tree separately for each operation.</p>
     *
     * <p>{@code floor} and {@code ceil} may be {@code null}. A null {@code floor}
     * means no value less than or equal to the requested value exists in the tree.
     * A null {@code ceil} means no value greater than or equal to the requested value
     * exists in the tree. When {@code contains} is {@code true}, both are set to the
     * matching tree value.</p>
     *
     * @param <T> the searched value type
     * @param contains {@code true} when the exact requested value exists in the tree
     * @param floor the greatest value less than or equal to the requested value, or
     *        {@code null} if no such value exists
     * @param ceil the smallest value greater than or equal to the requested value, or
     *        {@code null} if no such value exists
     */
    record SearchResult<T>(boolean contains, T floor, T ceil) {
    }

    /**
     * Executes a top-down lookup for the specified value starting from the root.
     *
     * @param value the value to search for
     * @return a {@link SearchResult} holding whether the exact value was found,
     *         as well as the floor and ceiling candidates
     */
    private @NotNull SearchResult<T> search(T value) {
        return searchHelper(value);
    }

    /**
     * Recursively searches for the specified value while tracking the floor
     * and ceiling candidates.
     *
     * @param value the target value being searched
     * @return the definitive {@link SearchResult}
     */
    private @NotNull SearchResult<T> searchHelper(T value) {
        N node = root;
        T floor = null;
        T ceil = null;
        while (node != null) {
            int cp = value.compareTo(node.getValue());
            if (cp == 0) {
                return new SearchResult<>(true, node.getValue(), node.getValue());
            }
            if (cp > 0) {
                floor = node.getValue();
                node = node.getRight();
            } else {
                ceil = node.getValue();
                node = node.getLeft();
            }
        }
        return new SearchResult<>(false, floor, ceil);
    }

    /**
     * Return the node with the same value
     *
     * @param node  the source of subtree node to search
     * @param value the value of the node to be searched
     * @return node with the same value otherwise null
     * @throws NullPointerException if {@code value} is {@code null} and {@code node}
     *                              is not {@code null}, because the search must compare {@code value}
     *                              with existing node values
     */
    protected N findNode(N node, T value) {
        while (node != null) {
            int cmp = compare(value, node);
            if (cmp == 0) return node;
            node = cmp > 0 ? node.getRight() : node.getLeft();
        }
        return null;
    }

    /**
     * Result of deleting a value from a subtree.
     *
     * @param root the updated subtree root
     * @param deleted {@code true} when the requested value was removed
     * @param <N> the node type
     */
    protected record DeleteResult<N>(N root, boolean deleted) {
    }

    /**
     * Creates a deletion result for use by specialized tree implementations.
     *
     * @param root the updated subtree root
     * @param deleted {@code true} when the requested value was removed
     * @param <M> the specialized node type
     * @return the deletion result
     */
    protected <M> DeleteResult<M> deleteResult(M root, boolean deleted) {
        return new DeleteResult<>(root, deleted);
    }

    /**
     * Deletes the specified non-null value from this tree if it exists.
     *
     * <p>The value is checked before deletion traversal starts. A null value cannot be
     * ordered against node values, so deletion cannot decide whether to move left,
     * move right, or delete the current node.</p>
     *
     * @param value the value to delete; must not be {@code null}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @Override
    public void delete(T value) {
        checkValue(value);
        DeleteResult<N> result = delete(root, value);
        root = result.root();
        if (result.deleted()) {
            size--;
            modCount++;
        }
    }
    /**
     * Deletes every value produced by the supplied iterable from this tree.
     *
     * <p>The iterable reference itself must be non-null. Each element is validated
     * for null before any deletion begins, ensuring that no partial deletions occur
     * if a null element is present. Missing values (not present in the tree) are
     * silently ignored.</p>
     *
     * @param values the values to delete; the iterable and each contained value must
     *               not be {@code null}
     * @throws NullPointerException   if {@code values} is {@code null}, or if any
     *                                element produced by {@code values} is {@code null}
     * @see #insertAll(Iterable)
     * @see #deleteAll(Iterable)
     * @see #mergeAll(Iterable)
     * @see #retainAll(Iterable)
     * @see #containsAll(Iterable)
     */
    @Override
    public void deleteAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);

        List<T> snapshot = new ArrayList<>();
        for (T value : values) {
            Objects.requireNonNull(value, "Value cannot be null");
            snapshot.add(value);
        }
        for (T value : snapshot) {
            delete(value);
        }
    }

    /**
     * Deletes a value from the subtree rooted at the specified node.
     *
     * @param node  the node from where the tree propagates for deletion
     * @param value the value to delete
     * @return the deletion result containing the updated subtree root and whether
     *         the value was removed
     */
    protected DeleteResult<N> delete(N node, T value) {
        if (node == null) return new DeleteResult<>(null, false);

        int compare = compare(value, node);

        if (compare > 0) {
            DeleteResult<N> result = delete(node.getRight(), value);
            if (!result.deleted()) return new DeleteResult<>(node, false);
            node.setRight(result.root());
        } else if (compare < 0) {
            DeleteResult<N> result = delete(node.getLeft(), value);
            if (!result.deleted()) return new DeleteResult<>(node, false);
            node.setLeft(result.root());
        } else {
            if (node.getLeft() == null) return new DeleteResult<>(node.getRight(), true);
            if (node.getRight() == null) return new DeleteResult<>(node.getLeft(), true);

            N successor = getMinNode(node.getRight());
            node.setValue(successor.getValue());
            DeleteResult<N> successorResult = delete(node.getRight(), successor.getValue());
            if (!successorResult.deleted()) {
                throw new IllegalStateException(
                        "BST invariant violated: in-order successor not found in right subtree");
            }
            node.setRight(successorResult.root());
        }
        return new DeleteResult<>(afterDelete(node), true);
    }

    /**
     * Does the update of metadata of respective tree after delete
     *
     * @param node the node associated to tree
     * @return the updated subtree root after deletion metadata has been adjusted
     */
    protected N afterDelete(N node) {
        return node;
    }

    /**
     * Retains only the values in this tree that are contained in the specified iterable.
     *
     * <p>This implementation first materializes the retention candidates into a
     * {@link HashSet} for O(1) lookup, then snapshots the current tree state via
     * {@link #inorder()} before deletion begins. The snapshot is necessary
     * because iterating and structurally modifying the tree simultaneously would
     * trigger {@link java.util.ConcurrentModificationException} from the underlying
     * fail-fast iterators.</p>
     *
     * <p>Values present in the iterable but absent from the tree are silently ignored,
     * consistent with the behavior of {@link #delete(Comparable)}. If a null element
     * appears anywhere in {@code values}, no modifications
     * are made to this tree because full validation occurs and throws {@link NullPointerException}.</p>
     *
     * @implNote If a subclass overrides {@link #delete(Comparable)} and that override
     * throws an exception mid-iteration, the tree will be left in a partially-retained
     * state. Callers that require atomicity must snapshots the tree beforehand.
     *
     * <p><b>Complexity:</b> O(n log n) time, O(n + k) space, where n is the number
     * of elements currently in the tree and k is the number of elements in the
     * iterable.</p>
     *
     * @param values the values to retain; the iterable and each contained value must
     *               not be {@code null}
     * @throws NullPointerException if {@code values} is {@code null}, or if any element
     *                              produced by {@code values} is {@code null}
     * @throws EmptyTreeException   if this tree is empty
     * @see #insertAll(Iterable)
     * @see #deleteAll(Iterable)
     * @see #mergeAll(Iterable)
     * @see #retainAll(Iterable)
     * @see #containsAll(Iterable)
     */
    @Override
    public void retainAll(Iterable<? extends T> values) {
        treeIsEmpty();
        Objects.requireNonNull(values);
        Set<T> retain = new HashSet<>();
        for (T value : values) {
            Objects.requireNonNull(value);
            retain.add(value);
        }
        List<T> snapshot = this.inorder();
        for (T value : snapshot) {
            if (!retain.contains(value)) {
                delete(value);
            }
        }
    }

    /**
     * Merges all values from the specified iterable into this tree,
     * silently ignoring values that already exist.
     *
     * <p>This implementation iterates over {@code values} and attempts
     * {@link #insert(Comparable)} for each element. If the element already
     * exists, the resulting {@link DuplicateNodeException} is caught and
     * suppressed — the value is skipped and iteration continues. This
     * preserves the existing tree structure for duplicate values while
     * inserting genuinely new ones.</p>
     *
     * <p>If a null element appears anywhere in {@code values}, no modifications
     *  are made to this tree because full validation occurs before insertion begins and throws
     *  {@link NullPointerException}</p>
     *
     * <p><b>Complexity:</b> O(k log n) where k is the number of unique elements
     * in the iterable and n is the number of elements in this tree after
     * each insertion.</p>
     *
     * @param values the values to merge; the iterable and each contained
     *               value must not be {@code null}
     * @throws NullPointerException if {@code values} is {@code null}, or
     *                              if any element produced by {@code values}
     *                              is {@code null}
     * @see #insertAll(Iterable)
     * @see #deleteAll(Iterable)
     * @see #mergeAll(Iterable)
     * @see #retainAll(Iterable)
     * @see #containsAll(Iterable)
     */
    @Override
    public void mergeAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);
        Set<T> unique = new HashSet<>();
        for (T value : values) {
            Objects.requireNonNull(value);
            unique.add(value);
        }
        for (T value : unique) {
            if (!contains(value)) {
                insert(value);
            }
        }
    }

    /**
     * Returns the greatest value less than or equal to the specified value.
     *
     * <p>This method first rejects an empty tree, then rejects a null search value.
     * The null check is required because floor calculation performs ordered comparisons
     * while walking down the tree.</p>
     *
     * @param value the value whose floor should be found; must not be {@code null}
     * @return the floor value, or {@code null} when every tree value is greater than
     * {@code value}
     * @throws EmptyTreeException   if this tree is empty
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @Override
    public T floor(T value) {
        treeIsEmpty();
        checkValue(value);
        return search(value).floor;
    }

    /**
     * Returns the smallest value greater than or equal to the specified value.
     *
     * <p>This method first rejects an empty tree, then rejects a null search value.
     * The null check is required because ceiling calculation performs ordered comparisons
     * while walking down the tree.</p>
     *
     * @param value the value whose ceiling should be found; must not be {@code null}
     * @return the ceiling value, or {@code null} when every tree value is less than
     * {@code value}
     * @throws EmptyTreeException   if this tree is empty
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @Override
    public T ceil(T value) {
        treeIsEmpty();
        checkValue(value);
        return search(value).ceil;
    }

    /**
     * Returns the least value strictly greater than the specified value.
     *
     * <p>A null value is rejected before traversal because successor lookup must compare
     * the requested value with node values to decide which branch can contain the next
     * greater value.</p>
     *
     * @param value the value whose successor should be found; must not be {@code null}
     * @return the successor value, or {@code null} if no greater value exists
     * @throws EmptyTreeException   if this tree is empty
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @Override
    public T successor(T value) {
        treeIsEmpty();
        checkValue(value);
        N node = root;
        N successor = null;
        while (node != null) {
            int cmp = compare(value, node);
            if (cmp < 0) {
                successor = node;
                node = node.getLeft();
            } else if (cmp > 0) {
                node = node.getRight();
            } else {
                if (node.getRight() != null)
                    return getMinNode(node.getRight()).getValue();
                break;
            }
        }
        return successor == null ? null : successor.getValue();
    }

    /**
     * Returns the greatest value strictly less than the specified value.
     *
     * <p>A null value is rejected before traversal because predecessor lookup must compare
     * the requested value with node values to decide which branch can contain the previous
     * smaller value.</p>
     *
     * @param value the value whose predecessor should be found; must not be {@code null}
     * @return the predecessor value, or {@code null} if no smaller value exists
     * @throws EmptyTreeException   if this tree is empty
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @Override
    public T predecessor(T value) {
        treeIsEmpty();
        checkValue(value);
        N node = root;
        N predecessor = null;
        while (node != null) {
            int cmp = compare(value, node);
            if (cmp > 0) {
                predecessor = node;
                node = node.getRight();
            } else if (cmp < 0) {
                node = node.getLeft();
            } else {
                if (node.getLeft() != null) predecessor = getMaxNode(node.getLeft());
                break;
            }
        }
        return predecessor == null ? null : predecessor.getValue();
    }

    /**
     * Returns the least common ancestor of two existing non-null values.
     *
     * <p>Both arguments are checked independently. If either argument is {@code null},
     * this method throws before calling {@link #contains(Comparable)} or descending
     * through the tree, because neither operation can compare a null value with node
     * values.</p>
     *
     * @param a the first value; must not be {@code null}
     * @param b the second value; must not be {@code null}
     * @return the least common ancestor value
     * @throws EmptyTreeException    if this tree is empty
     * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
     * @throws NodeNotFoundException if either value does not exist in this tree
     */
    @Override
    public T lca(T a, T b) {
        treeIsEmpty();
        checkValue(a);
        checkValue(b);
        N result = lca(root, a, b);
        if (result == null) {
            throw new NodeNotFoundException("Node not Found");
        }
        return result.getValue();
    }

    /**
     * Finds the lowest common ancestor node for two known values in a given subtree.
     *
     * @param node the root of the subtree to analyze
     * @param a the first value
     * @param b the second value
     * @return the node representing the least common ancestor of {@code a} and {@code b}
     */
    private N lca(N node, T a, T b) {
        if (node == null) return null;
        int cmpA = compare(a, node);
        int cmpB = compare(b, node);
        if (cmpA > 0 && cmpB > 0) return lca(node.getRight(), a, b);
        if (cmpA < 0 && cmpB < 0) return lca(node.getLeft(), a, b);
        boolean foundA = (cmpA == 0) || findNode(cmpA > 0 ? node.getRight() : node.getLeft(), a) != null;
        boolean foundB = (cmpB == 0) || findNode(cmpB > 0 ? node.getRight() : node.getLeft(), b) != null;
        if (!foundA || !foundB) return null;
        return node;
    }

    @Override
    public T kthSmallest(int k) {
        if (k < 1 || k > size) {
            throw new IllegalArgumentException("Out of Bound");
        }
        final int[] count = new int[]{k};
        N result = kthSmallest(root, count);
        return result.getValue();
    }

    /**
     * Recursively computes the k-th smallest node using an inorder traversal.
     *
     * @param node the current subtree root
     * @param count an array of size 1 holding the countdown of elements left to traverse
     * @return the k-th smallest node, or {@code null} if not found in this subtree
     */
    private N kthSmallest(N node, int[] count) {
        if (node == null) return null;
        N left = kthSmallest(node.getLeft(), count);
        if (left != null) return left;
        count[0]--;
        if (count[0] == 0) return node;
        return kthSmallest(node.getRight(), count);
    }


    @Override
    public List<T> inorder() {
        return copyToList(iterator(TraversalType.INORDER));
    }
    @Override
    public List<T> toList(TraversalType type) {
        return copyToList(iterator(type));
    }

    /**
     * Helper function to return as new {@link List} containing the element in the order produced by the supplied iterator.
     * @param iterator the Iterator to copy from.
     * @return new {@link List} containing the element in the iteration order.
     */
    private List<T> copyToList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>(this.size);
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Returns an iterator over the elements in this tree in-order.
     * <p>
     * <strong>Thread Safety Warning:</strong> Callers must synchronize on
     * the tree during iteration to prevent structural interference:
     * <pre>
     * synchronized(tree) {
     * for (T val : tree) { ... }
     * }
     * </pre>
     * Failure to do so when the tree is concurrently modified will result
     * in a {@link java.util.ConcurrentModificationException}.
     */
    @Override
    public @NotNull Iterator<T> iterator() {
        return iterator(TraversalType.INORDER);
    }

    /**
     * Returns an iterator over this tree using the specified traversal order.
     *
     * @param type the traversal order to use; must not be {@code null}
     * @return an iterator for the requested traversal order
     * @throws NullPointerException if {@code type} is {@code null}, because a traversal
     *                              strategy is required to choose the iterator implementation
     */
    @Override
    public Iterator<T> iterator(TraversalType type) {
        if (type == null) throw new NullPointerException("Traversal type cannot be null");

        return switch (type) {
            case PREORDER -> new PreOrderIterator();
            case INORDER -> new InOrderIterator();
            case POSTORDER -> new PostOrderIterator();
            case LEVEL_ORDER -> new LevelOrderIterator();
        };
    }

    /**
     * Returns a sequential stream over this tree using the specified traversal order.
     *
     * @param type the traversal order to use; must not be {@code null}
     * @return a sequential stream over this tree
     * @throws NullPointerException if {@code type} is {@code null}, because the stream's
     *                              spliterator characteristics and backing iterator depend on the traversal type
     */
    @Override
    public Stream<T> stream(TraversalType type) {
        if (type == null) throw new NullPointerException("Traversal type cannot be null");
        return java.util.stream.StreamSupport.stream(
                java.util.Spliterators.spliterator(
                        iterator(type),
                        size(),
                        getSpliteratorCharacteristics(type)
                ),
                false
        );
    }

    /**
     * Overriding Iterable's default spliterator to provide exact structural tracking metadata.
     * Default behavior uses INORDER traversal which maps natively to a sorted sequence.
     */
    @Override
    public Spliterator<T> spliterator() {
        return java.util.Spliterators.spliterator(
                iterator(TraversalType.INORDER),
                size(),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED
        );
    }

    /**
     * Helper to assign semantic characteristics to the Spliterator based on the traversal type.
     */
    private int getSpliteratorCharacteristics(TraversalType type) {
        int flags = Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED;
        if (type == TraversalType.INORDER) {
            flags |= Spliterator.SORTED;
        }
        return flags;
    }

    /**
     * Checks if the tree was structurally modified since the iterator was created.
     *
     * @param expectedModCount the modCount recorded when iterating began
     * @throws ConcurrentModificationException if the tree was modified
     */
    private void concurrentModificationCheck(long expectedModCount) {
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Pre-Order Iterator (Root -> Left -> Right)
     */
    private class PreOrderIterator implements Iterator<T> {
        private final Deque<N> stack = new ArrayDeque<>();
        private final long expectedModCount = modCount;

        PreOrderIterator() {
            if (root != null) stack.push(root);
        }

        @Override
        public boolean hasNext() {
            concurrentModificationCheck(expectedModCount);
            return !stack.isEmpty();
        }

        @Override
        public T next() {
            concurrentModificationCheck(expectedModCount);
            if (!hasNext()) throw new NoSuchElementException();

            N curr = stack.pop();
            if (curr.getRight() != null) stack.push(curr.getRight());
            if (curr.getLeft() != null) stack.push(curr.getLeft());
            return curr.getValue();
        }
    }

    /**
     * In-Order Iterator (Left -> Root -> Right)
     */
    private class InOrderIterator implements Iterator<T> {
        private final Deque<N> stack = new ArrayDeque<>();
        private final long expectedModCount = modCount;
        private N curr = root;

        @Override
        public boolean hasNext() {
            concurrentModificationCheck(expectedModCount);
            return curr != null || !stack.isEmpty();
        }

        @Override
        public T next() {
            concurrentModificationCheck(expectedModCount);
            if (!hasNext()) throw new NoSuchElementException();

            while (curr != null) {
                stack.push(curr);
                curr = curr.getLeft();
            }

            N node = stack.pop();
            T value = node.getValue();
            curr = node.getRight();
            return value;
        }
    }

    /**
     * Post-Order Iterator (Left -> Right -> Root)
     *
     * <p>Uses a lazy single-stack approach with a previous-node pointer to avoid
     * the O(n) eager memory allocation of the two-stack method. Only O(h) stack
     * space is used, where h is the height of the tree.</p>
     */
    private final class PostOrderIterator implements Iterator<T> {
        private final Deque<N> stack = new ArrayDeque<>();
        private final long expectedModCount = modCount;
        private N prev = null;

        PostOrderIterator() {
            pushLeftChain(root);
        }

        private void pushLeftChain(N node) {
            while (node != null) {
                stack.push(node);
                node = node.getLeft();
            }
        }

        @Override
        public boolean hasNext() {
            concurrentModificationCheck(expectedModCount);
            return !stack.isEmpty();
        }

        @Override
        public T next() {
            concurrentModificationCheck(expectedModCount);
            if (!hasNext()) throw new NoSuchElementException();

            while (true) {
                N curr = stack.peek();
                if (curr.getRight() != null && prev != curr.getRight()) {
                    pushLeftChain(curr.getRight());
                } else {
                    stack.pop();
                    prev = curr;
                    return curr.getValue();
                }
            }
        }
    }

    /**
     * Level-Order Iterator (Breadth-First Search)
     */
    private final class LevelOrderIterator implements Iterator<T> {
        private final Queue<N> queue = new ArrayDeque<>();
        private final long expectedModCount = modCount;

        LevelOrderIterator() {
            if (root != null) queue.offer(root);
        }

        @Override
        public boolean hasNext() {
            concurrentModificationCheck(expectedModCount);
            return !queue.isEmpty();
        }

        @Override
        public T next() {
            concurrentModificationCheck(expectedModCount);
            if (!hasNext()) throw new NoSuchElementException();

            N curr = queue.poll();
            if (curr.getLeft() != null) queue.offer(curr.getLeft());
            if (curr.getRight() != null) queue.offer(curr.getRight());
            return curr.getValue();
        }
    }

    private static final String BRANCH = "+-- ";
    private static final String LAST_BRANCH = "\\-- ";
    private static final String VERTICAL = "|   ";
    private static final String SPACE = "    ";

    /**
     * Returns a visual representation of this tree's node hierarchy.
     *
     * <p>Each node is rendered on its own line using {@link #nodeText(BiNode)}.
     * The root is rendered without a branch marker, while Unicode connectors and
     * indentation indicate parent-child relationships for all descendants. Left
     * children are rendered before right children. An empty tree is represented by
     * an empty string.</p>
     *
     * @return the rendered tree hierarchy
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildString(root, "", true, true, sb);
        return sb.toString();
    }

    /**
     * Returns the text used to render the supplied node in {@link #toString()}.
     *
     * @param node the node to render; must not be {@code null}
     * @return the string representation of {@code node}'s value
     * @throws NullPointerException if {@code node} is {@code null}; callers should
     *                              pass only nodes that were checked during tree rendering
     */
    protected String nodeText(N node) {
        return String.valueOf(node.getValue());
    }

    private void buildString(N node, String prefix, boolean isTail, boolean isRoot, StringBuilder sb) {
        if (node == null) {
            return;
        }
        sb.append(prefix);
        if (!isRoot) {
            sb.append(isTail ? LAST_BRANCH : BRANCH);
        }
        sb.append(nodeText(node)).append('\n');

        boolean hasLeft = node.getLeft() != null;
        boolean hasRight = node.getRight() != null;

        if (!hasLeft && !hasRight) {
            return;
        }

        String childPrefix = prefix + (isRoot ? "" : isTail ? SPACE : VERTICAL);

        if (hasLeft && hasRight) {
            buildString(node.getLeft(), childPrefix, false, false, sb);
            buildString(node.getRight(), childPrefix, true, false, sb);

        } else if (hasLeft) {
            buildString(node.getLeft(), childPrefix, true, false, sb);

        } else {
            buildString(node.getRight(), childPrefix, true, false, sb);
        }
    }

}
