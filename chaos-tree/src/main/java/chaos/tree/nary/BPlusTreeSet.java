package chaos.tree.nary;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Consumer;

/*
I prioritize mostly DOD over OOD
 */
public final class BPlusTreeSet<E> extends AbstractNaryTreeSet<E, BPlusTreeNode<E>> {

    /*
     Equivalent to maximum of ~127 keys per node and a minimum of ~63 keys
     */
    private static final int DEFAULT_DEGREE = 64;

    public BPlusTreeSet() {
        super(DEFAULT_DEGREE, null);
    }

    public BPlusTreeSet(Comparator<? super E> comparator) {
        super(DEFAULT_DEGREE, comparator);
    }

    public BPlusTreeSet(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    public BPlusTreeSet(SortedSet<E> s) {
        super(DEFAULT_DEGREE, s.comparator());
        addAll(s);
    }

    public BPlusTreeSet(int degree) {
        super(degree, null);
    }

    public BPlusTreeSet(int degree, Comparator<? super E> comparator) {
        super(degree, comparator);
    }

    /**
     * Constructs a ChaosTree using a configuration Builder.
     */
    public BPlusTreeSet(BPlusTreeSet.Builder<E> builder) {
        super(builder.degree, builder.comparator);

        if (builder.flatArray != null) {
            buildFromSortedArray(builder.flatArray, builder.factor);
        } else if (builder.sortedIterator != null) {
            bulkLoad(builder.sortedIterator, builder.factor);
        } else if (builder.collection != null) {
            addAll(builder.collection);
        }
    }

    public static final class Builder<E> {
        private int degree = DEFAULT_DEGREE;
        private Comparator<? super E> comparator = null;
        private float factor = 0.9f;

        private Object[] flatArray = null;
        private Iterator<E> sortedIterator = null;
        private Collection<? extends E> collection = null;

        private Builder() {}

        // Allows starting with defaults: BPlusTreeSet.newBuilder().build()
        public static <E> BPlusTreeSet.Builder<E> newBuilder() {
            return new BPlusTreeSet.Builder<>();
        }

        // Convenience start: BPlusTreeSet.Builder.degree(16).build()
        public static <E> BPlusTreeSet.Builder<E> degree(int degree) {
            return BPlusTreeSet.Builder.<E>newBuilder().setDegree(degree);
        }

        public BPlusTreeSet.Builder<E> setDegree(int degree) {
            if (degree < 2 || degree > Integer.MAX_VALUE / 2) {
                throw new IllegalArgumentException("Degree must be at least 2 and less than Integer.MAX_VALUE/2");
            }
            this.degree = degree;
            return this;
        }

        public BPlusTreeSet.Builder<E> comparator(Comparator<? super E> comparator) {
            this.comparator = comparator;
            return this;
        }

        public BPlusTreeSet.Builder<E> factor(float factor) {
            if (factor < 0.5f || factor > 1.0f) {
                throw new IllegalArgumentException("Fill factor must be between 0.5 and 1.0");
            }
            this.factor = factor;
            return this;
        }

        public BPlusTreeSet.Builder<E> importFlatArray(Object[] flatArray) {
            this.flatArray = flatArray;
            this.sortedIterator = null; // Clear others
            this.collection = null;
            return this;
        }

        public BPlusTreeSet.Builder<E> importSorted(Iterator<E> iterator) {
            this.sortedIterator = iterator;
            this.flatArray = null;      // Clear others
            this.collection = null;
            return this;
        }

        public BPlusTreeSet.Builder<E> importCollection(Collection<? extends E> c) {
            this.collection = c;
            this.flatArray = null;      // Clear others
            this.sortedIterator = null;
            return this;
        }

        // Return the actual Set!
        public BPlusTreeSet<E> build() {
            return new BPlusTreeSet<>(this);
        }
    }
    /**
     * Streams strictly sorted data directly into the tree in O(N) time.
     * <p>
     * <strong>WARNING:</strong> The provided iterator MUST yield elements in strict
     * ascending order according to this tree's comparator. If the data is unsorted,
     * the tree structure will be corrupted.
     *
     * @param sortedData An iterator providing strictly sorted elements.
     * @param fillFactor A value between 0.5 and 1.0 representing how full to pack each node.
     *                   Use 1.0 for read-only data, or lower to leave room for future insertions.
     *                   A use of 0.9f is used for bulk loading in my tree. For read purpose you can
     *                   have it 1.0f but after that any insert or remove information will
     *                   trigger massive split, merge, borrow, array shifting.
     */
    public void bulkLoad(Iterator<E> sortedData, float fillFactor) {
        if (!isEmpty()) {
            throw new IllegalStateException("Bulk load is only permitted on an empty tree.");
        }
        if (fillFactor < 0.5f || fillFactor > 1.0f) {
            throw new IllegalArgumentException("Fill factor must be between 0.5 and 1.0");
        }
        buildFromSorted(sortedData, fillFactor);
    }

    @Override
    BPlusTreeNode<E> createNode(int degree, boolean isLeaf) {
        return new BPlusTreeNode<>(degree, isLeaf);
    }

    void buildFromSorted(Iterator<E> it, float fillFactor) {
        int targetKeys = Math.max(minKeys, (int) (maxKeys * fillFactor)); //This is most important I came to mark it agin

        @SuppressWarnings("unchecked")
        BPlusTreeNode<E>[] rightEdge = new BPlusTreeNode[64];

        int height = 0;
        rightEdge[0] = createNode(degree, true);
        root = rightEdge[0];

        while (it.hasNext()) {
            BPlusTreeNode<E> rightLeaf = rightEdge[0];

            if (rightLeaf.keyCount < targetKeys) {//The diff!!
                rightLeaf.keys[rightLeaf.keyCount++] = it.next();
                size++;
            } else {
                // The VERY NEXT element is our routing key.
                // We need to pull it from the iterator, but we will duplicate it later!
                E routingKey = it.next();
                size++;

                int level = 1;
                while (level <= height && rightEdge[level].keyCount == targetKeys) {
                    level++;
                }

                // Height Crisis
                if (level > height) {
                    height++;
                    BPlusTreeNode<E> newRoot = createNode(degree, false);
                    newRoot.child[0] = rightEdge[height - 1];
                    rightEdge[height - 1].parent = newRoot;

                    rightEdge[height] = newRoot;
                    root = newRoot;
                }

                // Drop the routing key COPY into the internal node!
                BPlusTreeNode<E> targetNode = rightEdge[level];
                targetNode.keys[targetNode.keyCount++] = routingKey;

                // 3. REBUILD DOWNWARD
                for (int i = level - 1; i >= 0; i--) {
                    BPlusTreeNode<E> newNode = createNode(degree, i == 0);
                    if (i == 0) {
                        BPlusTreeNode<E> oldLeaf = rightEdge[0];
                        oldLeaf.next = newNode;
                        newNode.prev = oldLeaf;
                    }

                    rightEdge[i + 1].child[rightEdge[i + 1].keyCount] = newNode;
                    newNode.parent = rightEdge[i + 1];

                    rightEdge[i] = newNode;
                }

                // Because B+Tree stores all actual data in the leaves,
                // the routing key we just pushed UP must also be pushed DOWN into the new empty leaf!
                rightEdge[0].keys[rightEdge[0].keyCount++] = routingKey;
            }
        }
    }

    /**
     * Strictly sorted array data directly into the tree in O(N) time.
     * <p>
     * <strong>WARNING:</strong> The provided array MUST yield elements in strict
     * ascending order according to this tree's comparator. If the data is unsorted,
     * the tree structure will be corrupted.
     * <p>
     * <strong>IMPORTANT:</strong> The API only works for <strong>degree > 32</strong> because below that there
     * would be less meaning to have this. Internally it uses native System.arraycopy for fast building.
     *
     * @param blast An array providing strictly sorted elements.
     * @param fillFactor  A value between 0.5 and 1.0 representing how full to pack each node.
     *                    Use 1.0 for read-only data, or lower to leave room for future insertions.
     *                    A use of 0.9f is used for bulk loading in my tree. For read purpose you can
     *                    have it 1.0f but after that any insert will
     *                    trigger massive split, and new creation of node.
     *                    Hold the Chaos!!
     */
    public void importFlatMatrix(Object[] blast, float fillFactor) {

        if (blast == null || blast.length == 0) return;

        if (!isEmpty()) {
            throw new IllegalStateException("Bulk load is only permitted on an empty tree.");
        }

        if (degree < 32) {
            throw new IllegalStateException("Bulk load is only supported for large chunks; degree must be at least 32.");
        }

        if (fillFactor < 0.5f || fillFactor > 1.0f) {
            throw new IllegalArgumentException("Fill factor must be between 0.5 and 1.0");
        }
        buildFromSortedArray(blast, fillFactor);
    }

    @SuppressWarnings("unchecked")
    private void buildFromSortedArray(Object[] blast, float factor) {
        int targetKeys = Math.max(minKeys, (int) (maxKeys * factor)); //came for this fix!!

        @SuppressWarnings("unchecked")
        BPlusTreeNode<E>[] rightEdge = (BPlusTreeNode<E>[]) new BPlusTreeNode[32];
        rightEdge[0] = new BPlusTreeNode<>(degree, true);
        this.root = rightEdge[0];

        int index = 0;
        while (index < blast.length) {
            BPlusTreeNode<E> leaf = rightEdge[0];
            int chunk = Math.min(targetKeys, blast.length - index);
            System.arraycopy(blast, index, leaf.keys, 0, chunk);
            leaf.keyCount = chunk;
            this.size += chunk;
            index += chunk;

            if (index < blast.length) {
                @SuppressWarnings("unchecked")
                E routingKey = (E) blast[index];

                int level = 1;
                while (true) {
                    if (rightEdge[level] == null) {
                        BPlusTreeNode<E> newRoot = new BPlusTreeNode<>(degree, false);
                        newRoot.setChild(0, rightEdge[level - 1]);
                        rightEdge[level] = newRoot;
                        this.root = newRoot;
                    }

                    BPlusTreeNode<E> targetNode = rightEdge[level];
                    targetNode.keys[targetNode.keyCount++] = routingKey;

                    BPlusTreeNode<E> nextRight = new BPlusTreeNode<>(degree, (level - 1) == 0);
                    targetNode.setChild(targetNode.keyCount, nextRight);
                    if (level - 1 == 0) {
                        rightEdge[0].next = nextRight;
                        nextRight.prev = rightEdge[0];
                    }
                    rightEdge[level - 1] = nextRight;

                    if (targetNode.keyCount < targetKeys) {
                        for (int i = level - 2; i >= 0; i--) {
                            BPlusTreeNode<E> fillNode = new BPlusTreeNode<>(degree, i == 0);
                            rightEdge[i + 1].setChild(0, fillNode);
                            if (i == 0) {
                                rightEdge[0].next = fillNode;
                                fillNode.prev = rightEdge[0];
                            }
                            rightEdge[i] = fillNode;
                        }
                        break;
                    }
                    routingKey = (E) targetNode.keys[targetNode.keyCount - 1];
                    targetNode.keys[targetNode.keyCount - 1] = null;
                    targetNode.keyCount--;
                    level++;
                }
            }
        }
        this.modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean add(E e) {
        if (root == null) {
            compare(e, e);
            root = createNode(degree, true);
            root.keys[0] = e;
            root.keyCount = 1;
            size++;
            modCount++;
            return true;
        }

        BPlusTreeNode<E> current = root;

        while (true) {
            int idx = searchNode(current, e);

            if (current.isLeaf()) {
                // B+TREE RULE: Only reject duplicates if we are physically at the Leaf!
                if (idx >= 0) return false;

                int insertIdx = ~idx;
                System.arraycopy(current.keys, insertIdx, current.keys, insertIdx + 1, current.keyCount - insertIdx);
                current.keys[insertIdx] = e;
                current.keyCount++;
                size++;
                modCount++;

                while (current.keyCount > maxKeys) {
                    if (current == root) {
                        BPlusTreeNode<E> newRoot = createNode(degree, false);
                        newRoot.setChild(0, root);
                        splitNode(newRoot, 0, root);
                        root = newRoot;
                        break;
                    }

                    BPlusTreeNode<E> parent = current.parent;
                    idx = searchNode(parent, (E) current.keys[0]);
                    int childIdx = (idx >= 0) ? idx + 1 : ~idx;
                    splitNode(parent, childIdx, current);
                    current = parent;  // Move UP
                }
                return true;
            }

            // ROUTE DOWN
            // If it's an exact match in an internal node (idx >= 0), follow the right child (idx + 1)
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            current = current.child[childIdx];
        }
    }

    private void splitNode(BPlusTreeNode<E> parent, int childIdx, BPlusTreeNode<E> child) {
        BPlusTreeNode<E> sibling = createNode(degree, child.isLeaf());
        if (child.isLeaf()) {
            sibling.keyCount = degree;
            // Shift right-half keys (degree keys) to sibling
            System.arraycopy(child.keys, degree, sibling.keys, 0, degree);
            // GC Cleanup
            Arrays.fill(child.keys, degree, child.keyCount, null);
            child.keyCount = degree;
            //  Wire up the next and prev pointer!!
            BPlusTreeNode<E> childNext = child.next;
            sibling.next = childNext;
            if (childNext != null) {
                childNext.prev = sibling;
            }
            sibling.prev = child; // Sibling points back to child
            child.next = sibling; // Child points forward to sibling
            //  Shift parent arrays
            System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
            parent.setChild(childIdx + 1, sibling);
            System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);
            // Push a COPY of the sibling's first key up as the Routing Key!
            parent.keys[childIdx] = sibling.keys[0];

        } else {
            sibling.keyCount = degree;
            System.arraycopy(child.keys, degree, sibling.keys, 0, degree);

            System.arraycopy(child.child, degree, sibling.child, 0, degree + 1);
            for (int i = 0; i <= degree; i++) {
                if (sibling.child[i] != null) sibling.child[i].parent = sibling;
            }
            //clearing GC!!
            Arrays.fill(child.child, degree, child.keyCount + 1, null);
            Arrays.fill(child.keys, degree, child.keyCount, null);

            child.keyCount = degree - 1;

            System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
            parent.setChild(childIdx + 1, sibling);

            System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);
            // Push middle key UP and DELETE it from the child!
            parent.keys[childIdx] = child.keys[degree - 1];
            child.keys[degree - 1] = null;

        }
        parent.keyCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (root == null || o == null) return false;
        E e;
        try {
            @SuppressWarnings("unchecked")
            E temp = (E) o;
            e = temp;
            compare(e, e);
        } catch (ClassCastException | NullPointerException ex) {
            return false;
        }

        BPlusTreeNode<E> current = root;
        while (!current.isLeaf()) {
            int idx = searchNode(current, e);
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            current = current.child[childIdx];
        }

        //SEARCH THE LEAF
        int idx = searchNode(current, e);
        if (idx < 0) return false; // Key does not exist

        /*
        GHOST DELETE IN THE LEAF
        means I do not go up traversing deleting the route key.
         */
        System.arraycopy(current.keys, idx + 1, current.keys, idx, current.keyCount - idx - 1);
        current.keys[current.keyCount - 1] = null;
        current.keyCount--;
        size--;
        modCount++;


        // 4. REBALANCE PHASE (Bottom-Up)
        while (current != root && current.keyCount < minKeys) {
            BPlusTreeNode<E> parent = current.parent;

            int childIdx = 0;
            while (childIdx <= parent.keyCount && parent.child[childIdx] != current) {
                childIdx++;
            }

            BPlusTreeNode<E> leftSibling = (childIdx > 0) ? parent.child[childIdx - 1] : null;
            BPlusTreeNode<E> rightSibling = (childIdx < parent.keyCount) ? parent.child[childIdx + 1] : null;

            if (leftSibling != null && leftSibling.keyCount > minKeys) {
                borrowLeft(parent, childIdx, leftSibling, current);
                break;
            } else if (rightSibling != null && rightSibling.keyCount > minKeys) {
                borrowRight(parent, childIdx, current, rightSibling);
                break;
            } else {
                if (leftSibling != null) {
                    mergeNodes(parent, childIdx - 1, leftSibling, current);
                    current = parent;
                } else {
                    mergeNodes(parent, childIdx, current, rightSibling);
                    current = parent;
                }
            }
        }

        if (root.keyCount == 0) {
            if (root.isLeaf()) root = null;
            else {
                root = root.child[0];
                root.parent = null;
            }
        }

        return true;
    }

