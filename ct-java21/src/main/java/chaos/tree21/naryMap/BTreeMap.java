package chaos.tree21.naryMap;

import chaos.tree21.nary.BTreeNode;

import java.util.Arrays;

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



}
