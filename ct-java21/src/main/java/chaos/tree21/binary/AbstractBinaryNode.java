package chaos.tree21.binary;

sealed abstract class AbstractBinaryNode<E, N extends AbstractBinaryNode<E, N>>
        implements BinaryNode<E, N> permits AvlNode, RbtNode {

    protected E value;
    protected N left;
    protected N right;
    protected N parent;

    protected AbstractBinaryNode(E value) {
        this.value = value;
    }

    @Override
    public E getValue() {
        return value;
    }

    @Override
    public void setValue(E value) {
        this.value = value;
    }

    @Override
    public N getLeft() {
        return left;
    }

    @Override
    public void setLeft(N left) {
        this.left = left;
    }

    @Override
    public N getRight() {
        return right;
    }

    @Override
    public void setRight(N right) {
        this.right = right;
    }

    @Override
    public N getParent() {
        return parent;
    }

    @Override
    public void setParent(N parent) {
        this.parent = parent;
    }
}