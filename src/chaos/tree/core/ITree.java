package chaos.tree.core;

import java.util.Comparator;

public interface ITree<T extends Comparable<T>> {
//  Add #T into tree
    void insert(T value);

//  Search an element in tree and return "true" | "false"
    boolean search(T value);

//  Delete the element with the same value
    void delete(T value);


//  Return int value =>total number of nodes in the tree.
    int size();

//  Returns Number of edges on the longest path
//  From root to a leaf.
    int height();

//  Returns "true" on Tree having no node
//  Returns "false" on Tree having any node
    boolean isEmpty();

//  Clear all the tree nodes
    void clear();


}
