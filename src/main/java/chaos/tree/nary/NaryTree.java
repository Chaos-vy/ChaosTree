package chaos.tree.nary;

import chaos.tree.core.searchtree.ISearchTree;


/**
 * The public interface for all N-ary tree structures (e.g., B-Trees, B+ Trees).
 *
 * <p>Unlike the {@code BinaryTree} family which focuses on strict 2-child branching
 * topologies, the {@code NaryTree} family focuses on packing multiple elements into
 * contiguous memory nodes. This layout is commonly used to reduce cache misses
 * and disk I/O operations in storage and database systems.</p>
 *
 * <p>Because all N-ary trees in this library are fundamentally ordered search trees,
 * they inherit the complete positional query (min, max, floor, ceil) and range query
 * operations from {@link ISearchTree}. The structural mechanics of this family
 * are strictly governed by the CLRS "minimum degree" (t) arithmetic to guarantee
 * perfectly symmetrical node splits and merges.</p>
 *
 * @param <T> the comparable type of elements held in this tree
 * @since 1.0.0
 */
public interface NaryTree<T extends Comparable<T>> extends ISearchTree<T> {
    
    /**
     * Returns the minimum degree (t) of the B-Tree as defined by CLRS.
     * This defines the lower bound for children (t) and keys (t-1).
     *
     * @return the minimum degree (t)
     */
    int minDegree();

    /**
     * Returns the maximum degree of the B-Tree (2t).
     * This defines the upper bound for children (2t) and keys (2t-1).
     * This mathematically matches the Knuth Order.
     *
     * @return the maximum degree (2t)
     */
    int maxDegree();

}
