package chaos.tree.core.binary.node;

import chaos.tree.core.INode;

public abstract class ParentBiNode<T extends Comparable<T>, N extends ParentBiNode<T,N>> extends BiNode<T,N> implements INode<T> {

    private N parent;

    /**
     * Constructs the node with the specified value
     *
     * @param value the value to be stored in current node
     */
    public ParentBiNode(T value) {
        super(value);
    }

    public void setParent(N parent){
        this.parent=parent;
    }
    /**
     * Return the parent of node
     *
     * @return the parent node
     */
    public N getParent() {
        return parent;
    }
}
