package chaos.tree21.nary;

public sealed interface NaryNode<E, N extends NaryNode<E, N>> permits AbstractNaryNode {

    E getKey(int index);

    void setKey(int index, E key);

    void setKeyCount(int keyCount);

    N getChild(int index);

    void setChild(int index, N child);

    void keyCount_INC1();

    void keyCount_DEC1();

    /*
    I don't know anything is missing for the structure of the B-Tree node
    any tweaks will be updated as soon as the insert and delete operation are made for
    B-Tree
    B+Tree
    If you are going to make a Btree Hybrid tree from my Node Structure you are welcome.
    I use F-form polymorphism to remove those nasty class cast exception so don't think that ClassCaseException is joke.
     */

}
