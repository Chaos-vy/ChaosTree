

package chaos.tree.binary.avl;

import chaos.tree.core.searchtree.binary.node.BiNode;

/**
 * Represents a node in an Adelson-Velsky and Landis ({@link AVL}) tree.
 *
 * <p>Alongside the value and child references inherited from {@link BiNode}, this
 * node stores the height of its rooted subtree. The AVL tree uses this cached
 * height to calculate balance factors and determine when rotations are required.
 * Newly constructed nodes are leaves and therefore have a height of {@code 0}.</p>
 *
 * @param <T> the type of value stored in this node
 * @see AVL
 * @see BiNode
 * @since 1.0.0
 */
public class AVLNode<T> extends BiNode<T, AVLNode<T>> {
    /** The cached height of the subtree rooted at this node. */
    private int height;

    /**
     * Constructs a leaf node containing the specified value and initializes its
     * height to {@code 0}.
     *
     * @param value the value to store in this node
     */
    public AVLNode(T value){
        super(value);
        this.height=0;
    }

    /**
     * Returns the cached height of the subtree rooted at this node.
     *
     * @return the cached subtree height
     */
    public int getHeight(){
        return height;
    }

    /**
     * Updates the cached height of the subtree rooted at this node.
     *
     * @param height the new subtree height
     */
    public void setHeight(int height){
        this.height = height;
    }

}
