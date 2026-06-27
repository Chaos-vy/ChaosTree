package chaos.tree.binary;

import chaos.tree.core.searchtree.ISearchTree;
import chaos.tree.exception.*;
import chaos.tree.traversal.Traversal;
import chaos.tree.traversal.TraversalType;

import java.util.Iterator;
import java.util.List;
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
public interface BinaryTree<T extends Comparable<T>> extends ISearchTree<T>, Traversal<T> {


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
     * @throws EmptyTreeException    if this tree is empty
     * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
     * @throws NodeNotFoundException if either value does not exist in this tree
     */
    T lca(T a, T b);

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
