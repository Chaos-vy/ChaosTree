package chaos.tree.core.searchtree.binary.rotation;

import chaos.tree.core.searchtree.binary.AbstractBiTree;
import chaos.tree.core.searchtree.binary.node.BiNode;
/**
 * Core structural extension providing foundational tree rotation mechanics.
 * <p>This abstract class introduces pure 2-pointer tree rotations (Left and Right),
 * serving as the base architectural layer for self-balancing binary search trees
 * that resolve height or priority variations strictly on the recursive unwinding path
 * (such as {@code AVL} and {@code Treap}).</p>
 * <p>Because this layer assumes standard child references without parent tracking,
 * rotations are highly lightweight, only modifying two local pointers before
 * returning the new subtree root back to the caller.</p>
 * @param <T> the type of elements maintained by this tree, must be {@link Comparable}
 * @param <N> the specific type of {@link BiNode} managed by this tree implementation
 * @see AbstractBiTree
 * @see BiNode
 * @since 1.0.0
 */
public abstract class AbstractRotateTree<T extends Comparable<T>, N extends BiNode<T,N>> extends AbstractBiTree<T,N> {
    /**
     * Update the node after the rotation
     * @param node the node to be rotated
     */
    protected void updateMetadata(N node){}

    /**
     * Does left Rotation
     * @param node the node where the operation starts
     * @return updated node
     */
    protected N leftRotate(N node){
        N x = node.getRight();
        N transferChild = x.getLeft();
        x.setLeft(node);
        node.setRight(transferChild);
        updateMetadata(node);
        updateMetadata(x);
        return x;
    }
    /**
     * Does right Rotation
     * @param node the node where the operation starts
     * @return updated node
     */
    protected N rightRotate(N node){
        N x = node.getLeft();
        N transferChild = x.getRight();
        x.setRight(node);
        node.setLeft(transferChild);
        updateMetadata(node);
        updateMetadata(x);
        return x;
    }
}
