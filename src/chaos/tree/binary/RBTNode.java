package chaos.tree.binary;

import chaos.tree.core.BiNode;

/**
 * Red Black tree  node structure
 * @param <T> value to be stored in node
 */
public class RBTNode<T> extends BiNode<T,RBTNode<T>> {

    /**
     * Determine color of the node.
     * true represents Black node
     * false represents Red node
     */
    private boolean color;

    /**
     * Reference to parent node
     */
    private RBTNode<T> parent;

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

    /**
     * Returns the parent node of child.
     *
     * @return the parent node of child
     */
    public RBTNode<T> getParent(){
        return parent;
    }

    /**
     * Set the parent node.
     *
     * @param parent the parent node to be set
     */
    public void setParent(RBTNode<T> parent){
        this.parent = parent;
    }
}
