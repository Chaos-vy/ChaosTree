package chaos.tree21.binary;

final class RbtNode<E> extends AbstractBinaryNode<E, RbtNode<E>> {

    private Color color;
    RbtNode(E value) {
        super(value);
        this.color = Color.RED;
    }
     Color getColor(){
        return color;
     }
     void setColor(Color color){
        this.color = color;
     }
}
