package chaos.tree.core;

import java.util.List;

public abstract class AbstractBiTree<T extends Comparable<T>> implements ITree<T> {

//    Order of Implementation
    public abstract List<T> inorder();
    public abstract List<T> preorder();
    public abstract List<T> postorder();

//    Get minimum Value
    public abstract T getMin();

//    Get maximum Value
    public abstract T getMax();
}
