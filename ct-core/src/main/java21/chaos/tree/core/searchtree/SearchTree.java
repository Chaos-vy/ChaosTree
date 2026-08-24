package chaos.tree.core.searchtree;

import chaos.tree.core.Tree;

import java.util.*;
import java.util.stream.Stream;

/**
 * Java 21 specific version of SearchTree.
 * Natively integrates with SequencedSet!
 */
public interface SearchTree<T extends Comparable<? super T>> extends Tree, NavigableSet<T>, SequencedSet<T> {
    void insert(T value);
    void insertAll(Collection<? extends T> values);
    boolean contains(T value);
    boolean containsAllElements(Collection<? extends T> values);
    void delete(T value);
    void deleteAll(Collection<? extends T> values);
    int size();
    int height();
    boolean isEmpty();
    void clear();
    Stream<T> stream();
    Iterator<T> iterator();
    T min();
    T max();
    T floor(T value);
    T ceil(T value);
    T successor(T value);
    T predecessor(T value);
    T kthSmallest(int k);
    T pollMin();
    T pollMax();
    List<T> range(T fromInclusive, T toExclusive);
    void retainAllElements(Collection<? extends T> values);
    void mergeAll(Collection<? extends T> values);
    List<T> toList();
    Stream<T> rangeStream(T fromInclusive, T toExclusive);
    String toString(PrintStyle style);

    @Override
    default boolean add(T t) {
        int oldSize = size();
        insert(t);
        return size() != oldSize;
    }

    @Override
    default boolean remove(Object o) {
        try {
            @SuppressWarnings("unchecked")
            T val = (T) o;
            int oldSize = size();
            delete(val);
            return size() != oldSize;
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    @Override
    default boolean contains(Object o) {
        try {
            @SuppressWarnings("unchecked")
            T val = (T) o;
            return contains(val);
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    @Override default T first() { return min(); }
    @Override default T last() { return max(); }
    @Override default T lower(T t) { return predecessor(t); }
    @Override default T higher(T t) { return successor(t); }
    @Override default T ceiling(T t) { return ceil(t); }
    @Override default T pollFirst() { return pollMin(); }
    @Override default T pollLast() { return pollMax(); }
    @Override default Comparator<? super T> comparator() { return null; }
    
    @Override
    default Object[] toArray() {
        return stream().toArray();
    }

    @Override
    default <T1> T1[] toArray(T1[] a) {
        return toList().toArray(a);
    }

    @Override
    default boolean addAll(Collection<? extends T> c) {
        boolean modified = false;
        for (T e : c) {
            if (add(e)) modified = true;
        }
        return modified;
    }

    @Override
    default boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) return false;
        }
        return true;
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        List<T> toDelete = new ArrayList<>();
        for (T e : this) {
            if (!c.contains(e)) toDelete.add(e);
        }
        for (T e : toDelete) remove(e);
        return !toDelete.isEmpty();
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            if (remove(e)) modified = true;
        }
        return modified;
    }



    @Override
    default T removeFirst() {
        return pollMin();
    }

    @Override
    default T removeLast() {
        return pollMax();
    }

    // UME from here totaling 10 UME Some are reasonable constraints and some have been not worked on. Contribution is
    // allowed, but they must not heavily cost the Memory, gc , Time Complexity.
    @Override
    default void addFirst(T t) {
        throw new UnsupportedOperationException("SearchTrees sort naturally; cannot explicitly addFirst");
    }

    @Override
    default void addLast(T t) {
        throw new UnsupportedOperationException("SearchTrees sort naturally; cannot explicitly addLast");
    }

    @Override
    default NavigableSet<T> reversed() {
        throw new UnsupportedOperationException("Descending views are not supported yet.");
    }

    @Override
    default NavigableSet<T> descendingSet() {
        throw new UnsupportedOperationException("Descending views are not supported yet.");
    }



    @Override
    default NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        throw new UnsupportedOperationException("Subset views are not supported.");
    }

    @Override
    default NavigableSet<T> headSet(T toElement, boolean inclusive) {
        throw new UnsupportedOperationException("Subset views are not supported.");
    }

    @Override
    default NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        throw new UnsupportedOperationException("Subset views are not supported.");
    }

    @Override
    default SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException("Subset views are not supported.");
    }

    @Override
    default SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException("Subset views are not supported.");
    }

    @Override
    default SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException("Subset views are not supported.");
    }
}
