package chaos.tree.traversal;

/**
 * Defines the strategy used for traversing a tree structure.
 * <p>Standard Depth-First Search (DFS) traversals (Inorder, Preorder, Postorder)
 * as well as Breadth-First Search (BFS) traversal (Level Order) are supported.</p>
 *
 * @author Chaos-vy
 * @since 1.0
 */
public enum TraversalType {

    /**
     * Depth-First Search: Left subtree, Root node, Right subtree.
     * <p>Typically used for Binary Search Trees (BST) to retrieve data in sorted order.</p>
     * <pre>
     *       (2)
     *       / \
     *    (1)  (3)  => 1 -> 2 -> 3
     * </pre>
     */
    INORDER,

    /**
     * Depth-First Search: Root node, Left subtree, Right subtree.
     * <p>Typically used to create a copy of the tree or get prefix expressions.</p>
     * <pre>
     *       (2)
     *       / \
     *    (1)  (3)  => 2 -> 1 -> 3
     * </pre>
     */
    PREORDER,

    /**
     * Depth-First Search: Left subtree, Right subtree, Root node.
     * <p>Typically used for deleting the tree, post-fix expressions, or bottom-up calculations.</p>
     * <pre>
     *       (2)
     *       / \
     *    (1)  (3)  => 1 -> 3 -> 2
     * </pre>
     */
    POSTORDER,

    /**
     * Breadth-First Search: Level by level, from left to right.
     * <p>Explores all nodes at the current depth before moving to the next level.</p>
     * <pre>
     *       (2)           Level 0
     *       / \
     *    (1)  (3)         Level 1
     *                     => 2 -> 1 -> 3
     * </pre>
     */
    LEVEL_ORDER
}
