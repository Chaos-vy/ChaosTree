package chaos.tree.core.binary;

public abstract class AbstractParentRotateTree<T extends Comparable<T>, N extends ParentBiNode<T,N>> extends AbstractBiTree<T,N> {

    /**
     * Rewires parent references after a rotation.
     *
     * @param transferChild the child being transferred to {@code node}; may be {@code null}
     * @param parent        the original parent of {@code node}; {@code null} if {@code node} was root
     * @param newNode       the node taking {@code node}'s original position
     * @param node          the node being rotated down
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
