package chaos.tree;
 import chaos.tree.binary.AVL;
 import chaos.tree.binary.BST;
 import chaos.tree.binary.RBT;
 import chaos.tree.core.ITree;

 import java.util.List;

public class Main {

    public static void main(String[] args) {
        ITree<Integer> bst = new BST<>();
        ITree<Integer> avl = new AVL<>();
        ITree<Integer> rbt = new RBT<>();
        for (int i = 1; i <= 20; i++) {
            bst.insert(i);
            avl.insert(i);
            rbt.insert(i);
        }
        System.out.println(bst);
        System.out.println(avl);
        System.out.println(rbt);
        System.out.println(rbt.size());
        rbt.delete(5);
        avl.delete(5);
        bst.delete(5);
        System.out.println(rbt.size());
        System.out.println(avl.size());
        System.out.println(bst.size());
    }
}
