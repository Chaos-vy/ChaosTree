package chaos.tree.core;

public abstract class BiTreeAbstract<T extends Comparable<T>> implements ITree<T> {

//    Order of Implementation
    public abstract void inorder();
    public abstract void preorder();
    public abstract void postorder();

//    Get minimum Value
    public abstract T getMin();

//    Get maximum Value
    public abstract T getMax();
}
