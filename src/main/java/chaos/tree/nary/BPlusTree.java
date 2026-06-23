package chaos.tree.nary;

import chaos.tree.core.searchtree.nary.AbstractNaryTree;
import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.exception.EmptyTreeException;
import chaos.tree.nary.node.BPlusTreeNode;
import org.jetbrains.annotations.NotNull;


import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Self-balancing N-ary Search Tree implementation utilizing the B+ Tree invariant with Ghost Routing.
 *
 * <p>A B+ Tree is an advanced evolution of the standard B-Tree where all actual data elements are
 * strictly stored at the leaf level, forming a sequential linked list. Internal nodes
 * serve exclusively as "Ghost Routing" indexes, holding boundary copies of keys to direct traversal.</p>
 *
 * <p>By maintaining these invariants, the tree not only guarantees <b>O(log_t(N))</b> search, insertion,
 * and deletion times, but also provides massive <b>O(1)</b> sequential range traversals through its leaf-level
 * linked list. This architecture makes it the absolute industry standard for database indexing,
 * file systems, and heavy range-query workloads.</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @see BPlusTreeNode
 * @since 1.0.0
 */
public class BPlusTree<T extends Comparable<T>> extends AbstractNaryTree<T, BPlusTreeNode<T>> implements NaryTree<T> {

    private BPlusTreeNode<T> head;

    /**
     * Constructs an empty B+ Tree with the specified maximum degree.
     *
     * @param degree the maximum number of children an internal node can have (must be &ge; 2)
     * @throws IllegalArgumentException if the degree is less than 2
     */
    public BPlusTree(int degree) {
        super(degree);
        this.head = null;
    }
    /**
     * Constructs a B+ Tree with the specified degree and populates it with elements
     * from the provided iterable collection.
     * <p>If the provided collection is another {@code BPlusTree} instance with the exact
     * same degree, this constructor executes an optimized <b>O(N)</b> structural deep clone
     * of the source tree (including recreating the leaf-level linked list),
     * entirely bypassing sequential re-insertion logic. Otherwise, elements are
     * inserted sequentially yielding <b>O(N log_t(N))</b> time complexity.</p>
     *
     * @param degree     the maximum number of children an internal node can have (must be &ge; 2)
     * @param collection the collection whose elements are to be placed into this tree
     * @throws IllegalArgumentException if the degree is less than 2
     * @throws NullPointerException     if the collection or any of its elements are {@code null}
     */
     @SuppressWarnings("unchecked")
    public BPlusTree(int degree, Iterable<? extends T> collection) {
        super(degree);
        this.head = null;
        if (collection == null) return;

        if (collection instanceof BPlusTree) {
            BPlusTree<T> other = (BPlusTree<T>) collection;
            if (this.degree == other.degree) {
                if (other.root != null) {
                    BPlusTreeNode<T>[] tracker = new BPlusTreeNode[1];
                    this.root = deepCloneNode(other.root, tracker);
                    this.size = other.size;
                }
                return;
            }
        }
        for (T item : collection) {
            this.insert(item);
        }
    }


    /**
     * Constructs a physical deep clone of the provided B+ Tree, dynamically rebuilding
     * both the tree hierarchy and the leaf-level sequential linked list.
     * <p><b>Complexity:</b> O(N) time to physically copy the nodes, and O(log_t(N)) auxiliary space for the recursive call stack.</p>
     *
     * @param other the tree to clone
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public BPlusTree(BPlusTree<T> other) {
        super(other.degree);
        this.head = null;
        if (other.root != null) {
            @SuppressWarnings("unchecked")
            BPlusTreeNode<T>[] tracker = new BPlusTreeNode[1];
            this.root = deepCloneNode(other.root, tracker);
            this.size = other.size;
        }
    }

    /**
     * Recursively performs a physical deep clone of a node and its entire sub-hierarchy,
     * while dynamically reconstructing the leaf-level sequential linked list.
     * <p><b>Complexity:</b> O(N) time when N is the number of nodes in the subtree, O(log_t(N)) space for the recursive call stack.</p>
     *
     * @param original    the original node to clone
     * @param leafTracker a single-element array used to track and link the previously cloned leaf node
     * @return a completely unlinked, physically distinct clone of the node hierarchy
     */
    private BPlusTreeNode<T> deepCloneNode(BPlusTreeNode<T> original, BPlusTreeNode<T>[] leafTracker) {
        BPlusTreeNode<T> clone = createNode(this.degree, original.isLeaf());
        clone.setKeyCount(original.getKeyCount());
        System.arraycopy(original.getKeys(), 0, clone.getKeys(), 0, original.getKeyCount());

        if (original.isLeaf()) {
            if (this.head == null) {
                this.head = clone;
            }
            if (leafTracker[0] != null) {
                leafTracker[0].setNext(clone);
            }
            leafTracker[0] = clone;
        } else {
            for (int i = 0; i <= original.getKeyCount(); i++) {
                BPlusTreeNode<T> childToClone = original.getChild(i);
                if (childToClone != null) {
                    clone.setChild(i, deepCloneNode(childToClone, leafTracker));
                }
            }
        }

        return clone;
    }

