package chaos.tree.core;

import java.util.List;

public abstract class NaryAbstract<T extends Comparable<T>> implements ITree<T> {
    public abstract void addChild(T parentValue, T childValue);
    public abstract List<T> getChildren(T parentValue);
    public abstract boolean isLeaf(T value);
}
