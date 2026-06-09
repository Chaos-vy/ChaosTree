package chaos.tree.core.binary;

import chaos.tree.core.ISearchTree;
import chaos.tree.exception.*;

public interface BinaryTree<T extends Comparable<T>> extends ISearchTree<T> {

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
}