    @Override
    public void clear() {
        super.clear();
        head = null;
    }

    @Override
    protected BPlusTreeNode<T> createNode(int degree, boolean isLeaf) {
        return new BPlusTreeNode<>(degree, isLeaf);
    }

    private int routeIndex(BPlusTreeNode<T> valueNode, T key) {
        NodeSearchResult result = searchNode(valueNode, key);
        return result.found() ? result.index() + 1 : result.index();
    }


    @Override
    public boolean contains(T key) {
        if (root == null) return false;

        BPlusTreeNode<T> current = root;

        while (!current.isLeaf()) {

            current = current.getChild(routeIndex(current, key));
        }
        return binarySearch(current, key);
    }
    @Override
    public void insert(T value) {
        boolean wasEmpty = isEmpty();
        super.insert(value);
        if (wasEmpty) {
            this.head = root;
        }
    }

    @Override
    protected void insertNonFull(BPlusTreeNode<T> node, T value) {
        while (true) {
            if (node.isLeaf()) {
                NodeSearchResult result = searchNode(node, value);
                if (result.found()) throw new DuplicateNodeException("Duplicate value: " + value);

                int insertPos = result.index();
                System.arraycopy(node.getKeys(), insertPos, node.getKeys(), insertPos + 1, node.getKeyCount() - insertPos);
                node.setKey(insertPos, value);
                node.setKeyCount(node.getKeyCount() + 1);
                size++;
                modCount++;
                return;
            } else {
                int idx = routeIndex(node, value);
                BPlusTreeNode<T> child = node.getChild(idx);

                if (child.getKeyCount() == maxKeys) {
                    splitChild(node, idx, child);
                    if (value.compareTo(node.getKey(idx)) >= 0) {
                        idx++;
                        child = node.getChild(idx);
                    }
                }
                node = child;
            }
        }
    }

    @Override
    protected void splitChild(BPlusTreeNode<T> parent, int index, BPlusTreeNode<T> child) {
        BPlusTreeNode<T> rightChild = createNode(degree, child.isLeaf());

        if (child.isLeaf()) {
            rightChild.setKeyCount(degree);
            System.arraycopy(child.getKeys(), minKeys, rightChild.getKeys(), 0, degree);
            child.setKeyCount(minKeys);

            rightChild.setNext(child.getNext());
            child.setNext(rightChild);

            if (this.head == null) this.head = child;

            System.arraycopy(parent.getChildren(), index + 1, parent.getChildren(), index + 2, parent.getKeyCount() - index);
            parent.setChild(index + 1, rightChild);

            System.arraycopy(parent.getKeys(), index, parent.getKeys(), index + 1, parent.getKeyCount() - index);
            parent.setKey(index, rightChild.getKey(0));
            parent.setKeyCount(parent.getKeyCount() + 1);

            for(int i = minKeys; i < maxKeys; i++) child.setKey(i, null);

        } else {
            super.splitChild(parent, index, child);
        }
    }

    @Override
    public void delete(T value) {
        super.delete(value);
        if(this.root == null){
            head = null;
        }
    }

