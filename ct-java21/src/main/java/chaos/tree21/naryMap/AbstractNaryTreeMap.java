package chaos.tree21.naryMap;

import chaos.tree21.core.SearchTreeMap;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

sealed abstract class AbstractNaryTreeMap<K, V, N extends AbstractNaryMapNode<K, V, N>>
        extends AbstractMap<K, V> implements SearchTreeMap<K, V> permits BTreeMap, BPlusTreeMap {

    protected final Comparator<? super K> comparator;

    protected final transient int degree;
    protected final transient int maxKeys;
    protected final transient int minKeys;
    protected transient N root;
    protected transient int size;
    protected transient long modCount;
    protected transient Set<Entry<K, V>> entrySet;
    protected abstract Iterator<Map.Entry<K, V>> entryIterator();

    protected AbstractNaryTreeMap(int degree, Comparator<? super K> comparator){
        this.comparator = comparator;
        if (degree < 2 || degree > Integer.MAX_VALUE / 2) {
            throw new IllegalArgumentException("Degree must be at least 2 and less than Integer.MAX_VALUE/2");
        }
        this.degree = degree;
        this.maxKeys = (degree << 1) - 1;
        this.minKeys = degree - 1;
    }

    @SuppressWarnings("unchecked")
    protected int compare(K k1, K k2) {
        if (comparator != null) {
            return comparator.compare(k1, k2);
        }
        return ((Comparable<? super K>) k1).compareTo(k2);
    }

    @SuppressWarnings("unchecked")
    protected int searchNodeMap(N current, K k){
        if(current.keyCount < 12){
            for (int i = 0; i < current.keyCount; i++) {
                int cmp = compare((K)current.keys[i], k);
                if(cmp == 0) return i;
                if(cmp > 0) return ~i;
            }
            return ~current.keyCount;
        }
        return Arrays.binarySearch((K[]) current.keys, 0, current.keyCount, k, comparator);
    }

    protected int searchNodeMapValue(N current, V v){
        for (int i = 0; i < current.keyCount; i++){
            if(Objects.equals(current.values[i], v)) return i;
        }
        return -1;
    }

    abstract N createNode(int degree, boolean isLeaf);

    abstract void buildFromSorted(Iterator<Entry<K, V>> it, float factor);

    abstract void buildFromSortedArrays(Object[][] blast, float factor);

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        if (root == null) return null;
        K k = (K) key;
        N current = root;
        //These comment are part of insight so that I do not forget it.
        //It was done to ensure heavy lifting and miscellaneous API here.
        while (current != null) {
            int idx = searchNodeMap(current, k);

            if (idx >= 0) {
                // the chaos TRICK:
                // If values != null, it's either a B-Tree node or a B+Tree leaf. Data is here!
                if (current.values != null) {
                    return (V) current.values[idx];
                }
                // It's a B+Tree internal routing node. Route to the right child!
                current = current.child[idx + 1];
            } else {
                // Key not found in this node. Drop down the left-side child pointer.
                if (current.isLeaf()) return null;
                current = current.child[~idx];
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        if (root == null) return false;
        K k = (K) key;
        N current = root;

        while (current != null) {
            int idx = searchNodeMap(current, k);

            if (idx >= 0) {
                if (current.values != null) return true;
                current = current.child[idx + 1];
            } else {
                if (current.isLeaf()) return false;
                current = current.child[~idx];
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K firstKey() {
        if (root == null) throw new NoSuchElementException();
        N current = root;
        while (!current.isLeaf()) {
            current = current.child[0];
        }
        return (K) current.keys[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public K lastKey() {
        if (root == null) throw new NoSuchElementException();
        N current = root;
        while (!current.isLeaf()) {
            current = current.child[current.keyCount];
        }
        return (K) current.keys[current.keyCount - 1];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
        modCount++;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V value = get(key);
        return (value != null || containsKey(key)) ? value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    protected Map.Entry<K, V> exportEntry(N node, int index) {
        if (node == null || index < 0 || index >= node.keyCount) return null;
        return new AbstractMap.SimpleImmutableEntry<>(
                (K) node.keys[index],
                (V) node.values[index]
        );
    }

    // NavigableMap exact-match and boundary hooks
    @Override
    public Map.Entry<K, V> firstEntry() {
        if (root == null) return null;
        N current = root;
        while (!current.isLeaf()) current = current.child[0];
        return exportEntry(current, 0);
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
        if (root == null) return null;
        N current = root;
        while (!current.isLeaf()) current = current.child[current.keyCount];
        return exportEntry(current, current.keyCount - 1);
    }

    // These implementation will be part from concrete classes.
    @Override public abstract Map.Entry<K, V> ceilingEntry(K key);
    @Override public abstract Map.Entry<K, V> floorEntry(K key);
    @Override public abstract Map.Entry<K, V> higherEntry(K key);
    @Override public abstract Map.Entry<K, V> lowerEntry(K key);

    @Override public K ceilingKey(K key) {
        Map.Entry<K, V> e = ceilingEntry(key); return e == null ? null : e.getKey();
    }
    @Override public K floorKey(K key) {
        Map.Entry<K, V> e = floorEntry(key); return e == null ? null : e.getKey();
    }
    @Override public K higherKey(K key) {
        Map.Entry<K, V> e = higherEntry(key); return e == null ? null : e.getKey();
    }
    @Override public K lowerKey(K key) {
        Map.Entry<K, V> e = lowerEntry(key); return e == null ? null : e.getKey();
    }

    final class ChaosEntry implements Map.Entry<K, V> {
        private final K key;
        private V value;
        private final N node;
        private final int index;
        private final long expectedModCount;

        ChaosEntry(K key, V value, N node, int index, long expectedModCount) {
            this.key = key;
            this.value = value;
            this.node = node;
            this.index = index;
            this.expectedModCount = expectedModCount;
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
        public V setValue(V newValue) {
            if (AbstractNaryTreeMap.this.modCount != expectedModCount) {
                throw new ConcurrentModificationException(
                        "Tree was structurally modified. This Entry pointer is stale."
                );
            }

            V oldValue = this.value;
            this.value = newValue;
            this.node.values[index] = newValue;
            return oldValue;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry<?, ?> e)) return false;
            return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
        }
        @Override
        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }
        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

}
