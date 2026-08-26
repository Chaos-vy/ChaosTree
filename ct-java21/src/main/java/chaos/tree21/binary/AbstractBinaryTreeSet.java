package chaos.tree21.binary;

import chaos.tree21.core.SearchTreeSet;

import java.util.*;

/**
 * The core engine for all Binary Trees.
 * I used F-Form polymorphism to avoid unwanted casting
 * It is commonly known as CRTP in C++
 */
public sealed abstract class AbstractBinaryTreeSet<E, N extends AbstractBinaryNode<E, N>>
        implements SearchTreeSet<E>
        permits AvlTreeSet, RedBlackTreeSet, TreapTreeSet {


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
    public void addFirst(E e) {
        throw new UnsupportedOperationException("Cannot force addFirst on a mathematically sorted tree.");
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException("Cannot force addLast on a mathematically sorted tree.");
    }
}