    @Override
    protected DeleteResult delete(BPlusTreeNode<T> node, T key) {
        if (node == null) return new DeleteResult(false);

        if (node.isLeaf()) {
            int left = 0;
            int right = node.getKeyCount() - 1;
            int foundIdx = -1;

            while (left <= right) {
                int mid = left + (right - left) / 2;
                int cmp = key.compareTo(node.getKey(mid));
                if (cmp == 0) {
                    foundIdx = mid;
                    break;
                } else if (cmp > 0) left = mid + 1;
                else right = mid - 1;
            }

            if (foundIdx != -1) {
                System.arraycopy(node.getKeys(), foundIdx + 1, node.getKeys(), foundIdx, node.getKeyCount() - 1 - foundIdx);
                node.setKey(node.getKeyCount() - 1, null);
                node.setKeyCount(node.getKeyCount() - 1);
                return new DeleteResult(true);
            }
            return new DeleteResult(false);
        } else {
            int idx = routeIndex(node, key);
            BPlusTreeNode<T> child = node.getChild(idx);

            if (child.getKeyCount() == minKeys) {
                bPlusFill(node, idx);
                idx = routeIndex(node, key);
            }

            return delete(node.getChild(idx), key);
        }
    }

    private void bPlusFill(BPlusTreeNode<T> parent, int index) {
        BPlusTreeNode<T> child = parent.getChild(index);

        if (index > 0 && parent.getChild(index - 1).getKeyCount() > minKeys) {
            if (child.isLeaf()) borrowLeafFromPrev(parent, index);
            else borrowInternalFromPrev(parent, index);
        } else if (index < parent.getKeyCount() && parent.getChild(index + 1).getKeyCount() > minKeys) {
            if (child.isLeaf()) borrowLeafFromNext(parent, index);
            else borrowInternalFromNext(parent, index);
        } else {
            if (index < parent.getKeyCount()) {
                if (child.isLeaf()) mergeLeaves(parent, index);
                else mergeInternal(parent, index);
            } else {
                if (child.isLeaf()) mergeLeaves(parent, index - 1);
                else mergeInternal(parent, index - 1);
            }
        }
    }


    private void borrowLeafFromNext(BPlusTreeNode<T> parent, int index) {
        BPlusTreeNode<T> child = parent.getChild(index);
        BPlusTreeNode<T> next = parent.getChild(index + 1);

        child.setKey(child.getKeyCount(), next.getKey(0));
        child.setKeyCount(child.getKeyCount() + 1);

        System.arraycopy(next.getKeys(), 1, next.getKeys(), 0, next.getKeyCount() - 1);
        next.setKey(next.getKeyCount() - 1, null);
        next.setKeyCount(next.getKeyCount() - 1);

        parent.setKey(index, next.getKey(0));
    }

    private void borrowLeafFromPrev(BPlusTreeNode<T> parent, int index) {
        BPlusTreeNode<T> child = parent.getChild(index);
        BPlusTreeNode<T> prev = parent.getChild(index - 1);

        System.arraycopy(child.getKeys(), 0, child.getKeys(), 1, child.getKeyCount());

        child.setKey(0, prev.getKey(prev.getKeyCount() - 1));
        child.setKeyCount(child.getKeyCount() + 1);

        prev.setKey(prev.getKeyCount() - 1, null);
        prev.setKeyCount(prev.getKeyCount() - 1);

        parent.setKey(index - 1, child.getKey(0));
    }

    private void mergeLeaves(BPlusTreeNode<T> parent, int index) {
        BPlusTreeNode<T> left = parent.getChild(index);
        BPlusTreeNode<T> right = parent.getChild(index + 1);

        System.arraycopy(right.getKeys(), 0, left.getKeys(), left.getKeyCount(), right.getKeyCount());
        left.setKeyCount(left.getKeyCount() + right.getKeyCount());

        left.setNext(right.getNext());

        parentRouting(parent, index);
        parent.setKeyCount(parent.getKeyCount() - 1);
    }

    private void parentRouting(BPlusTreeNode<T> parent, int index) {
        System.arraycopy(parent.getKeys(), index + 1, parent.getKeys(), index, parent.getKeyCount() - 1 - index);
        System.arraycopy(parent.getChildren(), index + 2, parent.getChildren(), index + 1, parent.getKeyCount() - 1 - index);

        parent.setKey(parent.getKeyCount() - 1, null);
        parent.setChild(parent.getKeyCount(), null);
    }


    private void borrowInternalFromNext(BPlusTreeNode<T> parent, int index) {
        BPlusTreeNode<T> child = parent.getChild(index);
        BPlusTreeNode<T> next = parent.getChild(index + 1);

        child.setKey(child.getKeyCount(), parent.getKey(index));
        child.setChild(child.getKeyCount() + 1, next.getChild(0));
        parent.setKey(index, next.getKey(0));

        System.arraycopy(next.getKeys(), 1, next.getKeys(), 0, next.getKeyCount() - 1);
        System.arraycopy(next.getChildren(), 1, next.getChildren(), 0, next.getKeyCount());
        next.setKey(next.getKeyCount() - 1, null);
        next.setChild(next.getKeyCount(), null);

        child.setKeyCount(child.getKeyCount() + 1);
        next.setKeyCount(next.getKeyCount() - 1);
    }

