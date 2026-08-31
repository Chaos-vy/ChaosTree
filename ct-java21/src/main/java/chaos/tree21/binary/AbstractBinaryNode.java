package chaos.tree21.binary;

sealed abstract class AbstractBinaryNode<E, N extends AbstractBinaryNode<E, N>>
        permits AvlNode, RbtNode {

    protected E value;
    protected N left;
    protected N right;
    protected N parent;

    protected AbstractBinaryNode(E value) {
        this.value = value;
    }

}