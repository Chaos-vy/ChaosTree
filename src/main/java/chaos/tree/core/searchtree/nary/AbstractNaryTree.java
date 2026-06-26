package chaos.tree.core.searchtree.nary;

import chaos.tree.core.searchtree.PrintStyle;
import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.exception.EmptyTreeException;
import chaos.tree.nary.NaryTree;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Foundation abstract base class implementing the core operations of an N-ary Search Tree.
 * <p>This base class coordinates the underlying data-invariant tracking and structural
 * mechanics shared among all high-degree indexing variations (e.g., standard B-Tree, B+Tree).
 * It manages fundamental logic flows including multi-way recursive searching, proactive node
 * splitting, merging, borrowing, and element replacements.</p>
 * <p><b>Null-handling policy:</b> this implementation does not permit {@code null}
 * element values. N-ary search tree navigation compares every requested value with
 * existing node keys using {@link Comparable#compareTo(Object)}; allowing
 * {@code null} would make those comparisons undefined and would also break the
 * sorted-order contract of the arrays. Public value-based operations therefore fail
 * fast with {@link NullPointerException} before traversal begins.</p>
 * <p><b>Thread Safety:</b> this implementation is not synchronized. If multiple threads access
 * an N-ary tree concurrently, and at least one of the threads modifies the tree structurally,
 * it must be synchronized externally.</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @param <N> the specific {@link NaryNode} implementation used by the concrete tree
 * @see NaryNode
 * @since 1.0.0
 */
public abstract class AbstractNaryTree<T extends Comparable<T>, N extends NaryNode<T, N>> implements NaryTree<T> {

    protected final int degree;
    protected final int minKeys;
    protected final int maxKeys;
    protected N root;
    protected int size;
    protected long modCount = 0;

    protected AbstractNaryTree(int degree) {
        if (degree < 2) {
            throw new IllegalArgumentException("Degree must be at least 2");
        }
        this.degree = degree;
        this.maxKeys = 2 * degree - 1;
        this.minKeys = degree - 1;
        this.root = null;
        this.size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        this.size = 0;
        this.root = null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public final int maxDegree() {
        return maxKeys + 1;
    }

    @Override
    public final int minDegree() {
        return minKeys + 1;
    }

    /**
     * Creates a new node of the specific N-ary tree type.
     *
     * @param degree the degree of the node
     * @param isLeaf whether the node is a leaf
     * @return the newly created node
     */
    protected abstract N createNode(int degree, boolean isLeaf);


    protected record NodeSearchResult(boolean found, int index) {}

    protected NodeSearchResult searchNode(N node, T key) {
        int left = 0;
        int right = node.getKeyCount() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = key.compareTo(node.getKey(mid));
            if (cmp == 0) return new NodeSearchResult(true, mid);
            if (cmp > 0) left = mid + 1;
            else right = mid - 1;
        }
        return new NodeSearchResult(false, left);
    }

    protected int findIndex(N node, T key) {
        return searchNode(node, key).index();
    }

    protected boolean binarySearch(N node, T key) {
        return searchNode(node, key).found();
    }

    protected void checkValue(T value) {
        Objects.requireNonNull(value);
    }

    protected void treeIsEmpty() {
        if (isEmpty()) {
            throw new EmptyTreeException("Tree is empty");
        }
    }



    /**
     * Inserts the specified value into the tree, proactively splitting full nodes along the descent.
     * <p><b>Complexity:</b> O(log_t(N)) time for tree traversal, O(1) space since insertion is iterative.
     * Array shifting inside the leaf node takes O(t) time.</p>
     *
     * @param value the value to insert
     * @throws DuplicateNodeException if the value already exists
     * @throws NullPointerException if the value is null
     */
    @Override
    public void insert(T value) {
        checkValue(value);
        if (root == null) {
            root = createNode(degree, true);
            root.setKey(0, value);
            root.setKeyCount(1);
            size++;
            modCount++;
            return;
        }

        if (root.getKeyCount() == maxKeys) {
            N newRoot = createNode(degree, false);
            newRoot.setChild(0, root);
            splitChild(newRoot, 0, root);
            root = newRoot;
            insertNonFull(this.root, value);
        }
        else insertNonFull(this.root, value);
    }

    @Override
    public void insertAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);
        for(T item: values){
            insert(item);
        }
    }

    /**
     * Inserts a value into a node that is known to be non-full.
     *
     * @param node the node to insert into
     * @param value the value to insert
     */
    protected void insertNonFull(N node, T value) {
        while (true) {
            int index = findIndex(node, value);

            if (index < node.getKeyCount() && value.compareTo(node.getKey(index)) == 0) {
                throw new DuplicateNodeException("Value already present in tree");
            }
            if (node.isLeaf()) {
                Object[] keys = node.getKeys();
                System.arraycopy(keys, index, keys, index + 1, node.getKeyCount() - index);
                node.setKey(index, value);
                node.setKeyCount(node.getKeyCount() + 1);
                size++;
                modCount++;
                return;
            } else {
                N child = node.getChild(index);
                if (child.getKeyCount() == maxKeys) {
                    splitChild(node, index, child);
                    int cmp = value.compareTo(node.getKey(index));
                    if (cmp == 0) throw new DuplicateNodeException("Value already exists in the tree: " + value);
                    else if (cmp > 0) child = node.getChild(index + 1);
                }
                node = child;
            }
        }
    }

    /**
     * Splits a full child node into two nodes, elevating the median key to the parent.
     *
     * @param parent the parent node receiving the elevated key
     * @param index  the index of the child array where the full node resides
     * @param child  the full child node being split
     */
    protected void splitChild(N parent, int index, N child) {
        N rightChild = createNode(degree, child.isLeaf());
        rightChild.setKeyCount(minKeys);

        System.arraycopy(child.getKeys(), degree, rightChild.getKeys(), 0, minKeys);

        if (!child.isLeaf()) {
            System.arraycopy(child.getChildren(), degree, rightChild.getChildren(), 0, degree);
        }
        child.setKeyCount(minKeys);

        System.arraycopy(parent.getChildren(), index + 1, parent.getChildren(), index + 2, parent.getKeyCount() - index);

        parent.setChild(index + 1, rightChild);
        System.arraycopy(parent.getKeys(), index, parent.getKeys(), index + 1, parent.getKeyCount() - index);

        parent.setKey(index, child.getKey(minKeys));
        for (int i = minKeys; i < maxKeys; i++) {
            child.setKey(i, null);
        }
        if (!child.isLeaf()) {
            for (int i = degree; i <= maxKeys; i++) {
                child.setChild(i, null);
            }
        }

        parent.setKeyCount(parent.getKeyCount() + 1);
    }


    /**
     * Result of deleting a value from a subtree.
     *
     * @param deleted {@code true} when the requested value was removed
     */
    public record DeleteResult(boolean deleted) {}

    /**
     * Removes the specified value from the tree.
     * <p>If the value exists in an internal node, it is swapped with its predecessor/successor.
     * During descent, nodes with the minimum allowed keys are proactively filled via merging or borrowing
     * to guarantee a single-pass deletion without backtracking.</p>
     * <p><b>Complexity:</b> O(log_t(N)) time for traversal, O(log_t(N)) space due to the call stack of the recursive descent. Borrowing/Merging takes O(t) time.</p>
     *
     * @param value the value to remove
     */
    @Override
    public void delete(T value) {
        boolean deleted = delete(root, value).deleted;
        if (root != null && root.getKeyCount() == 0) {
            if (root.isLeaf()) {
                root = null;
            } else {
                root = root.getChild(0);
            }
        }

        if (deleted) {
            size--;
            modCount++;
        }
    }

    @Override
    public void deleteAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);
        for(T item: values){
            delete(item);
        }
    }

    /**
     * Deletes the specified key from the subtree rooted at the given node.
     *
     * @param node the root of the subtree to delete from
     * @param key the key to delete
     * @return a DeleteResult indicating whether the deletion was successful
     */
    protected DeleteResult delete(N node, T key) {
        if (node == null) return new DeleteResult(false);

        int idx = findIndex(node, key);
        if (idx < node.getKeyCount() && key.compareTo(node.getKey(idx)) == 0) {
            if (node.isLeaf()) removeFromLeaf(node, idx);
            else removeFromNonLeaf(node, idx);
            return new DeleteResult(true);
        } else {
            if (node.isLeaf()) return new DeleteResult(false);

            boolean isLastChild = (idx == node.getKeyCount());
            N x = node.getChild(idx);
            if (x.getKeyCount() == minKeys) fill(node, idx);

            if (isLastChild && idx > node.getKeyCount()) {
                return delete(node.getChild(idx - 1), key);
            } else {
                return delete(node.getChild(idx), key);
            }
        }
    }

    private void removeFromLeaf(N node, int index) {
        System.arraycopy(node.getKeys(), index + 1, node.getKeys(), index, node.getKeyCount() - 1 - index);
        node.setKey(node.getKeyCount() - 1, null);
        node.setKeyCount(node.getKeyCount() - 1);
    }

    private void removeFromNonLeaf(N node, int index) {
        T key = node.getKey(index);
        N leftChild = node.getChild(index);
        N rightChild = node.getChild(index + 1);

        if (leftChild == null || rightChild == null) {
            return;
        }
        if (leftChild.getKeyCount() >= degree) {
            T pred = getPredecessor(leftChild);
            node.setKey(index, pred);
            delete(leftChild, pred);
        } else if (rightChild.getKeyCount() >= degree) {
            T succ = getSuccessor(rightChild);
            node.setKey(index, succ);
            delete(rightChild, succ);
        } else {
            mergeChildren(node, index);
            delete(leftChild, key);
        }
    }

    private void mergeChildren(N parent, int index) {
        N child = parent.getChild(index);
        N sibling = parent.getChild(index+1);

        child.setKey(minKeys, parent.getKey(index));
        System.arraycopy(sibling.getKeys(), 0, child.getKeys(), degree, minKeys);


        if (!child.isLeaf()) {
            System.arraycopy(sibling.getChildren(), 0, child.getChildren(), minKeys + 1, minKeys + 1);
        }
        System.arraycopy(parent.getKeys(),index+1,parent.getKeys(),index,parent.getKeyCount()-index-1);

        System.arraycopy(parent.getChildren(), index + 2, parent.getChildren(), index + 1, parent.getKeyCount() - 1 - index);
        parent.setKey(parent.getKeyCount() - 1, null);
        parent.setChild(parent.getKeyCount(), null);

        child.setKeyCount(maxKeys);
        parent.setKeyCount(parent.getKeyCount() - 1);
    }

    private void fill(N node, int index) {
        if (index != 0 && node.getChild(index - 1) != null && node.getChild(index - 1).getKeyCount() >= degree) {
            borrowFromPrevious(node, index);
        }
        else if (index < node.getKeyCount() && node.getChild(index + 1) != null && node.getChild(index + 1).getKeyCount() >= degree) {
            borrowFromNext(node, index);
        }
        else {
            if (index != node.getKeyCount()) {
                mergeChildren(node, index);
            } else {
                mergeChildren(node, index - 1);
            }
        }
    }

    private void borrowFromNext(N parent, int index) {
        N child = parent.getChild(index);
        N next = parent.getChild(index +1);

        child.setKey(child.getKeyCount(), parent.getKey(index));

        if(!child.isLeaf()){
            child.setChild(child.getKeyCount() + 1, next.getChild(0));
            System.arraycopy(next.getChildren(), 1, next.getChildren(), 0, next.getKeyCount());
            next.setChild(next.getKeyCount(), null);
        }
        parent.setKey(index, next.getKey(0));

        System.arraycopy(next.getKeys(), 1, next.getKeys(), 0, next.getKeyCount() - 1);
        next.setKey(next.getKeyCount() - 1, null);

        child.setKeyCount(child.getKeyCount() + 1);
        next.setKeyCount(next.getKeyCount() - 1);
    }

    private void borrowFromPrevious(N parent, int index) {
        N child = parent.getChild(index);
        N previous = parent.getChild(index -1);

        System.arraycopy(child.getKeys(), 0, child.getKeys(), 1, child.getKeyCount());

        if(!child.isLeaf()){
            System.arraycopy(child.getChildren(), 0, child.getChildren(), 1, child.getKeyCount()+1);
        }
        child.setKey(0, parent.getKey(index-1));

        if (!child.isLeaf()) {
            child.setChild(0, previous.getChild(previous.getKeyCount()));
            previous.setChild(previous.getKeyCount(), null);
        }
        parent.setKey(index - 1, previous.getKey(previous.getKeyCount() - 1));
        previous.setKey(previous.getKeyCount() - 1, null);

        child.setKeyCount(child.getKeyCount() + 1);
        previous.setKeyCount(previous.getKeyCount() - 1);
    }

    private T getPredecessor(N node) {
        while (!node.isLeaf()) {
            node = node.getChild(node.getKeyCount());
        }
        return node.getKey(node.getKeyCount() - 1);
    }

    private T getSuccessor(N node) {
        while (!node.isLeaf()) {
            node = node.getChild(0);
        }
        return node.getKey(0);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new InorderIterator();
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(
            Spliterators.spliterator(iterator(), size, Spliterator.ORDERED | Spliterator.SORTED | Spliterator.NONNULL),
            false
        );
    }

    /**
    * O(log N) auxiliary-space inorder iterator backed by an explicit stack.
    * Suitable for B-Trees and other search trees where values are stored in every node.
    */
    private class InorderIterator implements Iterator<T> {

        private class NodeTracker {
            final N node;
            int index;
            NodeTracker(N node, int index) {
                this.node = node;
                this.index = index;
            }
        }

        private final Deque<NodeTracker> stack = new ArrayDeque<>();
        private final long expectedModCount = modCount;

        public InorderIterator() {
            if (root != null) pushLeft(root);
        }

        private void pushLeft(N node) {
            while (!node.isLeaf()) {
                stack.push(new NodeTracker(node, 0));
                node = node.getChild(0);
            }
            stack.push(new NodeTracker(node, 0));
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public T next() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (!hasNext()) throw new NoSuchElementException();

            NodeTracker top = stack.peek();
            N node = top.node;
            int index = top.index;

            T value = node.getKey(index);

            top.index++;
            if (top.index >= node.getKeyCount()) {
                stack.pop();
            }
            if (!node.isLeaf()) {
                pushLeft(node.getChild(index + 1));
            }

            return value;
        }
    }

    @Override
    public List<T> range(T fromInclusive, T toExclusive) {
        if (fromInclusive == null || toExclusive == null) throw new NullPointerException("Bounds cannot be null");
        if (fromInclusive.compareTo(toExclusive) > 0) throw new IllegalArgumentException("fromInclusive > toExclusive");

        List<T> result = new ArrayList<>();
        if (root != null) {
            collectRangeBounded(root, fromInclusive, toExclusive, result);
        }
        return result;
    }

    private void collectRangeBounded(N node, T min, T max, List<T> result) {
        if (node == null) return;
        int i = 0;
        int count = node.getKeyCount();
        while (i < count && node.getKey(i).compareTo(min) < 0) {
            i++;
        }

        if (!node.isLeaf()) {
            collectRangeBounded(node.getChild(i), min, max, result);
        }

        while (i < count) {
            T key = node.getKey(i);

            if (key.compareTo(max) >= 0) {
                return;
            }

            result.add(key);

            if (!node.isLeaf()) {
                collectRangeBounded(node.getChild(i + 1), min, max, result);
            }
            i++;
        }
    }

    @Override
    public Stream<T> rangeStream(T fromInclusive, T toExclusive) {
        if (fromInclusive == null || toExclusive == null) throw new NullPointerException("Bounds cannot be null");
        if (fromInclusive.compareTo(toExclusive) > 0) throw new IllegalArgumentException("fromInclusive > toExclusive");

        Iterator<T> iterator = new BoundedInorderIterator(fromInclusive, toExclusive);
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.SORTED | Spliterator.NONNULL),
            false
        );
    }

    private class BoundedInorderIterator implements Iterator<T> {
        private class NodeTracker {
            final N node; int index;
            NodeTracker(N node, int index) { this.node = node; this.index = index; }
        }

        private final Deque<NodeTracker> stack = new ArrayDeque<>();
        private final long expectedModCount = modCount;
        private final T min;
        private final T max;
        private T nextValue = null;

        public BoundedInorderIterator(T min, T max) {
            this.min = min;
            this.max = max;
            if (root != null) pushLeftBounded(root);
            advance();
        }

        private void pushLeftBounded(N node) {
            while (node != null) {
                int i = 0;
                while (i < node.getKeyCount() && node.getKey(i).compareTo(min) < 0) {
                    i++;
                }
                stack.push(new NodeTracker(node, i));
                if (node.isLeaf()) break;
                node = node.getChild(i);
            }
        }

        private void pushLeftmost(N node) {
            while (node != null) {
                stack.push(new NodeTracker(node, 0));
                if (node.isLeaf()) break;
                node = node.getChild(0);
            }
        }

        private void advance() {
            nextValue = null;
            while (!stack.isEmpty()) {
                NodeTracker top = stack.peek();
                N node = top.node;
                int i = top.index;

                if (i < node.getKeyCount()) {
                    T key = node.getKey(i);
                    top.index++;

                    if (!node.isLeaf()) pushLeftmost(node.getChild(i + 1));

                    if (key.compareTo(max) >= 0) {
                        stack.clear();
                        return;
                    }

                    nextValue = key;
                    return;
                } else {
                    stack.pop();
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            return nextValue != null;
        }

        @Override
        public T next() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (!hasNext()) throw new NoSuchElementException();
            T val = nextValue;
            advance();
            return val;
        }
    }

    @Override
    public List<T> toList() {
        List<T> list = new ArrayList<>(size);
        for (T item : this) {
            list.add(item);
        }
        return list;
    }
    /**
     * Returns {@code true} if the tree contains the specified value.
     * <p><b>Complexity:</b> O(log_t(N) * log_2(t)) time, O(1) space. Tree height is log_t(N),
     * and binary searching within each node takes log_2(t).</p>
     *
     * @param key the value to search for
     * @return {@code true} if found, {@code false} otherwise
     */
    @Override
    public boolean contains(T key) {
        checkValue(key);
        if (root == null) return false;

        N current = root;
        while (current != null) {
            NodeSearchResult result = searchNode(current, key);

            if (result.found()) {
                return true;
            }

            if (current.isLeaf()) {
                break;
            }
            current = current.getChild(result.index());
        }
        return false;
    }

    @Override
    public boolean containsAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);
        for(T item: values){
            if(!contains(item))return false;
        }
        return true;
    }

    /**
     * Returns the minimum value in the tree by traversing the leftmost child pointers.
     * <p><b>Complexity:</b> O(log_t(N)) time, O(1) space.</p>
     *
     * @return the minimum value
     * @throws EmptyTreeException if the tree is empty
     */
    @Override
    public T min() {
        treeIsEmpty();
        N node = root;
        while (!node.isLeaf()) {
            node = node.getChild(0);
        }
        return node.getKey(0);
    }

    /**
     * Returns the maximum value in the tree by traversing the rightmost child pointers.
     * <p><b>Complexity:</b> O(log_t(N)) time, O(1) space.</p>
     *
     * @return the maximum value
     * @throws EmptyTreeException if the tree is empty
     */
    @Override
    public T max() {
        treeIsEmpty();
        N node = root;
        while (!node.isLeaf()) {
            node = node.getChild(node.getKeyCount());
        }
        return node.getKey(node.getKeyCount() - 1);
    }

    @Override
    public T pollMin() {
        treeIsEmpty();
        T minVal = min();
        delete(minVal);
        return minVal;
    }

    @Override
    public T pollMax() {
        treeIsEmpty();
        T maxVal = max();
        delete(maxVal);
        return maxVal;
    }


    /**
     * Returns the greatest value in the tree less than or equal to the given key.
     * <p><b>Complexity:</b> O(log_t(N) * log_2(t)) time, O(1) space.</p>
     *
     * @param key the target key
     * @return the floor value, or {@code null} if no such value exists
     */
    @Override
    public T floor(T key) {
        treeIsEmpty();
        checkValue(key);
        N current = root;
        T floorVal = null;

        while (current != null) {
            NodeSearchResult result = searchNode(current, key);
            if (result.found()) return current.getKey(result.index());

            int left = result.index();
            if (left > 0) floorVal = current.getKey(left - 1);

            if (current.isLeaf()) break;
            current = current.getChild(left);
        }
        return floorVal;
    }

    /**
     * Returns the least value in the tree greater than or equal to the given key.
     * <p><b>Complexity:</b> O(log_t(N) * log_2(t)) time, O(1) space.</p>
     *
     * @param key the target key
     * @return the ceiling value, or {@code null} if no such value exists
     */
    @Override
    public T ceil(T key) {
        treeIsEmpty();
        checkValue(key);
        N current = root;
        T ceilVal = null;

        while (current != null) {
            NodeSearchResult result = searchNode(current, key);
            if (result.found()) return current.getKey(result.index());

            int left = result.index();
            if (left < current.getKeyCount()) ceilVal = current.getKey(left);

            if (current.isLeaf()) break;
            current = current.getChild(left);
        }
        return ceilVal;
    }

    @Override
    public T predecessor(T key) {
        treeIsEmpty();
        checkValue(key);
        N current = root;
        T predVal = null;

        while (current != null) {
            NodeSearchResult result = searchNode(current, key);
            int left = result.index();

            if (left > 0) predVal = current.getKey(left - 1);

            if (current.isLeaf()) break;
            current = current.getChild(left);
        }
        return predVal;
    }

    @Override
    public T successor(T key) {
        treeIsEmpty();
        checkValue(key);
        N current = root;
        T succVal = null;

        while (current != null) {
            NodeSearchResult result = searchNode(current, key);
            int left = result.index();

            if (result.found()) left++;

            if (left < current.getKeyCount()) succVal = current.getKey(left);

            if (current.isLeaf()) break;
            current = current.getChild(left);
        }
        return succVal;
    }

    /**
     * Returns the k-th smallest element in the tree (1-indexed).
     * <p><b>Complexity:</b> O(k) time since it relies on the inorder iterator, O(log_t(N)) space for the iterator's explicit stack.</p>
     *
     * @param k the 1-based index
     * @return the k-th smallest value
     * @throws IllegalArgumentException if {@code k} is out of bounds
     */
    @Override
    public T kthSmallest(int k) {
        if (k <= 0 || k > size) throw new IllegalArgumentException("k=" + k + " is out of bounds [1, " + size + "]");
        Iterator<T> it = iterator();
        T result = null;
        for (int i = 0; i < k; i++) {
            result = it.next();
        }
        return result;
    }

    @Override
    public int height() {
        if (root == null) return 0;
        int h = 0;
        N node = root;
        while (!node.isLeaf()) {
            node = node.getChild(0);
            h++;
        }
        return h;
    }
    @Override
    public void retainAll(Iterable<? extends T> values) {
        if (isEmpty()) return;
        Objects.requireNonNull(values);

        Set<T> retainSet = new HashSet<>();
        for (T val : values) {
            Objects.requireNonNull(val);
            retainSet.add(val);
        }

        List<T> toRemove = new ArrayList<>();
        for (T current : this) {
            if (!retainSet.contains(current)) {
                toRemove.add(current);
            }
        }
        for (T val : toRemove) {
            delete(val);
        }
    }

    @Override
    public void mergeAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);
        Set<T> uniqueKeys = new HashSet<>();
        for (T value : values) {
            Objects.requireNonNull(value);
            uniqueKeys.add(value);
        }
        for (T value : uniqueKeys) {
            if (!contains(value)) {
                insert(value);
            }
        }
    }
    /**
     * Returns a visual string representation of the tree's hierarchical structure.
     * <p>This visually represents both the standard {@code BTree} and the {@code BPlusTree}
     * topology. When called on a {@code BPlusTree}, the output explicitly renders the internal
     * "Ghost Routing" nodes alongside the leaf-level linked data blocks.</p>
     *
     * @return a multi-line formatted string detailing the exact tree topology
     */
     @Override
    public String toString() {
        return toString(PrintStyle.ASCII);
    }

    @Override
    public String toString(PrintStyle style) {
        if (root == null) {
            return "Tree is empty.";
        }
        StringBuilder sb = new StringBuilder();
        buildString(sb, root, "", true, style);
        return sb.toString();
    }

    private void buildString(StringBuilder sb, N node, String prefix, boolean isTail, PrintStyle style) {
        String lastBranch = (style == PrintStyle.UNICODE) ? "└── " : "\\-- ";
        String crossBranch = (style == PrintStyle.UNICODE) ? "├── " : "+-- ";
        String vertical = (style == PrintStyle.UNICODE) ? "│   " : "|   ";

        sb.append(prefix).append(isTail ? lastBranch : crossBranch);

        sb.append("[");
        for (int i = 0; i < node.getKeyCount(); i++) {
            sb.append(node.getKey(i));
            if (i < node.getKeyCount() - 1) sb.append(", ");
        }
        sb.append("]\n");

        if (!node.isLeaf()) {
            int numChildren = node.getKeyCount() + 1;
            for (int i = 0; i < numChildren; i++) {
                N child = node.getChild(i);
                if (child != null) {
                    boolean lastChild = (i == numChildren - 1);
                    buildString(sb, child, prefix + (isTail ? "    " : vertical), lastChild, style);
                }
            }
        }
    }

}