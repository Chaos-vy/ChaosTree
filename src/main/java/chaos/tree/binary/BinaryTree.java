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


}
