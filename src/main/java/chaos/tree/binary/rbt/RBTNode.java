package chaos.tree.binary.rbt;

import chaos.tree.core.searchtree.binary.node.ParentBiNode;
import static chaos.tree.binary.rbt.Color.*;
/**
 * Red Black tree  node structure
 * @param <T> value type to be stored in node
 */
public class RBTNode<T extends Comparable<T>> extends ParentBiNode<T,RBTNode<T>> {

    /** Color of this node; newly created nodes are RED by default. */
    private Color color;


    /**
     * Constructs a Red-Black Tree node with the specified value.
     *
     * <p>Newly created nodes are initialized with
     * {@link Color#RED} as required by the Red-Black Tree
     * insertion algorithm.</p>
     *
     * @param value the value to be stored in this node
     */
    public RBTNode(T value) {
        super(value);
        this.color=RED;
    }


    /**
     * Returns the current color of the node.
     * @return the node color
     */
    public Color getColor(){
        return color;
    }

    /**
     * Set the current color of the node.
     * @param color the new color
     */
    public void setColor(Color color){
        this.color = color;
    }

}
