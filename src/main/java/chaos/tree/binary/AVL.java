package chaos.tree.binary;

import chaos.tree.core.AbstractRotateTree;

public class AVL<T extends Comparable<T>> extends AbstractRotateTree<T,AVLNode<T>> {
    @Override
    protected AVLNode<T> createNode(T value) {
        return new AVLNode<>(value);
    }

    @Override
    protected AVLNode<T> afterInsert(AVLNode<T> root){
        updateMetadata(root);
        return rebalanced(root);
    }

    @Override
    protected String nodeText(AVLNode<T> node) {
        return node.getValue() + "(h=" + node.getHeight() + ")";
    }
    @Override
    public int height(){
        return nodeHeight(root);
    }
    @Override
    protected void updateMetadata(AVLNode<T> root) {
        root.setHeight(1+Math.max(nodeHeight(root.getLeft()), nodeHeight(root.getRight())));
    }
    private int nodeHeight(AVLNode<T> node){
        return node == null ? -1 : node.getHeight();
    }
    private int getBalance(AVLNode<T> root){
        return root==null? 0: nodeHeight(root.getLeft())- nodeHeight(root.getRight());
    }
    private AVLNode<T> rebalanced(AVLNode<T> root) {
        if(getBalance(root)>1){
            if(getBalance(root.getLeft())<0) {
                root.setLeft(leftRotate(root.getLeft()));
            }
            return rightRotate(root);
        }
        if(getBalance(root)<-1){
            if(getBalance(root.getRight())>0){
                root.setRight(rightRotate(root.getRight()));
            }
            return leftRotate(root);
        }
        return root;
    }
    @Override
    protected AVLNode<T> afterDelete(AVLNode<T> root){
        updateMetadata(root);
        return rebalanced(root);
    }
}
