package chaos.tree.binary.rbt;

import chaos.tree.core.searchtree.binary.node.ParentBiNode;
import static chaos.tree.binary.rbt.Color.*;
/**
 * Represents a node in a Red-Black Tree ({@link RBT}).
 *
 * <p>Alongside the value, parent, and child references inherited from {@link ParentBiNode}, this
 * node stores a {@link Color} attribute (either {@link Color#RED} or {@link Color#BLACK}).
 * The Red-Black Tree uses this color state to maintain structural balance after insertions
 * and deletions. Newly constructed nodes are initialized with {@link Color#RED} by default.</p>
 *
 * @param <T> the type of value stored in this node; must be {@link Comparable}
 * @see RBT
 * @see ParentBiNode
 * @see Color
 * @since 1.0.0
 */
public class RBTNode<T extends Comparable<T>> extends ParentBiNode<T,RBTNode<T>> {

    /** Color of this node; newly created nodes are RED by default. */
    private Color color;


    /**
     * Constructs a Red-Black Tree node with the specified value.
     *
     * <p>Newly created nodes are initialized with {@link Color#RED} by default,
     * as required by the Red-Black Tree insertion algorithm.</p>
     *
     * @param value the value to store in this node
     */
    public RBTNode(T value) {
        super(value);
        this.color=RED;
    }


    /**
     * Returns the current color of this node.
     *
     * @return the node color
     */
    public Color getColor(){
        return color;
    }

    /**
     * Sets the current color of this node.
     *
     * @param color the new color
     */
    public void setColor(Color color){
        this.color = color;
    }

}
