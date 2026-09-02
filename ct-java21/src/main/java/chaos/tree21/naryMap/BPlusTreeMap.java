package chaos.tree21.naryMap;

import java.util.Arrays;

public final class BPlusTreeMap<K, V> extends AbstractNaryTreeMap<K, V, BPlusTreeMapNode<K,V>> {

    BPlusTreeMapNode<K,V> createNode(int degree, boolean isLeaf){
        return new BPlusTreeMapNode<>(degree, isLeaf);
    }
    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        if (root == null){
            compare(key, key);
            root = new BPlusTreeMapNode<>(degree,true);
            root.keys[0] = key;
            root.values[0] = value;
            size++;
            modCount++;
            return null;
        }
        BPlusTreeMapNode<K, V> curr = root;
        while (true){
            int idx = searchNodeMap(curr, key);

            if(curr.isLeaf()){
                if (idx>=0){
                    V oldValue = (V) curr.values[idx];
                    curr.values[idx] = value;
                    return oldValue;
                }
                int insertIdx = ~idx;
                System.arraycopy(curr.keys, insertIdx, curr.keys, insertIdx+1, curr.keyCount - insertIdx);
                System.arraycopy(curr.values, insertIdx, curr.values, insertIdx+1, curr.keyCount - insertIdx);
                curr.keys[insertIdx] = key;
                curr.values[insertIdx] = value;
                curr.keyCount++;
                size++;
                modCount++;

                while (curr.keyCount > maxKeys){
                    if(curr == root){
                        BPlusTreeMapNode<K, V> n_root = createNode(degree,false);
                        n_root.setChild(0,root);
                        splitNode(n_root, 0, root);
                        root = n_root;
                        break;
                    }
                    BPlusTreeMapNode<K,V> parent = curr.parent;
                    idx = searchNodeMap(parent, (K) curr.keys[0]);
                    int childIdx = (idx >=0)?idx+1:~idx;
                    splitNode(parent,childIdx,curr);
                    curr = parent;
                }
                return null;
            }
            int childIdx = (idx>=0)? idx+1: ~idx;
            curr = curr.child[childIdx];
        }
    }
    private void splitNode(BPlusTreeMapNode<K,V> parent, int childIdx, BPlusTreeMapNode<K,V> child){
        BPlusTreeMapNode<K,V> sibling = createNode(degree, child.isLeaf());
        if (child.isLeaf()){
            sibling.keyCount = degree;

            System.arraycopy(child.keys,degree,sibling.keys,0,degree);
            System.arraycopy(child.values,degree,sibling.values,0,degree);

            Arrays.fill(child.keys,degree,child.keyCount,null);
            Arrays.fill(child.values,degree,child.keyCount,null);

            child.keyCount = degree;
            BPlusTreeMapNode<K,V> childNext = child.next;

            sibling.next = childNext;
            if(childNext!=null) childNext.prev = sibling;
            sibling.prev=child;
            child.next= sibling;

            System.arraycopy(parent.child,childIdx+1,parent.child,childIdx+2,parent.keyCount-childIdx);
            parent.setChild(childIdx+1,sibling);
            System.arraycopy(parent.keys,childIdx,parent.keys,childIdx+1,parent.keyCount-childIdx);

            parent.keys[childIdx] = sibling.keys[0];
        }
        else {
            sibling.keyCount = degree;
            System.arraycopy(child.keys,degree,sibling.keys,0,degree);
            System.arraycopy(child.child,degree,sibling.child,0,degree+1);
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
}