    private void mergeNodes(BPlusTreeNode<E> parent, int childIdx, BPlusTreeNode<E> left, BPlusTreeNode<E> right) {
        if (left.isLeaf()) {
            // LEAF MERGE
            System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);
            left.keyCount += right.keyCount;

            // SAFELY REPAIR THE DOUBLY-LINKED LIST!
            BPlusTreeNode<E> rightNext = right.next;
            left.next = rightNext;
            if (rightNext != null) {
                rightNext.prev = left;
            }

            // Shift parent arrays left to delete the routing key

        } else {
            // INTERNAL NODE MERGE (Exactly like B-Tree)
            left.keys[left.keyCount] = parent.keys[childIdx];
            left.keyCount++;

            System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);
            System.arraycopy(right.child, 0, left.child, left.keyCount, right.keyCount + 1);
            for (int i = 0; i <= right.keyCount; i++) {
                if (right.child[i] != null) right.child[i].parent = left;
            }
            left.keyCount += right.keyCount;

        }
        System.arraycopy(parent.keys, childIdx + 1, parent.keys, childIdx, parent.keyCount - childIdx - 1);
        parent.keys[parent.keyCount - 1] = null;
        System.arraycopy(parent.child, childIdx + 2, parent.child, childIdx + 1, parent.keyCount - childIdx - 1);
        parent.child[parent.keyCount] = null;
        parent.keyCount--;
    }

    private void borrowLeft(BPlusTreeNode<E> parent, int childIdx, BPlusTreeNode<E> sibling, BPlusTreeNode<E> starving) {
        if (starving.isLeaf()) {
            System.arraycopy(starving.keys, 0, starving.keys, 1, starving.keyCount);
            starving.keys[0] = sibling.keys[sibling.keyCount - 1];
            sibling.keys[sibling.keyCount - 1] = null;
            parent.keys[childIdx - 1] = starving.keys[0];

            sibling.keyCount--;
            starving.keyCount++;
        } else {
            System.arraycopy(starving.keys, 0, starving.keys, 1, starving.keyCount);
            System.arraycopy(starving.child, 0, starving.child, 1, starving.keyCount + 1);

            starving.keys[0] = parent.keys[childIdx - 1];

            starving.child[0] = sibling.child[sibling.keyCount];
            if (starving.child[0] != null) starving.child[0].parent = starving;
            sibling.child[sibling.keyCount] = null;

            parent.keys[childIdx - 1] = sibling.keys[sibling.keyCount - 1];
            sibling.keys[sibling.keyCount - 1] = null;

            sibling.keyCount--;
            starving.keyCount++;
        }
    }

    private void borrowRight(BPlusTreeNode<E> parent, int childIdx, BPlusTreeNode<E> starving, BPlusTreeNode<E> sibling) {
        if (starving.isLeaf()) {
            starving.keys[starving.keyCount] = sibling.keys[0];
            System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyCount - 1);
            sibling.keys[sibling.keyCount - 1] = null;
            parent.keys[childIdx] = sibling.keys[0];

            starving.keyCount++;
            sibling.keyCount--;
        } else {
            starving.keys[starving.keyCount] = parent.keys[childIdx];

            starving.child[starving.keyCount + 1] = sibling.child[0];
            if (starving.child[starving.keyCount + 1] != null) {
                starving.child[starving.keyCount + 1].parent = starving;
            }

            parent.keys[childIdx] = sibling.keys[0];

            System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyCount - 1);
            sibling.keys[sibling.keyCount - 1] = null;

            System.arraycopy(sibling.child, 1, sibling.child, 0, sibling.keyCount);
            sibling.child[sibling.keyCount] = null;

            starving.keyCount++;
            sibling.keyCount--;
        }
    }

    @Override
    public boolean contains(Object o) {
        if(root == null || o == null){
            return false;
        }
        try {
            @SuppressWarnings("unchecked")
            E val = (E) o;
            BPlusTreeNode<E> current = root;
            while (current != null) {
                int idx = searchNode(current, val);
                if (current.isLeaf()) return idx >= 0;
                int childIdx = (idx >= 0) ? idx + 1 : ~idx;
                current = current.child[childIdx];
            }
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public E ceiling(E e) {
        if (root == null) return null;
        BPlusTreeNode<E> current = root;
        while (!current.isLeaf()) {
            int idx = searchNode(current, e);
            current = current.child[((idx >= 0) ? idx + 1 : ~idx)];
        }
        int idx = searchNode(current, e);
        if (idx >= 0) return (E) current.keys[idx];
        int insertIdx = ~idx;
        if (insertIdx < current.keyCount) return (E) current.keys[insertIdx];
        if (current.next != null) return (E) current.next.keys[0];
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E floor(E e) {
        if (root == null) return null;
        BPlusTreeNode<E> current = root;
        while (!current.isLeaf()) {
            int idx = searchNode(current, e);
            current = current.child[((idx >= 0) ? idx + 1 : ~idx)];
        }
        int idx = searchNode(current, e);
        if (idx >= 0) return (E) current.keys[idx];
        int insertIdx = ~idx;
        if (insertIdx > 0) return (E) current.keys[insertIdx - 1];
        if (current.prev != null) return (E) current.prev.keys[current.prev.keyCount - 1];
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E lower(E e) {
        if (root == null) return null;
        BPlusTreeNode<E> current = root;
        while (!current.isLeaf()) {
            int idx = searchNode(current, e);
            current = current.child[((idx >= 0) ? idx + 1 : ~idx)];
        }
        int idx = searchNode(current, e);
        int insertIdx = (idx >= 0) ? idx : ~idx;

        if (insertIdx > 0) return (E) current.keys[insertIdx - 1];
        if (current.prev != null) return (E) current.prev.keys[current.prev.keyCount - 1];
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E higher(E e) {
        if (root == null) return null;
        BPlusTreeNode<E> current = root;
        while (!current.isLeaf()) {
            int idx = searchNode(current, e);
            current = current.child[((idx >= 0) ? idx + 1 : ~idx)];
        }
        int idx = searchNode(current, e);
        int insertIdx = (idx >= 0) ? idx + 1 : ~idx;

        if (insertIdx < current.keyCount) return (E) current.keys[insertIdx];
        if (current.next != null) return (E) current.next.keys[0];
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        long expectedModCount = modCount;

        if (root == null) return;
        BPlusTreeNode<E> current = root;
        while (!current.isLeaf()) {
            current = current.child[0];
        }
        while (current != null) {
            for (int i = 0; i < current.keyCount; i++) action.accept((E) current.keys[i]);
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            current = current.next;
        }
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        if (size == 0 || root == null) return array;
        BPlusTreeNode<E> current = root;
        while (!current.isLeaf()) {
            current = current.child[0];
        }

        //Blasting the chunks directly into the array via native memory copy
        int offset = 0;
        while (current != null) {
            System.arraycopy(current.keys, 0, array, offset, current.keyCount);
            offset += current.keyCount;
            current = current.next;
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        if (size == 0 || root == null) {
            if (a.length > size) a[size] = null;
            return a;
        }
        BPlusTreeNode<E> current = root;
        while (!current.isLeaf()) {
            current = current.child[0];
        }

        // Blasting the chunks directly into the array via native memory copy
        int offset = 0;
        while (current != null) {
            System.arraycopy(current.keys, 0, a, offset, current.keyCount);
            offset += current.keyCount;
            current = current.next;
        }
        if (a.length > size) a[size] = null;
        return a;
    }

    @Override
    public Iterator<E> iterator() {
        return baseIterator(null, true);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return baseDescendingIterator(null, true);
    }

    @Override
    protected Iterator<E> baseIterator(E startKey, boolean startInclusive) {
        return new BPlusTreeIterator(startKey, startInclusive);
    }

    @Override
    protected Iterator<E> baseDescendingIterator(E startKey, boolean startInclusive) {
        return new BPlusTreeReverseIterator(startKey, startInclusive);
    }

    private final class BPlusTreeIterator implements Iterator<E> {
        private BPlusTreeNode<E> currentLeaf;
        private int currentIndex;
        private long expectedModCount;
        private E lastReturned = null;

        BPlusTreeIterator(E startKey, boolean startInclusive) {
            this.expectedModCount = modCount;
            if (root == null) return;

            if (startKey == null) {
                currentLeaf = root;
                while (!currentLeaf.isLeaf()) currentLeaf = currentLeaf.child[0];
                currentIndex = 0;
            } else {
                BPlusTreeNode<E> curr = root;
                while (!curr.isLeaf()) {
                    int idx = searchNode(curr, startKey);
                    curr = curr.child[(idx >= 0) ? idx + 1 : ~idx];
                }
                int idx = searchNode(curr, startKey);
                if (idx >= 0) {
                    this.currentIndex = startInclusive ? idx : idx + 1;
                } else {
                    this.currentIndex = ~idx;
                }
                this.currentLeaf = curr;
                if (this.currentIndex >= this.currentLeaf.keyCount) {
                    this.currentLeaf = this.currentLeaf.next;
                    this.currentIndex = 0;
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            return currentLeaf != null && currentIndex < currentLeaf.keyCount;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();

            lastReturned = (E) currentLeaf.keys[currentIndex];

            currentIndex++;
            if (currentIndex >= currentLeaf.keyCount) {
                currentLeaf = currentLeaf.next;
                currentIndex = 0;
            }
            return lastReturned;
        }

        @Override
        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();

            @SuppressWarnings("unchecked")
            E nextTarget = (currentLeaf != null && currentIndex < currentLeaf.keyCount)
                    ? (E) currentLeaf.keys[currentIndex] : null;

            BPlusTreeSet.this.remove(lastReturned);
            expectedModCount = modCount;
            lastReturned = null;

            if (nextTarget != null) {
                currentLeaf = root;
                while (!currentLeaf.isLeaf()) {
                    int idx = searchNode(currentLeaf, nextTarget);
                    currentLeaf = currentLeaf.child[(idx >= 0) ? idx + 1 : ~idx];
                }
                currentIndex = searchNode(currentLeaf, nextTarget);
            } else {
                currentLeaf = null;
            }
        }
    }

    private final class BPlusTreeReverseIterator implements Iterator<E> {
        private BPlusTreeNode<E> currentLeaf;
        private int currentIndex;
        private long expectedModCount;
        private E lastReturned = null;

        BPlusTreeReverseIterator(E startKey, boolean startInclusive) {
            this.expectedModCount = modCount;
            if (root == null) return;

            if (startKey == null) {
                currentLeaf = root;
                while (!currentLeaf.isLeaf()) currentLeaf = currentLeaf.child[currentLeaf.keyCount];
                currentIndex = currentLeaf.keyCount - 1;
            } else {
                BPlusTreeNode<E> curr = root;
                while (!curr.isLeaf()) {
                    int idx = searchNode(curr, startKey);
                    curr = curr.child[(idx >= 0) ? idx + 1 : ~idx];
                }
                int idx = searchNode(curr, startKey);
                if (idx >= 0) {
                    this.currentIndex = startInclusive ? idx : idx - 1;
                } else {
                    this.currentIndex = ~idx - 1;
                }
                this.currentLeaf = curr;
                if (this.currentIndex < 0) {
                    this.currentLeaf = this.currentLeaf.prev;
                    if (this.currentLeaf != null) {
                        this.currentIndex = this.currentLeaf.keyCount - 1;
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            return currentLeaf != null && currentIndex >= 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();

            lastReturned = (E) currentLeaf.keys[currentIndex];

            currentIndex--;
            if (currentIndex < 0) {
                currentLeaf = currentLeaf.prev;
                if (currentLeaf != null) currentIndex = currentLeaf.keyCount - 1;
            }
            return lastReturned;
        }

        @Override
        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();

            @SuppressWarnings("unchecked")
            E nextTarget = (currentLeaf != null && currentIndex >= 0) ? (E) currentLeaf.keys[currentIndex] : null;

            BPlusTreeSet.this.remove(lastReturned);
            expectedModCount = modCount;
            lastReturned = null;

            if (nextTarget != null) {
                currentLeaf = root;
                while (!currentLeaf.isLeaf()) {
                    int idx = searchNode(currentLeaf, nextTarget);
                    currentLeaf = currentLeaf.child[(idx >= 0) ? idx + 1 : ~idx];
                }
                currentIndex = searchNode(currentLeaf, nextTarget);
            } else {
                currentLeaf = null;
            }
        }
    }

}
