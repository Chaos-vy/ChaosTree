package chaos.tree;

import chaos.tree.binary.avl.AVL;
import chaos.tree.binary.bst.BST;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.binary.splay.Splay;
import chaos.tree.binary.treap.Treap;
import chaos.tree.core.searchtree.binary.BinaryTree;
import chaos.tree.traversal.TraversalType;

import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        BinaryTree<Integer> tree = new RBT<>();
        for (int i = 1; i < 10; i++) {
            tree.insert(i);
        }
        System.out.println(tree);
        tree= new Treap<>();
        for (int i = 1; i < 10; i++) {
            tree.insert(i);
        }
        System.out.println(tree);
        tree = new Splay<>();
        for (int i = 1; i < 10; i++) {
            tree.insert(i);
        }
        System.out.println(tree);
        tree = new BST<>();
        for (int i = 1; i < 10; i++) {
            tree.insert(i);
        }
        System.out.println(tree);
        tree = new AVL<>();
        for (int i = 1; i < 10; i++) {
            tree.insert(i);
        }
        System.out.println(tree);
    }
}