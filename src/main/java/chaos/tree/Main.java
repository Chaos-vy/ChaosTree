package chaos.tree;

import chaos.tree.binary.avl.AVL;
import chaos.tree.core.searchtree.binary.BinaryTree;

public class Main {

    public static void main(String[] args) {

        BinaryTree<Integer> avl = new AVL<>();

        Integer i =1;
        while(true) {
            avl.insert(i);
            i++;
            if(i%1_000_000 ==0) {
                System.out.println("Inserted " + i + " nodes");
            }
        }
    }
}