    private void borrowInternalFromPrev(BPlusTreeNode<T> parent, int index) {
        BPlusTreeNode<T> child = parent.getChild(index);
        BPlusTreeNode<T> prev = parent.getChild(index - 1);

        System.arraycopy(child.getKeys(), 0, child.getKeys(), 1, child.getKeyCount());
        System.arraycopy(child.getChildren(), 0, child.getChildren(), 1, child.getKeyCount() + 1);

        child.setKey(0, parent.getKey(index - 1));
        child.setChild(0, prev.getChild(prev.getKeyCount()));
        parent.setKey(index - 1, prev.getKey(prev.getKeyCount() - 1));

        prev.setKey(prev.getKeyCount() - 1, null);
        prev.setChild(prev.getKeyCount(), null);

        child.setKeyCount(child.getKeyCount() + 1);
        prev.setKeyCount(prev.getKeyCount() - 1);
    }

    private void mergeInternal(BPlusTreeNode<T> parent, int index) {
        BPlusTreeNode<T> left = parent.getChild(index);
        BPlusTreeNode<T> right = parent.getChild(index + 1);

        left.setKey(minKeys, parent.getKey(index));
        System.arraycopy(right.getKeys(), 0, left.getKeys(), minKeys + 1, minKeys);
        System.arraycopy(right.getChildren(), 0, left.getChildren(), minKeys + 1, minKeys + 1);

        parentRouting(parent, index);

        left.setKeyCount(maxKeys);
        parent.setKeyCount(parent.getKeyCount() - 1);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iterator<>() {
            private final long expectedModCount = modCount;
            private BPlusTreeNode<T> currentLeaf = head;
            private int keyIndex = 0;

            @Override
            public boolean hasNext() {
                return currentLeaf != null && keyIndex < currentLeaf.getKeyCount();
            }

            @Override
            public T next() {
                if (modCount != expectedModCount) throw new ConcurrentModificationException();
                if (!hasNext()) throw new NoSuchElementException();

                T value = currentLeaf.getKey(keyIndex++);
                if (keyIndex >= currentLeaf.getKeyCount()) {
                    currentLeaf = currentLeaf.getNext();
                    keyIndex = 0;
                }
                return value;
            }
        };
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(
                Spliterators.spliterator(iterator(), size, Spliterator.ORDERED | Spliterator.SORTED | Spliterator.NONNULL),
                false
        );
    }

    @Override
    public List<T> range(T fromInclusive, T toExclusive) {
        if (fromInclusive == null || toExclusive == null) throw new NullPointerException("Bounds cannot be null");
        if (fromInclusive.compareTo(toExclusive) > 0) throw new IllegalArgumentException("fromInclusive > toExclusive");

        List<T> result = new ArrayList<>();
        if (root == null) return result;

        BPlusTreeNode<T> current = root;
        while (!current.isLeaf()) {
            current = current.getChild(routeIndex(current, fromInclusive));
        }
        int startIndex = searchNode(current, fromInclusive).index();

        while (current != null) {
            int count = current.getKeyCount();
            if (count == 0) {
                current = current.getNext();
                continue;
            }
            if (current.getKey(count - 1).compareTo(toExclusive) < 0) {
                for (int i = startIndex; i < count; i++) {
                    result.add(current.getKey(i));
                }
            } else {
                int endIndex = searchNode(current, toExclusive).index();
                for (int i = startIndex; i < endIndex; i++) {
                    result.add(current.getKey(i));
                }
                return result;
            }

            current = current.getNext();
            startIndex = 0;
        }
        return result;
    }

