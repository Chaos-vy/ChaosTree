package chaos.tree21.binary;

import chaos.tree21.core.SearchTreeSet;
import chaos.tree21.core.Style;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

/**
 * The core engine for all Binary Trees.
 * I used F-Form polymorphism to avoid unwanted casting
 * It is commonly known as CRTP in C++
 */
public sealed abstract class AbstractBinaryTreeSet<E, N extends AbstractBinaryNode<E, N>>
        implements SearchTreeSet<E>
        permits AvlTreeSet, RedBlackTreeSet {


    protected N root;
    protected int size = 0;
    protected long modCount = 0;
    protected int cachedHashcode = 0;
    protected final Comparator<? super E> comparator;


    protected AbstractBinaryTreeSet() {
        this.comparator = null;
    }

    protected AbstractBinaryTreeSet(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }


    @SuppressWarnings("unchecked")
    protected int compare(E e1, E e2) {
        if (comparator != null) {
            return comparator.compare(e1, e2);
        }
        return ((Comparable<? super E>) e1).compareTo(e2);
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
        cachedHashcode = 0;
    }

    @Override
    public E getFirst() {
        if (root == null) throw new NoSuchElementException();
        return getFirstNode().getValue();
    }

    @Override
    public E getLast() {
        if (root == null) throw new NoSuchElementException();
        return getLastNode().getValue();
    }

    @Override
    public boolean contains(Object o) {
        if(root == null || o == null) return false;
        try {
            @SuppressWarnings("unchecked")
            E val = (E) o;
            return nodeFinder(val) != null;
        } catch (ClassCastException e) {
            return false;
        }
    }
    //Prior to work of NPE must be done
    protected N nodeFinder(E val){
        if(root == null) return null;
        N current = root;
        int cmp =0;
        while (current != null){
            cmp = compare(val, current.getValue());
            if(cmp == 0) return current;
            else if (cmp > 0) current = current.getRight();
            else current = current.getLeft();
        }
        return null;
    }

    @Override
    public E floor(E e) {
        if (root == null || e == null) return null;
        N current = root;
        N prevCurrent = null;
        while (current != null) {
            int cmp = compare(e, current.getValue());
            if (cmp == 0) return current.getValue();
            else if (cmp < 0) current = current.getLeft();
            else {
                prevCurrent = current;
                current = current.getRight();
            }
        }
        return prevCurrent == null ? null : prevCurrent.getValue();
    }

    @Override
    public E ceiling(E e) {
        if (root == null || e == null) return null;
        N current = root;
        N prevCurrent = null;
        while (current != null) {
            int cmp = compare(e, current.getValue());
            if (cmp == 0) return current.getValue();
            else if (cmp > 0) current = current.getRight();
            else {
                prevCurrent = current;
                current = current.getLeft();
            }
        }
        return prevCurrent == null ? null : prevCurrent.getValue();
    }

    @Override
    public E higher(E e) {
        if (root == null || e == null) return null;
        N current = root;
        N prevNode = null;
        while (current != null) {
            int cmp = compare(e, current.getValue());
            if (cmp >= 0) current = current.getRight();
            else {
                prevNode = current;
                current = current.getLeft();
            }
        }
        return prevNode == null ? null : prevNode.getValue();
    }

    @Override
    public E lower(E e) {
        if (root == null || e == null) return null;
        N current = root;
        N prevNode = null;
        while (current != null) {
            int cmp = compare(e, current.getValue());
            if (cmp <= 0) current = current.getLeft();
            else {
                prevNode = current;
                current = current.getRight();
            }
        }
        return prevNode == null ? null : prevNode.getValue();
    }

    @Override
    public E pollFirst() {
        if (isEmpty()) return null;
        E first = getFirst();
        remove(first);
        return first;
    }

    @Override
    public E pollLast() {
        if (isEmpty()) return null;
        E last = getLast();
        remove(last);
        return last;
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
        N current = root;
        while(current.getLeft() != null){
            current = current.getLeft();
        }
        return current;
    }

    protected N successor(N node) {
        if (node == null) return null;
        if (node.getRight() != null) {
            N current = node.getRight();
            while (current.getLeft() != null) {
                current = current.getLeft();
            }
            return current;
        }
        N parent = node.getParent();
        N current = node;
        while (parent != null && current == parent.getRight()) {
            current = parent;
            parent = parent.getParent();
        }
        return parent;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private N nextNode = getFirstNode();
            private final long expectedModCount = modCount;
            @Override
            public boolean hasNext() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return nextNode != null;
            }
            @Override
            public E next() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                if (nextNode == null) {
                    throw new NoSuchElementException();
                }
                E value = nextNode.getValue();
                nextNode = successor(nextNode);
                return value;
            }
        };
    }

    protected N predecessor(N node) {
        if (node == null) return null;
        if (node.getLeft() != null) {
            N current = node.getLeft();
            while (current.getRight() != null) {
                current = current.getRight();
            }
            return current;
        }
        N parent = node.getParent();
        N current = node;
        while (parent != null && current == parent.getLeft()) {
            current = parent;
            parent = parent.getParent();
        }
        return parent;
    }

    private N getLastNode() {
        N current = root;
        while(current.getRight() != null){
            current = current.getRight();
        }
        return current;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new Iterator<E>() {
            private N nextNode = getLastNode();
            private final long expectedModCount = modCount;
            @Override
            public boolean hasNext() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return nextNode != null;
            }

            @Override
            public E next() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                if (nextNode == null) {
                    throw new NoSuchElementException();
                }
                E value = nextNode.getValue();
                nextNode = predecessor(nextNode);
                return value;
            }
        };
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object element : collection) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean modified = false;
        for (E e : collection) if (add(e)) modified = true;
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean modified = false;
        for(Object o : collection) if(remove(o)) modified = true;
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        List<E> snapshot = new ArrayList<>(this);
        boolean modified = false;
        for (E element : snapshot) {
            if (!collection.contains(element)) {
                remove(element);
                modified = true;
            }
        }
        return modified;
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
        private boolean inRange(E e) {
            if (lo != null) {
                int cmp = compare(e, lo);
                if (cmp < 0 || (cmp == 0 && !loInclusive)) return false;
            }
            if (hi != null) {
                int cmp = compare(e, hi);
                return cmp <= 0 && (cmp != 0 || hiInclusive);
            }
            return true;
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
            try {
                @SuppressWarnings("unchecked")
                E e = (E) o;
                return inRange(e) && AbstractBinaryTreeSet.this.contains(e);
            } catch (ClassCastException ex) {
                return false;
            }
        }


        @Override
        public E lower(E e) {
            E result = AbstractBinaryTreeSet.this.lower(e);
            return (result != null && inRange(result)) ? result : null;
        }

        @Override
        public E floor(E e) {
            E result = AbstractBinaryTreeSet.this.floor(e);
            return (result != null && inRange(result)) ? result : null;
        }

        @Override
        public E ceiling(E e) {
            E result = AbstractBinaryTreeSet.this.ceiling(e);
            return (result != null && inRange(result)) ? result : null;
        }

        @Override
        public E higher(E e) {
            E result = AbstractBinaryTreeSet.this.higher(e);
            return (result != null && inRange(result)) ? result : null;
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<>() {
                private N nextNode = getStartNode();
                private N getStartNode() {
                    if (lo == null) return getFirstNode();
                    N curr = root;
                    N bestMatch = null;
                    while (curr != null) {
                        int cmp = compare(lo, curr.getValue());
                        if (cmp < 0 || (cmp == 0 && loInclusive)) {
                            bestMatch = curr;
                            curr = curr.getLeft();
                        } else {
                            curr = curr.getRight();
                        }
                    }
                    return bestMatch;
                }

                @Override
                public boolean hasNext() {
                    return nextNode != null && inRange(nextNode.getValue());
                }
                @Override
                public E next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    E val = nextNode.getValue();
                    nextNode = successor(nextNode);
                    return val;
                }
            };
        }
        @Override
        public Iterator<E> descendingIterator() {
            return new Iterator<E>() {
                private N nextNode = getEndNode();

                private N getEndNode() {
                    if (hi == null) return getLastNode();

                    N curr = root;
                    N match = null;

                    while (curr != null) {
                        int cmp = compare(hi, curr.getValue());
                        if (cmp > 0 || (cmp == 0 && hiInclusive)) {
                            match = curr;
                            curr = curr.getRight();
                        } else {
                            curr = curr.getLeft();
                        }
                    }
                    return match;
                }

                @Override
                public boolean hasNext() {
                    return nextNode != null && inRange(nextNode.getValue());
                }

                @Override
                public E next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    E val = nextNode.getValue();
                    nextNode = predecessor(nextNode);
                    return val;
                }
            };
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
            E e = first();
            AbstractBinaryTreeSet.this.remove(e);
            return e;
        }

        @Override
        public E pollLast() {
            E e = last();
            AbstractBinaryTreeSet.this.remove(e);
            return e;
        }

        @Override
        public Comparator<? super E> comparator() {
            return AbstractBinaryTreeSet.this.comparator;
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return new TreeSubSet(lo, loInclusive, hi, hiInclusive, !descending);
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            if (!inRange(fromElement) || !inRange(toElement)) throw new IllegalArgumentException("Requested bounds are outside current window");
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


    @Override
    public String print() {
        return toString(Style.ASCII);
    }

    public String toString(Style style) {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        buildString(root, "", true, true, sb, style);
        return sb.toString();
    }

    /**
     * Returns the text used to render the supplied node in {@link #toString()}.
     *
     * @param node the node to render; must not be {@code null}
     * @throws NullPointerException if {@code node} is {@code null}; callers should
     *                              pass only nodes that were checked during tree rendering
     */

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
        sb.append('\n');

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

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException("Cannot force addFirst on a mathematically sorted tree.");
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException("Cannot force addLast on a mathematically sorted tree.");
    }
    @Override
    public int hashCode() {
        return cachedHashcode;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Set<?> c)) return false;
        if (c.size() != size()) return false;

        try {
            return containsAll(c);
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        int i = 0;
        Object[] result = a;
        for (E e : this) {
            result[i++] = e;
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        int i = 0;
        for (E e : this) {
            array[i++] = e;
        }
        return array;
    }
}