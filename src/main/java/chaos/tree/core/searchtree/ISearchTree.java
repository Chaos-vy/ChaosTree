package chaos.tree.core.searchtree;

import chaos.tree.core.ITree;
import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.traversal.Traversal;
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
 *
 * @since 1.0.0
 */
public interface ISearchTree<T extends Comparable<T>> extends Traversal<T>, ITree, Iterable<T>{
    /**
     * Inserts the specified value into this tree.
     * The tree will not be modified if the value already exists.
     *
     * @param value the value to insert; must not be {@code null}
     * @throws DuplicateNodeException if the specified value already
     *         exists in this tree
     * @throws NullPointerException if {@code value} is {@code null}
     */
    void insert(T value);
    /**
     * Inserts all values from the provided iterable into this tree.
     * This tree will insert all elements before encountering a duplicate.
     *
     * @param values the values to insert
     * @throws DuplicateNodeException if a duplicate value is encountered during insertion
     * @throws NullPointerException if {@code value} is {@code null}
     */
    void insertAll(Iterable<? extends T> values);
    /**
     * Check the value is present in the tree.
     * This operation does not modify the tree.
     *
     * @param value the value to search
     * @return {@code true} the value exist in the tree;
     *         {@code false} otherwise
     * @throws NullPointerException if the value is {@code null};
    */
    boolean contains(T value);

    /**
     * Check the value is present in the tree from the provided iterable.
     * This operation does not modify the tree.
     *
     * @param values the value to search
     * @return {@code true} the value exist in the tree;
     *         {@code false} otherwise
     * @throws NullPointerException if the value is {@code null};
    */
    boolean containsAll(Iterable<? extends T> values);

    /**
     * Delete the node containing {@code value} in the tree.
     * The tree will not modify if the element does not exist.
     *
     * @param value the value to be deleted; must not be {@code null}
     * @throws NullPointerException if the value is {@code null}
     */
    void delete(T value);

    /**
     * Deletes all values produced by the supplied iterable from this tree.
     *
     * <p>This method is best-effort and performs deletions in iteration order by
     * delegating to {@code delete(T)} for each element. Missing values are
     * ignored; the method does not attempt an all-or-nothing transaction.
     * The iterable reference and each element produced by it must be non-null.</p>
     *
     * @param values the values to delete; must not be {@code null}
     * @throws NullPointerException if {@code values} is {@code null} or if any
     *         element produced by {@code values} is {@code null}
     */
    void deleteAll(Iterable<? extends T> values);


    /**
     * Return the number of elements in this tree
     *
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
     *
     * @return {@code true} this tree does not have any element;
     * {@code false} this tree has one or more element
     */
    boolean isEmpty();
    /**
     * Removes all elements from this tree.
     * The tree will be empty after this call returns.
     */
    void clear();
}
