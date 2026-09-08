package chaos.tree.nary;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Consumer;

public final class BTreeSet<E> extends AbstractNaryTreeSet<E, BTreeNode<E>> {

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

    public BTreeSet(BTreeSet.Builder<E> builder) {
        super(builder.degree, builder.comparator);

        if (builder.flatArray != null) {
            buildFromSortedArray(builder.flatArray, builder.factor);
        } else if (builder.sortedIterator != null) {
            buildFromSorted(builder.sortedIterator, builder.factor);
        } else if (builder.collection != null) {
            addAll(builder.collection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void buildFromSorted(Iterator<? extends E> it, float factor) {
        if (!it.hasNext()) {
            return;
        }
        int targetKeys = Math.max(minKeys, (int) (maxKeys * factor));
        BTreeNode<E>[] rightEdge = (BTreeNode<E>[]) new BTreeNode[32];
        rightEdge[0] = createNode(degree, true);
        this.root = rightEdge[0];

        while (it.hasNext()) {
            BTreeNode<E> leaf = rightEdge[0];
            while (leaf.keyCount < targetKeys && it.hasNext()) {
                leaf.keys[leaf.keyCount] = it.next();
                leaf.keyCount++;
                this.size++;
            }

            if (it.hasNext()) {
                E sepKey = it.next();
                this.size++;

                int level = 1;
                while (true) {
                    BTreeNode<E> parent = rightEdge[level];
                    if (parent == null) {
                        parent = createNode(degree, false);
                        parent.setChild(0, rightEdge[level - 1]);
                        rightEdge[level - 1].parent = parent;              // FIX
                        rightEdge[level] = parent;
                        this.root = parent;
                    }

                    if (parent.keyCount < targetKeys) {
                        parent.keys[parent.keyCount] = sepKey;
                        parent.keyCount++;

                        BTreeNode<E> prevInternal = parent;
                        for (int d = level - 1; d >= 0; d--) {
                            BTreeNode<E> newNode = createNode(degree, d == 0);
                            prevInternal.setChild(prevInternal.keyCount, newNode);
                            newNode.parent = prevInternal;                 // FIX
                            rightEdge[d] = newNode;
                            prevInternal = newNode;
                        }
                        break;
                    } else {
                        level++;
                    }
                }
            }
        }
        int highestLevel = rightEdge.length - 1;
        while (highestLevel >= 1 && rightEdge[highestLevel] == null) highestLevel--;

        for (int level = highestLevel; level >= 1; level--) {
            BTreeNode<E> node = rightEdge[level];
            if (node == null || node == this.root) continue;

            if (node.keyCount < minKeys) {
                BTreeNode<E> parent = rightEdge[level + 1];
                int childIdx = parent.keyCount;
                if (childIdx == 0) continue;

                BTreeNode<E> leftSib = parent.child[childIdx - 1];

                while (node.keyCount < minKeys && leftSib.keyCount > minKeys) {
                    System.arraycopy(node.keys, 0, node.keys, 1, node.keyCount);
                    System.arraycopy(node.child, 0, node.child, 1, node.keyCount + 1);

                    node.keys[0] = parent.keys[childIdx - 1];
                    parent.keys[childIdx - 1] = leftSib.keys[leftSib.keyCount - 1];
                    leftSib.keys[leftSib.keyCount - 1] = null;

                    node.child[0] = leftSib.child[leftSib.keyCount];
                    leftSib.child[leftSib.keyCount] = null;
                    if (node.child[0] != null) node.child[0].parent = node;

                    leftSib.keyCount--;
                    node.keyCount++;
                }

                if (node.keyCount < minKeys) {
                    leftSib.keys[leftSib.keyCount] = parent.keys[childIdx - 1];
                    leftSib.keyCount++;

                    System.arraycopy(node.keys, 0, leftSib.keys, leftSib.keyCount, node.keyCount);
                    System.arraycopy(node.child, 0, leftSib.child, leftSib.keyCount, node.keyCount + 1);
                    for (int j = 0; j <= node.keyCount; j++) {
                        if (leftSib.child[leftSib.keyCount + j] != null) {
                            leftSib.child[leftSib.keyCount + j].parent = leftSib;
                        }
                    }
                    leftSib.keyCount += node.keyCount;

                    parent.keys[childIdx - 1] = null;
                    parent.child[childIdx] = null;
                    parent.keyCount--;

                    rightEdge[level] = leftSib;
                }
            }
        }

        if (rightEdge[0] != null && rightEdge[0].keyCount < minKeys && rightEdge[0] != this.root) {
            BTreeNode<E> node = rightEdge[0];
            BTreeNode<E> parent = rightEdge[1];
            int childIdx = parent.keyCount;
            if (childIdx > 0) {
                BTreeNode<E> leftSib = parent.child[childIdx - 1];

                while (node.keyCount < minKeys && leftSib.keyCount > minKeys) {
                    System.arraycopy(node.keys, 0, node.keys, 1, node.keyCount);
                    node.keys[0] = parent.keys[childIdx - 1];
                    parent.keys[childIdx - 1] = leftSib.keys[leftSib.keyCount - 1];
                    leftSib.keys[leftSib.keyCount - 1] = null;
                    leftSib.keyCount--;
                    node.keyCount++;
                }

                if (node.keyCount < minKeys) {
                    leftSib.keys[leftSib.keyCount] = parent.keys[childIdx - 1];
                    leftSib.keyCount++;
                    System.arraycopy(node.keys, 0, leftSib.keys, leftSib.keyCount, node.keyCount);
                    leftSib.keyCount += node.keyCount;

                    parent.keys[childIdx - 1] = null;
                    parent.child[childIdx] = null;
                    parent.keyCount--;

                    for (int cascadeLevel = 1; cascadeLevel < rightEdge.length; cascadeLevel++) {
                        BTreeNode<E> n = rightEdge[cascadeLevel];
                        if (n == null || n == this.root || n.keyCount >= minKeys) break;
                        BTreeNode<E> p = rightEdge[cascadeLevel + 1];
                        int ci = p.keyCount;
                        if (ci == 0) break;
                        BTreeNode<E> ls = p.child[ci - 1];

                        if (ls.keyCount > minKeys) {
                            System.arraycopy(n.keys, 0, n.keys, 1, n.keyCount);
                            System.arraycopy(n.child, 0, n.child, 1, n.keyCount + 1);

                            n.keys[0] = p.keys[ci - 1];
                            p.keys[ci - 1] = ls.keys[ls.keyCount - 1];
                            ls.keys[ls.keyCount - 1] = null;

                            n.child[0] = ls.child[ls.keyCount];
                            ls.child[ls.keyCount] = null;
                            if (n.child[0] != null) n.child[0].parent = n;

                            ls.keyCount--;
                            n.keyCount++;
                            break;
                        } else {
                            ls.keys[ls.keyCount] = p.keys[ci - 1];
                            ls.keyCount++;

                            System.arraycopy(n.keys, 0, ls.keys, ls.keyCount, n.keyCount);
                            System.arraycopy(n.child, 0, ls.child, ls.keyCount, n.keyCount + 1);
                            for (int j = 0; j <= n.keyCount; j++) {
                                if (ls.child[ls.keyCount + j] != null) {
                                    ls.child[ls.keyCount + j].parent = ls;
                                }
                            }
                            ls.keyCount += n.keyCount;

                            p.keys[ci - 1] = null;
                            p.child[ci] = null;
                            p.keyCount--;

                            rightEdge[cascadeLevel] = ls;
                        }
                    }
                }
            }
        }

        while (this.root.keyCount == 0 && !this.root.isLeaf()) {
            this.root = this.root.child[0];
            this.root.parent = null;
        }
        this.modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void buildFromSortedArray(Object[] sortedArray, float fillFactor) {
        if (sortedArray == null || sortedArray.length == 0) return;
        if (!isEmpty()) {
            throw new IllegalStateException("Bulk load is only permitted on an empty tree.");
        }
        if (degree < 32) {
            throw new IllegalStateException("Bulk load is only supported for large chunks; degree must be at least 32.");
        }
        if (fillFactor < 0.5f || fillFactor > 1.0f) {
            throw new IllegalArgumentException("Fill factor must be between 0.5 and 1.0");
        }

        int N = sortedArray.length;
        int targetKeys = Math.max(minKeys, (int) (maxKeys * fillFactor));

        int minChild = minKeys + 1;
        int maxChild = maxKeys + 1;
        int H = 0;

        while (N > Math.pow(maxChild, H + 1) - 1) {
            H++;
        }

        this.root = buildSubtree(sortedArray, 0, N - 1, H, true, targetKeys, minChild, maxChild);
        this.size = N;
        this.modCount++;
    }

    private BTreeNode<E> buildSubtree(Object[] keys, int start, int end, int h, boolean isRoot, int targetKeys, int minChild, int maxChild) {
        int numKeys = end - start + 1;

        if (h == 0) {
            BTreeNode<E> leaf = createNode(degree, true);
            System.arraycopy(keys, start, leaf.keys, 0, numKeys);
            leaf.keyCount = numKeys;
            return leaf;
        }

        double maxChild_h = Math.pow(maxChild, h);
        double minChild_h = Math.pow(minChild, h);

        int minAllowedChild = (int) Math.ceil((numKeys + 1) / maxChild_h);
        int maxAllowedChild = (int) Math.floor((numKeys + 1) / minChild_h);

        int minChildLimit = isRoot ? 2 : minChild;
        minAllowedChild = Math.max(minAllowedChild, minChildLimit);
        maxAllowedChild = Math.min(maxAllowedChild, maxChild);

        double targetChild_h = Math.pow(targetKeys + 1, h);
        int bestChild = (int) Math.round((numKeys + 1) / targetChild_h);
        int C = Math.clamp(bestChild, minAllowedChild, maxAllowedChild);

        int kSubtrees = numKeys - (C - 1);
        int baseSize = kSubtrees / C;
        int remainder = kSubtrees % C;

        BTreeNode<E> node = createNode(degree, false);
        int currStart = start;

        for (int i = 0; i < C; i++) {
            int childTotalKeys = baseSize + (i < remainder ? 1 : 0);

            BTreeNode<E> child = buildSubtree(keys, currStart, currStart + childTotalKeys - 1, h - 1, false, targetKeys, minChild, maxChild);
            node.child[i] = child;
            child.parent = node;
            currStart += childTotalKeys;

            if (i < C - 1) {
                node.keys[i] = keys[currStart];
                node.keyCount++;
                currStart++;
            }
        }
        return node;
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
            if (idx >= 0) {
                return false;
            }
            int childIdx = ~idx;

            if (current.isLeaf()) {

                System.arraycopy(current.keys, childIdx, current.keys, childIdx + 1, current.keyCount - childIdx);
                current.keys[childIdx] = e;
                current.keyCount++;
                size++;
                modCount++;

                while (current.keyCount > maxKeys) {
                    if (current == root) {
                        BTreeNode<E> newRoot = new BTreeNode<>(degree, false);
                        newRoot.setChild(0, root);
                        splitNode(newRoot, 0, root);
                        root = newRoot;
                        break;
                    }
                    BTreeNode<E> parent = current.parent;

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

        System.arraycopy(child.keys, degree, sibling.keys, 0, degree);

        if (!child.isLeaf()) {
            System.arraycopy(child.child, degree, sibling.child, 0, degree + 1);
            for (int i = 0; i <= degree; i++) {
                if (sibling.child[i] != null) sibling.child[i].parent = sibling;
            }

            Arrays.fill(child.child, degree, child.keyCount + 1, null);
        }
        Arrays.fill(child.keys, degree, child.keyCount, null);
        child.keyCount = degree - 1;


        System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
        parent.setChild(childIdx + 1, sibling);

        System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);

        parent.keys[childIdx] = child.keys[degree - 1];
        child.keys[degree - 1] = null;

        parent.keyCount++;
    }

    private void borrowLeft(BTreeNode<E> parent, int childIdx, BTreeNode<E> sibling, BTreeNode<E> starving) {

        System.arraycopy(starving.keys, 0, starving.keys, 1, starving.keyCount);
        if (!starving.isLeaf()) {
            System.arraycopy(starving.child, 0, starving.child, 1, starving.keyCount + 1);
        }

        starving.keys[0] = parent.keys[childIdx - 1];

        if (!starving.isLeaf()) {
            starving.child[0] = sibling.child[sibling.keyCount];
            if (starving.child[0] != null) {
                starving.child[0].parent = starving;
            }
            sibling.child[sibling.keyCount] = null;
        }

        parent.keys[childIdx - 1] = sibling.keys[sibling.keyCount - 1];
        sibling.keys[sibling.keyCount - 1] = null;

        sibling.keyCount--;
        starving.keyCount++;
    }

    private void borrowRight(BTreeNode<E> parent, int childIdx, BTreeNode<E> starving, BTreeNode<E> sibling) {

        starving.keys[starving.keyCount] = parent.keys[childIdx];
        if (!starving.isLeaf()) {
            starving.child[starving.keyCount + 1] = sibling.child[0];
            if (starving.child[starving.keyCount + 1] != null) {
                starving.child[starving.keyCount + 1].parent = starving;
            }
        }

        parent.keys[childIdx] = sibling.keys[0];

        System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyCount - 1);
        sibling.keys[sibling.keyCount - 1] = null; // GC

        if (!sibling.isLeaf()) {
            System.arraycopy(sibling.child, 1, sibling.child, 0, sibling.keyCount);
            sibling.child[sibling.keyCount] = null; // GC
        }

        starving.keyCount++;
        sibling.keyCount--;
    }

    private BTreeNode<E> getPredecessorLeaf(BTreeNode<E> node, int childIdx) {
        BTreeNode<E> current = node.child[childIdx];
        while (!current.isLeaf()) {
            current = current.child[current.keyCount];
        }
        return current;
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

        BTreeNode<E> current = root;
        int idx;

        while (true) {
            idx = searchNode(current, e);
            if (idx >= 0) break;

            if (current.isLeaf()) return false;
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
        current.keys[current.keyCount - 1] = null;
        current.keyCount--;
        size--;
        modCount++;
        while (current != root && current.keyCount < minKeys) {
            BTreeNode<E> parent = current.parent;

            int childIdx = 0;
            while (childIdx <= parent.keyCount && parent.child[childIdx] != current) {
                childIdx++;
            }

            BTreeNode<E> leftSibling = (childIdx > 0) ? parent.child[childIdx - 1] : null;
            BTreeNode<E> rightSibling = (childIdx < parent.keyCount) ? parent.child[childIdx + 1] : null;

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
            if (root.isLeaf()) {
                root = null;
            } else {
                root = root.child[0];
                root.parent = null;
            }
        }

        return true;
    }

    private void mergeNodes(BTreeNode<E> parent, int childIdx, BTreeNode<E> left, BTreeNode<E> right) {

        left.keys[left.keyCount++] = parent.keys[childIdx];
        System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);

        if (!left.isLeaf()) {
            System.arraycopy(right.child, 0, left.child, left.keyCount, right.keyCount + 1);
            for (int i = 0; i <= right.keyCount; i++) {
                if (right.child[i] != null) right.child[i].parent = left;
            }
        }

        left.keyCount += right.keyCount;

        System.arraycopy(parent.keys, childIdx + 1, parent.keys, childIdx, parent.keyCount - childIdx - 1);
        parent.keys[parent.keyCount - 1] = null;

        System.arraycopy(parent.child, childIdx + 2, parent.child, childIdx + 1, parent.keyCount - childIdx - 1);
        parent.child[parent.keyCount] = null;

        parent.keyCount--;
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
        Objects.requireNonNull(action);
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
                throw new ConcurrentModificationException();
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

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        if (size == 0 || root == null) return array;
        populateArray(root, array, 0);
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        if (size == 0 || root == null) {
            if (a.length > size) a[size] = null;
            return a;
        }
        populateArray(root, a, 0);
        if (a.length > size) a[size] = null;
        return a;
    }

    private int populateArray(BTreeNode<E> node, Object[] array, int offset) {
        if (node.isLeaf()) {
            System.arraycopy(node.keys, 0, array, offset, node.keyCount);
            return offset + node.keyCount;
        } else {
            for (int i = 0; i < node.keyCount; i++) {
                offset = populateArray(node.child[i], array, offset);
                array[offset++] = node.keys[i];
            }
            return populateArray(node.child[node.keyCount], array, offset);
        }
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
        return new BTreeIterator(startKey, startInclusive);
    }

    @Override
    protected Iterator<E> baseDescendingIterator(E startKey, boolean startInclusive) {
        return new BTreeReverseIterator(startKey, startInclusive);
    }

    public static final class Builder<E> {
        private int degree = DEFAULT_DEGREE;
        private Comparator<? super E> comparator = null;
        private float factor = 0.75f;

        private Object[] flatArray = null;
        private Iterator<? extends E> sortedIterator = null;
        private Collection<? extends E> collection = null;

        private Builder() {
        }

        public static <E> BTreeSet.Builder<E> newBuilder() {
            return new BTreeSet.Builder<>();
        }

        public static <E> BTreeSet.Builder<E> create(int degree) {
            return BTreeSet.Builder.<E>newBuilder().degree(degree);
        }

        public BTreeSet.Builder<E> degree(int degree) {
            if (degree < 2 || degree > Integer.MAX_VALUE / 2) {
                throw new IllegalArgumentException("Degree must be at least 2 and less than Integer.MAX_VALUE/2");
            }
            this.degree = degree;
            return this;
        }

        public BTreeSet.Builder<E> comparator(Comparator<? super E> comparator) {
            this.comparator = comparator;
            return this;
        }

        public BTreeSet.Builder<E> factor(float factor) {
            if (factor < 0.5f || factor > 1.0f) {
                throw new IllegalArgumentException("Fill factor must be between 0.5 and 1.0");
            }
            this.factor = factor;
            return this;
        }

        public BTreeSet.Builder<E> importFlatArray(Object[] flatArray) {
            this.flatArray = flatArray;
            this.sortedIterator = null;
            this.collection = null;
            return this;
        }

        public BTreeSet.Builder<E> importSorted(Iterator<? extends E> iterator) {
            this.sortedIterator = iterator;
            this.flatArray = null;
            this.collection = null;
            return this;
        }

        public BTreeSet.Builder<E> importCollection(Collection<? extends E> c) {
            this.collection = c;
            this.flatArray = null;
            this.sortedIterator = null;
            return this;
        }

        public BTreeSet<E> build() {
            return new BTreeSet<>(this);
        }
    }

    private final class BTreeIterator implements Iterator<E> {
        private BTreeNode<E> currentNode;
        private int currentIndex;
        private long expectedModCount;
        private E lastReturned = null;

        BTreeIterator(E startKey, boolean startInclusive) {
            this.expectedModCount = modCount;
            if (root == null) return;

            if (startKey == null) {
                currentNode = root;
                while (!currentNode.isLeaf()) currentNode = currentNode.child[0];
                currentIndex = 0;
            } else {
                BTreeNode<E> curr = root;
                BTreeNode<E> bestNode = null;
                int bestIdx = -1;

                while (curr != null) {
                    int idx = searchNode(curr, startKey);
                    if (idx >= 0) {
                        if (startInclusive) {
                            bestNode = curr;
                            bestIdx = idx;
                            break;
                        } else {
                            if (!curr.isLeaf()) {
                                curr = curr.child[idx + 1];
                                while (!curr.isLeaf()) curr = curr.child[0];
                                bestNode = curr;
                                bestIdx = 0;
                            } else if (idx + 1 < curr.keyCount) {
                                bestNode = curr;
                                bestIdx = idx + 1;
                            }
                            break;
                        }
                    }
                    int insertIdx = ~idx;
                    if (insertIdx < curr.keyCount) {
                        bestNode = curr;
                        bestIdx = insertIdx;
                    }
                    curr = curr.isLeaf() ? null : curr.child[insertIdx];
                }
                this.currentNode = bestNode;
                this.currentIndex = bestIdx;
            }
        }

        @Override
        public boolean hasNext() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            return currentNode != null && currentIndex < currentNode.keyCount;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            lastReturned = (E) currentNode.keys[currentIndex];

            if (!currentNode.isLeaf()) {
                currentNode = currentNode.child[currentIndex + 1];
                while (!currentNode.isLeaf()) currentNode = currentNode.child[0];
                currentIndex = 0;
            } else {
                currentIndex++;
                if (currentIndex >= currentNode.keyCount) {
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
            }
            return lastReturned;
        }

        @Override
        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();

            E nextTarget = higher(lastReturned);
            BTreeSet.this.remove(lastReturned);
            expectedModCount = modCount;
            lastReturned = null;

            if (nextTarget != null) {
                currentNode = root;
                while (currentNode != null) {
                    int idx = searchNode(currentNode, nextTarget);
                    if (idx >= 0) {
                        currentIndex = idx;
                        break;
                    }
                    currentNode = currentNode.isLeaf() ? null : currentNode.child[~idx];
                }
            } else {
                currentNode = null;
            }
        }
    }

    private final class BTreeReverseIterator implements Iterator<E> {
        private BTreeNode<E> currentNode;
        private int currentIndex;
        private long expectedModCount;
        private E lastReturned = null;

        BTreeReverseIterator(E startKey, boolean startInclusive) {
            this.expectedModCount = modCount;
            if (root == null) return;

            if (startKey == null) {
                this.currentNode = root;
                while (!this.currentNode.isLeaf()) this.currentNode = this.currentNode.child[this.currentNode.keyCount];
                this.currentIndex = this.currentNode.keyCount - 1;
            } else {
                BTreeNode<E> curr = root;
                BTreeNode<E> bestNode = null;
                int bestIdx = -1;

                while (curr != null) {
                    int idx = searchNode(curr, startKey);
                    if (idx >= 0) {
                        if (startInclusive) {
                            bestNode = curr;
                            bestIdx = idx;
                            break;
                        } else {
                            if (!curr.isLeaf()) {
                                curr = curr.child[idx];
                                while (!curr.isLeaf()) curr = curr.child[curr.keyCount];
                                bestNode = curr;
                                bestIdx = curr.keyCount - 1;
                            } else if (idx > 0) {
                                bestNode = curr;
                                bestIdx = idx - 1;
                            }
                            break;
                        }
                    }
                    int insertIdx = ~idx;
                    if (insertIdx > 0) {
                        bestNode = curr;
                        bestIdx = insertIdx - 1;
                    }
                    curr = curr.isLeaf() ? null : curr.child[insertIdx];
                }
                this.currentNode = bestNode;
                this.currentIndex = bestIdx;
            }
        }

        @Override
        public boolean hasNext() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            return currentNode != null && currentIndex >= 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            lastReturned = (E) currentNode.keys[currentIndex];

            if (!currentNode.isLeaf()) {
                currentNode = currentNode.child[currentIndex];
                while (!currentNode.isLeaf()) currentNode = currentNode.child[currentNode.keyCount];
                currentIndex = currentNode.keyCount - 1;
            } else {
                currentIndex--;
                if (currentIndex < 0) {
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
            return lastReturned;
        }

        @Override
        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();

            E nextTarget = lower(lastReturned);
            BTreeSet.this.remove(lastReturned);
            expectedModCount = modCount;
            lastReturned = null;

            if (nextTarget != null) {
                currentNode = root;
                while (currentNode != null) {
                    int idx = searchNode(currentNode, nextTarget);
                    if (idx >= 0) {
                        currentIndex = idx;
                        break;
                    }
                    currentNode = currentNode.isLeaf() ? null : currentNode.child[~idx];
                }
            } else {
                currentNode = null;
            }
        }
    }
}