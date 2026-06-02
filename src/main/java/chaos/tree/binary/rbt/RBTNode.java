package chaos.tree.binary.rbt;

import chaos.tree.core.binary.ParentBiNode;

/**
 * Red Black tree  node structure
 * @param <T> value to be stored in node
 */
public class RBTNode<T extends Comparable<T>> extends ParentBiNode<T,RBTNode<T>> {

    /**
     * Determine color of the node.
     * true represents Black node
     * false represents Red node
     */
    private boolean color;


    /**
     * Constructs the node with the specified value
     *
     * @param value the value to be stored in current node
     */
    public RBTNode(T value) {
        super(value);
        this.color=false;
    }


    /**
     * Returns the color of the node.
     * @return the color of the node
     */
    public boolean getColor(){
        return color;
    }

    /**
     * Set the color of the node.
     * @param color to be set
     */
    public void setColor(boolean color){
        this.color = color;
    }

}
