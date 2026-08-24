package chaos.tree.core.searchtree.binary;

import chaos.tree.binary.BinaryTree;
import chaos.tree.core.searchtree.PrintStyle;
import chaos.tree.core.searchtree.SearchTree;
import chaos.tree.core.searchtree.binary.node.BiNode;
import chaos.tree.core.searchtree.binary.node.ParentBiNode;
import chaos.tree.traversal.TraversalType;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Foundation abstract base class implementing the core operations of a Binary Search Tree.
 * <p>This base class coordinates the underlying data-invariant tracking and structural
 * mechanics shared among all specialized variations (e.g., standard BST, AVL, RBT, Splay, Treap).
 * It manages fundamental logic flows including recursive searching, tree metadata inquiries,
 * tree-structure string builders, boundaries, and element replacements.</p>
 * <p><b>Null-handling policy:</b> this implementation does not permit {@code null}
 * element values {@code null} is ignored is passed. Binary-search-tree navigation compares every requested value with
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
public abstract class AbstractBiTree<T extends Comparable<? super T>, N extends BiNode<T, N>> implements BinaryTree<T> {


    protected int cachedHashedCode = 0;
    /**
     * Root of the Binary Search tree
     */
    protected N root;

    /**
     * Total element present in this tree
     */
    protected int size;

