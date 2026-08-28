package chaos.tree21.binaryMap;

import java.util.Map;
import java.util.Objects;

sealed abstract class AbstractBinaryMapNode<K, V, N extends AbstractBinaryMapNode<K, V, N>>
        implements BinaryMapNode<K, V, N>, Map.Entry<K, V> permits AvlMapNode, RbtMapNode {

    private K key;
    private V value;
    private N left;
    private N right;
    private N parent;

    protected AbstractBinaryMapNode(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public void setKey(K key) {
        this.key = key;
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
    public N getLeft() {
        return left;
    }

    @Override
    public void setLeft(N left) {
        this.left = left;
    }

    @Override
    public N getRight() {
        return right;
    }

    @Override
    public void setRight(N right) {
        this.right = right;
    }

    @Override
    public N getParent() {
        return parent;
    }

    @Override
    public void setParent(N parent) {
        this.parent = parent;
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

    public void setPair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
