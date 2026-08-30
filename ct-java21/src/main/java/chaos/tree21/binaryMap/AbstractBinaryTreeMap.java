package chaos.tree21.binaryMap;

import chaos.tree21.core.SearchTreeMap;
import chaos.tree21.core.Style;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
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
import java.util.SortedSet;
import java.util.function.BiFunction;
import java.util.function.Function;

public sealed abstract class AbstractBinaryTreeMap<K, V, N extends AbstractBinaryMapNode<K, V, N>>
        implements SearchTreeMap<K, V> permits AvlTreeMap, RedBlackTreeMap {

    protected final Comparator<? super K> comparator;
    protected transient Set<K> keySetView;
    protected transient Collection<V> valuesView;
    protected transient Set<Map.Entry<K, V>> entrySetView;
    protected N root;
    protected int size;
    protected long modCount;

    protected AbstractBinaryTreeMap() {
        this.comparator = null;
    }

    protected AbstractBinaryTreeMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super K> comparator() {
        return this.comparator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map<?, ?> m)) return false;
        if (m.size() != this.size()) return false;
        if (m.hashCode() != this.hashCode()) return false;

        try {
            for (Map.Entry<K, V> e : this.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key))) return false;

                } else {
                    if (!value.equals(m.get(key))) {
                        return false;
                    }
                }
            }
            return true;
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    protected int compare(K k1, K k2) {
        if (comparator != null) {
            return comparator.compare(k1, k2);
        }
        return ((Comparable<? super K>) k1).compareTo(k2);
    }

    /*
    Tree emptiness must be checked prior
     */
    protected N nodeFinder(K key) {
        N current = root;
        while (current != null) {
            int cmp = compare(key, current.getKey());
            if (cmp == 0) return current;
            current = cmp < 0 ? current.getLeft() : current.getRight();
        }
        return null;
    }

    protected N leftMostNode() {
        N current = root;
        if (current == null) return null;
        while (current.getLeft() != null) current = current.getLeft();
        return current;
    }

    protected N rightMostNode() {
        N current = root;
        if (current == null) return null;
        while (current.getRight() != null) current = current.getRight();
        return current;
    }

    protected abstract N createNode(K key, V value);

    protected void afterInsert(N node) {
    }

    @Override
    public V put(K key, V value) {
        if (root == null) {
            compare(key, key); // JDK Semantic: Type (and possibly null) check!
            root = createNode(key, value);
            size = 1;
            modCount++;
            afterInsert(root);
            return null;
        }

        N parent = null;
        N current = root;
        int cmp = 0;

        while (current != null) {
            parent = current;
            cmp = compare(key, current.getKey());
            if (cmp == 0) {
                V oldValue = current.setValue(value);
                return oldValue;
            } else if (cmp < 0) current = current.getLeft();
            else current = current.getRight();
        }
        N newNode = createNode(key, value);
        newNode.setParent(parent);

        if (cmp < 0) parent.setLeft(newNode);
        else parent.setRight(newNode);

        size++;
        modCount++;
        afterInsert(newNode);
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (root == null) {
            compare(key, key);
            root = createNode(key, value);
            size = 1;
            modCount++;
            afterInsert(root);
            return null;
        }

        N parent = null;
        N current = root;
        int cmp = 0;
        while (current != null) {
            parent = current;
            cmp = compare(key, current.getKey());
            if (cmp == 0) {
                V oldValue = current.getValue();
                if (oldValue == null) {
                    current.setValue(value);
                }
                return oldValue;
            } else if (cmp < 0) current = current.getLeft();
            else current = current.getRight();
        }
        N newNode = createNode(key, value);
        newNode.setParent(parent);
        if (cmp < 0) parent.setLeft(newNode);
        else parent.setRight(newNode);
        size++;
        modCount++;
        afterInsert(newNode);
        return null;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        java.util.Objects.requireNonNull(mappingFunction);
        if (root == null) {
            compare(key, key);
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                root = createNode(key, newValue);
                size = 1;
                modCount++;
                afterInsert(root);
            }
            return newValue;
        }

        N parent = null;
        N current = root;
        int cmp = 0;
        while (current != null) {
            parent = current;
            cmp = compare(key, current.getKey());
            if (cmp == 0) {
                V oldValue = current.getValue();
                if (oldValue != null) {
                    return oldValue;
                }
                V newValue = mappingFunction.apply(key);
                if (newValue != null) {
                    current.setValue(newValue);
                }
                return newValue;
            } else if (cmp < 0) current = current.getLeft();
            else current = current.getRight();
        }
        V newValue = mappingFunction.apply(key);
        if (newValue != null) {
            N newNode = createNode(key, newValue);
            newNode.setParent(parent);
            if (cmp < 0) parent.setLeft(newNode);
            else parent.setRight(newNode);
            size++;
            modCount++;
            afterInsert(newNode);
        }
        return newValue;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        java.util.Objects.requireNonNull(remappingFunction);
        N current = root;
        while (current != null) {
            int cmp = compare(key, current.getKey());
            if (cmp == 0) {
                V oldValue = current.getValue();
                if (oldValue != null) {
                    V newValue = remappingFunction.apply(key, oldValue);
                    if (newValue != null) {
                        current.setValue(newValue);
                        return newValue;
                    } else {
                        remove(key);
                        return null;
                    }
                }
                return null;
            } else if (cmp < 0) {
                current = current.getLeft();
            } else {
                current = current.getRight();
            }
        }
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        if (root == null) {
            compare(key, key);
            V newValue = remappingFunction.apply(key, null);
            if (newValue != null) {
                root = createNode(key, newValue);
                size = 1;
                modCount++;
                afterInsert(root);
            }
            return newValue;
        }

        N parent = null;
        N current = root;
        int cmp = 0;
        while (current != null) {
            parent = current;
            cmp = compare(key, current.getKey());
            if (cmp == 0) {
                V oldValue = current.getValue();
                V newValue = remappingFunction.apply(key, oldValue);
                if (newValue != null) {
                    current.setValue(newValue);
                    return newValue;
                } else {
                    remove(key);
                    return null;
                }
            } else if (cmp < 0) current = current.getLeft();
            else current = current.getRight();
        }
        V newValue = remappingFunction.apply(key, null);
        if (newValue != null) {
            N newNode = createNode(key, newValue);
            newNode.setParent(parent);
            if (cmp < 0) parent.setLeft(newNode);
            else parent.setRight(newNode);
            size++;
            modCount++;
            afterInsert(newNode);
        }
        return newValue;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        if (root == null) {
            compare(key, key);
            root = createNode(key, value);
            size = 1;
            modCount++;
            afterInsert(root);
            return value;
        }
        N parent = null;
        N current = root;
        int cmp = 0;
        while (current != null) {
            parent = current;
            cmp = compare(key, current.getKey());
            if (cmp == 0) {
                V oldValue = current.getValue();
                V newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);
                if (newValue != null) {
                    current.setValue(newValue);
                } else {
                    remove(key);
                }
                return newValue;
            } else if (cmp < 0) current = current.getLeft();
            else current = current.getRight();
        }
        N newNode = createNode(key, value);
        newNode.setParent(parent);
        if (cmp < 0) parent.setLeft(newNode);
        else parent.setRight(newNode);
        size++;
        modCount++;
        afterInsert(newNode);
        return value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
        modCount++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object o) {
        @SuppressWarnings("unchecked")
        K key = (K) o;
        return nodeFinder(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        N current = leftMostNode();
        while (current != null) {
            if (Objects.equals(value, current.getValue())) {
                return true;
            }
            current = successor(current);
        }
        return false;
    }

    protected N successor(N t) {
        if (t == null) return null;
        N p;
        if (t.getRight() != null) {
            p = t.getRight();
            while (p.getLeft() != null) {
                p = p.getLeft();
            }
        } else {
            p = t.getParent();
            N ch = t;
            while (p != null && ch == p.getRight()) {
                ch = p;
                p = p.getParent();
            }
        }
        return p;
    }

    protected N predecessor(N t) {
        if (t == null) return null;
        N p;
        if (t.getLeft() != null) {
            p = t.getLeft();
            while (p.getRight() != null) {
                p = p.getRight();
            }
        } else {
            p = t.getParent();
            N ch = t;
            while (p != null && ch == p.getLeft()) {
                ch = p;
                p = p.getParent();
            }
        }
        return p;
    }

    @Override
    public V get(Object o) {
        if (isEmpty()) return null;
        try {
            @SuppressWarnings("unchecked")
            K k = (K) o;
            N node = nodeFinder(k);
            return node == null ? null : node.getValue();
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }

    protected void rotateLeft(N p) {
        N r = p.getRight();
        p.setRight(r.getLeft());
        if (r.getLeft() != null) {
            r.getLeft().setParent(p);
        }
        r.setParent(p.getParent());
        if (p.getParent() == null) {
            root = r;
        } else if (p.getParent().getLeft() == p) {
            p.getParent().setLeft(r);
        } else {
            p.getParent().setRight(r);
        }

        r.setLeft(p);
        p.setParent(r);
    }

    protected void rotateRight(N p) {
        N l = p.getLeft();
        p.setLeft(l.getRight());
        if (l.getRight() != null) {
            l.getRight().setParent(p);
        }
        l.setParent(p.getParent());
        if (p.getParent() == null) {
            root = l;
        } else if (p.getParent().getRight() == p) {
            p.getParent().setRight(l);
        } else {
            p.getParent().setLeft(l);
        }
        l.setRight(p);
        p.setParent(l);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return new KeySetView<>(this);
    }

    @Override
    public Set<K> keySet() {
        return navigableKeySet();
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
        N n = leftMostNode();
        return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
        N n = rightMostNode();
        return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
    }

    @Override
    public K firstKey() {
        N first = leftMostNode();
        if (first == null) throw new NoSuchElementException();
        return first.getKey();
    }

    @Override
    public K lastKey() {
        N last = rightMostNode();
        if (last == null) throw new NoSuchElementException();
        return last.getKey();
    }

    @Override
    public Map.Entry<K, V> pollFirstEntry() {
        N first = leftMostNode();
        if (first == null) return null;
        Map.Entry<K, V> result = new AbstractMap.SimpleImmutableEntry<>(first.getKey(), first.getValue());
        remove(first.getKey());
        return result;
    }

    @Override
    public Map.Entry<K, V> pollLastEntry() {
        N last = rightMostNode();
        if (last == null) return null;
        Map.Entry<K, V> result = new AbstractMap.SimpleImmutableEntry<>(last.getKey(), last.getValue());
        remove(last.getKey());
        return result;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySetView == null) {
            entrySetView = new AbstractSet<>() {
                @Override
                public Iterator<Map.Entry<K, V>> iterator() {
                    return new EntryIterator(leftMostNode());
                }

                @Override
                public int size() {
                    return AbstractBinaryTreeMap.this.size();
                }

                @Override
                public boolean contains(Object o) {
                    if (!(o instanceof Map.Entry<?, ?> e)) return false;
                    N node = nodeFinder((K) e.getKey());
                    return node != null && Objects.equals(node.getValue(), e.getValue());
                }

                @Override
                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry<?, ?> e)) return false;
                    if (contains(o)) {
                        AbstractBinaryTreeMap.this.remove((K) e.getKey());
                        return true;
                    }
                    return false;
                }

                @Override
                public void clear() {
                    AbstractBinaryTreeMap.this.clear();
                }
            };
        }
        return entrySetView;
    }


    @Override
    public Collection<V> values() {
        if (valuesView == null) {
            valuesView = new AbstractCollection<>() {
                @Override
                public Iterator<V> iterator() {
                    return new ValueIterator(leftMostNode());
                }

                @Override
                public int size() {
                    return AbstractBinaryTreeMap.this.size();
                }

                @Override
                public boolean contains(Object o) {
                    return AbstractBinaryTreeMap.this.containsValue(o);
                }

                @Override
                public void clear() {
                    AbstractBinaryTreeMap.this.clear();
                }
            };
        }
        return valuesView;
    }

    protected N getCeilingNode(K key) {
        N p = root;
        while (p != null) {
            int cmp = compare(key, p.getKey());
            if (cmp < 0) {
                if (p.getLeft() != null) p = p.getLeft();
                else return p;
            } else if (cmp > 0) {
                if (p.getRight() != null) p = p.getRight();
                else return successor(p);
            } else return p;
        }
        return null;
    }

    protected N getFloorNode(K key) {
        N p = root;
        while (p != null) {
            int cmp = compare(key, p.getKey());
            if (cmp > 0) {
                if (p.getRight() != null) p = p.getRight();
                else return p;
            } else if (cmp < 0) {
                if (p.getLeft() != null) p = p.getLeft();
                else return predecessor(p);
            } else return p;
        }
        return null;
    }

    protected N getHigherNode(K key) {
        N p = root;
        while (p != null) {
            int cmp = compare(key, p.getKey());
            if (cmp < 0) {
                if (p.getLeft() != null) p = p.getLeft();
                else return p;
            } else {
                if (p.getRight() != null) p = p.getRight();
                else return successor(p);
            }
        }
        return null;
    }

    protected N getLowerNode(K key) {
        N p = root;
        while (p != null) {
            int cmp = compare(key, p.getKey());
            if (cmp > 0) {
                if (p.getRight() != null) p = p.getRight();
                else return p;
            } else {
                if (p.getLeft() != null) p = p.getLeft();
                else return predecessor(p);
            }
        }
        return null;
    }

    @Override
    public Map.Entry<K, V> lowerEntry(K k) {
        N n = getLowerNode(k);
        return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
    }

    @Override
    public K lowerKey(K k) {
        N n = getLowerNode(k);
        return n == null ? null : n.getKey();
    }

    @Override
    public Map.Entry<K, V> floorEntry(K k) {
        N n = getFloorNode(k);
        return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
    }

    @Override
    public K floorKey(K k) {
        N n = getFloorNode(k);
        return n == null ? null : n.getKey();
    }

    @Override
    public Map.Entry<K, V> ceilingEntry(K k) {
        N n = getCeilingNode(k);
        return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
    }

    @Override
    public K ceilingKey(K k) {
        N n = getCeilingNode(k);
        return n == null ? null : n.getKey();
    }

    @Override
    public Map.Entry<K, V> higherEntry(K k) {
        N n = getHigherNode(k);
        return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
    }

    @Override
    public K higherKey(K k) {
        N n = getHigherNode(k);
        return n == null ? null : n.getKey();
    }


    @Override
    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
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
    public String toString() {
        Iterator<Map.Entry<K, V>> i = entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Map.Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);

            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return new TreeSubMap(true, null, true, true, null, true, true);
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return new TreeSubMap(false, fromKey, fromInclusive, false, toKey, toInclusive, false);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return new TreeSubMap(true, null, true, false, toKey, inclusive, false);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return new TreeSubMap(false, fromKey, inclusive, true, null, true, false);
    }

    @Override
    public String display(Style style) {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        buildString(root, "", true, true, sb, style);
        return sb.toString();
    }

    private String nodeText(N node) {
        return String.valueOf(node);
    }

    private void buildString(N node, String prefix, boolean isTail, boolean isRoot, StringBuilder sb, Style style) {
        if (node == null) {
            return;
        }

        String branch = (style == Style.UNICODE) ? "├── " : "+-- ";
        String lastBranch = (style == Style.UNICODE) ? "└── " : "\\-- ";
        String vertical = (style == Style.UNICODE) ? "│   " : "|   ";
        String space = "    ";

        sb.append(prefix);
        if (!isRoot) {
            sb.append(isTail ? lastBranch : branch);
        }

        sb.append('[').append(nodeText(node)).append(']').append('\n');

        boolean hasLeft = node.getLeft() != null;
        boolean hasRight = node.getRight() != null;

        if (!hasLeft && !hasRight) {
            return;
        }

        String childPrefix = prefix + (isRoot ? "" : isTail ? space : vertical);

        if (hasLeft && hasRight) {
            buildString(node.getLeft(), childPrefix, false, false, sb, style);
            buildString(node.getRight(), childPrefix, true, false, sb, style);

        } else if (hasLeft) {
            buildString(node.getLeft(), childPrefix, true, false, sb, style);

        } else {
            buildString(node.getRight(), childPrefix, true, false, sb, style);
        }
    }

    static final class KeySetView<K, V> extends AbstractSet<K> implements NavigableSet<K> {
        private final NavigableMap<K, V> m;

        KeySetView(NavigableMap<K, V> map) {
            this.m = map;
        }

        @Override
        public int size() {
            return m.size();
        }

        @Override
        public boolean isEmpty() {
            return m.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return m.containsKey(o);
        }

        @Override
        public void clear() {
            m.clear();
        }

        @Override
        public K lower(K e) {
            return m.lowerKey(e);
        }

        @Override
        public K floor(K e) {
            return m.floorKey(e);
        }

        @Override
        public K ceiling(K e) {
            return m.ceilingKey(e);
        }

        @Override
        public K higher(K e) {
            return m.higherKey(e);
        }

        @Override
        public K first() {
            return m.firstKey();
        }

        @Override
        public K last() {
            return m.lastKey();
        }

        @Override
        public Comparator<? super K> comparator() {
            return m.comparator();
        }

        @Override
        public K pollFirst() {
            Map.Entry<K, V> e = m.pollFirstEntry();
            return e == null ? null : e.getKey();
        }

        @Override
        public K pollLast() {
            Map.Entry<K, V> e = m.pollLastEntry();
            return e == null ? null : e.getKey();
        }

        @Override
        public Iterator<K> iterator() {
            return new Iterator<>() {
                final Iterator<Map.Entry<K, V>> i = m.entrySet().iterator();

                @Override
                public K next() {
                    return i.next().getKey();
                }

                @Override
                public void remove() {
                    i.remove();
                }

                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }
            };
        }

        @Override
        public Iterator<K> descendingIterator() {
            return new Iterator<>() {
                final Iterator<Map.Entry<K, V>> i = m.descendingMap().entrySet().iterator();

                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }

                @Override
                public K next() {
                    return i.next().getKey();
                }

                @Override
                public void remove() {
                    i.remove();
                }
            };
        }

        @Override
        public NavigableSet<K> descendingSet() {
            return new KeySetView<>(m.descendingMap());
        }

        @Override
        public NavigableSet<K> subSet(K fromElement, boolean fromInclusive, K toElement, boolean toInclusive) {
            return new KeySetView<>(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
        }

        @Override
        public NavigableSet<K> headSet(K toElement, boolean inclusive) {
            return new KeySetView<>(m.headMap(toElement, inclusive));
        }

        @Override
        public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
            return new KeySetView<>(m.tailMap(fromElement, inclusive));
        }

        @Override
        public SortedSet<K> subSet(K fromElement, K toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        @Override
        public SortedSet<K> headSet(K toElement) {
            return headSet(toElement, false);
        }

        @Override
        public SortedSet<K> tailSet(K fromElement) {
            return tailSet(fromElement, true);
        }
    }

    private abstract class MapIterator<T> implements Iterator<T> {
        N nextNode;
        N lastReturned;
        long expectedModCount;

        MapIterator(N first) {
            expectedModCount = modCount;
            lastReturned = null;
            nextNode = first;
        }

        public final boolean hasNext() {
            return nextNode != null;
        }

        final N stepForward() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (nextNode == null) {
                throw new NoSuchElementException();
            }
            lastReturned = nextNode;
            nextNode = successor(nextNode);
            return lastReturned;
        }

        public final void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            AbstractBinaryTreeMap.this.remove(lastReturned.getKey());
            expectedModCount = modCount;
            lastReturned = null;
        }
    }

    private final class EntryIterator extends MapIterator<Map.Entry<K, V>> {
        EntryIterator(N first) {
            super(first);
        }

        public Map.Entry<K, V> next() {
            return stepForward();
        }
    }

    private final class KeyIterator extends MapIterator<K> {
        KeyIterator(N first) {
            super(first);
        }

        public K next() {
            return stepForward().getKey();
        }
    }

    private final class ValueIterator extends MapIterator<V> {
        ValueIterator(N first) {
            super(first);
        }

        public V next() {
            return stepForward().getValue();
        }
    }

    private abstract class DescendingMapIterator<T> implements Iterator<T> {
        N nextNode;
        N lastReturned;
        long expectedModCount;

        DescendingMapIterator(N last) {
            expectedModCount = modCount;
            lastReturned = null;
            nextNode = last;
        }

        public final boolean hasNext() {
            return nextNode != null;
        }

        final N stepBackward() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (nextNode == null) {
                throw new NoSuchElementException();
            }
            lastReturned = nextNode;
            nextNode = predecessor(nextNode);
            return lastReturned;
        }

        public final void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();

            AbstractBinaryTreeMap.this.remove(lastReturned.getKey());
            expectedModCount = modCount;
            lastReturned = null;
        }
    }

    private final class DescendingEntryIterator extends DescendingMapIterator<Map.Entry<K, V>> {
        DescendingEntryIterator(N last) {
            super(last);
        }

        public Map.Entry<K, V> next() {
            return stepBackward();
        }
    }

    private final class DescendingKeyIterator extends DescendingMapIterator<K> {
        DescendingKeyIterator(N last) {
            super(last);
        }

        public K next() {
            return stepBackward().getKey();
        }
    }

    final class TreeSubMap extends AbstractMap<K, V> implements NavigableMap<K, V> {
        final K lo, hi;
        final boolean fromStart, toEnd;
        final boolean loInclusive, hiInclusive;
        final boolean descending;

        TreeSubMap(boolean fromStart, K lo, boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive, boolean descending) {
            if (!fromStart && !toEnd) {
                if (compare(lo, hi) > 0) throw new IllegalArgumentException("fromKey > toKey");
            }
            this.fromStart = fromStart;
            this.lo = lo;
            this.loInclusive = loInclusive;
            this.toEnd = toEnd;
            this.hi = hi;
            this.hiInclusive = hiInclusive;
            this.descending = descending;
        }

        boolean tooLow(Object key) {
            if (!fromStart) {
                int c = compare((K) key, lo);
                return c < 0 || (c == 0 && !loInclusive);
            }
            return false;
        }

        boolean tooHigh(Object key) {
            if (!toEnd) {
                int c = compare((K) key, hi);
                return c > 0 || (c == 0 && !hiInclusive);
            }
            return false;
        }

        boolean inRange(Object key) {
            return !tooLow(key) && !tooHigh(key);
        }

        @Override
        public V put(K key, V value) {
            if (!inRange(key)) throw new IllegalArgumentException("key out of range");
            return AbstractBinaryTreeMap.this.put(key, value);
        }

        @Override
        public V get(Object key) {
            if (!inRange(key)) return null;
            return AbstractBinaryTreeMap.this.get(key);
        }

        @Override
        public boolean containsKey(Object key) {
            return inRange(key) && AbstractBinaryTreeMap.this.containsKey(key);
        }

        @Override
        public V remove(Object key) {
            if (!inRange(key)) return null;
            return AbstractBinaryTreeMap.this.remove(key);
        }

        final N subCeiling(K key) {
            if (inRange(key)) return getCeilingNode(key);
            if (tooLow(key)) return absLowest();
            return null;
        }

        final N subHigher(K key) {
            if (inRange(key)) return getHigherNode(key);
            if (tooLow(key)) return absLowest();
            return null;
        }

        final N subFloor(K key) {
            if (inRange(key)) return getFloorNode(key);
            if (tooHigh(key)) return absHighest();
            return null;
        }

        final N subLower(K key) {
            if (inRange(key)) return getLowerNode(key);
            if (tooHigh(key)) return absHighest();
            return null;
        }

        @Override
        public Map.Entry<K, V> lowerEntry(K k) {
            N n = descending ? subHigher(k) : subLower(k);
            return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
        }

        @Override
        public K lowerKey(K k) {
            N n = descending ? subHigher(k) : subLower(k);
            return n == null ? null : n.getKey();
        }

        @Override
        public Map.Entry<K, V> floorEntry(K k) {
            N n = descending ? subCeiling(k) : subFloor(k);
            return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
        }

        @Override
        public K floorKey(K k) {
            N n = descending ? subCeiling(k) : subFloor(k);
            return n == null ? null : n.getKey();
        }

        @Override
        public Map.Entry<K, V> ceilingEntry(K k) {
            N n = descending ? subFloor(k) : subCeiling(k);
            return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
        }

        @Override
        public K ceilingKey(K k) {
            N n = descending ? subFloor(k) : subCeiling(k);
            return n == null ? null : n.getKey();
        }

        @Override
        public Map.Entry<K, V> higherEntry(K k) {
            N n = descending ? subLower(k) : subHigher(k);
            return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
        }

        @Override
        public K higherKey(K k) {
            N n = descending ? subLower(k) : subHigher(k);
            return n == null ? null : n.getKey();
        }

        @Override
        public Map.Entry<K, V> firstEntry() {
            N n = descending ? absHighest() : absLowest();
            return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
        }

        @Override
        public Map.Entry<K, V> lastEntry() {
            N n = descending ? absLowest() : absHighest();
            return n == null ? null : new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
        }

        @Override
        public Map.Entry<K, V> pollFirstEntry() {
            N n = descending ? absHighest() : absLowest();
            if (n == null) return null;
            Map.Entry<K, V> result = new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
            TreeSubMap.this.remove(n.getKey());
            return result;
        }

        @Override
        public Map.Entry<K, V> pollLastEntry() {
            N n = descending ? absLowest() : absHighest();
            if (n == null) return null;
            Map.Entry<K, V> result = new AbstractMap.SimpleImmutableEntry<>(n.getKey(), n.getValue());
            TreeSubMap.this.remove(n.getKey());
            return result;
        }

        @Override
        public NavigableSet<K> navigableKeySet() {
            return new KeySetView<>(this);
        }

        @Override
        public NavigableSet<K> descendingKeySet() {
            return descendingMap().navigableKeySet();
        }

        @Override
        public Comparator<? super K> comparator() {
            if (descending) {
                return Collections.reverseOrder(AbstractBinaryTreeMap.this.comparator);
            }
            return AbstractBinaryTreeMap.this.comparator;
        }

        @Override
        public SortedMap<K, V> subMap(K k, K k1) {
            return subMap(k, true, k1, false);
        }

        @Override
        public SortedMap<K, V> headMap(K k) {
            return headMap(k, false);
        }

        @Override
        public SortedMap<K, V> tailMap(K k) {
            return tailMap(k, true);
        }

        @Override
        public NavigableMap<K, V> descendingMap() {
            return new TreeSubMap(fromStart, lo, loInclusive, toEnd, hi, hiInclusive, !descending);
        }

        @Override
        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            if (!inRange(fromKey) || !inRange(toKey))
                throw new IllegalArgumentException("Requested bounds out of range");
            if (descending) {
                return new TreeSubMap(false, toKey, toInclusive, false, fromKey, fromInclusive, true);
            } else {
                return new TreeSubMap(false, fromKey, fromInclusive, false, toKey, toInclusive, false);
            }
        }

        @Override
        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            if (!inRange(toKey)) throw new IllegalArgumentException("Requested bounds out of range");
            if (descending) {
                return new TreeSubMap(false, toKey, inclusive, toEnd, hi, hiInclusive, true);
            } else {
                return new TreeSubMap(fromStart, lo, loInclusive, false, toKey, inclusive, false);
            }
        }

        @Override
        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            if (!inRange(fromKey)) throw new IllegalArgumentException("Requested bounds out of range");
            if (descending) {
                return new TreeSubMap(fromStart, lo, loInclusive, false, fromKey, inclusive, true);
            } else {
                return new TreeSubMap(false, fromKey, inclusive, toEnd, hi, hiInclusive, false);
            }
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return new AbstractSet<>() {

                @Override
                public Iterator<Map.Entry<K, V>> iterator() {
                    return new Iterator<>() {
                        N nextNode = descending ? absHighest() : absLowest();
                        N lastReturned = null;
                        long expectedModCount = modCount;

                        @Override
                        public boolean hasNext() {
                            return nextNode != null;
                        }

                        @Override
                        public Map.Entry<K, V> next() {
                            if (modCount != expectedModCount) {
                                throw new ConcurrentModificationException();
                            }
                            if (nextNode == null) {
                                throw new NoSuchElementException();
                            }

                            lastReturned = nextNode;

                            if (descending) {
                                nextNode = predecessor(nextNode);
                                if (nextNode != null && tooLow(nextNode.getKey())) {
                                    nextNode = null;
                                }
                            } else {
                                nextNode = successor(nextNode);
                                if (nextNode != null && tooHigh(nextNode.getKey())) {
                                    nextNode = null;
                                }
                            }

                            return lastReturned;
                        }

                        @Override
                        public void remove() {
                            if (lastReturned == null) {
                                throw new IllegalStateException();
                            }
                            if (modCount != expectedModCount) {
                                throw new ConcurrentModificationException();
                            }

                            TreeSubMap.this.remove(lastReturned.getKey());
                            expectedModCount = modCount;
                            lastReturned = null;
                        }
                    };
                }

                @Override
                public int size() {
                    int count = 0;
                    for (Map.Entry<K, V> ignored : this) {
                        count++;
                    }
                    return count;
                }
            };
        }

        @Override
        public K firstKey() {
            N e = descending ? absHighest() : absLowest();
            if (e == null) throw new NoSuchElementException();
            return e.getKey();
        }

        @Override
        public K lastKey() {
            N e = descending ? absLowest() : absHighest();
            if (e == null) throw new NoSuchElementException();
            return e.getKey();
        }

        final N absLowest() {
            N e = fromStart ? leftMostNode() : (loInclusive ? getCeilingNode(lo) : getHigherNode(lo));
            return (e == null || tooHigh(e.getKey())) ? null : e;
        }

        final N absHighest() {
            N e = toEnd ? rightMostNode() : (hiInclusive ? getFloorNode(hi) : getLowerNode(hi));
            return (e == null || tooLow(e.getKey())) ? null : e;
        }
    }
}
