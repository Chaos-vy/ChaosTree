package chaos.tree.core;

public interface INode<T> {

//  Get the value of the current node
    T getValue();

//  Set the value of the current node
    void setValue(T value);
}
