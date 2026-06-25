package chaos.tree;
import chaos.tree.binary.RBT;
import chaos.tree.binary.BinaryTree;
import chaos.tree.binary.Splay;
import chaos.tree.core.searchtree.PrintStyle;
import chaos.tree.traversal.TraversalType;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import chaos.tree.binary.RBT;
import java.util.List;


public class main {
    public static void main(String[] args) {

        RBT<String> tree = new RBT<>(List.of("delta", "alpha", "charlie", "bravo"));
        System.out.println(tree.size());  // 4
        tree.min();              // "alpha"
        tree.max();              // "delta"
        tree.floor("cat");       // "bravo"   — greatest key less than "cat"
        tree.ceil("cat");        // "charlie" — smallest key greater than "cat"
        tree.successor("bravo"); // "charlie"
        tree.kthSmallest(2);     // "bravo"
        tree.lca("alpha", "charlie"); // "bravo" (or the actual LCA node value)
        System.out.println(tree.min());
        System.out.println(tree.max());
        System.out.println(tree.floor("cat"));
        System.out.println(tree.ceil("cat"));
        System.out.println(tree.successor("bravo"));
        System.out.println(tree.kthSmallest(2));
        System.out.println(tree.lca("alpha","charlie"));
        Splay<Integer> tre = new Splay<>();
        for (int i = 0; i < 100; i++) {
            tre.insert(i);
        }
        System.out.println(tre.rangeStream(25,50).filter(v->v%2==0).toList());
    }
}