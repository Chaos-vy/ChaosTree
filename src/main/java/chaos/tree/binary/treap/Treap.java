package chaos.tree.binary.treap;

import chaos.tree.core.binary.rotation.AbstractRotateTree;
/**
 * Randomized Binary Search Tree implementation known as a Treap.
 * * <p>A Treap combines the structural characteristics of a Binary Search Tree (BST)
 * and a Heap. Node keys maintain strict BST order, while node priorities
 * (randomly generated upon insertion) satisfy max-heap properties.</p>
 * * <p>This probabilistic balancing strategy guarantees expected <b>O(log n)</b>
 * time complexity for search, insert, and delete operations, completely eliminating
 * the worst-case degradation associated with un-balanced trees without the strict
 * rebalancing overhead of AVL or Red-Black trees.</p>
 *
 * @param <T> the type of elements maintained by this tree, must be {@link Comparable}
 * @see AbstractRotateTree
 */
public class Treap<T extends Comparable<T>> extends AbstractRotateTree<T, TreapNode<T>> {

    @Override
    protected TreapNode<T> createNode(T key) {
        return new TreapNode<>(key);
    }

    @Override
    protected TreapNode<T> afterInsert(TreapNode<T> node) {
        if(node.getLeft()!=null && node.getLeft().getPriority()>node.getPriority()){
            return rightRotate(node);
        }
        if(node.getRight()!=null && node.getRight().getPriority()>node.getPriority()){
            return leftRotate(node);
        }
        return node;
    }

    @Override
    protected TreapNode<T> delete(TreapNode<T> node, T value, boolean[] isDeleted) {
        if(node == null)return null;
        int cmp = compare(value, node);

        if(cmp>0){
            node.setRight(delete(node.getRight(),value,isDeleted));
        }
        else if(cmp<0){
            node.setLeft(delete(node.getLeft(),value,isDeleted));
        }
        else {
            isDeleted[0]=true;
            if(node.getRight()==null)return node.getLeft();
            if(node.getLeft()==null)return node.getRight();
            if(node.getRight().getPriority()>node.getLeft().getPriority()){
                node = leftRotate(node);
                node.setLeft(delete(node.getLeft(), value, isDeleted));
            }
            else{
                node = rightRotate(node);
                node.setRight(delete(node.getRight(), value, isDeleted));
            }
        }
        return node;
    }
}
