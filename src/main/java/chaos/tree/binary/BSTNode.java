package chaos.tree.binary;

import chaos.tree.core.BiNode; /**
 * Binary Search Tree Node structure
 * @param <T> The Type of value stored in the node
 */
public class BSTNode<T> extends BiNode<T,BSTNode<T>> {
    public BSTNode(T value) {
        super(value);
    }
}
