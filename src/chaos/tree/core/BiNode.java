package chaos.tree.core;


public abstract class BiNode<T,N extends BiNode<T,N>> implements INode<T> {

    private T value;
    private N left, right;

    public BiNode(T value) {
        this.value = value;
    }

    // From INode<T>
    @Override
    public T getValue() { return value; }

    @Override
    public void setValue(T value) { this.value = value; }


    public N getLeft() {
        return left;
    }
    public N getRight() {
        return right;
    }
    public void setLeft(N left) {
        this.left = left;
    }
    public void setRight(N right) {
        this.right = right;
    }
}
