package chaos.tree.traversal;

import java.util.List;

/**
 * Defines list-producing traversal operations for tree implementations.

 * <p>Each method returns the current tree contents in a specific traversal order.
 * Implementations should return a new {@link List} containing the visited values,
 * not a live view backed by the tree. Therefore, later structural changes to the
 * tree should not mutate a previously returned traversal list.</p>
 *
 * <p>The relative meaning of left and right subtrees is implementation-specific,
 * but for binary search trees the traversal names follow the standard definitions
 * used by {@link TraversalType}.</p>
 *
 * @param <T> the type of values produced by the traversal; values must be comparable
 *        when used by sorted tree implementations
 * @see TraversalType
 * @since 1.0.0
 */
public interface Traversal<T extends Comparable<T>> {


    /**
     * Returns this tree's values in inorder traversal order.
     *
     * <p>For a tree, inorder visits the left subtree first, then the current
     * node, then the right subtree: Left, Root, Right. In a binary search tree, this
     * normally produces values in ascending sorted order.</p>
     *
     * @return a list containing the tree values in inorder; an empty list if the
     *         tree has no values
     * @see TraversalType#INORDER
     */
     List<T> inorder();

    /**
     * Returns this tree's values in Traversal type order.
     *
     * <p><b>POSTORDER: </b> visits the left subtree first, then the right
     * subtree, then the current node: Left, Right, Root.</p>
     * <p><b>INORDER: </b> visits the left subtree first, then the current
     * node, then the right subtree: Left, Root, Right. In a binary search tree, this
     * normally produces values in ascending sorted order.</p>
     * <p><b>PREORDER: </b>visits the left subtree first, then the right
     * subtree, then the current node: Left, Right, Root.</p>
     * <p><b>LEVEL_ORDER: </b> visits nodes breadth-first, from the root level down
     * to deeper levels.</p>
     *
     * @param type the traversal type to execute
     * @return a list containing the tree values in the specified order; an empty list if the
     *         tree has no values
     * @see TraversalType#POSTORDER
     * @see TraversalType#INORDER
     * @see TraversalType#PREORDER
     * @see TraversalType#LEVEL_ORDER
     */
     List<T> toList(TraversalType type);

}
