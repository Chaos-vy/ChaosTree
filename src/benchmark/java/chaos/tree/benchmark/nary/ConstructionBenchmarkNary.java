package chaos.tree.benchmark.nary;

import chaos.tree.nary.BTree;
import chaos.tree.nary.BPlusTree;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ConstructionBenchmarkNary extends AbstractNaryBenchmark {

    private List<Integer> sourceData;

    private BTree<Integer> prebuiltBTree;
    private BPlusTree<Integer> prebuiltBPlusTree;

    @Setup(Level.Trial)
    public void setup() {
        sourceData = new ArrayList<>(size);
        int[] shuffled = getShuffledInts(size);
        for (int v : shuffled) {
            sourceData.add(v);
        }

        prebuiltBTree = new BTree<>(degree);
        prebuiltBPlusTree = new BPlusTree<>(degree);

        for (int v : sourceData) {
            prebuiltBTree.insert(v);
            prebuiltBPlusTree.insert(v);
        }
    }

    @Benchmark
    public BTree<Integer> bTreeFromIterable() {
        return new BTree<>(degree, sourceData);
    }

    @Benchmark
    public BPlusTree<Integer> bPlusTreeFromIterable() {
        return new BPlusTree<>(degree, sourceData);
    }

    @Benchmark
    public BTree<Integer> bTreeClone() {
        return new BTree<>(prebuiltBTree);
    }

    @Benchmark
    public BPlusTree<Integer> bPlusTreeClone() {
        return new BPlusTree<>(prebuiltBPlusTree);
    }
}
