package chaos.tree;
import chaos.tree.binary.RBT;
import chaos.tree.binary.Splay;

import java.util.List;


public class Main {
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
        Splay<Integer> tre = new Splay<>();
        for (int i = 0; i < 100; i++) {
            tre.insert(i);
        }
        System.out.println(tre.rangeStream(25,50).filter(v->v%2==0).toList());
    }
}