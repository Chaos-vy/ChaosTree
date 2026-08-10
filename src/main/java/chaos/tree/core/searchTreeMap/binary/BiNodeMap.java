package chaos.tree.core.searchTreeMap.binary;

/**
 * Base node implementation for binary search tree map structures.
 *
 * <p>This class stores a node <key, value> pair together with references to its left and
 * right children. It is the common node abstraction used by the binary search
 * tree hierarchy in ChaosTree.</p>
 *
 * @param <K> the key to be stored
 * @param <V> the value to be stored
 * @param <N> the concrete node type
 * @since 2.0.0
 */
public abstract class BiNodeMap<K extends Comparable<? super K>,V,N extends BiNodeMap<K,V,N>>{
    private K key;
    private V value;
    private N left;
    private N right;

    //I am not writing docs anymore here.
    BiNodeMap(K key){
        this.key = key;
    }
    public void setKey(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setLeft(N left) {
        this.left = left;
    }

    public void setRight(N right) {
        this.right = right;
    }

    public N getLeft() {
        return left;
    }

    public N getRight() {
        return right;
    }
}
