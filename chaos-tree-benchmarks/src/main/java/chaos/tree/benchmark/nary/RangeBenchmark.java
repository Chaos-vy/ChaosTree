package chaos.tree.benchmark.nary;

import chaos.tree.nary.BTree;
import chaos.tree.nary.BPlusTree;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;


public class RangeBenchmark extends AbstractNaryBenchmark {

    private BTree<Integer> btree;
    private BPlusTree<Integer> bplus;

    private int rangeStart;
    private int rangeEnd;

    @Setup(Level.Trial)
    public void buildStaticTrees() {
        int[] data = getShuffledInts(size);

        btree = new BTree<>(degree);
        bplus = new BPlusTree<>(degree);

        for (int v : data) {
            btree.insert(v);
            bplus.insert(v);
        }

        rangeStart = size / 2;
        rangeEnd = rangeStart + (size / 10);
    }

    @Benchmark
    public void bTreeRange(Blackhole bh) {
        bh.consume(btree.range(rangeStart, rangeEnd));
    }

    @Benchmark
    public void bPlusTreeRange(Blackhole bh) {
        bh.consume(bplus.range(rangeStart, rangeEnd));
    }
}
