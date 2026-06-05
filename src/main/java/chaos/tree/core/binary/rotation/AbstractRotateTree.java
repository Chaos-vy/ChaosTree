package chaos.tree.core.binary;

public abstract class AbstractRotateTree<T extends Comparable<T>, N extends BiNode<T,N>> extends AbstractBiTree<T,N> {
    /**
     * Update the root after the rotation
     * @param root the node to be rotated
     */
    protected void updateMetadata(N root){}

    /**
     * Does left Rotation
     * @param root the node where the operation starts
     * @return updated root
     */
    protected N leftRotate(N root){
        N x = root.getRight();
        N transferChild = x.getLeft();
        x.setLeft(root);
        root.setRight(transferChild);
        updateMetadata(root);
        updateMetadata(x);
        return x;
    }
    /**
     * Does right Rotation
     * @param root the node where the operation starts
     * @return updated root
     */
    protected N rightRotate(N root){
        N x = root.getLeft();
        N transferChild = x.getRight();
        x.setRight(root);
        root.setLeft(transferChild);
        updateMetadata(root);
        updateMetadata(x);
        return x;
    }
}
