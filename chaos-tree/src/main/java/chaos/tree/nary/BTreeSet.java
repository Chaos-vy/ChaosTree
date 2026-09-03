package chaos.tree.nary;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.function.Consumer;

public final class BTreeSet<E> extends AbstractNaryTreeSet<E, BTreeNode<E>> {

    //TODO: Making a Buider fn
    /*
     Equivalent to maximum of ~127 keys per node and a minimum of ~63 keys
     */
    private static final int DEFAULT_DEGREE = 64;

    public BTreeSet() {
        super(DEFAULT_DEGREE, null);
    }

    public BTreeSet(Comparator<? super E> comparator) {
        super(DEFAULT_DEGREE, comparator);
    }

    public BTreeSet(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    public BTreeSet(SortedSet<E> s) {
        super(DEFAULT_DEGREE, s.comparator());
        addAll(s);
    }

    public BTreeSet(int degree) {
        super(degree, null);
    }

    public BTreeSet(int degree, Comparator<? super E> comparator) {
        super(degree, comparator);
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
     *                   Hold the Chaos!!
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

    /**
     * <pre>
     * B-Tree bulk construction / sorted-input strategy.
     *
     * References:
     * - CLRS
     * - Jenny's lectures
     * - Discussions with Gemini and ChatGPT for concept exploration
     *
     * I am experimenting with a "wavy-curve" style construction pattern
     * observed while studying SQLite and visualizing the structure myself.
     * I will document the final decisions separately in an ADR as well.
     * Well SQLite idea was taken I never read any such code. The explaination
     * part was done by Jenny's lecture and CLRS book. But the curiosity of making
     * it from sorted data was seen from Official JDK source code. By seeing that
     * the tree now supports clone and Serializable.
     *
     * The basic idea:
     *
     * 1. Start by filling only the first leaf.
     * 2. Maintain a target occupancy of approximately 75% for this experiment.
     * 3. For this example, use targetKey = 2.
     *
     * Input:
     *
     *     [10, 20, 30, 40, 50, 60, 70, 80, 90, ...]
     *
     * Observed insertion/filling pattern:
     *
     *     L   R   L   L   R   L
     *
     * A pattern starts to appear.
     *
     * After filling the first leaf:
     * For doubt of rightEdge :
     * dev, don't think too much
     * rightEdge you always move to right never to left so that's why rightEdge
     *
     *              [30]               <-- rightEdge[1] (Root)
     *             /    \
     *      [10,20]    [40,50]         <-- rightEdge[0] (Leaf)
     *
     * The same pattern appears when the root becomes full.
     *
     * When 60 arrives, it is pulled upward as the routing key into
     * rightEdge[1] (the parent/root).
     *
     * 70 and 80 then go into the newly created leaf:
     *
     *              [30, 60]            <-- rightEdge[1] is now FULL
     *             /    |    \
     *      [10,20] [40,50] [70,80]     <-- rightEdge[0] is FULL
     *
     * When 90 is inserted, the tree height increases by one:
     *
     *     height: 1 → 2
     *
     * This structure is supported directly during construction through
     * the constructor configuration.
     *
     * ---------------------------------------------------------------
     *
     * B+Tree variant
     * ---------------------------------------------------------------
     *
     * I want to use the same general construction idea for the B+Tree,
     * but the routing-key mapping is different.
     *
     * The B+Tree also requires:
     *
     * - different routing-key semantics
     * - leaf-level next pointers
     * - leaf-level previous pointers
     *
     * I do not plan to create a separate long-form document for the
     * B+Tree construction. The important differences will be documented
     * directly in the implementation comments and/or ADR.
     *
     * Example B+Tree filling pattern:
     *
     *     [10, 20, 30, 40, 50, 60, 70, ...]
     *
     *     L   LR   L   LR   L   LR   L
     *
     * Jenny's lecture helps visualize this pattern and the relationship
     * between the leaf filling and routing-key movement.
     *
     * The goal here is not to blindly reproduce a textbook implementation,
     * but to understand the pattern, formalize the invariants, and then
     * turn the observation into a deterministic construction strategy.
     * </pre>
     */
    void buildFromSorted(Iterator<E> it, float fillFactor) {
        // Calculate the future mighty chaos target (e.g., 0.75 * 63 = 47 keys per node)
        /*
        Well lemme explain it. If I banged with 100% node capacity there will a tremendous trigger of merge and split node LOL
         */
        int targetKeys = Math.max(minKeys, (int) (maxKeys * fillFactor));
        // Track the right-most path of the tree (index 0 is the Leaf layer)
        @SuppressWarnings("unchecked")
        BTreeNode<E>[] rightEdge = new BTreeNode[64]; // 64 2,3,4 trees structure rest will never touch that depth LOL

        int height = 0;
        rightEdge[0] = createNode(degree, true);
        root = rightEdge[0];

        while (it.hasNext()) {
            BTreeNode<E> rightLeaf = rightEdge[0];

            if (rightLeaf.keyCount < targetKeys) {
                // 1. FAST APPEND: Shove the data into the leaf!
                rightLeaf.keys[rightLeaf.keyCount++] = it.next();
                size++;
            } else {
                // The VERY NEXT element acts as the routing key up above!
                E routingKey = it.next();
                size++;

                // Find the lowest level on the right edge that has room for the routing key
                int level = 1;
                while (level <= height && rightEdge[level].keyCount == targetKeys) {
                    level++;
                }
                // If I ran out of height grow the tree upwards! (New Root)
                if (level > height) {
                    height++;
                    BTreeNode<E> newRoot = createNode(degree, false);
                    newRoot.child[0] = rightEdge[height - 1]; // Link old root
                    rightEdge[height - 1].parent = newRoot;      // Set parent pointer!

                    rightEdge[height] = newRoot;
                    root = newRoot;
                }
                // Insert the routing key into the target level
                BTreeNode<E> targetNode = rightEdge[level];
                targetNode.keys[targetNode.keyCount++] = routingKey;
                // 3. REBUILD DOWNWARD: Create a fresh empty path down to the leaf layer
                for (int i = level - 1; i >= 0; i--) {
                    BTreeNode<E> newNode = createNode(degree, i == 0);
                    // Link it to the parent above it
                    rightEdge[i + 1].child[rightEdge[i + 1].keyCount] = newNode;
                    newNode.parent = rightEdge[i + 1]; // Set parent pointer!
                    // Update tracking array
                    rightEdge[i] = newNode;
                }
            }
        }

        // Optional: If the very last leaf didn't reach minKeys, the B-Tree rules technically
        // allow the right-most edge to be underfull immediately after a bulk load.
        // Future inserts will naturally fix it!
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
     * @param sortedArray An array providing strictly sorted elements.
     * @param fillFactor  A value between 0.5 and 1.0 representing how full to pack each node.
     *                    Use 1.0 for read-only data, or lower to leave room for future insertions.
     *                    A use of 0.9f is used for bulk loading in my tree. For read purpose you can
     *                    have it 1.0f but after that any insert or remove information will
     *                    trigger massive split, merge, borrow, array shifting.
     *                    Hold the Chaos!!
     */
    public void bulkLoadArray(Object[] sortedArray, float fillFactor) {

        if (sortedArray == null || sortedArray.length == 0) return;

        if (!isEmpty()) {
            throw new IllegalStateException("Bulk load is only permitted on an empty tree.");
        }
        if (degree < 32) {
            throw new IllegalStateException("Bulk load only service for large chunks, degree must be greater than 32");
        }
        if (fillFactor < 0.5f || fillFactor > 1.0f) {
            throw new IllegalArgumentException("Fill factor must be between 0.5 and 1.0");
        }
        buildFromSortedArray(sortedArray, fillFactor);
    }

    private void buildFromSortedArray(Object[] sortedArray, float fillfactor) {
        int maxKeys = (degree << 1) - 1;
        int targetKeys = Math.max(1, (int) (maxKeys * fillfactor));

        @SuppressWarnings("unchecked")
        BTreeNode<E>[] rightEdge = (BTreeNode<E>[]) new BTreeNode[32];
        rightEdge[0] = new BTreeNode<>(degree, true);
        this.root = rightEdge[0];

        int index = 0;
        while (index < sortedArray.length) {
            BTreeNode<E> leaf = rightEdge[0];
            int chunk = Math.min(targetKeys, sortedArray.length - index);
            System.arraycopy(sortedArray, index, leaf.keys, 0, chunk);
            leaf.keyCount = chunk;
            this.size += chunk;
            index += chunk;

            if (index < sortedArray.length) {
                @SuppressWarnings("unchecked")
                E routingKey = (E) sortedArray[index++];
                this.size++;

                int level = 1;
                while (true) {
                    if (rightEdge[level] == null) {
                        BTreeNode<E> newRoot = new BTreeNode<>(degree, false);
                        newRoot.setChild(0, rightEdge[level - 1]);
                        rightEdge[level] = newRoot;
                        this.root = newRoot;
                    }

                    BTreeNode<E> targetNode = rightEdge[level];
                    targetNode.keys[targetNode.keyCount++] = routingKey;

                    BTreeNode<E> nextRight = new BTreeNode<>(degree, (level - 1) == 0);
                    targetNode.setChild(targetNode.keyCount, nextRight);
                    rightEdge[level - 1] = nextRight;

                    if (targetNode.keyCount < targetKeys) {
                        for (int i = level - 2; i >= 0; i--) {
                            BTreeNode<E> fillNode = new BTreeNode<>(degree, i == 0);
                            rightEdge[i + 1].setChild(0, fillNode);
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
    BTreeNode<E> createNode(int degree, boolean isLeaf) {
        return new BTreeNode<>(degree, isLeaf);
    }

    @Override
    public boolean add(E e) {
        if (root == null) {
            compare(e, e);
            root = new BTreeNode<>(degree, true);
            root.keys[0] = e;
            root.keyCount = 1;
            size++;
            modCount++;
            return true;
        }

        BTreeNode<E> current = root;

        while (true) {
            int idx = searchNode(current, e);

            // B-TREE RULE: Any exact match, anywhere in the tree, is a duplicate!
            if (idx >= 0) {
                return false;
            }
            int childIdx = ~idx;

            if (current.isLeaf()) {
                /*
                 1. INLINED LEAF INSERTION
                 No checking the leaf is full prior to it
                 */
                System.arraycopy(current.keys, childIdx, current.keys, childIdx + 1, current.keyCount - childIdx);
                current.keys[childIdx] = e;
                current.keyCount++;
                size++;
                modCount++;
                /*
                 2. ITERATIVE BUBBLE-UP
                 Now here I designed to catch the overflow
                 */
                while (current.keyCount > maxKeys) {
                    if (current == root) {
                        BTreeNode<E> newRoot = new BTreeNode<>(degree, false);
                        newRoot.setChild(0, root);
                        splitNode(newRoot, 0, root);
                        root = newRoot;
                        break;
                    }
                    BTreeNode<E> parent = current.parent;
                    /*
                    Since I have completely made the new node I need to also map with the parent node
                     */
                    @SuppressWarnings("unchecked")
                    E eval = (E) current.keys[0];
                    int pIdx = ~searchNode(parent, eval);
                    splitNode(parent, pIdx, current);
                    current = parent; // Move UP
                }
                return true;
            }
            current = current.child[childIdx];
        }
    }

    private void splitNode(BTreeNode<E> parent, int childIdx, BTreeNode<E> child) {
        BTreeNode<E> sibling = new BTreeNode<>(degree, child.isLeaf());
        sibling.keyCount = degree;
        // Shift right-half keys
        System.arraycopy(child.keys, degree, sibling.keys, 0, degree);
        // Shift right-half child and update parent pointers
        if (!child.isLeaf()) {
            System.arraycopy(child.child, degree, sibling.child, 0, degree + 1);
            for (int i = 0; i <= degree; i++) {
                if (sibling.child[i] != null) sibling.child[i].parent = sibling;
            }
            // GC Cleanup
            Arrays.fill(child.child, degree, child.keyCount + 1, null);
        }
        Arrays.fill(child.keys, degree, child.keyCount, null);
        child.keyCount = degree - 1;

        // Shift parent arrays to make room
        System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
        parent.setChild(childIdx + 1, sibling); // This auto-sets sibling.parent = parent!

        System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);

        // Push the middle key UP and DELETE it from the child!
        parent.keys[childIdx] = child.keys[degree - 1];
        child.keys[degree - 1] = null;

        parent.keyCount++;
    }

    private void borrowLeft(BTreeNode<E> parent, int childIdx, BTreeNode<E> sibling, BTreeNode<E> starving) {
        // Shift starving node's keys & child RIGHT by 1 to make space at index 0
        System.arraycopy(starving.keys, 0, starving.keys, 1, starving.keyCount);
        if (!starving.isLeaf()) {
            System.arraycopy(starving.child, 0, starving.child, 1, starving.keyCount + 1);
        }

        // Pull the Parent's routing key DOWN into the starving node's 0 index
        starving.keys[0] = parent.keys[childIdx - 1];

        // Move the Sibling's largest child over to the starving node
        if (!starving.isLeaf()) {
            starving.child[0] = sibling.child[sibling.keyCount];
            if (starving.child[0] != null) {
                starving.child[0].parent = starving;
            }
            sibling.child[sibling.keyCount] = null; // GC
        }

        // Push the Sibling's largest key UP to the parent
        parent.keys[childIdx - 1] = sibling.keys[sibling.keyCount - 1];
        sibling.keys[sibling.keyCount - 1] = null; // GC

        //  Update counts
        sibling.keyCount--;
        starving.keyCount++;
    }

    private void borrowRight(BTreeNode<E> parent, int childIdx, BTreeNode<E> starving, BTreeNode<E> sibling) {
        //  Pull the Parent's routing key DOWN into the end of the starving node
        starving.keys[starving.keyCount] = parent.keys[childIdx];

        //  Move the Sibling's smallest child over to the end of the starving node
        if (!starving.isLeaf()) {
            starving.child[starving.keyCount + 1] = sibling.child[0];
            if (starving.child[starving.keyCount + 1] != null) {
                starving.child[starving.keyCount + 1].parent = starving;
            }
        }

        // Push the Sibling's smallest key UP to the parent
        parent.keys[childIdx] = sibling.keys[0];

        // Shift sibling's keys & child LEFT by 1 to fill the gap
        System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyCount - 1);
        sibling.keys[sibling.keyCount - 1] = null; // GC

        if (!sibling.isLeaf()) {
            System.arraycopy(sibling.child, 1, sibling.child, 0, sibling.keyCount);
            sibling.child[sibling.keyCount] = null; // GC
        }

        // Update counts
        starving.keyCount++;
        sibling.keyCount--;
    }

    private BTreeNode<E> getPredecessorLeaf(BTreeNode<E> node, int childIdx) {
        BTreeNode<E> current = node.child[childIdx]; // Go left once
        while (!current.isLeaf()) {
            current = current.child[current.keyCount]; // Go right all the way down
        }
        return current;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (root == null) return false;

        BTreeNode<E> current = root;
        int idx = -1;

        E e = (E) o;
        while (true) {
            idx = searchNode(current, e);
            if (idx >= 0) break; // Found it!

            if (current.isLeaf()) return false; // Key does not exist
            current = current.child[~idx];
        }

        if (!current.isLeaf()) {
            BTreeNode<E> predLeaf = getPredecessorLeaf(current, idx);

            E predKey = (E) predLeaf.keys[predLeaf.keyCount - 1];
            current.keys[idx] = predKey;

            current = predLeaf;
            idx = current.keyCount - 1;
        }
        System.arraycopy(current.keys, idx + 1, current.keys, idx, current.keyCount - idx - 1);
        current.keys[current.keyCount - 1] = null; // GC Cleanup
        current.keyCount--;
        size--;
        modCount++;
        while (current != root && current.keyCount < minKeys) {
            BTreeNode<E> parent = current.parent;

            // Find which child index 'current' represents in the parent
            int childIdx = 0;
            while (childIdx <= parent.keyCount && parent.child[childIdx] != current) {
                childIdx++;
            }

            BTreeNode<E> leftSibling = (childIdx > 0) ? parent.child[childIdx - 1] : null;
            BTreeNode<E> rightSibling = (childIdx < parent.keyCount) ? parent.child[childIdx + 1] : null;

            // Trying the  borrowing first!
            if (leftSibling != null && leftSibling.keyCount > minKeys) {
                borrowLeft(parent, childIdx, leftSibling, current);
                break; // Rebalance complete!
            } else if (rightSibling != null && rightSibling.keyCount > minKeys) {
                borrowRight(parent, childIdx, current, rightSibling);
                break; // Rebalance complete!
            } else {
                // Must Merge (Smasher)
                if (leftSibling != null) {
                    mergeNodes(parent, childIdx - 1, leftSibling, current);
                } else {
                    mergeNodes(parent, childIdx, current, rightSibling);
                    // The parent lost a key, so underflow bubbles UP!
                    current = parent;
                }
            }
        }
        if (root.keyCount == 0) {
            if (root.isLeaf()) {
                root = null; // The tree is now completely empty
            } else {
                root = root.child[0];
                root.parent = null; // Drop the old root to the GC
            }
        }

        return true;
    }

    private void mergeNodes(BTreeNode<E> parent, int childIdx, BTreeNode<E> left, BTreeNode<E> right) {
        // Pull the parent's routing key DOWN into the middle of the left node
        left.keys[left.keyCount++] = parent.keys[childIdx];

        // merge the right node's keys into the left node
        System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);

        // merge the right node's children into the left node only for non-leaf
        if (!left.isLeaf()) {
            System.arraycopy(right.child, 0, left.child, left.keyCount, right.keyCount + 1);
            // Update the parent pointers for the children I just moved!
            for (int i = 0; i <= right.keyCount; i++) {
                if (right.child[i] != null) right.child[i].parent = left;
            }
        }

        left.keyCount += right.keyCount;

        // Shifting the parent's keys and children LEFT by 1 to close the gap left by the routing key
        System.arraycopy(parent.keys, childIdx + 1, parent.keys, childIdx, parent.keyCount - childIdx - 1);
        parent.keys[parent.keyCount - 1] = null; // Clean up GC

        System.arraycopy(parent.child, childIdx + 2, parent.child, childIdx + 1, parent.keyCount - childIdx - 1);
        parent.child[parent.keyCount] = null; // Clean up GC

        parent.keyCount--;
    }

    @Override
    public Iterator<E> iterator() {
        return new BTreeIterator(true);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new BTreeIterator(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E ceiling(E e) {
        if (root == null) return null;

        BTreeNode<E> current = root;
        E bestMatch = null;

        while (true) {
            int idx = searchNode(current, e);
            if (idx >= 0) return (E) current.keys[idx];
            int childIdx = ~idx;
            if (childIdx < current.keyCount) {
                bestMatch = (E) current.keys[childIdx];
            }
            if (current.isLeaf()) return bestMatch;
            current = current.child[childIdx];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public E floor(E e) {
        if (root == null) return null;
        BTreeNode<E> current = root;
        E bestMatch = null;

        while (true) {
            int idx = searchNode(current, e);
            if (idx >= 0) {
                return (E) current.keys[idx];
            }
            int childIdx = ~idx;
            if (childIdx > 0) {
                bestMatch = (E) current.keys[childIdx - 1];
            }
            if (current.isLeaf()) {
                return bestMatch;
            }
            current = current.child[childIdx];
        }
    }

    private int findChildIndex(BTreeNode<E> parent, BTreeNode<E> child) {
        if (parent == null) return -1;
        for (int i = 0; i <= parent.keyCount; i++) {
            if (parent.child[i] == child) return i;
        }
        return -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E lower(E e) {
        if (root == null) return null;
        BTreeNode<E> current = root;
        E bestMatch = null;

        while (true) {
            int idx = searchNode(current, e);
            int childIdx = (idx >= 0) ? idx : ~idx;
            if (childIdx > 0) {
                bestMatch = (E) current.keys[childIdx - 1];
            }
            if (current.isLeaf()) return bestMatch;
            current = current.child[childIdx];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public E higher(E e) {
        if (root == null) return null;
        BTreeNode<E> current = root;
        E bestMatch = null;

        while (true) {
            int idx = searchNode(current, e);
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            if (childIdx < current.keyCount) {
                bestMatch = (E) current.keys[childIdx];
            }
            if (current.isLeaf()) return bestMatch;
            current = current.child[childIdx];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEach(Consumer<? super E> action) {
        java.util.Objects.requireNonNull(action);
        long expectedModCount = modCount;

        if (root == null) return;

        BTreeNode<E> current = root;
        while (!current.isLeaf()) {
            current = current.child[0];
        }
        int index = 0;

        while (current != null) {
            action.accept((E) current.keys[index]);

            if (expectedModCount != modCount) {
                throw new java.util.ConcurrentModificationException();
            }
            if (!current.isLeaf()) {
                current = current.child[index + 1];
                while (!current.isLeaf()) {
                    current = current.child[0];
                }
                index = 0;
            } else if (index + 1 < current.keyCount) {
                index++;
            } else {
                BTreeNode<E> parent = current.parent;
                int childIdx = (parent != null) ? findChildIndex(parent, current) : -1;
                while (parent != null && childIdx == parent.keyCount) {
                    current = parent;
                    parent = current.parent;
                    childIdx = (parent != null) ? findChildIndex(parent, current) : -1;
                }

                if (parent != null) {
                    current = parent;
                    index = childIdx;
                } else {
                    current = null;
                }
            }
        }
    }

    private class BTreeIterator implements Iterator<E> {
        private final boolean ascending;
        private BTreeNode<E> currentNode;
        private int currentIndex;
        private long expectedModCount;

        private E lastReturned = null;
        private E nextElement = null;

        @SuppressWarnings("unchecked")
        BTreeIterator(boolean ascending) {
            this.ascending = ascending;
            this.expectedModCount = modCount;

            if (root == null) {
                currentNode = null;
            } else if (ascending) {
                currentNode = root;
                while (!currentNode.isLeaf()) currentNode = currentNode.child[0];
                currentIndex = 0;
            } else {
                currentNode = root;
                while (!currentNode.isLeaf()) currentNode = currentNode.child[currentNode.keyCount];
                currentIndex = currentNode.keyCount - 1;
            }

            if (currentNode != null) nextElement = (E) currentNode.keys[currentIndex];
        }

        @Override
        public boolean hasNext() {
            return currentNode != null && currentIndex < currentNode.keyCount;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (currentNode == null) throw new NoSuchElementException();

            lastReturned = (E) currentNode.keys[currentIndex];

            if (ascending) {
                if (!currentNode.isLeaf()) {
                    currentNode = currentNode.child[currentIndex + 1];
                    while (!currentNode.isLeaf()) currentNode = currentNode.child[0];
                    currentIndex = 0;
                } else if (currentIndex + 1 < currentNode.keyCount) {
                    currentIndex++;
                } else {
                    BTreeNode<E> parent = currentNode.parent;
                    int childIdx = findChildIndex(parent, currentNode);

                    while (parent != null && childIdx == parent.keyCount) {
                        currentNode = parent;
                        parent = currentNode.parent;
                        childIdx = findChildIndex(parent, currentNode);
                    }

                    if (parent != null) {
                        currentNode = parent;
                        currentIndex = childIdx;
                    } else {
                        currentNode = null;
                    }
                }
            } else {
                if (!currentNode.isLeaf()) {
                    currentNode = currentNode.child[currentIndex];
                    while (!currentNode.isLeaf()) currentNode = currentNode.child[currentNode.keyCount];
                    currentIndex = currentNode.keyCount - 1;
                } else if (currentIndex - 1 >= 0) {
                    currentIndex--;
                } else {
                    BTreeNode<E> parent = currentNode.parent;
                    int childIdx = findChildIndex(parent, currentNode);

                    while (parent != null && childIdx == 0) {
                        currentNode = parent;
                        parent = currentNode.parent;
                        childIdx = findChildIndex(parent, currentNode);
                    }

                    if (parent != null) {
                        currentNode = parent;
                        currentIndex = childIdx - 1;
                    } else {
                        currentNode = null;
                    }
                }
            }

            if (currentNode != null) nextElement = (E) currentNode.keys[currentIndex];
            else nextElement = null;

            return lastReturned;
        }

        @Override
        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();

            BTreeSet.this.remove(lastReturned);

            expectedModCount = modCount;
            lastReturned = null;

            if (nextElement != null) {
                currentNode = root;
                while (true) {
                    int idx = searchNode(currentNode, nextElement);
                    if (idx >= 0) {
                        currentIndex = idx;
                        break;
                    }
                    currentNode = currentNode.child[~idx];
                }
            } else {
                currentNode = null;
            }
        }

        private int findChildIndex(BTreeNode<E> parent, BTreeNode<E> child) {
            if (parent == null) return -1;
            for (int i = 0; i <= parent.keyCount; i++) {
                if (parent.child[i] == child) return i;
            }
            return -1;
        }
    }
}