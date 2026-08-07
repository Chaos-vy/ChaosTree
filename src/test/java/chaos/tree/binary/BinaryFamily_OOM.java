package chaos.tree.binary;

import org.junit.jupiter.api.Disabled;

import java.util.Set;
import java.util.TreeSet;

@Disabled("This is a manual OOM stress test. Do not run during mvn package.")
public class BinaryFamily_OOM {
    public static void main(String[] args) {
        //Not BST because it will degrade to Linked list and BST don't have tail pointer.
        BinaryTree<Integer> tree = new AVL<>();
        int count = 0;
        try {
            while (true){
                tree.insert(count++);
            }
        }
        catch (OutOfMemoryError e){
            tree = null;
            System.gc();
            System.out.println("Node count of AVL Tree              : "+count);
        }
        tree = new RBT<>();
        count = 0;
        try {
            while (true){
                tree.insert(count++);
            }
        }
        catch (OutOfMemoryError e){
            tree = null;
            System.gc();
            System.out.println("Node count of RB  Tree              : "+count);
        }
        tree = new Splay<>();
        count = 0;
        try {
            while (true){
                tree.insert(count++);
            }
        }
        catch (OutOfMemoryError e){
            tree = null;
            System.gc();
            System.out.println("Node count of Splay Tree            : "+count);
        }
        tree = new Treap<>();
        count = 0;
        try {
            while (true){
                tree.insert(count++);
            }
        }
        catch (OutOfMemoryError e){
            tree = null;
            System.gc();
            System.out.println("Node count of Treap Tree             : "+count);
        }
        Set<Integer> tree0 = new TreeSet<>();
        count = 0;
        try {
            while (true){
                tree0.add(count++);
            }
        }
        catch (OutOfMemoryError e){
            tree0 = null;
            System.gc();
            System.out.println("Node count of Tree Set               : "+count);
        }
    }
}
