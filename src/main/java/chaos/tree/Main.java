package chaos.tree;

import chaos.tree.binary.avl.AVL;
import chaos.tree.core.searchtree.binary.BinaryTree;
import chaos.tree.traversal.TraversalType;

import java.util.Iterator;

public class Main {

    public static void main(String[] args) {

        BinaryTree<Integer> avl = new AVL<>();

        Integer i =1;
        while(true) {
            avl.insert(i);
            i++;
            if(i==100) {
                break;
            }
        }
        Iterator<Integer> it = avl.iterator();
        while (it.hasNext()){
            System.out.println(it.next());
        }
        for (Integer x: avl){
            System.out.println(x);
        }

    }
}