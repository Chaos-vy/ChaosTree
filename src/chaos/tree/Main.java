package chaos.tree;
 import chaos.tree.binary.BST;
 import chaos.tree.core.ITree;

 import java.util.List;

public class Main {
    public static void main(String[] args) {
        BST<Integer> bst = new BST<>();
        bst.insert(45);
        bst.insert(52);
        bst.insert(23);
        bst.insert(50);
        bst.insert(30);
        bst.insert(70);
        bst.insert(20);
        bst.insert(40);
        bst.insert(60);
        bst.insert(80);

        System.out.println(bst.search(23));
        System.out.println("Is the value present = "+bst.search(40));

        System.out.println(bst.height());

        System.out.println("Size = "+bst.size());
        List<Integer> list = bst.preorder();
        System.out.println(list);

        bst.insertAll(List.of(112,150,152,145,298,352));
        System.out.println(bst.inorder());

        ITree<Integer> bst1 = new BST<>();
        bst1.insertAll(List.of(112,150,152,145,298,352));
        System.out.println(bst1.inorder());
    }
}
