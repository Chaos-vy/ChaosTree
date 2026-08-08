package chaos.tree.benchmark.binary;

import chaos.tree.binary.AVL;
import chaos.tree.binary.BST;
import chaos.tree.binary.RBT;
import chaos.tree.binary.Treap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

import org.openjdk.jmh.infra.Blackhole;
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
    public void javaTreeSetInsertDelete(Blackhole bh) {
        bh.consume(javaTreeSet.add(mutationTarget));
        bh.consume(javaTreeSet.remove(mutationTarget));
        bh.consume(javaTreeSet);
    }

    @Benchmark
    public void bstInsertDelete(Blackhole bh) {
        bst.insert(mutationTarget);
        bst.delete(mutationTarget);
        bh.consume(bst);
    }

    @Benchmark
    public void avlInsertDelete(Blackhole bh) {
        avl.insert(mutationTarget);
        avl.delete(mutationTarget);
        bh.consume(avl);
    }

    @Benchmark
    public void rbtInsertDelete(Blackhole bh) {
        rbt.insert(mutationTarget);
        rbt.delete(mutationTarget);
        bh.consume(rbt);
    }

    @Benchmark
    public void treapInsertDelete(Blackhole bh) {
        treap.insert(mutationTarget);
        treap.delete(mutationTarget);
        bh.consume(treap);
    }


}