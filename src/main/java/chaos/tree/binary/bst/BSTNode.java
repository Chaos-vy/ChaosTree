package chaos.tree.binary.bst;

import chaos.tree.core.searchtree.binary.node.BiNode;

/**
 * Concrete node representation for the Binary Search Tree.
 *
 * <p>This node is utilized specifically by the {@link BST} class to construct
 * its tree structure. It extends {@link BiNode} to inherit left and right child pointers
 * as well as the value storage.</p>
 *
 * @param <T> the type of value stored in the node
 * @see BST
 * @see BiNode
 * @since 1.0.0
 */
public class BSTNode<T> extends BiNode<T,BSTNode<T>> {
    /**
     * Constructs a new BST node with the specified value.
     *
     * @param value the value to be stored in the node, which will be managed by {@link BST}
     */
    public BSTNode(T value) {
        super(value);
    }
}
