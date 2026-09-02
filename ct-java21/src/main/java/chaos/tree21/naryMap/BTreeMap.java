package chaos.tree21.naryMap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public final class BTreeMap<K, V> extends AbstractNaryTreeMap<K, V, BTreeMapNode<K, V>> {

    @Override
    BTreeMapNode<K, V> createNode(int degree, boolean isLeaf) {
        return new BTreeMapNode<>(degree, isLeaf);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        if (root == null){
            compare(key,key);
            root = createNode(degree, true);
            root.keys[0]= key;
            root.values[0] = value;
            size++;
            modCount++;
            return null;
        }
        BTreeMapNode<K,V> current = root;
        V old_val = null;
        while (true){
            int idx = searchNodeMap(current, key);

            if(idx>=0){
                old_val = (V) current.values[idx];
                current.values[idx] = value;
                return old_val;
            }
            int childIdx = ~idx;
            if(current.isLeaf()){
                System.arraycopy(current.keys, childIdx, current.keys, childIdx+1,current.keyCount-childIdx);
                System.arraycopy(current.values, childIdx, current.values, childIdx+1,current.keyCount-childIdx);
                current.keys[childIdx] = key;
                current.values[childIdx] = value;
                current.keyCount++;
                size++;
                modCount++;
                while (current.keyCount>maxKeys){
                    if (current == root){
                        BTreeMapNode<K,V> n_root = createNode(degree,false);
                        n_root.setChild(0,root);
                        splitNode(n_root,0,root);
                        root = n_root;
                        break;
                    }
                    BTreeMapNode<K,V> parent = current.parent;
                    K k = (K) current.keys[0];
                    int pIdx = ~searchNodeMap(parent, k);
                    splitNode(parent, pIdx, current);
                    current = parent;
                }
                return old_val;
            }
            current = current.child[childIdx];
        }
    }

    private void splitNode(BTreeMapNode<K,V> parent, int childIdx, BTreeMapNode<K,V> child) {
        BTreeMapNode<K,V> sibling = new BTreeMapNode<>(degree, child.isLeaf());
        sibling.keyCount = degree;
        System.arraycopy(child.keys, degree, sibling.keys, 0, degree);
        System.arraycopy(child.values,degree,sibling.values,0,degree);
        if (!child.isLeaf()) {
            System.arraycopy(child.child, degree, sibling.child, 0, degree + 1);
            for (int i = 0; i <= degree; i++) {
                if (sibling.child[i] != null) sibling.child[i].parent = sibling;
            }
            Arrays.fill(child.child, degree, child.keyCount + 1, null);
        }
        Arrays.fill(child.keys, degree, child.keyCount, null);
        Arrays.fill(child.values,degree,child.keyCount,null);
        child.keyCount = degree - 1;
        System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
        parent.setChild(childIdx + 1, sibling);

        System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);
        System.arraycopy(parent.values, childIdx, parent.values, childIdx + 1, parent.keyCount - childIdx);

        parent.keys[childIdx] = child.keys[degree - 1];
        parent.values[childIdx] = child.values[degree -1];
        child.keys[degree - 1] = null;
        child.values[degree - 1] = null;

        parent.keyCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        if (root == null) return null;

        BTreeMapNode<K,V> current = root;
        int idx = -1;
        K k = (K) key;
        V old_val = null;
        while (true) {
            idx = searchNodeMap(current, k);
            if (idx >= 0) break;
            if (current.isLeaf()) return null;
            current = current.child[~idx];
        }
        if(!current.isLeaf()){
            BTreeMapNode<K,V> predLeaf = getPredecessorLeaf(current,idx);
            K predKey = (K) predLeaf.keys[predLeaf.keyCount-1];
            V predValue = (V) predLeaf.values[predLeaf.keyCount -1];
            current.keys[idx] = predKey;
            current.values[idx] = predValue;
            current = predLeaf;
            idx = current.keyCount-1;
        }

        old_val = (V) current.values[idx];

        System.arraycopy(current.keys, idx + 1, current.keys, idx, current.keyCount - idx - 1);
        System.arraycopy(current.values, idx + 1, current.values, idx, current.keyCount - idx - 1);

        current.keys[current.keyCount - 1] = null;
        current.values[current.keyCount -1] = null;
        current.keyCount--;
        size--;
        modCount++;

        while (current != root && current.keyCount < minKeys){
            BTreeMapNode<K,V> parent = current.parent;
            int childIdx =0;
            while (childIdx <= parent.keyCount && parent.child[childIdx] != current) {
                childIdx++;
            }
            BTreeMapNode<K,V> leftSibling = (childIdx>0) ? parent.child[childIdx -1]:null;
            BTreeMapNode<K,V> rightSibling = (childIdx<parent.keyCount)?parent.child[childIdx+1]:null;

            if (leftSibling != null && leftSibling.keyCount > minKeys) {
                borrowLeft(parent, childIdx, leftSibling, current);
                break;
            } else if (rightSibling != null && rightSibling.keyCount > minKeys) {
                borrowRight(parent, childIdx, current, rightSibling);
                break;
            } else {
                if (leftSibling != null) {
                    mergeNodes(parent, childIdx - 1, leftSibling, current);
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
        return old_val;
    }

    private void borrowLeft(BTreeMapNode<K,V> parent, int childIdx, BTreeMapNode<K,V> sibling, BTreeMapNode<K,V> starving) {
        System.arraycopy(starving.keys, 0, starving.keys, 1, starving.keyCount);
        System.arraycopy(starving.values, 0, starving.values, 1, starving.keyCount);
        if (!starving.isLeaf()) {
            System.arraycopy(starving.child, 0, starving.child, 1, starving.keyCount + 1);
        }

        starving.keys[0] = parent.keys[childIdx - 1];
        starving.values[0] = parent.values[childIdx -1];

        if (!starving.isLeaf()) {
            starving.child[0] = sibling.child[sibling.keyCount];
            if (starving.child[0] != null) {
                starving.child[0].parent = starving;
            }
            sibling.child[sibling.keyCount] = null;
        }

        parent.keys[childIdx - 1] = sibling.keys[sibling.keyCount - 1];
        parent.values[childIdx -1] = sibling.values[sibling.keyCount -1];
        sibling.keys[sibling.keyCount - 1] = null;
        sibling.values[sibling.keyCount -1] = null;

        sibling.keyCount--;
        starving.keyCount++;
    }

    private void borrowRight(BTreeMapNode<K,V> parent, int childIdx, BTreeMapNode<K,V> starving, BTreeMapNode<K,V> sibling) {
        starving.keys[starving.keyCount] = parent.keys[childIdx];
        starving.values[starving.keyCount] = parent.values[childIdx];

        if (!starving.isLeaf()) {
            starving.child[starving.keyCount + 1] = sibling.child[0];
            if (starving.child[starving.keyCount + 1] != null) {
                starving.child[starving.keyCount + 1].parent = starving;
            }
        }

        parent.keys[childIdx] = sibling.keys[0];
        parent.values[childIdx] = sibling.values[0];

        System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyCount - 1);
        System.arraycopy(sibling.values, 1, sibling.values, 0, sibling.keyCount - 1);

        sibling.keys[sibling.keyCount - 1] = null;
        sibling.values[sibling.keyCount -1] = null;

        if (!sibling.isLeaf()) {
            System.arraycopy(sibling.child, 1, sibling.child, 0, sibling.keyCount);
            sibling.child[sibling.keyCount] = null; // GC
        }

        starving.keyCount++;
        sibling.keyCount--;
    }

    private BTreeMapNode<K,V> getPredecessorLeaf(BTreeMapNode<K,V> node, int childIdx) {
        BTreeMapNode<K,V> current = node.child[childIdx];
        while (!current.isLeaf()) {
            current = current.child[current.keyCount];
        }
        return current;
    }

    private void mergeNodes(BTreeMapNode<K,V> parent, int childIdx, BTreeMapNode<K,V> left, BTreeMapNode<K,V> right) {
        left.keys[left.keyCount] = parent.keys[childIdx];
        left.values[left.keyCount++] = parent.values[childIdx];

        System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);
        System.arraycopy(right.values, 0, left.values, left.keyCount, right.keyCount);

        if (!left.isLeaf()) {
            System.arraycopy(right.child, 0, left.child, left.keyCount, right.keyCount + 1);
            for (int i = 0; i <= right.keyCount; i++) {
                if (right.child[i] != null) right.child[i].parent = left;
            }
        }

        left.keyCount += right.keyCount;


        System.arraycopy(parent.keys, childIdx + 1, parent.keys, childIdx, parent.keyCount - childIdx - 1);
        System.arraycopy(parent.values, childIdx + 1, parent.values, childIdx, parent.keyCount - childIdx - 1);
        parent.keys[parent.keyCount - 1] = null;
        parent.values[parent.keyCount -1] = null;

        System.arraycopy(parent.child, childIdx + 2, parent.child, childIdx + 1, parent.keyCount - childIdx - 1);
        parent.child[parent.keyCount] = null;

        parent.keyCount--;
    }


    @SuppressWarnings("unchecked")
    void buildFromSorted(Iterator<Entry<K, V>> it, float factor) {
        int targetKeys = Math.max(minKeys, (int) (maxKeys * factor));
        BTreeMapNode<K, V>[] rightEdge = (BTreeMapNode<K, V>[]) new BTreeMapNode[32];

        rightEdge[0] = createNode(degree, true);
        this.root = rightEdge[0];

        while (it.hasNext()) {
            BTreeMapNode<K, V> leaf = rightEdge[0];
            while (leaf.keyCount < targetKeys && it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                leaf.keys[leaf.keyCount] = entry.getKey();
                leaf.values[leaf.keyCount] = entry.getValue();
                leaf.keyCount++;
                this.size++;
            }

            if (it.hasNext()) {
                Map.Entry<K, V> sepEntry = it.next();
                this.size++;

                int level = 1;
                while (true) {
                    BTreeMapNode<K, V> parent = rightEdge[level];

                    if (parent == null) {
                        parent = createNode(degree, false);
                        parent.setChild(0, rightEdge[level - 1]);
                        rightEdge[level - 1].parent = parent;
                        rightEdge[level] = parent;
                        this.root = parent;
                    }

                    if (parent.keyCount < targetKeys) {
                        parent.keys[parent.keyCount] = sepEntry.getKey();
                        parent.values[parent.keyCount] = sepEntry.getValue();
                        parent.keyCount++;

                        BTreeMapNode<K, V> prevInternal = parent;
                        for (int d = level - 1; d >= 0; d--) {
                            BTreeMapNode<K, V> newNode = createNode(degree, d == 0);
                            prevInternal.setChild(prevInternal.keyCount, newNode);
                            newNode.parent = prevInternal;

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
        this.modCount++;
    }

    @SuppressWarnings("unchecked")
    void buildFromSortedArrays(Object[][] blast, float factor) {
        Object[] inKeys = blast[0];
        Object[] inValues = blast[1];
        int totalSize = inKeys.length;
        if (totalSize == 0) return;

        int targetKeys = Math.max(minKeys, (int) (maxKeys * factor));
        BTreeMapNode<K, V>[] rightEdge = (BTreeMapNode<K, V>[]) new BTreeMapNode[32];

        rightEdge[0] = createNode(degree, true);
        this.root = rightEdge[0];

        int i = 0;
        while (i < totalSize) {
            BTreeMapNode<K, V> leaf = rightEdge[0];
            int chunk = Math.min(targetKeys - leaf.keyCount, totalSize - i);
            System.arraycopy(inKeys, i, leaf.keys, leaf.keyCount, chunk);
            System.arraycopy(inValues, i, leaf.values, leaf.keyCount, chunk);
            leaf.keyCount += chunk;
            i += chunk;
            if (i < totalSize) {
                K sepKey = (K) inKeys[i];
                V sepVal = (V) inValues[i];
                i++;

                int level = 1;
                while (true) {
                    BTreeMapNode<K, V> parent = rightEdge[level];
                    if (parent == null) {
                        parent = createNode(degree, false);
                        parent.setChild(0, rightEdge[level - 1]);
                        rightEdge[level - 1].parent = parent;
                        rightEdge[level] = parent;
                        this.root = parent;
                    }

                    if (parent.keyCount < targetKeys) {
                        parent.keys[parent.keyCount] = sepKey;
                        parent.values[parent.keyCount] = sepVal;
                        parent.keyCount++;

                        BTreeMapNode<K, V> prevInternal = parent;
                        for (int d = level - 1; d >= 0; d--) {
                            BTreeMapNode<K, V> newNode = createNode(degree, d == 0);
                            prevInternal.setChild(prevInternal.keyCount, newNode);
                            newNode.parent = prevInternal;

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
        this.size = totalSize;
        this.modCount++;
    }

    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
        BTreeMapNode<K, V> curr = root;
        BTreeMapNode<K, V> bestNode = null;
        int bestIdx = -1;

        while (curr != null) {
            int idx = searchNodeMap(curr, key);
            if (idx >= 0) return exportEntry(curr, idx);

            int insertIdx = ~idx;
            if (insertIdx < curr.keyCount) {
                bestNode = curr;
                bestIdx = insertIdx;
            }
            curr = curr.isLeaf() ? null : curr.child[insertIdx];
        }
        return exportEntry(bestNode, bestIdx);
    }

    @Override
    public Map.Entry<K, V> floorEntry(K key) {
        BTreeMapNode<K, V> curr = root;
        BTreeMapNode<K, V> bestNode = null;
        int bestIdx = -1;

        while (curr != null) {
            int idx = searchNodeMap(curr, key);
            if (idx >= 0) return exportEntry(curr, idx);

            int insertIdx = ~idx;
            if (insertIdx > 0) {
                bestNode = curr;
                bestIdx = insertIdx - 1;
            }
            curr = curr.isLeaf() ? null : curr.child[insertIdx];
        }
        return exportEntry(bestNode, bestIdx);
    }

    @Override
    public Map.Entry<K, V> higherEntry(K key) {
        BTreeMapNode<K, V> curr = root;
        BTreeMapNode<K, V> bestNode = null;
        int bestIdx = -1;

        while (curr != null) {
            int idx = searchNodeMap(curr, key);
            if (idx >= 0) {
                if (!curr.isLeaf()) {
                    curr = curr.child[idx + 1];
                    while (!curr.isLeaf()) curr = curr.child[0];
                    return exportEntry(curr, 0);
                }
                return exportEntry(bestNode, bestIdx);
            }

            int insertIdx = ~idx;
            if (insertIdx < curr.keyCount) {
                bestNode = curr;
                bestIdx = insertIdx;
            }
            curr = curr.isLeaf() ? null : curr.child[insertIdx];
        }
        return exportEntry(bestNode, bestIdx);
    }

    @Override
    public Map.Entry<K, V> lowerEntry(K key) {
        BTreeMapNode<K, V> curr = root;
        BTreeMapNode<K, V> bestNode = null;
        int bestIdx = -1;

        while (curr != null) {
            int idx = searchNodeMap(curr, key);
            if (idx >= 0) {
                if (!curr.isLeaf()) {
                    curr = curr.child[idx];
                    while (!curr.isLeaf()) curr = curr.child[curr.keyCount];
                    return exportEntry(curr, curr.keyCount - 1);
                }
                return exportEntry(bestNode, bestIdx);
            }

            int insertIdx = ~idx;
            if (insertIdx > 0) {
                bestNode = curr;
                bestIdx = insertIdx - 1;
            }
            curr = curr.isLeaf() ? null : curr.child[insertIdx];
        }
        return exportEntry(bestNode, bestIdx);
    }

}
