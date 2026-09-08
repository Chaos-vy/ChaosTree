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

public final class BPlusTreeSet<E> extends AbstractNaryTreeSet<E, BPlusTreeNode<E>> {

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

    public BPlusTreeSet(BPlusTreeSet.Builder<E> builder) {
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
    BPlusTreeNode<E> createNode(int degree, boolean isLeaf) {
        return new BPlusTreeNode<>(degree, isLeaf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void buildFromSorted(Iterator<? extends E> it, float factor) {
        if (!isEmpty()) {
            throw new IllegalStateException("Bulk load is only permitted on an empty tree.");
        }
        if (factor < 0.5f || factor > 1.0f) {
            throw new IllegalArgumentException("Fill factor must be between 0.5 and 1.0");
        }
        if (!it.hasNext()) {
            return;
        }

        int targetKeys = Math.max(minKeys, (int) (maxKeys * factor));
        BPlusTreeNode<E>[] rightEdge = (BPlusTreeNode<E>[]) new BPlusTreeNode[32];
        rightEdge[0] = createNode(degree, true);
        this.root = rightEdge[0];

        while (it.hasNext()) {
            E element = it.next();
            BPlusTreeNode<E> leaf = rightEdge[0];

            if (leaf.keyCount < targetKeys) {
                leaf.keys[leaf.keyCount] = element;
                leaf.keyCount++;
                this.size++;
            } else {
                BPlusTreeNode<E> newLeaf = createNode(degree, true);
                leaf.next = newLeaf;
                newLeaf.prev = leaf;

                newLeaf.keys[0] = element;
                newLeaf.keyCount = 1;
                this.size++;

                E routingKey = element;
                BPlusTreeNode<E> leftChild = leaf;
                BPlusTreeNode<E> rightChild = newLeaf;

                rightEdge[0] = newLeaf;

                int level = 1;
                while (true) {
                    BPlusTreeNode<E> parent = rightEdge[level];

                    if (parent == null) {
                        parent = createNode(degree, false);
                        parent.setChild(0, leftChild);
                        leftChild.parent = parent;
                        rightEdge[level] = parent;
                        this.root = parent;
                    }

                    if (parent.keyCount < targetKeys) {
                        parent.keys[parent.keyCount] = routingKey;
                        parent.setChild(parent.keyCount + 1, rightChild);
                        rightChild.parent = parent;
                        parent.keyCount++;
                        break;
                    } else {
                        BPlusTreeNode<E> newInternal = createNode(degree, false);
                        newInternal.setChild(0, rightChild);
                        rightChild.parent = newInternal;
                        rightEdge[level] = newInternal;

                        leftChild = parent;
                        rightChild = newInternal;
                        level++;
                    }
                }
            }
        }

        int highestLevel = rightEdge.length - 1;
        while (highestLevel >= 1 && rightEdge[highestLevel] == null) highestLevel--;

        int level = highestLevel;
        while (level >= 1) {
            BPlusTreeNode<E> node = rightEdge[level];
            if (node == null) {
                level--;
                continue;
            }
            if (node.keyCount == 0 && node != this.root) {
                BPlusTreeNode<E> parent = rightEdge[level + 1];
                int childIdx = parent.keyCount;
                BPlusTreeNode<E> leftSib = parent.child[childIdx - 1];

                if (leftSib.keyCount > minKeys) {
                    node.keys[0] = parent.keys[childIdx - 1];
                    node.child[1] = node.child[0];
                    node.child[0] = leftSib.child[leftSib.keyCount];
                    if (node.child[0] != null) node.child[0].parent = node;

                    parent.keys[childIdx - 1] = leftSib.keys[leftSib.keyCount - 1];
                    leftSib.keys[leftSib.keyCount - 1] = null;
                    leftSib.child[leftSib.keyCount] = null;
                    leftSib.keyCount--;
                    node.keyCount++;
                    level--;
                } else {
                    leftSib.keys[leftSib.keyCount] = parent.keys[childIdx - 1];
                    leftSib.child[leftSib.keyCount + 1] = node.child[0];
                    if (leftSib.child[leftSib.keyCount + 1] != null) {
                        leftSib.child[leftSib.keyCount + 1].parent = leftSib;
                    }
                    leftSib.keyCount++;
                    rightEdge[level] = leftSib;

                    parent.keys[childIdx - 1] = null;
                    parent.child[childIdx] = null;
                    parent.keyCount--;

                    if (parent.keyCount == 0 && parent != this.root) {
                        level++;
                    }
                    else {
                        level--;
                    }
                }
            }
            else {
                level--;
            }
        }
        if (rightEdge[0] != null && rightEdge[0].keyCount < minKeys && rightEdge[0] != this.root) {
            BPlusTreeNode<E> node = rightEdge[0];
            BPlusTreeNode<E> parent = rightEdge[1];
            int childIdx = parent.keyCount;
            BPlusTreeNode<E> leftSib = parent.child[childIdx - 1];

            while (node.keyCount < minKeys && leftSib.keyCount > minKeys) {
                System.arraycopy(node.keys, 0, node.keys, 1, node.keyCount);
                node.keys[0] = leftSib.keys[leftSib.keyCount - 1];
                leftSib.keys[leftSib.keyCount - 1] = null;
                leftSib.keyCount--;
                node.keyCount++;
                parent.keys[childIdx - 1] = node.keys[0];
            }

            if (node.keyCount < minKeys) {
                System.arraycopy(node.keys, 0, leftSib.keys, leftSib.keyCount, node.keyCount);
                leftSib.keyCount += node.keyCount;

                parent.keys[childIdx - 1] = null;
                parent.child[childIdx] = null;
                parent.keyCount--;

                leftSib.next = node.next;
                if (leftSib.next != null) {
                    leftSib.next.prev = leftSib;
                }

                for (int cascadeLevel = 1; cascadeLevel < rightEdge.length; cascadeLevel++) {
                    BPlusTreeNode<E> n = rightEdge[cascadeLevel];
                    if (n == null || n == this.root || n.keyCount > 0) break;
                    BPlusTreeNode<E> p = rightEdge[cascadeLevel + 1];
                    int ci = p.keyCount;
                    BPlusTreeNode<E> ls = p.child[ci - 1];

                    if (ls.keyCount > minKeys) {
                        n.keys[0] = p.keys[ci - 1];
                        n.child[1] = n.child[0];
                        n.child[0] = ls.child[ls.keyCount];
                        if (n.child[0] != null) n.child[0].parent = n;
                        p.keys[ci - 1] = ls.keys[ls.keyCount - 1];
                        ls.keys[ls.keyCount - 1] = null;
                        ls.child[ls.keyCount] = null;
                        ls.keyCount--;
                        n.keyCount++;
                        break;
                    }
                    else {
                        ls.keys[ls.keyCount] = p.keys[ci - 1];
                        ls.child[ls.keyCount + 1] = n.child[0];
                        if (ls.child[ls.keyCount + 1] != null) {
                            ls.child[ls.keyCount + 1].parent = ls;
                        }
                        ls.keyCount++;
                        rightEdge[cascadeLevel] = ls;

                        p.keys[ci - 1] = null;
                        p.child[ci] = null;
                        p.keyCount--;
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
    protected void buildFromSortedArray(Object[] flatArray, float factor) {
        if (flatArray == null || flatArray.length == 0) return;
        if (!isEmpty()) {
            throw new IllegalStateException("Bulk load is only permitted on an empty tree.");
        }
        if (degree < 32) {
            throw new IllegalStateException("Bulk load is only supported for large chunks; degree must be at least 32.");
        }
        if (factor < 0.5f || factor > 1.0f) {
            throw new IllegalArgumentException("Fill factor must be between 0.5 and 1.0");
        }

        int N = flatArray.length;
        int targetKeys = Math.max(minKeys, (int) (maxKeys * factor));

        int minChild = minKeys + 1;
        int maxChild = maxKeys + 1;
        int H = 0;
        
        while (true) {
            double maxAtH = (double) maxKeys * Math.pow(maxChild, H);
            if (N <= maxAtH) break;
            H++;
        }

        this.builderPrevLeaf = null;
        this.root = buildSubtree(flatArray, 0, N - 1, H, true, targetKeys, minChild, maxChild);
        this.size = N;
        this.modCount++;
    }

    private BPlusTreeNode<E> builderPrevLeaf;

    private BPlusTreeNode<E> buildSubtree(Object[] keys, int start, int end, int h, boolean isRoot, int targetKeys, int minChild, int maxChild) {
        int numKeys = end - start + 1;
        
        if (h == 0) {
            BPlusTreeNode<E> leaf = createNode(degree, true);
            System.arraycopy(keys, start, leaf.keys, 0, numKeys);
            leaf.keyCount = numKeys;
            
            if (builderPrevLeaf != null) {
                builderPrevLeaf.next = leaf;
                leaf.prev = builderPrevLeaf;
            }
            builderPrevLeaf = leaf;
            return leaf;
        }

        double maxSubtree = (double) maxKeys * Math.pow(maxChild, h - 1);
        double minSubtree = (double) minKeys * Math.pow(minChild, h - 1);
        
        int minAllowedChild = (int) Math.ceil(numKeys / maxSubtree);
        int maxAllowedChild = (int) Math.floor(numKeys / minSubtree);
        
        int minChildLimit = isRoot ? 2 : minChild;
        minAllowedChild = Math.max(minAllowedChild, minChildLimit);
        maxAllowedChild = Math.min(maxAllowedChild, maxChild);

        double targetSubtree = (double) targetKeys * Math.pow(targetKeys + 1, h - 1);
        int bestChild = (int) Math.round(numKeys / targetSubtree);
        int C = Math.clamp(bestChild, minAllowedChild, maxAllowedChild);

        int baseSize = numKeys / C;
        int remainder = numKeys % C;

        BPlusTreeNode<E> node = createNode(degree, false);
        int currStart = start;

        for (int i = 0; i < C; i++) {
            int childTotalKeys = baseSize + (i < remainder ? 1 : 0);
            
            BPlusTreeNode<E> child = buildSubtree(keys, currStart, currStart + childTotalKeys - 1, h - 1, false, targetKeys, minChild, maxChild);
            node.child[i] = child;
            child.parent = node;
            
            if (i > 0) {
                BPlusTreeNode<E> leftmost = child;
                while (!leftmost.isLeaf()) {
                    leftmost = leftmost.child[0];
                }
                node.keys[i - 1] = leftmost.keys[0];
                node.keyCount++;
            }
            currStart += childTotalKeys;
        }
        return node;
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
                    current = parent;
                }
                return true;
            }
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            current = current.child[childIdx];
        }
    }

    private void splitNode(BPlusTreeNode<E> parent, int childIdx, BPlusTreeNode<E> child) {
        BPlusTreeNode<E> sibling = createNode(degree, child.isLeaf());
        if (child.isLeaf()) {
            sibling.keyCount = degree;
            System.arraycopy(child.keys, degree, sibling.keys, 0, degree);
            Arrays.fill(child.keys, degree, child.keyCount, null);
            child.keyCount = degree;

            BPlusTreeNode<E> childNext = child.next;
            sibling.next = childNext;

            if (childNext != null) childNext.prev = sibling;

            sibling.prev = child;
            child.next = sibling;

            System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
            parent.setChild(childIdx + 1, sibling);
            System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);

            parent.keys[childIdx] = sibling.keys[0];

        } else {
            sibling.keyCount = degree;
            System.arraycopy(child.keys, degree, sibling.keys, 0, degree);

            System.arraycopy(child.child, degree, sibling.child, 0, degree + 1);
            for (int i = 0; i <= degree; i++) {
                if (sibling.child[i] != null) sibling.child[i].parent = sibling;
            }

            Arrays.fill(child.child, degree, child.keyCount + 1, null);
            Arrays.fill(child.keys, degree, child.keyCount, null);

            child.keyCount = degree - 1;

            System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
            parent.setChild(childIdx + 1, sibling);

            System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);

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

        int idx = searchNode(current, e);
        if (idx < 0) return false;

        System.arraycopy(current.keys, idx + 1, current.keys, idx, current.keyCount - idx - 1);
        current.keys[current.keyCount - 1] = null;
        current.keyCount--;
        size--;
        modCount++;

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

            System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);
            left.keyCount += right.keyCount;

            BPlusTreeNode<E> rightNext = right.next;
            left.next = rightNext;
            if (rightNext != null) {
                rightNext.prev = left;
            }
        }
        else {

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
        if (root == null || o == null) {
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
            for (int i = 0; i < current.keyCount; i++) {
                action.accept((E) current.keys[i]);
                if (expectedModCount != modCount) throw new ConcurrentModificationException();
            }
            current = current.next;
        }
    }

    //You can use this or identify as exportFlatMatrix();
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        if (size == 0 || root == null) return array;
        BPlusTreeNode<E> current = root;
        while (!current.isLeaf()) {
            current = current.child[0];
        }

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

    public static final class Builder<E> {
        private int degree = DEFAULT_DEGREE;
        private Comparator<? super E> comparator = null;
        private float factor = 0.75f;

        private Object[] flatArray = null;
        private Iterator<? extends E> sortedIterator = null;
        private Collection<? extends E> collection = null;

        private Builder() {
        }

        public static <E> BPlusTreeSet.Builder<E> newBuilder() {
            return new BPlusTreeSet.Builder<>();
        }

        public static <E> BPlusTreeSet.Builder<E> create(int degree) {
            return BPlusTreeSet.Builder.<E>newBuilder().degree(degree);
        }

        public BPlusTreeSet.Builder<E> degree(int degree) {
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
            this.sortedIterator = null;
            this.collection = null;
            return this;
        }

        public BPlusTreeSet.Builder<E> importSorted(Iterator<? extends E> iterator) {
            this.sortedIterator = iterator;
            this.flatArray = null;
            this.collection = null;
            return this;
        }

        public BPlusTreeSet.Builder<E> importCollection(Collection<? extends E> c) {
            this.collection = c;
            this.flatArray = null;
            this.sortedIterator = null;
            return this;
        }

        public BPlusTreeSet<E> build() {
            return new BPlusTreeSet<>(this);
        }
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
