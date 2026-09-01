package chaos.tree21.nary;

import chaos.tree21.core.SearchTreeSet;
import chaos.tree21.core.Style;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;


/**
 * Base Engine for B-Tree and B+Tree variants.
 * And also B-Tree* and B+Tree* variants
 * Fuses CLRS node arithmetic with Lehman & Yao concurrent/bottom-up memory layout.
 */
sealed abstract class AbstractNaryTreeSet<E, N extends AbstractNaryNode<E, N>> extends AbstractSet<E>
        implements SearchTreeSet<E>, Serializable, Cloneable permits BPlusTreeSet, BTreeSet {

    protected final int degree;
    protected final int maxKeys;
    protected final int minKeys;
    protected final Comparator<? super E> comparator;
    protected transient N root;
    protected transient int size;
    protected transient long modCount;

    protected AbstractNaryTreeSet(int degree, Comparator<? super E> comparator) {
        this.comparator = comparator;
        if (degree < 2 || degree > Integer.MAX_VALUE / 2) {
            throw new IllegalArgumentException("Degree must be at least 2 and less than Integer.MAX_VALUE/2");
        }
        this.degree = degree;
        this.maxKeys = (degree << 1) - 1;
        this.minKeys = degree - 1;
    }

    abstract void buildFromSorted(Iterator<E> it, float f);

    @SuppressWarnings("unchecked")
    protected int compare(E e1, E e2) {
        if (comparator != null) {
            return comparator.compare(e1, e2);
        }
        return ((Comparable<? super E>) e1).compareTo(e2);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    abstract N createNode(int degree, boolean isLeaf);

    @SuppressWarnings("unchecked")
    protected int searchNode(N node, E key) {
        if (node.keyCount < 12) { //actually faster.
            for (int i = 0; i < node.keyCount; i++) {
                int cmp = compare((E) node.keys[i], key);
                if (cmp == 0) return i;// Match found
                if (cmp > 0) return ~i;// Not found, insertion point is 'i' (Bitwise NOT to make it negative)
            }
            return ~node.keyCount;// Not found, belongs at the very end
        }
        // Arrays.binarySearch already returns ~insertionPoint for missing elements
        return Arrays.binarySearch((E[]) node.keys, 0, node.keyCount, key, comparator);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public void clear() {
        this.root = null;
        this.size = 0;
        this.modCount++;
    }

    @Override
    public boolean contains(Object o) {
        @SuppressWarnings("unchecked")
        E val = (E) o;
        N current = root;
        while (current != null) {
            int idx = searchNode(current, val);
            if (idx >= 0) return true;
            if (current.isLeaf()) return false;
            current = current.child[~idx];
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E first() {
        if (root == null) throw new NoSuchElementException();
        N current = root;
        while (!current.isLeaf()) current = current.child[0];
        return (E) current.keys[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public E last() {
        if (root == null) throw new NoSuchElementException();
        N current = root;
        while (!current.isLeaf()) current = current.child[current.keyCount];
        return (E) current.keys[current.keyCount - 1];
    }

    @Override
    public E pollFirst() {
        if (root == null) return null;
        E first = first();
        remove(first);
        return first;
    }

    @Override
    public E pollLast() {
        if (root == null) return null;
        E last = last();
        remove(last);
        return last;
    }

    @Override
    public E getFirst() {
        return first();
    }

    @Override
    public E getLast() {
        return last();
    }

    @Override
    public E removeFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        return pollFirst();
    }

    @Override
    public E removeLast() {
        if (isEmpty()) throw new NoSuchElementException();
        return pollLast();
    }

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException("Cannot force addFirst on a mathematically sorted tree.");
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException("Cannot force addLast on a mathematically sorted tree.");
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends E> collection) {
        Objects.requireNonNull(collection);
        if (this.size == 0 && !collection.isEmpty() && collection instanceof SortedSet<?> ss) {
            if (Objects.equals(this.comparator(), ss.comparator())) {
                buildFromSorted((Iterator<E>) collection.iterator(), 0.9f);
                return true;
            }
        }
        boolean modified = false;
        for (E e : collection) if (add(e)) modified = true;
        return modified;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Set<?> c)) return false;
        if (c.size() != size()) return false;
        if (c instanceof SortedSet<?> ss) {
            try {
                if (Objects.equals(this.comparator(), ss.comparator())) {
                    Iterator<E> it1 = this.iterator();
                    Iterator<?> it2 = ss.iterator();
                    while (it1.hasNext() && it2.hasNext()) {
                        if (!Objects.equals(it1.next(), it2.next())) return false;
                    }
                    return true;
                }
            } catch (ClassCastException | NullPointerException unused) {
                return false;
            }
        }
        return super.equals(o);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            AbstractNaryTreeSet<E, N> clone = (AbstractNaryTreeSet<E, N>) super.clone();
            clone.root = null;
            clone.size = 0;
            clone.modCount = 0;
            if (this.size > 0) {
                clone.buildFromSorted(this.iterator(), 0.9f); //90% of node filled
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
        for (E x : this) {
            s.writeObject(x);
        }
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int setSize = s.readInt();
        if (setSize > 0) {
            Iterator<E> it = new Iterator<>() {
                int count = 0;

                @Override
                public boolean hasNext() {
                    return count < setSize;
                }

                @Override
                @SuppressWarnings("unchecked")
                public E next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    try {
                        E value = (E) s.readObject();
                        count++;
                        return value;
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException("Failed to deserialize tree", e);
                    }
                }
            };
            buildFromSorted(it, 0.9f); // Pack to 90% on load!
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
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

        sb.append("[");
        for (int i = 0; i < node.keyCount; i++) {
            sb.append(node.keys[i]);
            if (i < node.keyCount - 1) sb.append(", ");
        }
        sb.append("]\n");

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
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new NarySubSet(fromElement, fromInclusive, toElement, toInclusive, false);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new NarySubSet(null, true, toElement, inclusive, false);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new NarySubSet(fromElement, inclusive, null, true, false);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new NarySubSet(null, true, null, true, true);
    }

    private final class NarySubSet extends AbstractSet<E> implements NavigableSet<E> {
        private final E lo;
        private final boolean loInclusive;
        private final E hi;
        private final boolean hiInclusive;
        private final boolean descending;

        NarySubSet(E lo, boolean loInclusive, E hi, boolean hiInclusive, boolean descending) {
            if (lo != null && hi != null && compare(lo, hi) > 0) {
                throw new IllegalArgumentException("fromKey > toKey");
            }
            this.lo = lo;
            this.loInclusive = loInclusive;
            this.hi = hi;
            this.hiInclusive = hiInclusive;
            this.descending = descending;
        }

        private boolean tooLow(Object key) {
            if (lo != null) {
                @SuppressWarnings("unchecked") int c = compare((E) key, lo);
                return c < 0 || (c == 0 && !loInclusive);
            }
            return false;
        }

        private boolean tooHigh(Object key) {
            if (hi != null) {
                @SuppressWarnings("unchecked") int c = compare((E) key, hi);
                return c > 0 || (c == 0 && !hiInclusive);
            }
            return false;
        }

        private boolean inRange(Object key) {
            return !tooLow(key) && !tooHigh(key);
        }

        private boolean inClosedRange(Object key) {
            boolean lowPass = (lo == null);
            if (!lowPass) {
                @SuppressWarnings("unchecked") int c = compare((E) key, lo);
                lowPass = (c >= 0);
            }
            boolean highPass = (hi == null);
            if (!highPass) {
                @SuppressWarnings("unchecked") int c = compare((E) key, hi);
                highPass = (c <= 0);
            }
            return lowPass && highPass;
        }

        @Override
        public boolean add(E e) {
            if (!inRange(e)) throw new IllegalArgumentException("Key out of range");
            return AbstractNaryTreeSet.this.add(e);
        }

        @Override
        public boolean contains(Object o) {
            @SuppressWarnings("unchecked") E e = (E) o;
            return inRange(e) && AbstractNaryTreeSet.this.contains(e);
        }

        @Override
        public boolean remove(Object o) {
            @SuppressWarnings("unchecked") E e = (E) o;
            return inRange(e) && AbstractNaryTreeSet.this.remove(e);
        }

        private E getAbsLowest() {
            if (AbstractNaryTreeSet.this.isEmpty()) return null;
            E min = (lo == null) ? AbstractNaryTreeSet.this.first() : (loInclusive ? AbstractNaryTreeSet.this.ceiling(lo) : AbstractNaryTreeSet.this.higher(lo));
            return (min != null && !tooHigh(min)) ? min : null;
        }

        private E getAbsHighest() {
            if (AbstractNaryTreeSet.this.isEmpty()) return null;
            E max = (hi == null) ? AbstractNaryTreeSet.this.last() : (hiInclusive ? AbstractNaryTreeSet.this.floor(hi) : AbstractNaryTreeSet.this.lower(hi));
            return (max != null && !tooLow(max)) ? max : null;
        }

        private E subCeiling(E e) {
            if (tooHigh(e)) return null;
            if (tooLow(e)) return getAbsLowest();
            E res = AbstractNaryTreeSet.this.ceiling(e);
            return (res != null && !tooHigh(res)) ? res : null;
        }

        private E subFloor(E e) {
            if (tooLow(e)) return null;
            if (tooHigh(e)) return getAbsHighest();
            E res = AbstractNaryTreeSet.this.floor(e);
            return (res != null && !tooLow(res)) ? res : null;
        }

        private E subHigher(E e) {
            if (tooHigh(e)) return null;
            if (tooLow(e)) return getAbsLowest();
            E res = AbstractNaryTreeSet.this.higher(e);
            return (res != null && !tooHigh(res)) ? res : null;
        }

        private E subLower(E e) {
            if (tooLow(e)) return null;
            if (tooHigh(e)) return getAbsHighest();
            E res = AbstractNaryTreeSet.this.lower(e);
            return (res != null && !tooLow(res)) ? res : null;
        }

        @Override
        public E ceiling(E e) { return descending ? subFloor(e) : subCeiling(e); }
        @Override
        public E floor(E e) { return descending ? subCeiling(e) : subFloor(e); }
        @Override
        public E higher(E e) { return descending ? subLower(e) : subHigher(e); }
        @Override
        public E lower(E e) { return descending ? subHigher(e) : subLower(e); }

        @Override
        public E first() {
            E min = descending ? getAbsHighest() : getAbsLowest();
            if (min == null) throw new NoSuchElementException();
            return min;
        }

        @Override
        public E last() {
            E max = descending ? getAbsLowest() : getAbsHighest();
            if (max == null) throw new NoSuchElementException();
            return max;
        }

        @Override
        public E pollFirst() {
            E e = descending ? getAbsHighest() : getAbsLowest();
            if (e != null) AbstractNaryTreeSet.this.remove(e);
            return e;
        }

        @Override
        public E pollLast() {
            E e = descending ? getAbsLowest() : getAbsHighest();
            if (e != null) AbstractNaryTreeSet.this.remove(e);
            return e;
        }

        @Override
        public int size() {
            int count = 0;
            for (E ignored : this) count++;
            return count;
        }

        @Override
        public boolean isEmpty() {
            return getAbsLowest() == null;
        }

        @Override
        public Comparator<? super E> comparator() {
            if (descending) return Collections.reverseOrder(AbstractNaryTreeSet.this.comparator);
            return AbstractNaryTreeSet.this.comparator;
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return new NarySubSet(lo, loInclusive, hi, hiInclusive, !descending);
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            if (!inClosedRange(fromElement) || !inClosedRange(toElement))
                throw new IllegalArgumentException("Requested bounds out of range");

            if (descending) return new NarySubSet(toElement, toInclusive, fromElement, fromInclusive, true);
            return new NarySubSet(fromElement, fromInclusive, toElement, toInclusive, false);
        }

        @Override
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            if (!inClosedRange(toElement)) throw new IllegalArgumentException("Requested bounds out of range");

            if (descending) return new NarySubSet(lo, loInclusive, toElement, inclusive, true);
            return new NarySubSet(lo, loInclusive, toElement, inclusive, false);
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            if (!inClosedRange(fromElement)) throw new IllegalArgumentException("Requested bounds out of range");

            if (descending) return new NarySubSet(fromElement, inclusive, hi, hiInclusive, true);
            return new NarySubSet(fromElement, inclusive, hi, hiInclusive, false);
        }

        @Override
        public SortedSet<E> subSet(E fromElement, E toElement) { return subSet(fromElement, true, toElement, false); }
        @Override
        public SortedSet<E> headSet(E toElement) { return headSet(toElement, false); }
        @Override
        public SortedSet<E> tailSet(E fromElement) { return tailSet(fromElement, true); }

        @Override
        public Iterator<E> iterator() { return new SubSetIterator(false); }

        @Override
        public Iterator<E> descendingIterator() { return new SubSetIterator(true); }

        private class SubSetIterator implements Iterator<E> {
            private long expectedModCount = AbstractNaryTreeSet.this.modCount;
            private E nextElement;
            private E lastReturned = null;
            private final boolean iterateDescending;

            SubSetIterator(boolean reverseCall) {
                // If the map is already descending, and we ask for reverse, it goes forward!
                this.iterateDescending = (descending != reverseCall);
                nextElement = this.iterateDescending ? getAbsHighest() : getAbsLowest();
            }

            @Override
            public boolean hasNext() { return nextElement != null; }

            @Override
            public E next() {
                if (expectedModCount != AbstractNaryTreeSet.this.modCount) throw new ConcurrentModificationException();
                if (nextElement == null) throw new NoSuchElementException();

                lastReturned = nextElement;
                if (iterateDescending) {
                    nextElement = AbstractNaryTreeSet.this.lower(lastReturned);
                    if (nextElement != null && tooLow(nextElement)) nextElement = null;
                } else {
                    nextElement = AbstractNaryTreeSet.this.higher(lastReturned);
                    if (nextElement != null && tooHigh(nextElement)) nextElement = null;
                }
                return lastReturned;
            }

            @Override
            public void remove() {
                if (lastReturned == null) throw new IllegalStateException();
                if (expectedModCount != AbstractNaryTreeSet.this.modCount) throw new ConcurrentModificationException();

                AbstractNaryTreeSet.this.remove(lastReturned);
                expectedModCount = AbstractNaryTreeSet.this.modCount;
                lastReturned = null;
            }
        }
    }
}
