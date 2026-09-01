package chaos.tree21.binary;

import chaos.tree21.core.SearchTreeSet;
import chaos.tree21.core.Style;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The core engine for all Binary Trees.
 * I used F-Form polymorphism to avoid unwanted casting
 * It is commonly known as CRTP in C++
 * For people wondering no docs it's just that these API work same as of like Java Tree behaves.
 */
sealed abstract class AbstractBinaryTreeSet<E, N extends AbstractBinaryNode<E, N>> extends AbstractSet<E>
        implements SearchTreeSet<E>, Cloneable, Serializable
        permits AvlTreeSet, RedBlackTreeSet {


    protected final Comparator<? super E> comparator;
    protected transient N root;
    protected transient int size = 0;
    protected transient long modCount = 0;

    protected AbstractBinaryTreeSet() {
        this.comparator = null;
    }

    protected AbstractBinaryTreeSet(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    protected static int computeRedLevel(int sz) {
        int level = 0;
        for (int m = sz - 1; m >= 0; m = (m / 2) - 1) level++;
        return level;
    }

    final void buildFromSorted(int size, Iterator<? extends E> it) {
        this.size = size;
        root = buildFromSortedRecursive(0, 0, size - 1, computeRedLevel(size), it);
        this.modCount++;
    }

    final N buildFromSortedRecursive(int level, int lo, int hi, int redLevel, Iterator<? extends E> it) {
        if (hi < lo) return null;
        int mid = lo + ((hi - lo) >>> 1);
        N left = null;
        if (lo < mid) {
            left = buildFromSortedRecursive(level + 1, lo, mid - 1, redLevel, it);
        }
        E entry = it.next();
        N middle = createNode(entry);
        if (left != null) {
            middle.left = left;
            left.parent = middle;
        }

        if (mid < hi) {
            N right = buildFromSortedRecursive(level + 1, mid + 1, hi, redLevel, it);
            middle.right = right;
            right.parent = middle;
        }
        afterNodeBuiltFromSorted(middle, level, redLevel);
        return middle;
    }

    abstract N createNode(E entry);

    abstract void afterNodeBuiltFromSorted(N node, int level, int redLevel);

    @SuppressWarnings("unchecked")
    protected int compare(E e1, E e2) {
        if (comparator != null) {
            return comparator.compare(e1, e2);
        }
        return ((Comparable<? super E>) e1).compareTo(e2);
    }


    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        long expectedModCount = modCount;

        for (N node = getFirstNode(); node != null; node = successor(node)) {
            action.accept(node.value);
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }
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
        root = null;
        size = 0;
        modCount++;
    }

    @Override
    public E getFirst() {
        if (root == null) throw new NoSuchElementException();
        return getFirstNode().value;
    }

    @Override
    public E getLast() {
        if (root == null) throw new NoSuchElementException();
        return getLastNode().value;
    }

    @Override
    public boolean contains(Object o) {
        @SuppressWarnings("unchecked")
        E val = (E) o;
        // I removed the contains invoking nodeFinder with code directly
        N curr = root;
        if(root == null) return false;
        while (curr != null){
            int cmp = compare(val, curr.value);
            if(cmp == 0) return true;
            else if (cmp > 0) curr = curr.right;
            else curr = curr.left;
        }
        return false;
    }

    //Prior to work of NPE must be done
    protected N nodeFinder(E val) {
        if (root == null) return null;
        N current = root;
        int cmp = 0;
        while (current != null) {
            cmp = compare(val, current.value);
            if (cmp == 0) return current;
            else if (cmp > 0) current = current.right;
            else current = current.left;
        }
        return null;
    }

    @Override
    public E floor(E e) {
        if (root == null) return null;
        N current = root;
        N prevCurrent = null;
        while (current != null) {
            int cmp = compare(e, current.value);
            if (cmp == 0) return current.value;
            else if (cmp < 0) current = current.left;
            else {
                prevCurrent = current;
                current = current.right;
            }
        }
        return prevCurrent == null ? null : prevCurrent.value;
    }

    @Override
    public E ceiling(E e) {
        if (root == null) return null;
        N current = root;
        N prevCurrent = null;
        while (current != null) {
            int cmp = compare(e, current.value);
            if (cmp == 0) return current.value;
            else if (cmp > 0) current = current.right;
            else {
                prevCurrent = current;
                current = current.left;
            }
        }
        return prevCurrent == null ? null : prevCurrent.value;
    }


    @Override
    public E higher(E e) {
        if (root == null) return null;
        N current = root;
        N prevNode = null;
        while (current != null) {
            int cmp = compare(e, current.value);
            if (cmp >= 0) current = current.right;
            else {
                prevNode = current;
                current = current.left;
            }
        }
        return prevNode == null ? null : prevNode.value;
    }

    @Override
    public E lower(E e) {
        if (root == null) return null;
        N current = root;
        N prevNode = null;
        while (current != null) {
            int cmp = compare(e, current.value);
            if (cmp <= 0) current = current.left;
            else {
                prevNode = current;
                current = current.right;
            }
        }
        return prevNode == null ? null : prevNode.value;
    }

    @Override
    public E pollFirst() {
        if (root == null) return null;
        E e = getFirstNode().value;
        remove(e);
        return e;
    }

    @Override
    public E pollLast() {
        if (root == null) return null;
        E e = getLastNode().value;
        remove(e);
        return e;
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

    private N getFirstNode() {
        if (root == null) return null;
        N current = root;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    protected N successor(N node) {
        if (node == null) return null;
        if (node.right != null) {
            N current = node.right;
            while (current.left != null) {
                current = current.left;
            }
            return current;
        }
        N parent = node.parent;
        N current = node;
        while (parent != null && current == parent.right) {
            current = parent;
            parent = parent.parent;
        }
        return parent;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private N nextNode = getFirstNode();
            private E lastReturned = null;
            private long expectedModCount = modCount;

            @Override
            public boolean hasNext() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return nextNode != null;
            }

            @Override
            public void remove() {
                if (lastReturned == null) {
                    throw new IllegalStateException();
                }
                AbstractBinaryTreeSet.this.remove(lastReturned);
                lastReturned = null;
                expectedModCount = modCount;
            }

            @Override
            public E next() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                if (nextNode == null) {
                    throw new NoSuchElementException();
                }
                lastReturned = nextNode.value;
                nextNode = successor(nextNode);
                return lastReturned;
            }
        };
    }

    protected N predecessor(N node) {
        if (node == null) return null;
        if (node.left != null) {
            N current = node.left;
            while (current.right != null) {
                current = current.right;
            }
            return current;
        }
        N parent = node.parent;
        N current = node;
        while (parent != null && current == parent.left) {
            current = parent;
            parent = parent.parent;
        }
        return parent;
    }

    private N getLastNode() {
        if (root == null) return null;
        N current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new Iterator<>() {
            private N nextNode = getLastNode();
            private E lastReturned = null;
            private long expectedModCount = modCount;

            @Override
            public boolean hasNext() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return nextNode != null;
            }

            @Override
            public void remove() {
                if (lastReturned == null) {
                    throw new IllegalStateException();
                }
                AbstractBinaryTreeSet.this.remove(lastReturned);
                lastReturned = null;
                expectedModCount = modCount;
            }

            @Override
            public E next() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                if (nextNode == null) {
                    throw new NoSuchElementException();
                }
                lastReturned = nextNode.value;
                nextNode = predecessor(nextNode);
                return lastReturned;
            }
        };
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (this.size == 0 && !c.isEmpty() && c instanceof SortedSet<?> sortedSet) {
            if (Objects.equals(this.comparator(), sortedSet.comparator())) {
                buildFromSorted(c.size(), c.iterator());
                return true;
            }
        }
        boolean modified = false;
        for (E e : c) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    protected void rotateLeft(N p) {
        N r = p.right;
        p.right = r.left;
        if (r.left != null) {
            r.left.parent = p;
        }
        r.parent = p.parent;
        if (p.parent == null) {
            root = r;
        } else if (p.parent.left == p) {
            p.parent.left = r;
        } else {
            p.parent.right = r;
        }

        r.left = p;
        p.parent = r;
    }

    protected void rotateRight(N p) {
        N l = p.left;
        p.left = l.right;
        if (l.right != null) {
            l.right.parent = p;
        }
        l.parent = p.parent;
        if (p.parent == null) {
            root = l;
        } else if (p.parent.right == p) {
            p.parent.right = l;
        } else {
            p.parent.left = l;
        }
        l.right = p;
        p.parent = l;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public E first() {
        return getFirst();
    }

    @Override
    public E last() {
        return getLast();
    }

    public Stream<E> rangeStream(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, true).stream();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new TreeSubSet(fromElement, fromInclusive, toElement, toInclusive, false);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new TreeSubSet(null, false, toElement, inclusive, false);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new TreeSubSet(fromElement, inclusive, null, false, false);
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new TreeSubSet(null, false, null, false, true);
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
    public String toString() {
        if (isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
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
        return String.valueOf(node.value);
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
        sb.append(nodeText(node)).append('\n');

        boolean hasLeft = node.left != null;
        boolean hasRight = node.right != null;

        if (!hasLeft && !hasRight) {
            return;
        }

        String childPrefix = prefix + (isRoot ? "" : isTail ? space : vertical);

        if (hasLeft && hasRight) {
            buildString(node.left, childPrefix, false, false, sb, style);
            buildString(node.right, childPrefix, true, false, sb, style);

        } else if (hasLeft) {
            buildString(node.left, childPrefix, true, false, sb, style);

        } else {
            buildString(node.right, childPrefix, true, false, sb, style);
        }
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
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            AbstractBinaryTreeSet<E, N> clone = (AbstractBinaryTreeSet<E, N>) super.clone();
            clone.root = null;
            clone.size = 0;
            clone.modCount = 0;

            if (this.size > 0) {
                clone.buildFromSorted(this.size, this.iterator());
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
            Iterator<E> it = new Iterator<E>() {
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
                        throw new RuntimeException("Failed to deserialize tree data", e);
                    }
                }
            };
            buildFromSorted(setSize, it);
        }
    }

    protected final class TreeSubSet extends AbstractSet<E> implements NavigableSet<E> {
        private final E lo;
        private final boolean loInclusive;
        private final E hi;
        private final boolean hiInclusive;
        private final boolean descending;

        TreeSubSet(E lo, boolean loInclusive, E hi, boolean hiInclusive, boolean descending) {
            if (lo != null && hi != null && compare(lo, hi) > 0) {
                throw new IllegalArgumentException();
            }
            this.lo = lo;
            this.loInclusive = loInclusive;
            this.hi = hi;
            this.hiInclusive = hiInclusive;
            this.descending = descending;
        }

        private boolean tooLow(E e) {
            return lo != null && (compare(e, lo) < 0 || (compare(e, lo) == 0 && !loInclusive));
        }

        private boolean tooHigh(E e) {
            return hi != null && (compare(e, hi) > 0 || (compare(e, hi) == 0 && !hiInclusive));
        }

        private boolean inRange(E e) {
            return !tooLow(e) && !tooHigh(e);
        }

        @Override
        public boolean add(E e) {
            if (!inRange(e)) {
                throw new IllegalArgumentException("Key out of range");
            }
            return AbstractBinaryTreeSet.this.add(e);
        }

        @Override
        public boolean contains(Object o) {
            @SuppressWarnings("unchecked")
            E e = (E) o;
            return inRange(e) && AbstractBinaryTreeSet.this.contains(e);
        }


        @Override
        public E lower(E e) {
            if (tooLow(e)) return null;
            if (tooHigh(e)) return isEmpty() ? null : last();
            E result = AbstractBinaryTreeSet.this.lower(e);
            return (result != null && inRange(result)) ? result : null;
        }

        @Override
        public E floor(E e) {
            if (tooLow(e)) return null;
            if (tooHigh(e)) return isEmpty() ? null : last();
            E result = AbstractBinaryTreeSet.this.floor(e);
            return (result != null && inRange(result)) ? result : null;
        }

        @Override
        public E ceiling(E e) {
            if (tooHigh(e)) return null;
            if (tooLow(e)) return isEmpty() ? null : first();
            E result = AbstractBinaryTreeSet.this.ceiling(e);
            return (result != null && inRange(result)) ? result : null;
        }

        @Override
        public E higher(E e) {
            if (tooHigh(e)) return null;
            if (tooLow(e)) return isEmpty() ? null : first();
            E result = AbstractBinaryTreeSet.this.higher(e);
            return (result != null && inRange(result)) ? result : null;
        }

        @Override
        public Iterator<E> iterator() {
            return descending ? descendingIteratorImpl() : ascendingIterator();
        }

        @Override
        public Iterator<E> descendingIterator() {
            return descending ? ascendingIterator() : descendingIteratorImpl();
        }

        private Iterator<E> ascendingIterator() {
            return new Iterator<>() {
                private N nextNode = getStartNode();
                private E val = null;
                private long expectedModCount = modCount;

                private N getStartNode() {
                    if (lo == null) return getFirstNode();
                    N curr = root;
                    N bestMatch = null;
                    while (curr != null) {
                        int cmp = compare(lo, curr.value);
                        if (cmp < 0 || (cmp == 0 && loInclusive)) {
                            bestMatch = curr;
                            curr = curr.left;
                        } else {
                            curr = curr.right;
                        }
                    }
                    return bestMatch;
                }

                @Override
                public boolean hasNext() {
                    if (modCount != expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    return nextNode != null && inRange(nextNode.value);
                }

                @Override
                public void remove() {
                    if (val == null) {
                        throw new IllegalStateException();
                    }
                    AbstractBinaryTreeSet.this.remove(val);
                    val = null;
                    expectedModCount = modCount;
                }

                @Override
                public E next() {
                    if (modCount != expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    if (nextNode == null) {
                        throw new NoSuchElementException();
                    }
                    if (!hasNext()) throw new NoSuchElementException();
                    val = nextNode.value;
                    nextNode = successor(nextNode);
                    return val;
                }
            };
        }

        private Iterator<E> descendingIteratorImpl() {
            return new Iterator<E>() {
                private N nextNode = getEndNode();
                private E val = null;
                private long expectedModCount = modCount;

                private N getEndNode() {
                    if (hi == null) return getLastNode();

                    N curr = root;
                    N match = null;

                    while (curr != null) {
                        int cmp = compare(hi, curr.value);
                        if (cmp > 0 || (cmp == 0 && hiInclusive)) {
                            match = curr;
                            curr = curr.right;
                        } else {
                            curr = curr.left;
                        }
                    }
                    return match;
                }

                @Override
                public boolean hasNext() {
                    return nextNode != null && inRange(nextNode.value);
                }

                @Override
                public void remove() {
                    if (val == null) {
                        throw new IllegalStateException();
                    }
                    AbstractBinaryTreeSet.this.remove(val);
                    val = null;
                    expectedModCount = modCount;
                }

                @Override
                public E next() {
                    if (modCount != expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    if (nextNode == null) {
                        throw new NoSuchElementException();
                    }
                    if (!hasNext()) throw new NoSuchElementException();
                    val = nextNode.value;
                    nextNode = predecessor(nextNode);
                    return val;
                }
            };
        }

        @Override
        public Spliterator<E> spliterator() {
            return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED);
        }

        @Override
        public boolean remove(Object o) {
            if (!contains(o)) return false; // Out of bounds or not found
            return AbstractBinaryTreeSet.this.remove(o);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            List<E> toRemove = new ArrayList<>();
            for (E e : this) {
                if (!c.contains(e)) toRemove.add(e);
            }
            boolean modified = false;
            for (E e : toRemove) modified |= remove(e);
            return modified;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean modified = false;
            for (Object o : c) modified |= remove(o);
            return modified;
        }

        @Override
        public int size() {
            int count = 0;
            for (E e : this) {
                count++;
            }
            return count;
        }

        @Override
        public E first() {
            Iterator<E> i = iterator();
            if (!i.hasNext()) throw new NoSuchElementException();
            return i.next();
        }

        @Override
        public E last() {
            Iterator<E> i = descendingIterator();
            if (!i.hasNext()) throw new NoSuchElementException();
            return i.next();
        }

        @Override
        public E pollFirst() {
            if (isEmpty()) return null;
            E e = first();
            AbstractBinaryTreeSet.this.remove(e);
            return e;
        }

        @Override
        public E pollLast() {
            if (isEmpty()) return null;
            E e = last();
            AbstractBinaryTreeSet.this.remove(e);
            return e;
        }

        @Override
        public Comparator<? super E> comparator() {
            Comparator<? super E> cmp = AbstractBinaryTreeSet.this.comparator;
            if (!descending) return cmp;
            return cmp == null ? Collections.reverseOrder() : Collections.reverseOrder(cmp);
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return new TreeSubSet(lo, loInclusive, hi, hiInclusive, !descending);
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            if (!inRange(fromElement) || !inRange(toElement))
                throw new IllegalArgumentException("Requested bounds are outside current window");
            return new TreeSubSet(fromElement, fromInclusive, toElement, toInclusive, descending);
        }

        @Override
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            if (!inRange(toElement)) throw new IllegalArgumentException("Requested bound is outside current window");
            return new TreeSubSet(lo, loInclusive, toElement, inclusive, descending);
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            if (!inRange(fromElement)) throw new IllegalArgumentException("Requested bound is outside current window");
            return new TreeSubSet(fromElement, inclusive, hi, hiInclusive, descending);
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
    }
    //Play with it, destroy with it, LOL!!
}