package chaos.tree21.naryMap;

public final class BPlusTreeMap<K, V> extends AbstractNaryTreeMap<K, V, BPlusTreeMapNode<K,V>> {

    @Override
    public V put(K key, V value) {
        if (root == null){
            compare(key, key);
            root = new BPlusTreeMapNode<>(degree,true);
            root.keys[0] = key;
            root.values[0] = value;
            size++;
            modCount++;
            return value;
        }
        BPlusTreeMapNode<K, V> curr = root;
        while (true){

        }
    }
}
