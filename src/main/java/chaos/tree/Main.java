package chaos.tree;

import chaos.tree.binary.avl.AVL;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.core.binary.BinaryTree;
import chaos.tree.exception.DuplicateNodeException;

import java.util.Random;

public class Main {

    public static void main(String[] args) {

        BinaryTree<String> avl = new AVL<>();

        avl.insert("10");
        avl.insert("11");
        avl.insert("15");
        avl.insert("13");
        System.out.println(avl);
        System.out.println(avl.ceil("14"));
        System.out.println(avl.ceil("12"));

        avl = new RBT<>();
        avl.insert("11");
        avl.insert("45");
        avl.insert("5");
        avl.insert("12");
        System.out.println(avl);
        AVL<Integer> tree = new AVL<>();
        Random r = new Random(42);

        for(int i = 0; i < 131072; i++) {
            try {
                tree.insert(r.nextInt());
            } catch (DuplicateNodeException ignored) {
            }
        }

        System.out.println(tree.height());
        System.out.println(tree.size());
    }
}