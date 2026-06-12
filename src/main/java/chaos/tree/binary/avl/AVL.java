package chaos.tree.binary.avl;

import chaos.tree.core.searchtree.binary.rotation.AbstractRotateTree;
import chaos.tree.core.searchtree.binary.AbstractBiTree;
/**
 * Height-balanced Binary Search Tree implementation utilizing the AVL invariant.
 *  <p>An AVL tree is a strictly self-balancing structure where the height difference
 * (balance factor) between the left and right subtrees of any node is guaranteed
 * to be at most <b>1</b>.</p>
 *  <p>By enforcing this strict structural constraint on every insertion and deletion,
 * the tree maintains an absolute worst-case height bound of approximately 1.44 log n.
 * This translates into exceptionally fast, highly deterministic <b>O(log n)</b> lookups,
 * making it an excellent fit for read-heavy datasets.</p>
 *
 * @param <T> the type of elements maintained by this tree, must be {@link Comparable}
 * @see AbstractRotateTree
 * @see AVLNode
 * @see AbstractBiTree
 * @since 1.0.0
 */
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
