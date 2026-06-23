package chaos.tree.benchmark.nary;

import chaos.tree.nary.BTree;
import chaos.tree.nary.BPlusTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Specifically designed to find the CPU Cache "Inflection Point" of the N-ary family.
 * Runs on a fixed 1M data set (L3 Cache/RAM boundary) while drastically scaling
 * the node capacity (degree). 
 * 
 * At lower degrees (4, 8), the tree is deep -> lots of object dereferences (Cache Misses).
 * At higher degrees (200, 512), the tree is shallow -> massive array shifts on insert.
 * The sweet spot is usually between 32 and 128 (where the node perfectly fits L1/L2 cache lines).
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class DegreeScalingBenchmark {

    private static final int DATA_SIZE = 1_000_000;

    @Param({"4", "8", "16", "32", "64", "100", "128", "200", "512"})
    public int degree;

    private int[] shuffledData;

    private BTree<Integer> btree;
    private BPlusTree<Integer> bplus;

    private int rangeStart;
    private int rangeEnd;

    @Setup(Level.Trial)
    public void setup() {
        shuffledData = new int[DATA_SIZE];
        for (int i = 0; i < DATA_SIZE; i++) shuffledData[i] = i;
        Random rng = new Random(42L);
        for (int i = DATA_SIZE - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = shuffledData[i];
            shuffledData[i] = shuffledData[j];
            shuffledData[j] = tmp;
        }

        btree = new BTree<>(degree);
        bplus = new BPlusTree<>(degree);

        for (int v : shuffledData) {
            btree.insert(v);
            bplus.insert(v);
        }

        rangeStart = DATA_SIZE / 2;
        rangeEnd = rangeStart + (DATA_SIZE / 10);
    }

    @Benchmark
    public void bTreeRandomRead(Blackhole bh) {
        bh.consume(btree.contains(shuffledData[(int) (Math.random() * DATA_SIZE)]));
    }

    @Benchmark
    public void bPlusTreeRandomRead(Blackhole bh) {
        bh.consume(bplus.contains(shuffledData[(int) (Math.random() * DATA_SIZE)]));
    }

    @Benchmark
    public void bTreeRangeScan(Blackhole bh) {
        bh.consume(btree.range(rangeStart, rangeEnd));
    }

    @Benchmark
    public void bPlusTreeRangeScan(Blackhole bh) {
        bh.consume(bplus.range(rangeStart, rangeEnd));
    }
}
