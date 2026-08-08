package chaos.tree.benchmark.nary;

import chaos.tree.nary.BTree;
import chaos.tree.nary.BPlusTree;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;


public class InsertDeleteBenchmark extends AbstractNaryBenchmark {

    private BTree<Integer> btree;
    private BPlusTree<Integer> bplus;

    private int mutationTarget;

    @Setup(Level.Trial)
    public void buildStaticTrees() {
        int[] data = getShuffledInts(size);
        mutationTarget = size;

        btree = new BTree<>(degree);
        bplus = new BPlusTree<>(degree);

        for (int v : data) {
            btree.insert(v);
            bplus.insert(v);
        }
    }

    @Benchmark
    public void bTreeInsertDelete(Blackhole bh) {
        btree.insert(mutationTarget);
        btree.delete(mutationTarget);
        bh.consume(btree);
    }

    @Benchmark
    public void bPlusTreeInsertDelete(Blackhole bh) {
        bplus.insert(mutationTarget);
        bplus.delete(mutationTarget);
        bh.consume(bplus);
    }
}
