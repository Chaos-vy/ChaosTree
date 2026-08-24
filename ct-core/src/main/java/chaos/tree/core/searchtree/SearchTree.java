package chaos.tree.core.searchtree;

import chaos.tree.core.Tree;

import java.util.*;
import java.util.stream.Stream;

/**
 * Root interface for all tree data structures in the ChaosTree library.
 *
 * <p>Defines the fundamental contract that every tree implementation must fulfill,
 * including insertion, deletion, search, and structural queries. All operations
 * are defined in terms of a single comparable element type {@code T}.
 *
 * <p>Implementations of this interface are not required to be thread-safe.
 * External synchronization is recommended when multiple threads access
 * a tree instance concurrently.
 *
 * @param <T> the type of elements held in this tree;
 *            must implement {@link Comparable}
 * @since 1.0.0
 */
public interface SearchTree<T extends Comparable<? super T>> extends Tree, NavigableSet<T> {
    /**
     * Inserts the specified value into this tree.
     * The tree will not be modified if the value already exists.
     * {@code null} and {@code duplicate value} are silently ignored.
     *
     * @param value the value to insert;
     */
    void insert(T value);

    /**
     * Inserts all values from the provided iterable into this tree.
     * This tree will insert all elements ignoring a duplicate and null.
     *
     * @param values the values to insert
     * @throws NullPointerException   if {@code value} is {@code null}
     */
    void insertAll(Iterable<? extends T> values);

    /**
     * Check the value is present in the tree.
     * This operation does not modify the tree.
     *
     * @param value the value to search
     * @return {@code true} the value exist in the tree;
     * {@code false} otherwise on null parameter, empty tree.
     */
    boolean contains(T value);

    /**
     * Check the value is present in the tree from the provided iterable.
     * This operation does not modify the tree.
     *
     * @param values the value to search
     * @return {@code true} the value exist in the tree;
     * {@code false} otherwise on null parameter, empty tree.
     * @throws NullPointerException if the value is {@code null};
     */
    boolean containsAllElements(Iterable<? extends T> values);

    /**
     * Delete the node containing {@code value} in the tree.
     * The tree will not modify if the element does not exist.
     * {@code null} will be no operation.
     *
     * @param value the value to be deleted;
     */
    void delete(T value);

    /**
     * Deletes all values produced by the supplied iterable from this tree.
     * @param values the values to delete; must not be {@code null}
     * @throws NullPointerException if {@code values} is {@code null}
     */
    void deleteAll(Iterable<? extends T> values);


    /**
     * Return the number of elements in this tree
     * @return the number of element; {@code 0} if tree is empty
     */
    int size();

    /**
     * Return the height of this tree.
     *
     * @return the height of this tree;
     * {@code -1} if the tree is empty,
     * {@code 0} if the tree has only a root element
     */
    int height();

    /**
     * Check whether the tree is empty or not
     * @return {@code true} this tree does not have any element;
     * {@code false} this tree has one or more element
     */
    boolean isEmpty();

    /**
     * Removes all elements from this tree.
     * The tree will be empty after this call returns.
     */
    void clear();


    /**
     * Returns a sequential {@code Stream} with this tree as its source.
     * The stream processes elements in their natural sorted order (INORDER).
     *
     * @return a sequential {@code Stream} over the elements in this tree
     */
    Stream<T> stream();

    /**
     * Returns an iterator over the elements in this tree based on the default traversal type.
     *
     * @return an iterator in {@code  TraversalType.INORDER}
     */
    Iterator<T> iterator();

    /**
     * Return the minimum value in this tree.
     *
     * @return {@code T} minimum value in this tree. null if tree is empty
     */
    T min();

    /**
     * Return the maximum value in this tree.
     *
     * @return {@code T} maximum value in this tree. null if tree is empty.
     */
    T max();

    /**
     * Returns the largest value less than or equal to the specified value.
     *
     * <p>If the specified value exists in the tree, that value is returned.
     * Otherwise, the next smaller value in the tree is returned.
     *
     * @param value the value whose floor is to be found
     * @return the floor value;
     * {@code null} if no greater value exist, value passed is null and tree is empty.
     */
    T floor(T value);

    /**
     * Returns the smallest value greater than or equal to the specified value.
     *
     * <p>If the specified value exists in the tree, that value is returned.
     * Otherwise, the next smaller value in the tree is returned.
     *
     * @param value the value whose ceiling is to be found
     * {@code null} if no smaller value exist, value passed is null and tree is empty.
     */
    T ceil(T value);

    /**
     * Returns the least value strictly greater than the specified value.
     *
     * <p>If the specified value exists in the tree, the successor is the next
     * value in the tree's sorted order. If the value does not exist, the
     * successor is the smallest value greater than the specified value.
     *
     * @param value the value whose successor is to be found
     * @return the successor value;
     * {@code null} if no greater value exist, value passed is null and tree is empty.
     */
    T successor(T value);

    /**
     * Returns the large value strictly less than the specified value.
     *
     * <p>If the specified value exists in the tree, the predecessor is the next
     * value in the tree's sorted order. If the value does not exist, the
     * predecessor is the largest value smaller than the specified value.
     *
     * @param value the value whose predecessor is to be found
     * @return the predecessor value;
     * {@code null} if no smaller value exist, value passed is null and tree is empty.
     */
    T predecessor(T value);

