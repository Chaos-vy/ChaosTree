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
 *
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 3)
@Fork(1)
public class DegreeScalingBenchmark {

    private static final int DATA_SIZE = 1_000_000;

    private static final int READ_POOL_SIZE = 1 << 20; // ~1,048,576

    @Param({"4", "8", "16", "32", "64", "100", "128", "200", "512"})
    public int degree;

    private int[] shuffledData;
    private BTree<Integer> btree;
    private BPlusTree<Integer> bplus;

    private int rangeStart;
    private int rangeEnd;

    private int[] readIndexPool;
    private int readPoolMask;

    private int btreeReadCursor;
    private int bplusReadCursor;

    @Setup(Level.Trial)
    public void setup() {
        shuffledData = new int[DATA_SIZE];
        for (int i = 0; i < DATA_SIZE; i++) shuffledData[i] = i;

        Random rng = new Random(42L);
        shuffleInPlace(shuffledData, rng);

        btree = new BTree<>(degree);
        bplus = new BPlusTree<>(degree);
        for (int v : shuffledData) {
            btree.insert(v);
            bplus.insert(v);
        }

        rangeStart = DATA_SIZE / 2;
        rangeEnd = rangeStart + (DATA_SIZE / 10);

        // Pre-generate random read indices (0 .. DATA_SIZE-1), fixed seed
        // for reproducibility across runs/degrees.
        readPoolMask = READ_POOL_SIZE - 1;
        readIndexPool = new int[READ_POOL_SIZE];
        Random readRng = new Random(7L);
        for (int i = 0; i < READ_POOL_SIZE; i++) {
            readIndexPool[i] = readRng.nextInt(DATA_SIZE);
        }

        btreeReadCursor = 0;
        bplusReadCursor = 0;
    }

    private static void shuffleInPlace(int[] arr, Random rng) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    @Benchmark
    public void bTreeRandomRead(Blackhole bh) {
        int idx = readIndexPool[btreeReadCursor++ & readPoolMask];
        bh.consume(btree.contains(shuffledData[idx]));
    }

    @Benchmark
    public void bPlusTreeRandomRead(Blackhole bh) {
        int idx = readIndexPool[bplusReadCursor++ & readPoolMask];
        bh.consume(bplus.contains(shuffledData[idx]));
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