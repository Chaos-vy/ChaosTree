package chaos.tree.core;

public interface INode<T> {

    /**
     * Return the value of current node
     *
     * @return value of current node
     */
    T getValue();

    /**
     * Set the value for the current node
     * @param value value to be set
     */
    void setValue(T value);
}
