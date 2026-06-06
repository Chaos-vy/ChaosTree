package chaos.tree.binary.avl;

import chaos.tree.core.binary.rotation.AbstractRotateTree;

public class AVL<T extends Comparable<T>> extends AbstractRotateTree<T, AVLNode<T>> {
    @Override
    protected AVLNode<T> createNode(T value) {
        return new AVLNode<>(value);
    }

    @Override
    protected AVLNode<T> afterInsert(AVLNode<T> node){
        updateMetadata(node);
        return rebalanced(node);
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
    private int getBalance(AVLNode<T> node){
        return node==null? 0: nodeHeight(node.getLeft())- nodeHeight(node.getRight());
    }
    private AVLNode<T> rebalanced(AVLNode<T> node) {
        if(getBalance(node)>1){
            if(getBalance(node.getLeft())<0) {
                node.setLeft(leftRotate(node.getLeft()));
            }
            return rightRotate(node);
        }
        if(getBalance(node)<-1){
            if(getBalance(node.getRight())>0){
                node.setRight(rightRotate(node.getRight()));
            }
            return leftRotate(node);
        }
        return node;
    }
    @Override
    protected AVLNode<T> afterDelete(AVLNode<T> node){
        updateMetadata(node);
        return rebalanced(node);
    }
}