    /**
     * Stores the current modification of this tree
     */
    protected long modCount = 0;

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
        cachedHashedCode = 0;
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
        if(isEmpty()) return null;
        return getMinNode(root).getValue();
    }

    /**
     * Retrieves and removes the minimum value from this tree.
     *
     * @return the minimum value
     * @throws EmptyTreeException if the tree is empty
     */
    @Override
    public T pollMin() {
        if(isEmpty()) return null;
        T minValue = getMinNode(root).getValue();
        delete(minValue);
        return minValue;
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
        if(isEmpty()) return null;
        return getMaxNode(root).getValue();
    }

    /**
     * Retrieves and removes the maximum value from this tree.
     *
     * @return the maximum value
     * @throws EmptyTreeException if the tree is empty
     */
    @Override
    public T pollMax() {
        if(isEmpty()) return null;
        T maxValue = getMaxNode(root).getValue();
        delete(maxValue);
        return maxValue;
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


    @Override
    public void insertAll(Iterable<? extends T> values) {
        //Iterable must not be null
        Objects.requireNonNull(values);
        for (T value : values) {
            insert(value);
        }
    }

    @Override
    public boolean contains(T value) {
        if(value == null) return false;
        N node = root;
        while (node != null) {
            int cp = value.compareTo(node.getValue());
            if (cp == 0) return true;
            node = cp > 0 ? node.getRight() : node.getLeft();
        }
        return false;
    }

    @Override
    public boolean containsAllElements(Iterable<? extends T> values) {
        //Iterable mut not be null
        Objects.requireNonNull(values, "Values cannot be null");
        for (T value : values) {
            if (!contains(value)) {
                return false;
            }
        }
        return true;
    }

   // It finds the target node with the value otherwise return null.
    protected N findNode(N node, T value) {
        while (node != null) {
            int cmp = compare(value, node);
            if (cmp == 0) return node;
            node = cmp > 0 ? node.getRight() : node.getLeft();
        }
        return null;
    }


    @Override
    public void deleteAll(Iterable<? extends T> values) {
        //Iterable source must not be null
        Objects.requireNonNull(values, "values cannot be null");
        for (T value : values) {
            Objects.requireNonNull(value, "Value cannot be null");
            delete(value);
        }
    }


    @Override
    public void retainAllElements(Iterable<? extends T> values) {
        //Iterable source must not be null
        Objects.requireNonNull(values);
        if (isEmpty()) return;
        Set<T> retain = new HashSet<>();
        for (T value : values) {
            retain.add(value);
        }
        for (T value : inorder()) {
            if (!retain.contains(value)) {
                delete(value);
            }
        }
    }

    @Override
    public void mergeAll(Iterable<? extends T> values) {
        Objects.requireNonNull(values);
        for (T value : values) {
            insert(value);
        }
    }

    @Override
    public T floor(T value) {
        if(isEmpty()) return null;
        if(value == null) return null;
        N node = root;
        T floor = null;
        while (node != null) {
            int cp = value.compareTo(node.getValue());
            if (cp == 0) return node.getValue();
            if (cp > 0) {
                floor = node.getValue();
                node = node.getRight();
            } else {
                node = node.getLeft();
            }
        }
        return floor;
    }

    @Override
    public T ceil(T value) {
        if(isEmpty()) return null;
        if(value == null) return null;
        N node = root;
        T ceil = null;
        while (node != null) {
            int cp = value.compareTo(node.getValue());
            if (cp == 0) return node.getValue();
            if (cp > 0) {
                node = node.getRight();
            } else {
                ceil = node.getValue();
                node = node.getLeft();
            }
        }
        return ceil;
    }

    @Override
    public T successor(T value) {
        if(isEmpty()) return null;
        if(value == null) return null;
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

    //Preferred recursive call stack rather than iterative stack It seems sometime we need to dependent on thread stack too.
    @Override
    public List<T> range(T fromInclusive, T toExclusive) {
        if (fromInclusive == null || toExclusive == null) {
            throw new NullPointerException("Bounds cannot be null");
        }
        if (fromInclusive.compareTo(toExclusive) > 0) {
            throw new IllegalArgumentException("fromInclusive must be <= toExclusive");
        }
        List<T> result = new ArrayList<>();
        rangeHelper(root, fromInclusive, toExclusive, result);
        return result;
    }

    @Override
    public Stream<T> rangeStream(T fromInclusive, T toExclusive) {
        if (fromInclusive == null || toExclusive == null) {
            throw new NullPointerException("Bounds cannot be null");
        }
        if (fromInclusive.compareTo(toExclusive) > 0) {
            throw new IllegalArgumentException("fromInclusive must be <= toExclusive");
        }
        Iterator<T> iterator = new BoundedInOrderIterator(fromInclusive, toExclusive);
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator,
                        Spliterator.ORDERED | Spliterator.SORTED | Spliterator.NONNULL),
                false);
    }

    private void rangeHelper(N node, T from, T to, List<T> result) {
        if (node == null) return;
        int cmpFrom = from.compareTo(node.getValue());
        int cmpTo = to.compareTo(node.getValue());

        if (cmpFrom < 0) {
            rangeHelper(node.getLeft(), from, to, result);
        }
        if (cmpFrom <= 0 && cmpTo > 0) {
            result.add(node.getValue());
        }
        if (cmpTo > 0) {
            rangeHelper(node.getRight(), from, to, result);
        }
    }

    @Override
    public T predecessor(T value) {
        if(isEmpty()) return null;
        if(value == null) return null;
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

    @Override
    public T lca(T a, T b) {
        if (isEmpty()) return null;
        if (a == null || b == null) return null;
        if (!contains(a)) {
            throw new NoSuchElementException("First param does not exist in tree!");
        }
        if (!contains(b)) {
            throw new NoSuchElementException("Second param does not exist in tree!");
        }
        N node = root;
        while (node != null) {
            T val = node.getValue();
            if (a.compareTo(val) < 0 && b.compareTo(val) < 0) {
                node = node.getLeft();
            } else if (a.compareTo(val) > 0 && b.compareTo(val) > 0) {
                node = node.getRight();
            } else {
                return val;
            }
        }
        return null;
    }

    @Override
    public T kthSmallest(int k) {
        if (k <= 0 || k > size) throw new IllegalArgumentException("k=" + k + " is out of bounds [1, " + size + "]");
        Iterator<T> it = iterator();
        for (int i = 1; i < k; i++) {
            it.next();
        }
        return it.next();
    }

    @Override
    public List<T> inorder() {
        return copyToList(iterator());
    }

    @Override
    public List<T> toList() {
        return toList(TraversalType.INORDER);
    }

    @Override
    public List<T> toList(TraversalType type) {
        return copyToList(iterator(type));
    }

    private List<T> copyToList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>(this.size);
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new ReverseInOrderIterator();
    }

    @Override
    public Iterator<T> iterator() {
        return iterator(TraversalType.INORDER);
    }

    @Override
    public Iterator<T> iterator(TraversalType type) {
        if (type == null) throw new NullPointerException("Traversal type cannot be null");

        switch (type) {
            case PREORDER:
                return new PreOrderIterator();
            case INORDER:
                return new InOrderIterator();
            case POSTORDER:
                return new PostOrderIterator();
            case LEVEL_ORDER:
                return new LevelOrderIterator();
            default:
                throw new IllegalArgumentException("Unknown traversal type: " + type);
        }
    }

    /**
     * Returns a sequential stream over this tree using the inorder traversal order.
     *
     * @return a sequential stream over this tree
     * @throws NullPointerException if {@code type} is {@code null}, because the stream's
     * spliterator characteristics and backing iterator depend on the traversal type
     */
    @Override
    public Stream<T> stream() {
        return stream(TraversalType.INORDER);
    }

    /**
     * Returns a sequential stream over this tree using the specified traversal order.
     *
     * @param type the traversal order to use; must not be {@code null}
     * @return a sequential stream over this tree
     * @throws NullPointerException if {@code type} is {@code null}, because the stream's
     * spliterator characteristics and backing iterator depend on the traversal type
     */
    @Override
    public Stream<T> stream(TraversalType type) {
        if (type == null) throw new NullPointerException("Traversal type cannot be null");
        return StreamSupport.stream(Spliterators.spliterator(iterator(type), size(), getSpliteratorCharacteristics(type)), false);
    }

    /**
     * Overriding Iterable's default spliterator to provide exact structural tracking metadata.
     * Default behavior uses INORDER traversal which maps natively to a sorted sequence.
     */
    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(
                iterator(TraversalType.INORDER),
                size(),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED
        );
    }

    private int getSpliteratorCharacteristics(TraversalType type) {
        int flags = Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED;
        if (type == TraversalType.INORDER) {
            flags |= Spliterator.SORTED;
        }
        return flags;
    }

    private void concurrentModificationCheck(long expectedModCount) {
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchTree)) return false;
        SearchTree<?> other = (SearchTree<?>) o;
        if (this.size() != other.size()) return false;
        Iterator<T> it1 = this.iterator();
        Iterator<?> it2 = other.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            T val1 = it1.next();
            Object val2 = it2.next();
            if (!val1.equals(val2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return cachedHashedCode;
    }

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
        buildString(root, "", true, true, sb, style);
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

    private void buildString(N node, String prefix, boolean isTail, boolean isRoot, StringBuilder sb, PrintStyle style) {
        if (node == null) {
            return;
        }

        String branch = (style == PrintStyle.UNICODE) ? "├── " : "+-- ";
        String lastBranch = (style == PrintStyle.UNICODE) ? "└── " : "\\-- ";
        String vertical = (style == PrintStyle.UNICODE) ? "│   " : "|   ";
        String space = "    ";

        sb.append(prefix);
        if (!isRoot) {
            sb.append(isTail ? lastBranch : branch);
        }
        sb.append(nodeText(node)).append('\n');

        boolean hasLeft = node.getLeft() != null;
        boolean hasRight = node.getRight() != null;

        if (!hasLeft && !hasRight) {
            return;
        }

        String childPrefix = prefix + (isRoot ? "" : isTail ? space : vertical);

        if (hasLeft && hasRight) {
            buildString(node.getLeft(), childPrefix, false, false, sb, style);
            buildString(node.getRight(), childPrefix, true, false, sb, style);

        } else if (hasLeft) {
            buildString(node.getLeft(), childPrefix, true, false, sb, style);

        } else {
            buildString(node.getRight(), childPrefix, true, false, sb, style);
        }
    }

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


    private class ReverseInOrderIterator implements Iterator<T> {
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
                curr = curr.getRight();
            }

            N node = stack.pop();
            T value = node.getValue();
            curr = node.getLeft();
            return value;
        }
    }

    private class BoundedInOrderIterator implements Iterator<T> {
        private final Deque<N> stack = new ArrayDeque<>();
        private final long expectedModCount = modCount;
        private final T fromInclusive;
        private final T toExclusive;
        private T nextValue;

        BoundedInOrderIterator(T fromInclusive, T toExclusive) {
            this.fromInclusive = fromInclusive;
            this.toExclusive = toExclusive;
            pushLeftHelper(root);
            advance();
        }

        private void pushLeftHelper(N node) {
            while (node != null) {
                if (compare(fromInclusive, node) <= 0) {
                    stack.push(node);
                    node = node.getLeft();
                } else {
                    node = node.getRight();
                }
            }
        }

        private void advance() {
            if (stack.isEmpty()) {
                nextValue = null;
                return;
            }
            N node = stack.pop();
            nextValue = node.getValue();
            if (nextValue.compareTo(toExclusive) >= 0) {
                nextValue = null;
                stack.clear();
            } else {
                pushLeftHelper(node.getRight());
            }
        }

        @Override
        public boolean hasNext() {
            concurrentModificationCheck(expectedModCount);
            return nextValue != null;
        }

        @Override
        public T next() {
            concurrentModificationCheck(expectedModCount);
            if (!hasNext()) throw new NoSuchElementException();
            T val = nextValue;
            advance();
            return val;
        }
    }

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
}
