package chaos.tree.core.searchtree.binary.rotation;

import chaos.tree.core.searchtree.binary.AbstractBiTree;
import chaos.tree.core.searchtree.binary.node.ParentBiNode;

/**
 * Core structural extension providing foundational tree rotation mechanics for parent aware nodes.
 * <p>This abstract class introduces pure 3-pointer tree rotations (Left, Right and parent),
 * serving as the base architectural layer for self-balancing binary search trees
 * that resolve color or recency variations strictly on the recursive unwinding path
 * (such as {@code RBT} and {@code Splay}).</p>
 * <p>Because this layer assumes standard parent references ,
 * rotations are moderate lightweight, only modifying three local pointers before
 * returning the new subtree root back to the caller.</p>
 * @param <T> the type of elements maintained by this tree, must be {@link Comparable}
 * @param <N> the specific type of {@link ParentBiNode} managed by this tree implementation
 * @see AbstractBiTree
 * @see ParentBiNode
 * @since 1.0.0
 */
public abstract class AbstractParentRotateTree<T extends Comparable<T>, N extends ParentBiNode<T,N>> extends AbstractBiTree<T,N> {

    /**
     * Overrides the base clone to additionally wire parent references
     * for each cloned child node.
     */
    @Override
    protected N cloneStructure(N node) {
        if (node == null) return null;
        N clone = copyNode(node);
        N left = cloneStructure(node.getLeft());
        N right = cloneStructure(node.getRight());
        clone.setLeft(left);
        clone.setRight(right);
        if (left != null) left.setParent(clone);
        if (right != null) right.setParent(clone);
        return clone;
    }

    /**
     * Rewires parent references after a rotation.
     *
     * @param transferChild the child being transferred to {@code node}; may be {@code null}
     * @param parent the original parent of {@code node}; {@code null} if {@code node} was root
     * @param newNode the node taking {@code node}'s original position
     * @param node the node being rotated down
     */
    private void refactorParents(N transferChild, N parent, N newNode,N node){
        node.setParent(newNode);
        if(transferChild!=null){
            transferChild.setParent(node);
        }
        if(parent==null){ root = newNode; }
        else {
            if(parent.getLeft()==node){ parent.setLeft(newNode); }
            else { parent.setRight(newNode); }
        }
        newNode.setParent(parent);
    }
    /**
     * Does left Rotation
     * @param node the node where the operation starts
     * @return updated root
     */
    protected N leftRotate(N node){
       N parent = node.getParent();
       N x = node.getRight();
       N transferChild = x.getLeft();
       x.setLeft(node);
       node.setRight(transferChild);
       refactorParents(transferChild,parent,x,node);
       return x;
    }
    /**
     * Does right Rotation
     * @param node the node where the operation starts
     * @return updated root
     */
    protected N rightRotate(N node){
        N parent = node.getParent();
        N x = node.getLeft();
        N  transferChild = x.getRight();
        x.setRight(node);
        node.setLeft(transferChild);
        refactorParents(transferChild,parent,x,node);
        return x;
    }

}
