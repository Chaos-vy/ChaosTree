package chaos.tree.core.searchtree.binary;

import chaos.tree.core.searchtree.ISearchTree;
import chaos.tree.exception.*;
import chaos.tree.traversal.TraversalType;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Represents a binary search tree interface that supports custom traversals,
 * streaming, and advanced position queries like floor, ceil, and k-th smallest.
 *
 * <p>Extends {@link ISearchTree} and adds specialized operations that leverage
 * the binary and sorted nature of the tree structure.</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @since 1.0.0
 */
public interface BinaryTree<T extends Comparable<T>> extends ISearchTree<T>, Iterable<T>{

    /**
     * Returns a sequential {@code Stream} with this tree as its source
     * using the default INORDER (sorted) traversal type.
     *
     * @return a sequential {@code Stream} over the elements in this tree
     */
    default Stream<T> stream() {
        return stream(TraversalType.INORDER);
    }

    /**
     * Returns a sequential {@code Stream} with this tree as its source
     * based on the specified traversal type.
     *
     * @param type the type of tree traversal
     * @return a sequential {@code Stream} over the elements in this tree
     */
    Stream<T> stream(TraversalType type);
    /**
     * Returns an iterator over the elements in this tree based on the specified traversal type.
     *
     * @param type the type of tree traversal
     * @return an iterator
     */
    Iterator<T> iterator(TraversalType type);

    /**
     * Return the minimum value in this tree.
     * @throws EmptyTreeException this tree is empty.
     * @return {@code T} minimum value in this tree.
     */
    T min();

    /**
     * Return the maximum value in this tree.
     * @return {@code T} maximum value in this tree.
     * @throws EmptyTreeException this tree is empty.
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
     *         {@code null} if no greater value exists
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws EmptyTreeException if this tree is empty
     */
    T floor(T value);

    /**
     * Returns the smallest value greater than or equal to the specified value.
     *
     * <p>If the specified value exists in the tree, that value is returned.
     * Otherwise, the next smaller value in the tree is returned.
     *
     * @param value the value whose ceiling is to be found
     * @return the ceiling value;
     *         {@code null} if no greater value exists
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws EmptyTreeException if this tree is empty
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
     *         {@code null} if no greater value exists
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws EmptyTreeException if this tree is empty
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
     *         {@code null} if no greater value exists
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws EmptyTreeException if this tree is empty
     */
    T predecessor(T value);

    /**
     * Returns the least common ancestor (LCA) of the two specified values.
     *
     * <p>The least common ancestor is the lowest node in the tree that has
     * both values as descendants, where a node may be considered a descendant
     * of itself.</p>
     *
     * <p>If one of the values is an ancestor of the other, that value is
     * returned as the LCA.</p>
     *
     * @param a the first value
     * @param b the second value
     * @return the value of the least common ancestor;
     *         {@code null} if either value does not exist in this tree
     * @throws EmptyTreeException if this tree is empty
     */
    T lca(T a, T b);

    /**
     * Returns the k-th smallest value in this tree according to the
     * natural sorted order of its elements.
     *
     * <p>The first smallest element corresponds to {@code k = 1}.</p>
     *
     * @param k the 1-based position of the element to retrieve
     * @return the k-th smallest value
     * @throws IllegalArgumentException if {@code k < 1} or
     *         {@code k > size()}
     */
    T kthSmallest(int k);

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
     * @param values the values to retain; the iterable and each contained value must
     *               not be {@code null}
     * @throws NullPointerException if {@code values} is {@code null}, or if any element
     *                              produced by {@code values} is {@code null}
     * @throws EmptyTreeException   if this tree is empty
     * @see #deleteAll(Iterable)
     * @see #insertAll(Iterable)
     * @see #containsAll(Iterable)
     * @see #mergeAll(Iterable)
     */
    void retainAll(Iterable<? extends T> values);

    /**
     * Merges all values from the specified iterable into this tree,
     * silently ignoring values that already exist.
     *
     * <p>This operation is equivalent to a set union — after completion,
     * this tree will contain all values it previously held plus any new
     * values from the iterable that were not already present. Duplicate
     * values are silently skipped rather than throwing
     * {@link DuplicateNodeException}, distinguishing this method from
     * {@link #insertAll(Iterable)} which enforces strict uniqueness.</p>
     *
     * <p>If a null element appears in {@code values} after earlier elements
     * were merged, those earlier merges remain applied because this method
     * does not perform a pre-validation pass.</p>
     *
     * @param values the values to merge; the iterable and each contained
     *               value must not be {@code null}
     * @throws NullPointerException if {@code values} is {@code null}, or
     *                              if any element produced by {@code values}
     *                              is {@code null}
     * @see #insertAll(Iterable)
     * @see #retainAll(Iterable)
     * @see #containsAll(Iterable)
     * @see #retainAll(Iterable)
     */
    void mergeAll(Iterable<? extends T> values);

}
