package chaos.tree21.naryMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;

public final class BPlusTreeMap<K, V> extends AbstractNaryTreeMap<K, V, BPlusTreeMapNode<K, V>> {


    private static final int DEFAULT_DEGREE = 64;

    public BPlusTreeMap() {
        super(DEFAULT_DEGREE, null);
    }

    public BPlusTreeMap(Comparator<? super K> comparator) {
        super(DEFAULT_DEGREE, comparator);
    }

    public BPlusTreeMap(Map<? extends K, ? extends V> m) {
        super(DEFAULT_DEGREE, null);
        putAll(m);
    }

    public BPlusTreeMap(SortedMap<K, ? extends V> m) {
        super(DEFAULT_DEGREE, null);
        buildFromSorted(m.entrySet().iterator(), 0.9f);
    }
    public BPlusTreeMap(int degree){
        super(degree,null);
    }

    @Override
    BPlusTreeMapNode<K, V> createNode(int degree, boolean isLeaf) {
        return new BPlusTreeMapNode<>(degree, isLeaf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        if (root == null) {
            compare(key, key);
            root = new BPlusTreeMapNode<>(degree, true);
            root.keys[0] = key;
            root.values[0] = value;
            root.keyCount = 1;
            size++;
            modCount++;
            return null;
        }
        BPlusTreeMapNode<K, V> curr = root;
        while (true) {
            int idx = searchNodeMap(curr, key);

            if (curr.isLeaf()) {
                if (idx >= 0) {
                    V oldValue = (V) curr.values[idx];
                    curr.values[idx] = value;
                    return oldValue;
                }
                int insertIdx = ~idx;
                System.arraycopy(curr.keys, insertIdx, curr.keys, insertIdx + 1, curr.keyCount - insertIdx);
                System.arraycopy(curr.values, insertIdx, curr.values, insertIdx + 1, curr.keyCount - insertIdx);
                curr.keys[insertIdx] = key;
                curr.values[insertIdx] = value;
                curr.keyCount++;
                size++;
                modCount++;

                while (curr.keyCount > maxKeys) {
                    if (curr == root) {
                        BPlusTreeMapNode<K, V> n_root = createNode(degree, false);
                        n_root.setChild(0, root);
                        splitNode(n_root, 0, root);
                        root = n_root;
                        break;
                    }
                    BPlusTreeMapNode<K, V> parent = curr.parent;
                    idx = searchNodeMap(parent, (K) curr.keys[0]);
                    int childIdx = (idx >= 0) ? idx + 1 : ~idx;
                    splitNode(parent, childIdx, curr);
                    curr = parent;
                }
                return null;
            }
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            curr = curr.child[childIdx];
        }
    }

    private void splitNode(BPlusTreeMapNode<K, V> parent, int childIdx, BPlusTreeMapNode<K, V> child) {
        BPlusTreeMapNode<K, V> sibling = createNode(degree, child.isLeaf());
        if (child.isLeaf()) {
            sibling.keyCount = degree;

            System.arraycopy(child.keys, degree, sibling.keys, 0, degree);
            System.arraycopy(child.values, degree, sibling.values, 0, degree);

            Arrays.fill(child.keys, degree, child.keyCount, null);
            Arrays.fill(child.values, degree, child.keyCount, null);

            child.keyCount = degree;
            BPlusTreeMapNode<K, V> childNext = child.next;

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
            //uff..
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
    public V remove(Object o) {
        if (isEmpty()) return null;
        K key = (K) o;
        BPlusTreeMapNode<K, V> curr = root;
        int idx, childIdx;
        while (!curr.isLeaf()) {
            idx = searchNodeMap(curr, key);
            childIdx = (idx >= 0) ? idx + 1 : ~idx;
            curr = curr.child[childIdx];
        }
        idx = searchNodeMap(curr, key);
        if (idx < 0) return null;

        //Ghost deletion in leaf only
        V val = (V) curr.values[idx];
        System.arraycopy(curr.keys, idx + 1, curr.keys, idx, curr.keyCount - idx - 1);
        System.arraycopy(curr.values, idx + 1, curr.values, idx, curr.keyCount - idx - 1);

        curr.keys[curr.keyCount - 1] = null;
        curr.values[curr.keyCount - 1] = null;
        curr.keyCount--;
        size--;
        modCount++;

        while (curr != root && curr.keyCount < minKeys) {
            BPlusTreeMapNode<K, V> parent = curr.parent;

            childIdx = 0;
            while (childIdx <= parent.keyCount && parent.child[childIdx] != curr) childIdx++;

            BPlusTreeMapNode<K, V> leftSibling = (childIdx > 0) ? parent.child[childIdx - 1] : null;
            BPlusTreeMapNode<K, V> rightSibling = (childIdx < parent.keyCount) ? parent.child[childIdx + 1] : null;

            if (leftSibling != null && leftSibling.keyCount > minKeys) {
                borrowLeft(parent, childIdx, leftSibling, curr);
                break;
            } else if (rightSibling != null && rightSibling.keyCount > minKeys) {
                borrowRight(parent, childIdx, curr, rightSibling);
                break;
            } else {
                if (leftSibling != null) {
                    mergeNodes(parent, childIdx - 1, leftSibling, curr);
                    curr = parent;
                } else {
                    mergeNodes(parent, childIdx, curr, rightSibling);
                    curr = parent;
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

        return val;
    }

    private void mergeNodes(BPlusTreeMapNode<K, V> parent, int childIdx, BPlusTreeMapNode<K, V> left, BPlusTreeMapNode<K, V> right) {
        if (left.isLeaf()) {
            System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);
            System.arraycopy(right.values, 0, left.values, left.keyCount, right.keyCount);
            left.keyCount += right.keyCount;
            BPlusTreeMapNode<K, V> rightNext = right.next;
            left.next = rightNext;
            if (rightNext != null) {
                rightNext.prev = left;
            }
        } else {
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
        System.arraycopy(parent.child, childIdx + 2, parent.child, childIdx + 1, parent.keyCount - childIdx - 1);

        parent.keys[parent.keyCount - 1] = null;
        parent.child[parent.keyCount] = null;
        parent.keyCount--;
    }

    private void borrowLeft(BPlusTreeMapNode<K, V> parent, int childIdx, BPlusTreeMapNode<K, V> sibling, BPlusTreeMapNode<K, V> starving) {
        if (starving.isLeaf()) {
            System.arraycopy(starving.keys, 0, starving.keys, 1, starving.keyCount);
            System.arraycopy(starving.values, 0, starving.values, 1, starving.keyCount);

            starving.keys[0] = sibling.keys[sibling.keyCount - 1];
            starving.values[0] = sibling.values[sibling.keyCount - 1];

            sibling.keys[sibling.keyCount - 1] = null;
            sibling.values[sibling.keyCount - 1] = null;

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

    private void borrowRight(BPlusTreeMapNode<K, V> parent, int childIdx, BPlusTreeMapNode<K, V> starving, BPlusTreeMapNode<K, V> sibling) {
        if (starving.isLeaf()) {
            starving.keys[starving.keyCount] = sibling.keys[0];
            starving.values[starving.keyCount] = sibling.values[0];

            System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyCount - 1);
            System.arraycopy(sibling.values, 1, sibling.values, 0, sibling.keyCount - 1);

            sibling.keys[sibling.keyCount - 1] = null;
            sibling.values[sibling.keyCount - 1] = null;

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
            System.arraycopy(sibling.child, 1, sibling.child, 0, sibling.keyCount);

            sibling.keys[sibling.keyCount - 1] = null;
            sibling.child[sibling.keyCount] = null;

            starving.keyCount++;
            sibling.keyCount--;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    void buildFromSorted(Iterator<? extends Map.Entry<? extends K, ? extends V>> it, float factor) {
        int targetKeys = Math.max(minKeys, (int) (maxKeys * factor));
        BPlusTreeMapNode<K, V>[] rightEdge = (BPlusTreeMapNode<K, V>[]) new BPlusTreeMapNode[32];
        rightEdge[0] = createNode(degree, true);
        this.root = rightEdge[0];

        while (it.hasNext()) {
            Map.Entry<? extends K, ? extends V> entry = it.next();
            K key = entry.getKey();
            V value = entry.getValue();

            BPlusTreeMapNode<K, V> leaf = rightEdge[0];

            if (leaf.keyCount < targetKeys) {
                leaf.keys[leaf.keyCount] = key;
                leaf.values[leaf.keyCount] = value;
                leaf.keyCount++;
                this.size++;
            } else {
                BPlusTreeMapNode<K, V> newLeaf = createNode(degree, true);
                leaf.next = newLeaf;
                newLeaf.prev = leaf;

                newLeaf.keys[0] = key;
                newLeaf.values[0] = value;
                newLeaf.keyCount = 1;
                this.size++;

                K routingKey;
                routingKey = key;
                BPlusTreeMapNode<K, V> leftChild = leaf;
                BPlusTreeMapNode<K, V> rightChild = newLeaf;

                rightEdge[0] = newLeaf;

                int level = 1;
                while (true) {
                    BPlusTreeMapNode<K, V> parent = rightEdge[level];

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
                        BPlusTreeMapNode<K, V> newInternal = createNode(degree, false);
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
        this.modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    void buildFromSortedArrays(Object[][] blast, float factor) {
        Object[] inKeys = blast[0];
        Object[] inValues = blast[1];
        int totalSize = inKeys.length;

        if (totalSize == 0) return;

        int targetKeys = Math.max(minKeys, (int) (maxKeys * factor));
        BPlusTreeMapNode<K, V>[] rightEdge = (BPlusTreeMapNode<K, V>[]) new BPlusTreeMapNode[32];

        int i = 0;
        while (i < totalSize) {
            int chunk = Math.min(targetKeys, totalSize - i);
            BPlusTreeMapNode<K, V> leaf = createNode(degree, true);
            System.arraycopy(inKeys, i, leaf.keys, 0, chunk);
            System.arraycopy(inValues, i, leaf.values, 0, chunk);
            leaf.keyCount = chunk;

            if (i == 0) {
                rightEdge[0] = leaf;
                this.root = leaf;
            } else {
                BPlusTreeMapNode<K, V> prevLeaf = rightEdge[0];
                prevLeaf.next = leaf;
                leaf.prev = prevLeaf;
                K routingKey = (K) inKeys[i];
                BPlusTreeMapNode<K, V> leftChild = prevLeaf;
                BPlusTreeMapNode<K, V> rightChild = leaf;

                rightEdge[0] = leaf;

                int level = 1;
                while (true) {
                    BPlusTreeMapNode<K, V> parent = rightEdge[level];

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
                        BPlusTreeMapNode<K, V> newInternal = createNode(degree, false);
                        newInternal.setChild(0, rightChild);
                        rightChild.parent = newInternal;
                        rightEdge[level] = newInternal;

                        leftChild = parent;
                        rightChild = newInternal;
                        level++;
                    }
                }
            }
            i += chunk;
        }

        this.size = totalSize;
        this.modCount++;
    }

    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
        if (root == null) return null;
        BPlusTreeMapNode<K, V> curr = root;
        while (!curr.isLeaf()) {
            int idx = searchNodeMap(curr, key);
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            curr = curr.child[childIdx];
        }
        int idx = searchNodeMap(curr, key);
        if (idx >= 0) {
            return exportEntry(curr, idx);
        }
        int insertIdx = ~idx;
        if (insertIdx < curr.keyCount) {
            return exportEntry(curr, insertIdx);
        }
        if (curr.next != null) {
            return exportEntry(curr.next, 0);
        }
        return null;
    }

    @Override
    public Map.Entry<K, V> floorEntry(K key) {
        if (root == null) return null;
        BPlusTreeMapNode<K, V> curr = root;
        while (!curr.isLeaf()) {
            int idx = searchNodeMap(curr, key);
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            curr = curr.child[childIdx];
        }
        int idx = searchNodeMap(curr, key);
        if (idx >= 0) {
            return exportEntry(curr, idx);
        }

        int insertIdx = ~idx;

        if (insertIdx > 0) {
            return exportEntry(curr, insertIdx - 1);
        }

        if (curr.prev != null) {
            return exportEntry(curr.prev, curr.prev.keyCount - 1);
        }

        return null;
    }

    @Override
    public Map.Entry<K, V> higherEntry(K key) {
        if (root == null) return null;
        BPlusTreeMapNode<K, V> curr = root;

        while (!curr.isLeaf()) {
            int idx = searchNodeMap(curr, key);
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            curr = curr.child[childIdx];
        }

        int idx = searchNodeMap(curr, key);
        int targetIdx = (idx >= 0) ? idx + 1 : ~idx;

        if (targetIdx < curr.keyCount) {
            return exportEntry(curr, targetIdx);
        }
        if (curr.next != null) {
            return exportEntry(curr.next, 0);
        }

        return null;
    }

    @Override
    public Map.Entry<K, V> lowerEntry(K key) {
        if (root == null) return null;
        BPlusTreeMapNode<K, V> curr = root;

        while (!curr.isLeaf()) {
            int idx = searchNodeMap(curr, key);
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            curr = curr.child[childIdx];
        }

        int idx = searchNodeMap(curr, key);
        int targetIdx = (idx >= 0) ? idx - 1 : ~idx - 1;

        if (targetIdx >= 0) {
            return exportEntry(curr, targetIdx);
        }
        if (curr.prev != null) {
            return exportEntry(curr.prev, curr.prev.keyCount - 1);
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsValue(Object value) {
        if (root == null) return false;
        BPlusTreeMapNode<K, V> curr = root;
        while (!curr.isLeaf()) curr = curr.child[0];
        while (curr != null) {
            if (searchNodeMapValue(curr, (V) value) >= 0) return true;
            curr = curr.next;
        }
        return false;
    }

    @Override
    protected Iterator<Map.Entry<K, V>> entryIterator(K fromKey, boolean fromInclusive) {
        return new BPlusTreeIterator(fromKey, fromInclusive);
    }

    @Override
    protected Iterator<Map.Entry<K, V>> descendingEntryIterator(K fromKey, boolean fromInclusive) {
        return new BPlusTreeReverseIterator(fromKey, fromInclusive);
    }

    private final class BPlusTreeIterator implements Iterator<Map.Entry<K, V>> {
        private long expectedModCount;
        private BPlusTreeMapNode<K, V> currentLeaf;
        private int currentIndex;

        BPlusTreeIterator(K startKey, boolean startInclusive) {
            this.expectedModCount = modCount;

            if (root == null) {
                this.currentLeaf = null;
                return;
            }

            if (startKey == null) {
                BPlusTreeMapNode<K, V> curr = root;
                while (!curr.isLeaf()) curr = curr.child[0];
                this.currentLeaf = curr;
                this.currentIndex = 0;
            } else {
                BPlusTreeMapNode<K, V> curr = root;
                while (!curr.isLeaf()) {
                    int idx = searchNodeMap(curr, startKey);
                    curr = curr.child[(idx >= 0) ? idx + 1 : ~idx];
                }

                int idx = searchNodeMap(curr, startKey);
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
            return currentLeaf != null && currentIndex < currentLeaf.keyCount;
        }

        private Map.Entry<K,V> lastReturned = null;
        @Override
        public Map.Entry<K, V> next() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Map.Entry<K, V> entry = new ChaosEntry(currentLeaf, currentIndex);
            lastReturned = entry;
            currentIndex++;
            if (currentIndex >= currentLeaf.keyCount) {
                currentLeaf = currentLeaf.next;
                currentIndex = 0;
            }
            return entry;
        }
        @Override
        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();

            K keyToRemove = lastReturned.getKey();
            Map.Entry<K, V> nextTarget = higherEntry(keyToRemove);
            BPlusTreeMap.this.remove(keyToRemove);
            expectedModCount = modCount;
            lastReturned = null;
            if (nextTarget == null) {
                currentLeaf = null;
            } else {
                BPlusTreeMapNode<K, V> curr = root;
                while (!curr.isLeaf()) {
                    int idx = searchNodeMap(curr, nextTarget.getKey());
                    curr = curr.child[(idx >= 0) ? idx + 1 : ~idx];
                }
                currentLeaf = curr;
                currentIndex = searchNodeMap(curr, nextTarget.getKey());
            }
        }

    }

    private final class BPlusTreeReverseIterator implements Iterator<Map.Entry<K, V>> {
        private long expectedModCount;
        private BPlusTreeMapNode<K, V> currentLeaf;
        private int currentIndex;

        BPlusTreeReverseIterator(K startKey, boolean startInclusive) {
            this.expectedModCount = modCount;
            if (root == null) return;

            if (startKey == null) {
                BPlusTreeMapNode<K, V> curr = root;
                while (!curr.isLeaf()) curr = curr.child[curr.keyCount];
                this.currentLeaf = curr;
                this.currentIndex = curr.keyCount - 1;
            } else {
                BPlusTreeMapNode<K, V> curr = root;
                while (!curr.isLeaf()) {
                    int idx = searchNodeMap(curr, startKey);
                    curr = curr.child[(idx >= 0) ? idx + 1 : ~idx];
                }

                int idx = searchNodeMap(curr, startKey);
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
            return currentLeaf != null && currentIndex >= 0;
        }

        Map.Entry<K,V> lastReturned = null;
        @Override
        public Map.Entry<K, V> next() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (!hasNext()) throw new NoSuchElementException();

            Map.Entry<K, V> entry = new ChaosEntry(currentLeaf, currentIndex);
            lastReturned = entry;
            currentIndex--;
            if (currentIndex < 0) {
                currentLeaf = currentLeaf.prev;
                if (currentLeaf != null) currentIndex = currentLeaf.keyCount - 1;
            }
            return entry;
        }

        @Override
        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();

            K keyToRemove = lastReturned.getKey();
            Map.Entry<K, V> nextTarget = higherEntry(keyToRemove);
            BPlusTreeMap.this.remove(keyToRemove);
            expectedModCount = modCount;
            lastReturned = null;
            if (nextTarget == null) {
                currentLeaf = null;
            } else {
                BPlusTreeMapNode<K, V> curr = root;
                while (!curr.isLeaf()) {
                    int idx = searchNodeMap(curr, nextTarget.getKey());
                    curr = curr.child[(idx >= 0) ? idx + 1 : ~idx];
                }
                currentLeaf = curr;
                currentIndex = searchNodeMap(curr, nextTarget.getKey());
            }
        }

    }
}
