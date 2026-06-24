package chaos.tree;

import chaos.tree.binary.BinaryTree;
import chaos.tree.binary.RBT;

import java.util.Arrays;

public class main {
    public static void main(String[] args) {
        BinaryTree<String> tree0 = new RBT<>();
        tree0.insertAll(Arrays.asList("Chaos", "Tree", "Java", "Performance"));
    }
}
