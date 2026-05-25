package chaos.tree.core;

import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.traversal.Traversal;

public interface ITree<T extends Comparable<T>> extends Traversal<T> {
    /**
     * Inserts the specified value into the tree.
     * Duplicate values are not allowed.
     *
     * @param value the value to insert, must not be null
     * @throws DuplicateNodeException if value already exists in the tree
     */
    void insert(T value);

    /**
     * Search the element in the tree
     *
     * @param value the value to be searched
     * @return true if value is present in the tree otherwise false
    */
    boolean search(T value);

    /**
     * Delete the element in the tree
     *
     * @param value the value to be deleted if present: deleted
     * @return true if value is found to be deleted otherwise false
     */
    boolean delete(T value);


    /**
     * Return the total no of element present in the tree
     *
     * @return the total no of element
    */
    int size();

    /**
     * Return the height of tree where root height reference is 0
     *
     * @return the height of tree
     */
    int height();

    /**
     * Check whether the tree is empty or not
     *
     * @return true if tree is empty otherwise false
     */
    boolean isEmpty();

    /**
     * Make the tree empty
     */
    void clear();
}
