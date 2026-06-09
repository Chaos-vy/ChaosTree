package chaos.tree.binary.bst;

import chaos.tree.core.searchtree.binary.node.BiNode;

/**
 * Binary Search Tree Node structure.
 *
 * @param <T> the type of value stored in the node
 */
public class BSTNode<T> extends BiNode<T,BSTNode<T>> {
    /**
     * Constructs a new BST node with the specified value.
     *
     * @param value the value to be stored in the node
     */
    public BSTNode(T value) {
        super(value);
    }
}
