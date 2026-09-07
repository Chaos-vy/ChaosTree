package chaos.tree.benchmark.map;

import chaos.tree.binaryMap.AvlTreeMap;
import chaos.tree.binaryMap.RedBlackTreeMap;
import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * This benchmark is different from others it does use Fischer yate shuffle data setup at trial
 * and then put() is used
 * My system config is 5.8GB default heap
 * 1MB default stack
 */

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
public class WriteHeavyMapShuffled {

    // Fixed seed: both benchmark methods, every fork, every iteration must
    // see the identical shuffled order, or the comparison stops being paired.
    private static final long SHUFFLE_SEED = 42L;
    @Param({"5000000"})
    public int size;
    private Integer[] shuffledKeys;
    private String[] valuesByKey;

    @Setup(Level.Trial)
    public void setup() {
        shuffledKeys = new Integer[size];
        valuesByKey = new String[size];

        for (int i = 0; i < size; i++) {
            shuffledKeys[i] = i;
            valuesByKey[i] = "CHAOS-" + i;
        }

        // Fisher-Yates shuffle, seeded — deterministic across forks/methods
        java.util.Random rnd = new java.util.Random(SHUFFLE_SEED);
        for (int i = size - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Integer tmp = shuffledKeys[i];
            shuffledKeys[i] = shuffledKeys[j];
            shuffledKeys[j] = tmp;
        }
    }

    @Benchmark
    public void jdkTreeMapIterativePutShuffled(Blackhole bh) {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            int key = shuffledKeys[i];
            map.put(key, valuesByKey[key]);
        }
        bh.consume(map);
    }

    @Benchmark
    public void bPlusTreeMapIterativePutShuffled(Blackhole bh) {
        BPlusTreeMap<Integer, String> map = new BPlusTreeMap<>();
        for (int i = 0; i < size; i++) {
            int key = shuffledKeys[i];
            map.put(key, valuesByKey[key]);
        }
        bh.consume(map);
    }

    @Benchmark
    public void bTreeMapIterativePutShuffled(Blackhole bh) {
        BTreeMap<Integer, String> map = new BTreeMap<>();
        for (int i = 0; i < size; i++) {
            int key = shuffledKeys[i];
            map.put(key, valuesByKey[key]);
        }
        bh.consume(map);
    }

    @Benchmark
    public void rbtTreeMapIterativePutShuffled(Blackhole bh) {
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        for (int i = 0; i < size; i++) {
            int key = shuffledKeys[i];
            map.put(key, valuesByKey[key]);
        }
        bh.consume(map);
    }

    @Benchmark
    public void avlTreeMapIterativePutShuffled(Blackhole bh) {
        AvlTreeMap<Integer, String> map = new AvlTreeMap<>();
        for (int i = 0; i < size; i++) {
            int key = shuffledKeys[i];
            map.put(key, valuesByKey[key]);
        }
        bh.consume(map);
    }
}