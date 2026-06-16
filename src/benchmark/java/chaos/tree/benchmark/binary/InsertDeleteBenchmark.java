package chaos.tree.benchmark.binary;

import chaos.tree.binary.avl.AVL;
import chaos.tree.binary.bst.BST;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.binary.treap.Treap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

import java.util.TreeSet;

/**
 * Measures combined insert + delete latency for each tree implementation.
 *
 * <p>Each benchmark method inserts a value not present in the tree, then
 * immediately deletes it. This rollback pattern keeps the tree at a
 * consistent size across invocations.</p>
 */
public class InsertDeleteBenchmark extends AbstractBinaryBenchmark {

    private BST<Integer> bst;
    private AVL<Integer> avl;
    private RBT<Integer> rbt;
    private Treap<Integer> treap;

    //Standard Java Library
    private TreeSet<Integer> javaTreeSet;

    private int mutationTarget;

    @Setup(Level.Trial)
    public void buildStaticTrees() {
        int[] data = getShuffledInts(size);
        mutationTarget = size;

        bst = new BST<>();
        avl = new AVL<>();
        rbt = new RBT<>();
        treap = new Treap<>();
        javaTreeSet = new TreeSet<>();

        for (int v : data) {
            bst.insert(v);
            avl.insert(v);
            rbt.insert(v);
            treap.insert(v);
            javaTreeSet.add(v); // Populates the baseline
        }
    }

    @Benchmark
    public void javaTreeSetInsertDelete() {
        javaTreeSet.add(mutationTarget);
        javaTreeSet.remove(mutationTarget);
    }

    @Benchmark
    public void bstInsertDelete() {
        bst.insert(mutationTarget);
        bst.delete(mutationTarget);
    }

    @Benchmark
    public void avlInsertDelete() {
        avl.insert(mutationTarget);
        avl.delete(mutationTarget);
    }

    @Benchmark
    public void rbtInsertDelete() {
        rbt.insert(mutationTarget);
        rbt.delete(mutationTarget);
    }

    @Benchmark
    public void treapInsertDelete() {
        treap.insert(mutationTarget);
        treap.delete(mutationTarget);
    }


}