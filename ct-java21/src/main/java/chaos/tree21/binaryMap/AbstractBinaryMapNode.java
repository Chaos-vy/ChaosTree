package chaos.tree21.binaryMap;

import java.util.Map;
import java.util.Objects;

sealed abstract class AbstractBinaryMapNode<K, V, N extends AbstractBinaryMapNode<K, V, N>>
        implements Map.Entry<K, V> permits AvlMapNode, RbtMapNode {

    protected K key;
    protected V value;
    protected N left;
    protected N right;
    protected N parent;

    protected AbstractBinaryMapNode(K key, V value) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map.Entry<?, ?> e)) return false;
        return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
    }

    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return key + "=" + value; // Standard Java Map formatting
    }

    protected void setPair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
