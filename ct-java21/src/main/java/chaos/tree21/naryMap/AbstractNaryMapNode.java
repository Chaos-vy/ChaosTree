package chaos.tree21.naryMap;

import java.util.Map;
import java.util.Objects;

sealed abstract class AbstractNaryMapNode<K, V, N extends AbstractNaryMapNode<K, V, N>>
        permits BTreeMapNode, BPlusTreeMapNode {
    //First problem how do I hold the chaos of Map.Entry???
    protected final Object[] keys;
    protected final Object[] values;
    protected final N[] child;
    protected int keyCount;
    protected N parent;

    protected AbstractNaryMapNode(int degree, N[] child, boolean isLeaf) {
        int maxKeys = degree << 1;
        this.keys = new Object[maxKeys];
        boolean needsValues = isLeaf || (this instanceof BTreeMapNode);
        this.values = needsValues ? new Object[maxKeys] : null;
        this.child = child;
        this.keyCount = 0;
    }

    // Hold ChaosEntry in game!!
    static final class ChaosEntry<K, V> implements Map.Entry<K, V> {
        private final AbstractNaryMapNode<K, V, ?> node;
        private final int index;

        ChaosEntry(AbstractNaryMapNode<K, V, ?> node, int index) {
            this.node = node;
            this.index = index;
        }

        @Override
        @SuppressWarnings("unchecked")
        public K getKey() {
            return (K) node.keys[index];
        }

        @Override
        @SuppressWarnings("unchecked")
        public V getValue() {
            return (V) node.values[index];
        }

        @Override
        @SuppressWarnings("unchecked")
        public V setValue(V newValue) {
            V oldValue = (V) node.values[index];
            node.values[index] = newValue;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Map.Entry<?, ?> e)) return false;
            return Objects.equals(getKey(), e.getKey()) && Objects.equals(getValue(), e.getValue());
        }

        @Override
        public int hashCode() {
            K k = getKey();
            V v = getValue();
            return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    @SuppressWarnings("unchecked")
    void setChild(int index, N node) {
        child[index] = node;
        if (node != null) node.parent = (N) this;
    }

    boolean isLeaf() {
        return child == null;
    }
}
