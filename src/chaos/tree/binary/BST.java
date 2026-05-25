package chaos.tree.binary;
import chaos.tree.core.AbstractBiTree;


public class BST<T extends Comparable<T>> extends AbstractBiTree<T,BSTNode<T>> {
    @Override
    protected BSTNode<T> createNode(T value) {
        return new BSTNode<>(value);
    }
}
