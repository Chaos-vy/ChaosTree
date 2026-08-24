package chaos.tree.binary;

import chaos.tree.core.searchtree.SearchTree;
import chaos.tree.traversal.Traversal;
import chaos.tree.traversal.TraversalType;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.NoSuchElementException;

/**
 * Represents a binary search tree interface that supports custom traversals,
 * streaming, and advanced position queries like floor, ceil, and k-th smallest.
 *
 * <p>Extends {@link SearchTree} and adds specialized operations that leverage
 * the binary and sorted nature of the tree structure.</p>
 *
 * @param <T> the type of elements maintained by this tree; must implement {@link Comparable}
 * @since 1.0.0
 */
public interface BinaryTree<T extends Comparable<? super T>> extends SearchTree<T>, Traversal<T> {


    /**
     * Returns the least common ancestor of two existing non-null values.
     *
     * <p>Both arguments are checked independently. If either argument is {@code null},
     * this method throws before calling {@link #contains(Comparable)} or descending
     * through the tree, because neither operation can compare a null value with node
     * values.</p>
     *
     * @param a the first value; must not be {@code null}
     * @param b the second value; must not be {@code null}
     * @return the least common ancestor value
     * null  if {@code a} or {@code b} is {@code null}
     * @throws NoSuchElementException on missing value of {@code a} and {@code b}
     */
    T lca(T a, T b);

    /**
     * Returns a sequential stream over this tree using the specified traversal order.
     *
     * @param type the traversal order to use; must not be {@code null}
     * @return a sequential stream over this tree
     * @throws NullPointerException if {@code type} is {@code null}, because the stream's
     * spliterator characteristics and backing iterator depend on the traversal type
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
     * Returns all elements using the specified traversal strategy.
     * <p>
     * This operation traverses the entire tree and materializes the
     * traversal result into a new list.
     * </p>
     *
     * @param type the traversal order to perform
     * @return a new list containing all elements in the specified traversal order
     * @throws NullPointerException if {@code type} is null
     */
    List<T> toList(TraversalType type);
}