    /**
     * Returns the k-th smallest value in this tree according to the
     * natural sorted order of its elements.
     *
     * <p>The first smallest element corresponds to {@code k = 1}.</p>
     *
     * <p><b>Implementation Note:</b> In this library, this operation is executed in
     * O(k) time across all tree implementations (both Binary and N-ary). This is an intentional
     * architectural decision to avoid augmenting tree nodes with subtree size tracking,
     * which would incur significant memory overhead and break contiguous memory density.</p>
     *
     * @param k the 1-based position of the element to retrieve
     * @return the k-th smallest value
     * @throws IllegalArgumentException if {@code k < 1} or
     *                                  {@code k > size()}
     */
    T kthSmallest(int k);

    /**
     * Retrieves and removes the minimum value from this tree.
     *
     * @return the minimum value, {@code null} if tree is empty.
     */
    T pollMin();

    /**
     * Retrieves and removes the maximum value from this tree.
     *
     * @return the maximum value, {@code null} if tree is empty.
     */
    T pollMax();

    /**
     * Returns a list of values in the tree that fall within the specified range.
     * The bounds are half-open: includes {@code fromInclusive} and excludes {@code toExclusive}.
     *
     * @param fromInclusive the lower bound (inclusive)
     * @param toExclusive   the upper bound (exclusive)
     * @return a list of values within the given range
     * @throws IllegalArgumentException if {@code fromInclusive > toExclusive}
     * @throws NullPointerException     if either bound is {@code null}
     */
    List<T> range(T fromInclusive, T toExclusive);

    /**
     * Retains only the values in this tree that are contained in the specified iterable,
     * removing all other values.
     *
     * <p>After this operation completes, the tree will contain only values that appear
     * in both the tree and the iterable. Values present in the iterable but absent from
     * the tree are silently ignored. The iterable may contain duplicates; each unique
     * value is treated as a single retention candidate.</p>
     *
     * <p><b>Note:</b> This method snapshots the current tree state before deletion begins.
     * Modifications made to the iterable after this method is called have no effect on
     * the outcome.</p>
     *
     * <p><b>Complexity:</b> O(n log n) where n is the number of elements in this tree,
     * as each non-retained value requires a deletion costing O(log n).</p>
     *
     * @param values the values to retain; the iterable  must
     *               not be {@code null}
     * @throws NullPointerException if {@code values} is {@code null}
     */
    void retainAllElements(Iterable<? extends T> values);

    /**
     * Merges all values from the specified iterable into this tree,
     * silently ignoring values that already exist.
     *
     * @param values the values to merge; the iterable source must not be {@code null}
     * @throws NullPointerException if {@code values} is {@code null}.
     */
    void mergeAll(Iterable<? extends T> values);

    /**
     * Returns all elements in inorder (natural sorted) order as a new list.
     * <p>
     * This operation traverses the entire tree and materializes all elements
     * into memory. For lazy traversal, prefer {@link #stream()} or
     * {@link #iterator()}.
     * </p>
     *
     * @return a new list containing all elements in inorder
     */
    List<T> toList();

    /**
     * Returns a lazy, sequential Stream of values within the specified half-open range.
     * <p>Unlike {@code range()}, which materializes all elements into memory as a List,
     * this stream acts as a lazy cursor. This is the industrial standard for retrieving
     * massive blocks of sequential data from an N-ary index without triggering an
     * OutOfMemoryError.</p>
     *
     * @param fromInclusive the lower bound (inclusive)
     * @param toExclusive   the upper bound (exclusive)
     * @return a lazy stream of values within the given range
     */
    Stream<T> rangeStream(T fromInclusive, T toExclusive);

    /**
     * Returns a string representation of this tree using the specified visual style.
     *
     * @param style the visual styling to apply (e.g., ASCII or UNICODE box-drawing)
     * @return a multi-line formatted string detailing the exact tree topology
     */
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

    /**
     * first here is min
     * @return minimum element from tree, {@code null} is tree is empty.
     */
    @Override
    default T first() {
        return min();
    }

    /**
     * last here is max
     * @return minimum element from tree, {@code null} is tree is empty.
     */
    @Override
    default T last() {
        return max();
    }

    /**
     * lower here is predecessor
     * @return predecessor value from tree, {@code null} is tree is empty.
     */
    @Override
    default T lower(T t) {
        return predecessor(t);
    }

    /**
     * higher here is successor
     * @return successor value from tree, {@code null} is tree is empty.
     */
    @Override
    default T higher(T t) {
        return successor(t);
    }

    /**
     * @see #ceil(Comparable)
     */
    @Override
    default T ceiling(T t) {
        return ceil(t);
    }

    /**
     * @see #pollFirst()
     */
    @Override
    default T pollFirst() {
        return pollMin();
    }

    /**
     * @see #pollLast()
     */
    @Override
    default T pollLast() {
        return pollMax();
    }

    @Override
    default Comparator<? super T> comparator() {
        return null; // Natural ordering is used
    }

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

    // These API are not supported.

    @Override
    default NavigableSet<T> descendingSet() {
        throw new UnsupportedOperationException("Descending views are not supported.");
    }

    @Override
    default Iterator<T> descendingIterator() {
        throw new UnsupportedOperationException("Descending iterator is not supported.");
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