    @Override
    public Stream<T> rangeStream(T fromInclusive, T toExclusive) {
        if (fromInclusive == null || toExclusive == null) throw new NullPointerException("Bounds cannot be null");
        if (fromInclusive.compareTo(toExclusive) > 0) throw new IllegalArgumentException("fromInclusive > toExclusive");

        Iterator<T> rangeIterator = new Iterator<T>() {
            private final long expectedModCount = modCount;
            private BPlusTreeNode<T> currentLeaf;
            private int keyIndex = 0;
            private T nextValue = null;

            {
                if (root != null) {
                    currentLeaf = root;
                    while (!currentLeaf.isLeaf()) {
                        currentLeaf = currentLeaf.getChild(routeIndex(currentLeaf, fromInclusive));
                    }
                    keyIndex = searchNode(currentLeaf, fromInclusive).index();
                    advance();
                }
            }

            private void advance() {
                nextValue = null;
                while (currentLeaf != null) {
                    if (keyIndex < currentLeaf.getKeyCount()) {
                        T key = currentLeaf.getKey(keyIndex++);
                        if (key.compareTo(toExclusive) < 0) {
                            nextValue = key;
                            return;
                        } else {
                            currentLeaf = null;
                            return;
                        }
                    }
                    currentLeaf = currentLeaf.getNext();
                    keyIndex = 0;
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
                T valueToReturn = nextValue;
                advance();
                return valueToReturn;
            }
        };

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(rangeIterator, Spliterator.ORDERED | Spliterator.SORTED | Spliterator.NONNULL),
            false
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> toList() {
        int currentSize = size();
        if (currentSize == 0) return new ArrayList<>();

        ArrayList<T> result = new ArrayList<>(currentSize);
        BPlusTreeNode<T> currentLeaf = head;
        while (currentLeaf != null) {
            int count = currentLeaf.getKeyCount();
            Object[] keys = currentLeaf.getKeys();
            for (int i = 0; i < count; i++) {
                result.add((T) keys[i]);
            }
            currentLeaf = currentLeaf.getNext();
        }
        return result;
    }

     @Override
    public T min() {
        if (isEmpty() || head == null || head.getKeyCount() == 0) {
            throw new EmptyTreeException("Tree is empty");
        }
        return head.getKey(0);
    }


    private BPlusTreeNode<T> ceil_floorHelper(BPlusTreeNode<T> current, T key){
        if (root == null || key == null) return null;
        while (!current.isLeaf()) {
            current = current.getChild(routeIndex(current, key));
        }
        return current;
    }
    @Override
    public T floor(T key) {
        treeIsEmpty();
        BPlusTreeNode<T> current = ceil_floorHelper(root, key);
        if (current == null) return null;
        NodeSearchResult result = searchNode(current, key);
        if (result.found()) return current.getKey(result.index());
        if (result.index() > 0) return current.getKey(result.index() - 1);
        return null;
    }

    @Override
    public T ceil(T key) {
        treeIsEmpty();
        BPlusTreeNode<T> current = ceil_floorHelper(root, key);
        if (current == null) return null;
        NodeSearchResult result = searchNode(current, key);
        if (result.found()) return current.getKey(result.index());

        if (result.index() < current.getKeyCount()) return current.getKey(result.index());

        if (current.getNext() != null) return current.getNext().getKey(0);
        return null;
    }

    @Override
    public T predecessor(T key) {
        treeIsEmpty();
        if (root == null || key == null) return null;
        BPlusTreeNode<T> current = root;
        BPlusTreeNode<T> lastLeftNode = null;
        int lastLeftIdx = -1;
        while (!current.isLeaf()) {
            int idx = routeIndex(current, key);
            if (idx > 0) {
                lastLeftNode = current;
                lastLeftIdx = idx - 1;
            }
            current = current.getChild(idx);
        }

        NodeSearchResult result = searchNode(current, key);
        int left = result.index();
        if (left > 0) return current.getKey(left - 1);
        if (lastLeftNode != null) {
            BPlusTreeNode<T> siblingLeaf = getRightmostLeaf(lastLeftNode.getChild(lastLeftIdx));
            return siblingLeaf.getKey(siblingLeaf.getKeyCount() - 1);
        }
        return null;
    }

    @Override
    public T successor(T key) {
        treeIsEmpty();
        if (root == null || key == null) return null;
        BPlusTreeNode<T> current = root;
        while (!current.isLeaf()) {
            current = current.getChild(routeIndex(current, key));
        }

        NodeSearchResult result = searchNode(current, key);
        int left = result.index();
        if (result.found()) left++;
        if (left < current.getKeyCount()) return current.getKey(left);
        if (current.getNext() != null) return current.getNext().getKey(0);
        return null;
    }
    private BPlusTreeNode<T> getRightmostLeaf(BPlusTreeNode<T> node) {
        while (!node.isLeaf()) {
            node = node.getChild(node.getKeyCount());
        }
        return node;
    }
}