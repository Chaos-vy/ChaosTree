package chaos.tree21.binary;

final class AvlNode<E> extends AbstractBinaryNode<E, AvlNode<E>> {

    int height;

    AvlNode(E value) {
        super(value);
    }

    int getHeight(){
        return height;
    }
    void setHeight(int height){
        this.height = height;
    }
}
