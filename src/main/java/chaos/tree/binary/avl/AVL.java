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

    /**
     * Constructs an empty AVL tree.
     */
    public AVL() {}

    /**
     * Constructs a new AVL tree by inserting all elements from the specified iterable.
     *
     * @param source the iterable collection containing elements to insert
     * @throws NullPointerException if {@code source} is {@code null}
     * @see #insertAll(Iterable)
     */
    public AVL(Iterable<T> source) {
        if (source == null) throw new NullPointerException("Source collection cannot be null.");
        insertAll(source);
    }

    /**
     * Constructs a deep structural copy of the specified source tree.
     *
     * <p>Clones nodes via pre-order traversal in <b>O(n)</b> time and <b>O(h)</b>
     * stack space, bypassing the insertion pipeline entirely.</p>
     *
     * @param source the AVL instance to deep copy
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public AVL(AVL<T> source) {
        if (source == null) throw new NullPointerException("Source tree cannot be null.");
        if (!source.isEmpty()) {
            this.root = cloneStructure(source.root);
            this.size = source.size();
        }
    }

    @Override
    protected AVLNode<T> createNode(T value) {
        return new AVLNode<>(value);
    }

    @Override
    protected AVLNode<T> copyNode(AVLNode<T> source) {
        AVLNode<T> copy = new AVLNode<>(source.getValue());
        copy.setHeight(source.getHeight());
        return copy;
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
