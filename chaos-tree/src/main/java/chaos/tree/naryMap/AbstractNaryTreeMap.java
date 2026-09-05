package chaos.tree.naryMap;

import chaos.tree.core.SearchTreeMap;
import chaos.tree.core.Style;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

abstract sealed class AbstractNaryTreeMap<K, V, N extends AbstractNaryMapNode<K, V, N>>
        extends AbstractMap<K, V> implements SearchTreeMap<K, V>, Serializable, Cloneable permits BTreeMap, BPlusTreeMap {

    @Serial
    private static final long serialVersionUID = 0xCAFEBABE000C4A05L;
    private static final String RESET = "\u001B[0m";
    private static final String KEY = "\u001B[1;38;2;0;229;255m";    // #00E5FF
    private static final String EQUALS = "\u001B[38;2;176;190;197m";     // #B0BEC5
    private static final String VALUE = "\u001B[1;38;2;255;121;198m";  // #FF79C6
    private static final String BRACKET = "\u001B[38;2;84;110;122m";     // #546E7A
    private static final String BRIGHT_WHITE = "\u001B[97m";
    protected final Comparator<? super K> comparator;
    protected final int degree;
    protected final int maxKeys;
    protected final int minKeys;
    protected transient N root;
    protected transient int size;
    protected transient long modCount;
    protected transient NavigableSet<K> keySetView;
    protected transient Collection<V> valuesView;
    protected transient Set<Map.Entry<K, V>> entrySetView;
    protected transient NavigableMap<K, V> descendingMapView;

    protected AbstractNaryTreeMap(int degree, Comparator<? super K> comparator) {
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
    protected int searchNodeMap(N current, K k) {
        if (current.keyCount < 12) {
            for (int i = 0; i < current.keyCount; i++) {
                int cmp = compare((K) current.keys[i], k);
                if (cmp == 0) return i;
                if (cmp > 0) return ~i;
            }
            return ~current.keyCount;
        }
        return Arrays.binarySearch((K[]) current.keys, 0, current.keyCount, k, comparator);
    }

    protected int searchNodeMapValue(N current, V v) {
        for (int i = 0; i < current.keyCount; i++) {
            if (Objects.equals(current.values[i], v)) return i;
        }
        return -1;
    }

    abstract N createNode(int degree, boolean isLeaf);

    abstract void buildFromSorted(Iterator<? extends Map.Entry<? extends K, ? extends V>> it, float factor);

    /**
     * <strong>WARNING: THE TRUE DRAGON OF CHAOSTREE.</strong>
     * <p>
     * This is a high-performance, bare-metal array ingestion engine. It is hungry for raw
     * array throughput, but it is extremely unforgiving. Use with absolute precision.
     * <p>
     * <strong>THE FLAT MATRIX RULES:</strong>
     * <ul>
     * <li><strong>Matrix Layout:</strong> The {@code blast} parameter must be exactly 2D: {@code blast[0]} contains the keys, and {@code blast[1]} contains the values.</li>
     * <li><strong>Array Integrity:</strong> Neither array can be null, and both must be of exactly equal length.</li>
     * <li><strong>No Null Keys:</strong> A key must never be null. If a value is empty/missing, you must explicitly place {@code null} in the value array at that index.</li>
     * <li><strong>Strictly Sorted:</strong> The keys array <strong>MUST</strong> be strictly sorted according to the tree's comparator. Feeding unsorted data will instantly and silently corrupt the entire tree structure.</li>
     * <li><strong>Minimum Degree:</strong> This API relies on chunked array-copying and only services trees with a {@code degree >= 32}.</li>
     * </ul>
     * <p>
     * <strong>FILL FACTOR:</strong>
     * The {@code factor} determines node occupancy and has strict limits between {@code 0.5f} and {@code 1.0f}.
     * A factor of {@code 0.9f} is highly recommended for bulk loading. This packs the nodes densely while leaving
     * exactly enough buffer room to prevent future insertions from triggering massive, cascading split operations.
     * <p>
     * Hold the Chaos!!
     *
     * @param blast  A 2D array where {@code blast[0]} is the sorted keys and {@code blast[1]} is the mapped values.
     * @param factor The node fill factor, restricted to the range {@code [0.5, 1.0]}.
     */
    abstract void importFlatMatrix(Object[][] blast, float factor);

    /**
     * <strong>THE MASTER EXPORTER OF CHAOSTREE</strong>
     * <p>
     * Rips the entire internal state of the tree into a highly optimized, contiguous 2D array matrix
     * in strictly sorted order. This bypasses {@code Map.Entry} instantiation entirely by directly
     * blasting memory into flat arrays.
     * <p>
     * <strong>Matrix Layout:</strong>
     * <ul>
     * <li>{@code matrix[0]} &rarr; Array of strictly sorted keys.</li>
     * <li>{@code matrix[1]} &rarr; Array of corresponding values.</li>
     * </ul>
     * <p>
     * Unlike the ingestion engine, this extraction process is universally safe and natively
     * supports trees of <strong>all degrees</strong> with zero restrictions.
     * <p>
     * <strong>Note:</strong> If you intend to reconstruct a tree by feeding this matrix back
     * into the engine, you must review the strict limitations (such as {@code degree >= 32})
     * documented in {@link #importFlatMatrix}.
     *
     * @return A 2D {@code Object[][]} representing the flat matrix of keys and values.
     */
    abstract Object[][] exportFlatMatrix();

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (size == 0 && m instanceof SortedMap && ((SortedMap<?, ?>) m).comparator() == comparator) {
            buildFromSorted(m.entrySet().iterator(), 0.9f);
            return;
        }
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySetView;
        return (es != null) ? es : (entrySetView = new EntrySetView());
    }

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

    @Override
    public Map.Entry<K, V> pollFirstEntry() {
        Map.Entry<K, V> first = firstEntry();
        if (first != null) remove(first.getKey());
        return first;
    }

    @Override
    public Map.Entry<K, V> pollLastEntry() {
        Map.Entry<K, V> last = lastEntry();
        if (last != null) remove(last.getKey());
        return last;
    }

    @Override
    public abstract Map.Entry<K, V> ceilingEntry(K key);

    @Override
    public abstract Map.Entry<K, V> floorEntry(K key);

    @Override
    public abstract Map.Entry<K, V> higherEntry(K key);

    @Override
    public abstract Map.Entry<K, V> lowerEntry(K key);

    @Override
    public K ceilingKey(K key) {
        Map.Entry<K, V> e = ceilingEntry(key);
        return e == null ? null : e.getKey();
    }

    @Override
    public K floorKey(K key) {
        Map.Entry<K, V> e = floorEntry(key);
        return e == null ? null : e.getKey();
    }

    @Override
    public K higherKey(K key) {
        Map.Entry<K, V> e = higherEntry(key);
        return e == null ? null : e.getKey();
    }

    @Override
    public K lowerKey(K key) {
        Map.Entry<K, V> e = lowerEntry(key);
        return e == null ? null : e.getKey();
    }

    protected abstract Iterator<Map.Entry<K, V>> entryIterator(K fromKey, boolean fromInclusive);

    protected abstract Iterator<Map.Entry<K, V>> descendingEntryIterator(K fromKey, boolean fromInclusive);

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return new SubNaryMap(false, fromKey, fromInclusive, false, toKey, toInclusive, false);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return new SubNaryMap(true, null, true, false, toKey, inclusive, false);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return new SubNaryMap(false, fromKey, inclusive, true, null, true, false);
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return headMap(toKey, false);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    @Override
    public String display(Style style) {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        buildString(sb, root, "", true, style);
        return sb.toString();
    }

    private void buildString(StringBuilder sb, N node, String prefix, boolean isTail, Style style) {
        String lastBranch = (style == Style.UNICODE) ? "└── " : "\\-- ";
        String crossBranch = (style == Style.UNICODE) ? "├── " : "+-- ";
        String vertical = (style == Style.UNICODE) ? "│   " : "|   ";

        sb.append(prefix).append(isTail ? lastBranch : crossBranch);

        sb.append(BRIGHT_WHITE).append("[").append(RESET);

        for (int i = 0; i < node.keyCount; i++) {

            sb.append(BRACKET).append("[").append(RESET);
            sb.append(KEY).append(node.keys[i]).append(RESET);
            if (node.values != null) {
                sb.append(EQUALS).append("=").append(RESET);
                sb.append(VALUE).append(node.values[i]).append(RESET);
            }

            sb.append(BRACKET).append("]").append(RESET);
            if (i < node.keyCount - 1) {
                sb.append(BRACKET).append(", ").append(RESET);
            }
        }

        sb.append(BRIGHT_WHITE).append("]").append(RESET).append("\n");

        if (!node.isLeaf()) {
            int numChildren = node.keyCount + 1;
            for (int i = 0; i < numChildren; i++) {
                N child = node.child[i];
                if (child != null) {
                    boolean lastChild = (i == numChildren - 1);
                    buildString(sb, child, prefix + (isTail ? "    " : vertical), lastChild, style);
                }
            }
        }
    }

    @Override
    public Set<K> keySet() {
        return navigableKeySet();
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        NavigableSet<K> nks = keySetView;
        return (nks != null) ? nks : (keySetView = new KeySetView(this));
    }

    @Override
    public Collection<V> values() {
        Collection<V> vs = valuesView;
        return (vs != null) ? vs : (valuesView = new ValuesView());
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        NavigableMap<K, V> dm = descendingMapView;
        return (dm != null) ? dm : (descendingMapView = new DescendingMapFacade());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            AbstractNaryTreeMap<K, V, N> clone = (AbstractNaryTreeMap<K, V, N>) super.clone();
            clone.root = null;
            clone.size = 0;
            clone.modCount = 0;

            clone.keySetView = null;
            clone.valuesView = null;
            clone.entrySetView = null;
            clone.descendingMapView = null;

            if (this.size > 0) {
                clone.buildFromSorted(this.entrySet().iterator(), 1.0f);
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeInt(size);
        for (Map.Entry<K, V> e : entrySet()) {
            s.writeObject(e.getKey());
            s.writeObject(e.getValue());
        }
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        int mapSize = s.readInt();
        if (mapSize > 0) {
            Iterator<Map.Entry<K, V>> streamIterator = new Iterator<>() {
                int count = 0;

                @Override
                public boolean hasNext() {
                    return count < mapSize;
                }

                @Override
                @SuppressWarnings("unchecked")
                public Map.Entry<K, V> next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    try {
                        K key = (K) s.readObject();
                        V value = (V) s.readObject();
                        count++;
                        return new AbstractMap.SimpleImmutableEntry<>(key, value);
                    } catch (java.io.IOException | ClassNotFoundException e) {
                        throw new RuntimeException("Failed to deserialize tree data", e);
                    }
                }
            };
            buildFromSorted(streamIterator, 1.0f);
        }
    }

    protected abstract Iterator<K> keyIterator(K fromKey, boolean fromInclusive);

    protected abstract Iterator<V> valueIterator(K fromKey, boolean fromInclusive);

    final class ChaosEntry implements Map.Entry<K, V> {
        private final K key;
        private transient final N node;
        private transient final int index;
        private transient final long expectedModCount;
        private V value;

        @SuppressWarnings("unchecked")
        ChaosEntry(N node, int index) {
            this.node = node;
            this.index = index;
            this.key = (K) node.keys[index];
            this.value = (V) node.values[index];
            this.expectedModCount = AbstractNaryTreeMap.this.modCount;
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
            if (node == null) {
                V oldValue = this.value;
                this.value = newValue;
                AbstractNaryTreeMap.this.put(key, newValue);
                return oldValue;
            }

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

    private final class EntrySetView extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return entryIterator(null, true);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry<?, ?> e)) return false;
            Object key = e.getKey();
            if (key == null) return false;

            try {
                V v = get(key);
                return Objects.equals(v, e.getValue()) && (v != null || containsKey(key));
            } catch (ClassCastException ex) {
                return false;
            }
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry<?, ?> e)) return false;
            Object key = e.getKey();

            if (key == null) return false;

            try {
                V v = get(key);
                if (Objects.equals(v, e.getValue()) && (v != null || containsKey(key))) {
                    AbstractNaryTreeMap.this.remove(key);
                    return true;
                }
                return false;
            } catch (ClassCastException ex) {
                return false;
            }
        }

        @Override
        public void clear() {
            AbstractNaryTreeMap.this.clear();
        }
    }

    private final class SubNaryMap extends AbstractMap<K, V> implements NavigableMap<K, V>, Serializable {
        private final boolean fromStart, toEnd;
        private final K lo;
        private final boolean loInclusive;
        private final K hi;
        private final boolean hiInclusive;
        private final boolean descending;

        SubNaryMap(boolean fromStart, K lo, boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive, boolean descending) {
            if (!fromStart && !toEnd && compare(lo, hi) > 0) throw new IllegalArgumentException("fromKey > toKey");
            this.fromStart = fromStart;
            this.lo = lo;
            this.loInclusive = loInclusive;
            this.toEnd = toEnd;
            this.hi = hi;
            this.hiInclusive = hiInclusive;
            this.descending = descending;
        }

        private boolean tooLow(Object key) {
            if (!fromStart) {
                @SuppressWarnings("unchecked") int cmp = compare((K) key, lo);
                return cmp < 0 || (cmp == 0 && !loInclusive);
            }
            return false;
        }

        private boolean tooHigh(Object key) {
            if (!toEnd) {
                @SuppressWarnings("unchecked") int cmp = compare((K) key, hi);
                return cmp > 0 || (cmp == 0 && !hiInclusive);
            }
            return false;
        }

        private boolean inRange(Object key) {
            return !tooLow(key) && !tooHigh(key);
        }

        @Override
        public V put(K key, V value) {
            if (!inRange(key)) throw new IllegalArgumentException("Key out of range");
            return AbstractNaryTreeMap.this.put(key, value);
        }

        @Override
        public V remove(Object key) {
            return inRange(key) ? AbstractNaryTreeMap.this.remove(key) : null;
        }

        @Override
        public boolean containsKey(Object key) {
            return inRange(key) && AbstractNaryTreeMap.this.containsKey(key);
        }

        @Override
        public V get(Object key) {
            return inRange(key) ? AbstractNaryTreeMap.this.get(key) : null;
        }

        private Map.Entry<K, V> absLowest() {
            Map.Entry<K, V> e = fromStart ? AbstractNaryTreeMap.this.firstEntry() : (loInclusive ? AbstractNaryTreeMap.this.ceilingEntry(lo) : AbstractNaryTreeMap.this.higherEntry(lo));
            return (e == null || tooHigh(e.getKey())) ? null : e;
        }

        private Map.Entry<K, V> absHighest() {
            Map.Entry<K, V> e = toEnd ? AbstractNaryTreeMap.this.lastEntry() : (hiInclusive ? AbstractNaryTreeMap.this.floorEntry(hi) : AbstractNaryTreeMap.this.lowerEntry(hi));
            return (e == null || tooLow(e.getKey())) ? null : e;
        }

        @Override
        public Map.Entry<K, V> firstEntry() {
            return descending ? absHighest() : absLowest();
        }

        @Override
        public Map.Entry<K, V> lastEntry() {
            return descending ? absLowest() : absHighest();
        }

        @Override
        public K firstKey() {
            Map.Entry<K, V> e = firstEntry();
            if (e == null) throw new NoSuchElementException();
            return e.getKey();
        }

        @Override
        public K lastKey() {
            Map.Entry<K, V> e = lastEntry();
            if (e == null) throw new NoSuchElementException();
            return e.getKey();
        }

        @Override
        public Map.Entry<K, V> ceilingEntry(K key) {
            if (descending) {
                if (tooHigh(key)) return absHighest();
                Map.Entry<K, V> e = AbstractNaryTreeMap.this.floorEntry(key);
                return (e == null || tooLow(e.getKey())) ? null : e;
            } else {
                if (tooLow(key)) return absLowest();
                Map.Entry<K, V> e = AbstractNaryTreeMap.this.ceilingEntry(key);
                return (e == null || tooHigh(e.getKey())) ? null : e;
            }
        }

        @Override
        public Map.Entry<K, V> floorEntry(K key) {
            if (descending) {
                if (tooLow(key)) return absLowest();
                Map.Entry<K, V> e = AbstractNaryTreeMap.this.ceilingEntry(key);
                return (e == null || tooHigh(e.getKey())) ? null : e;
            } else {
                if (tooHigh(key)) return absHighest();
                Map.Entry<K, V> e = AbstractNaryTreeMap.this.floorEntry(key);
                return (e == null || tooLow(e.getKey())) ? null : e;
            }
        }

        @Override
        public Map.Entry<K, V> higherEntry(K key) {
            if (descending) {
                if (tooHigh(key)) return absHighest();
                Map.Entry<K, V> e = AbstractNaryTreeMap.this.lowerEntry(key);
                return (e == null || tooLow(e.getKey())) ? null : e;
            } else {
                if (tooLow(key)) return absLowest();
                Map.Entry<K, V> e = AbstractNaryTreeMap.this.higherEntry(key);
                return (e == null || tooHigh(e.getKey())) ? null : e;
            }
        }

        @Override
        public Map.Entry<K, V> lowerEntry(K key) {
            if (descending) {
                if (tooLow(key)) return absLowest();
                Map.Entry<K, V> e = AbstractNaryTreeMap.this.higherEntry(key);
                return (e == null || tooHigh(e.getKey())) ? null : e;
            } else {
                if (tooHigh(key)) return absHighest();
                Map.Entry<K, V> e = AbstractNaryTreeMap.this.lowerEntry(key);
                return (e == null || tooLow(e.getKey())) ? null : e;
            }
        }

        @Override
        public K ceilingKey(K key) {
            Map.Entry<K, V> e = ceilingEntry(key);
            return e == null ? null : e.getKey();
        }

        @Override
        public K floorKey(K key) {
            Map.Entry<K, V> e = floorEntry(key);
            return e == null ? null : e.getKey();
        }

        @Override
        public K higherKey(K key) {
            Map.Entry<K, V> e = higherEntry(key);
            return e == null ? null : e.getKey();
        }

        @Override
        public K lowerKey(K key) {
            Map.Entry<K, V> e = lowerEntry(key);
            return e == null ? null : e.getKey();
        }

        @Override
        public Map.Entry<K, V> pollFirstEntry() {
            Map.Entry<K, V> e = firstEntry();
            if (e != null) remove(e.getKey());
            return e;
        }

        @Override
        public Map.Entry<K, V> pollLastEntry() {
            Map.Entry<K, V> e = lastEntry();
            if (e != null) remove(e.getKey());
            return e;
        }

        @Override
        public Comparator<? super K> comparator() {
            Comparator<? super K> cmp = AbstractNaryTreeMap.this.comparator();
            return descending ? Collections.reverseOrder(cmp) : cmp;
        }

        @Override
        public NavigableSet<K> keySet() {
            return navigableKeySet();
        }

        @Override
        public NavigableSet<K> navigableKeySet() {
            return new KeySetView(this);
        }

        @Override
        public NavigableSet<K> descendingKeySet() {
            return descendingMap().navigableKeySet();
        }

        @Override
        public NavigableMap<K, V> descendingMap() {
            return new SubNaryMap(fromStart, lo, loInclusive, toEnd, hi, hiInclusive, !descending);
        }

        @Override
        public NavigableMap<K, V> subMap(K from, boolean fromInc, K to, boolean toInc) {
            if (!inRange(from) || !inRange(to)) throw new IllegalArgumentException("Bounds out of range");
            return descending ? new SubNaryMap(false, to, toInc, false, from, fromInc, true)
                    : new SubNaryMap(false, from, fromInc, false, to, toInc, false);
        }

        @Override
        public NavigableMap<K, V> headMap(K to, boolean inc) {
            if (!inRange(to)) throw new IllegalArgumentException("Bounds out of range");
            return descending ? new SubNaryMap(false, to, inc, toEnd, hi, hiInclusive, true)
                    : new SubNaryMap(fromStart, lo, loInclusive, false, to, inc, false);
        }

        @Override
        public NavigableMap<K, V> tailMap(K from, boolean inc) {
            if (!inRange(from)) throw new IllegalArgumentException("Bounds out of range");
            return descending ? new SubNaryMap(fromStart, lo, loInclusive, false, from, inc, true)
                    : new SubNaryMap(false, from, inc, toEnd, hi, hiInclusive, false);
        }

        @Override
        public SortedMap<K, V> subMap(K from, K to) {
            return subMap(from, true, to, false);
        }

        @Override
        public SortedMap<K, V> headMap(K to) {
            return headMap(to, false);
        }

        @Override
        public SortedMap<K, V> tailMap(K from) {
            return tailMap(from, true);
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return new SubMapEntrySet();
        }

        private final class SubMapEntrySet extends AbstractSet<Map.Entry<K, V>> {

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<>() {
                    private Iterator<Map.Entry<K, V>> it = descending
                            ? descendingEntryIterator(toEnd ? null : hi, hiInclusive)
                            : entryIterator(fromStart ? null : lo, loInclusive);
                    private Map.Entry<K, V> nextEntry = null;
                    private Map.Entry<K, V> lastReturned = null;

                    {
                        advance();
                    }

                    private void advance() {
                        if (it.hasNext()) {
                            nextEntry = it.next();
                            if (descending) {
                                if (!fromStart && tooLow(nextEntry.getKey())) nextEntry = null;
                            } else {
                                if (!toEnd && tooHigh(nextEntry.getKey())) nextEntry = null;
                            }
                        } else {
                            nextEntry = null;
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        return nextEntry != null;
                    }

                    @Override
                    public Map.Entry<K, V> next() {
                        if (nextEntry == null) throw new NoSuchElementException();
                        lastReturned = nextEntry;
                        advance();
                        return lastReturned;
                    }

                    @Override
                    public void remove() {
                        if (lastReturned == null) throw new IllegalStateException();
                        SubNaryMap.this.remove(lastReturned.getKey());
                        lastReturned = null;
                        if (nextEntry != null) {
                            it = descending ? descendingEntryIterator(nextEntry.getKey(), true)
                                    : entryIterator(nextEntry.getKey(), true);
                            advance();
                        }
                    }
                };
            }

            @Override
            public int size() {
                int count = 0;
                for (Map.Entry<K, V> ignored : this) count++;
                return count;
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry<?, ?> e)) return false;
                Object key = e.getKey();
                if (key == null) return false;
                try {
                    V v = SubNaryMap.this.get(key);
                    return Objects.equals(v, e.getValue()) && (v != null || SubNaryMap.this.containsKey(key));
                } catch (ClassCastException ex) {
                    return false;
                }
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry<?, ?> e)) return false;
                Object key = e.getKey();
                if (key == null) return false;
                try {
                    V v = SubNaryMap.this.get(key);
                    if (Objects.equals(v, e.getValue()) && (v != null || SubNaryMap.this.containsKey(key))) {
                        SubNaryMap.this.remove(key);
                        return true;
                    }
                    return false;
                } catch (ClassCastException ex) {
                    return false;
                }
            }
        }
    }

    private final class ValuesView extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            return valueIterator(null, true);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            return AbstractNaryTreeMap.this.containsValue(o);
        }

        @Override
        public void clear() {
            AbstractNaryTreeMap.this.clear();
        }
    }


    private final class KeySetView extends AbstractSet<K> implements NavigableSet<K> {
        private final NavigableMap<K, V> map;

        KeySetView(NavigableMap<K, V> map) {
            this.map = map;
        }

        @Override
        public Iterator<K> iterator() {
            if (map == AbstractNaryTreeMap.this) {
                return keyIterator(null, true);
            }
            final Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
            return new Iterator<>() {
                public boolean hasNext() {
                    return it.hasNext();
                }

                public K next() {
                    return it.next().getKey();
                }

                public void remove() {
                    it.remove();
                }
            };
        }

        @Override
        public Comparator<? super K> comparator() {
            return map.comparator();
        }

        @Override
        public Iterator<K> descendingIterator() {
            return descendingSet().iterator();
        }

        @Override
        public NavigableSet<K> descendingSet() {
            return map.descendingMap().navigableKeySet();
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(Object o) {
            return map.containsKey((K) o);
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public K lower(K k) {
            return map.lowerKey(k);
        }

        @Override
        public K floor(K k) {
            return map.floorKey(k);
        }

        @Override
        public K ceiling(K k) {
            return map.ceilingKey(k);
        }

        @Override
        public K higher(K k) {
            return map.higherKey(k);
        }

        @Override
        public K pollFirst() {
            Map.Entry<K, V> e = map.pollFirstEntry();
            return e == null ? null : e.getKey();
        }

        @Override
        public K pollLast() {
            Map.Entry<K, V> e = map.pollLastEntry();
            return e == null ? null : e.getKey();
        }

        @Override
        public NavigableSet<K> subSet(K from, boolean fromInc, K to, boolean toInc) {
            return map.subMap(from, fromInc, to, toInc).navigableKeySet();
        }

        @Override
        public NavigableSet<K> headSet(K to, boolean inc) {
            return map.headMap(to, inc).navigableKeySet();
        }

        @Override
        public NavigableSet<K> tailSet(K from, boolean inc) {
            return map.tailMap(from, inc).navigableKeySet();
        }

        @Override
        public NavigableSet<K> subSet(K from, K to) {
            return subSet(from, true, to, false);
        }

        @Override
        public NavigableSet<K> headSet(K to) {
            return headSet(to, false);
        }

        @Override
        public NavigableSet<K> tailSet(K from) {
            return tailSet(from, true);
        }

        @Override
        public K first() {
            return map.firstKey();
        }

        @Override
        public K last() {
            return map.lastKey();
        }

    }

    private final class DescendingMapFacade extends AbstractMap<K, V> implements NavigableMap<K, V>, Serializable {
        @Override
        public Comparator<? super K> comparator() {
            Comparator<? super K> cmp = AbstractNaryTreeMap.this.comparator();
            return (cmp == null) ? Collections.reverseOrder() : Collections.reverseOrder(cmp);
        }

        @Override
        public Map.Entry<K, V> firstEntry() {
            return AbstractNaryTreeMap.this.lastEntry();
        }

        @Override
        public Map.Entry<K, V> lastEntry() {
            return AbstractNaryTreeMap.this.firstEntry();
        }

        @Override
        public Map.Entry<K, V> pollFirstEntry() {
            return AbstractNaryTreeMap.this.pollLastEntry();
        }

        @Override
        public Map.Entry<K, V> pollLastEntry() {
            return AbstractNaryTreeMap.this.pollFirstEntry();
        }

        @Override
        public Map.Entry<K, V> ceilingEntry(K key) {
            return AbstractNaryTreeMap.this.floorEntry(key);
        }

        @Override
        public Map.Entry<K, V> floorEntry(K key) {
            return AbstractNaryTreeMap.this.ceilingEntry(key);
        }

        @Override
        public Map.Entry<K, V> higherEntry(K key) {
            return AbstractNaryTreeMap.this.lowerEntry(key);
        }

        @Override
        public Map.Entry<K, V> lowerEntry(K key) {
            return AbstractNaryTreeMap.this.higherEntry(key);
        }

        @Override
        public K firstKey() {
            return AbstractNaryTreeMap.this.lastKey();
        }

        @Override
        public K lastKey() {
            return AbstractNaryTreeMap.this.firstKey();
        }

        @Override
        public K ceilingKey(K key) {
            return AbstractNaryTreeMap.this.floorKey(key);
        }

        @Override
        public K floorKey(K key) {
            return AbstractNaryTreeMap.this.ceilingKey(key);
        }

        @Override
        public K higherKey(K key) {
            return AbstractNaryTreeMap.this.lowerKey(key);
        }

        @Override
        public K lowerKey(K key) {
            return AbstractNaryTreeMap.this.higherKey(key);
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return new DescendingEntrySet();
        }

        public NavigableSet<K> keySet() {
            return navigableKeySet();
        }

        @Override
        public NavigableSet<K> navigableKeySet() {
            return new KeySetView(this);
        }

        @Override
        public NavigableSet<K> descendingKeySet() {
            return AbstractNaryTreeMap.this.navigableKeySet();
        }

        @Override
        public NavigableMap<K, V> descendingMap() {
            return AbstractNaryTreeMap.this;
        }

        @Override
        public NavigableMap<K, V> subMap(K from, boolean fromInc, K to, boolean toInc) {
            return AbstractNaryTreeMap.this.subMap(to, toInc, from, fromInc).descendingMap();
        }

        @Override
        public NavigableMap<K, V> headMap(K to, boolean inc) {
            return AbstractNaryTreeMap.this.tailMap(to, inc).descendingMap();
        }

        @Override
        public NavigableMap<K, V> tailMap(K from, boolean inc) {
            return AbstractNaryTreeMap.this.headMap(from, inc).descendingMap();
        }

        @Override
        public SortedMap<K, V> subMap(K from, K to) {
            return subMap(from, true, to, false);
        }

        @Override
        public SortedMap<K, V> headMap(K to) {
            return headMap(to, false);
        }

        @Override
        public SortedMap<K, V> tailMap(K from) {
            return tailMap(from, true);
        }

        @Override
        public V put(K key, V value) {
            return AbstractNaryTreeMap.this.put(key, value);
        }

        @Override
        public V get(Object key) {
            return AbstractNaryTreeMap.this.get(key);
        }

        @Override
        public V remove(Object key) {
            return AbstractNaryTreeMap.this.remove(key);
        }

        @Override
        public boolean containsKey(Object key) {
            return AbstractNaryTreeMap.this.containsKey(key);
        }

        @Override
        public int size() {
            return AbstractNaryTreeMap.this.size();
        }

        private final class DescendingEntrySet extends AbstractSet<Map.Entry<K, V>> implements Serializable {

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                // Calls the outer AbstractNaryTreeMap's descending iterator
                return descendingEntryIterator(null, true);
            }

            @Override
            public int size() {
                return AbstractNaryTreeMap.this.size();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry<?, ?> e)) return false;
                Object key = e.getKey();
                if (key == null) return false;

                try {
                    V v = AbstractNaryTreeMap.this.get(key);
                    return Objects.equals(v, e.getValue()) && (v != null || AbstractNaryTreeMap.this.containsKey(key));
                } catch (ClassCastException ex) {
                    return false;
                }
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry<?, ?> e)) return false;
                Object key = e.getKey();
                if (key == null) return false;

                try {
                    V v = AbstractNaryTreeMap.this.get(key);
                    if (Objects.equals(v, e.getValue()) && (v != null || AbstractNaryTreeMap.this.containsKey(key))) {
                        AbstractNaryTreeMap.this.remove(key);
                        return true;
                    }
                    return false;
                } catch (ClassCastException ex) {
                    return false;
                }
            }

            @Override
            public void clear() {
                AbstractNaryTreeMap.this.clear();
            }
        }
    }
}
