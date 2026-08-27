package chaos.tree.core.searchTreeMap.binary;

import java.util.Map;

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
public abstract class BiNodeMap<K extends Comparable<? super K>, V, N extends BiNodeMap<K, V, N>> implements Map.Entry<K, V> {
    private K key;
    private V value;
    private N left;
    private N right;

    //I am not writing docs anymore here.
    BiNodeMap(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old_value = this.value;
        this.value = value;
        return old_value;
    }

    public N getLeft() {
        return left;
    }

    public void setLeft(N left) {
        this.left = left;
    }

    public N getRight() {
        return right;
    }

    public void setRight(N right) {
        this.right = right;
    }
}
