package chaos.tree.core;


public abstract class BiNode<T> implements INode<T> {

    private T value;
    private BiNode<T> left, right;

    public BiNode(T value) {
        this.value = value;
    }

    // From INode<T>
    @Override
    public T getValue() { return value; }

    @Override
    public void setValue(T value) { this.value = value; }


    public BiNode<T> getLeft() {
        return left;
    }
    public BiNode<T> getRight() {
        return right;
    }
    public void setLeft(BiNode<T> left) {
        this.left = left;
    }
    public void setRight(BiNode<T> right) {
        this.right = right;
    }
}
