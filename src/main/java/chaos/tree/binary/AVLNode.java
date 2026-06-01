

package chaos.tree.binary;

import chaos.tree.core.BiNode;

/**
 * Adelson-Velsky and Landis(AVL) tree  node structure
 * @param <T> value to be stored in node
 */
public class AVLNode<T> extends BiNode<T, AVLNode<T>> {
    private int height;
    public AVLNode(T value){
        super(value);
        this.height=0;
    }

    /**
     * Return the current height of node.
     * @return the height of node
     */
    public int getHeight(){
        return height;
    }

    /**
     * Set the height of node.
     * @param height the height to be set
     */
    public void setHeight(int height){
        this.height = height;
    }

}
