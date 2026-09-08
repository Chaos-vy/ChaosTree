package chaos.tree.core;

import java.util.Iterator;

/**
 * Interface defining the bulk-loading contract for all N-ary Sets
 * (e.g., BTreeSet, BPlusTreeSet).
 * <p>
 * Provides O(N) operations to build and reconstruct the tree from 1D Arrays
 * and Iterators.
 * <p>
 * <strong>Important: Do Read NaryMap</strong>
 *
 * @param <E> the type of elements maintained by this set
 * @see NaryMap
 */
public interface NarySet<E> extends SearchTreeSet<E> {

    /**
     * Builds the tree from a sorted iterator.
     *
     * @param it     an iterator providing elements in sorted order (Producer Extends)
     * @param factor the fill factor for the nodes
     */
    void buildFromSorted(Iterator<? extends E> it, float factor);

    /**
     * /**
     * Imports an optimally packed 1D array to reconstruct a tree in O(N).
     *
     * @param flatArray  the exported primitive object array
     * @param fillFactor the target node fill factor
     */
    void importFlatArray(Object[] flatArray, float fillFactor);

    // Note: Object[] toArray() is inherited from java.util.Collection
}
