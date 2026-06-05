package chaos.tree.core.nary;

import chaos.tree.core.INode;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNaryNode<T> implements INode<T> {
    private T value;
    private List<AbstractNaryNode<T>> children;

    public AbstractNaryNode(T value){
        this.value = value;
        children = new ArrayList<>();
    }
    // From INode<T>
    @Override
    public T getValue() { return value; }

    @Override
    public void setValue(T value) { this.value = value; }

    // Nary specific
    public List<AbstractNaryNode<T>> getChildren() {
        return children;
    }
    public void setChildren(List<AbstractNaryNode<T>> child) {
        this.children = child;
    }
    public void addChild(AbstractNaryNode<T> child) {
        this.children.add(child);
    }
    public void removeChild(AbstractNaryNode<T> child) {
        this.children.remove(child);
    }
    public boolean isLeaf() {
        return children.isEmpty();
    }
}
