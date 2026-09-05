package chaos.tree.benchmark;

import chaos.tree.binaryMap.AvlTreeMap;
import chaos.tree.binaryMap.RedBlackTreeMap;
import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * It currently run on default GC
 * G1CC
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
public class ChaosTreeMapReadBenchmark {

    @Param({"100000", "1000000"})
    public int size;

    @Param({"JavaTreeMap", "BTreeMap", "BPlusTreeMap", "RedBlackTreeMap", "AvlTreeMap"})
    public String mapType;

    private Map<Integer, String> map;
    private Integer[] queryKeys;

    @Setup(Level.Trial)
    public void setup() {
        List<Integer> keys = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keys.add(i);
        }
        // Shuffle insertion order so the tree isn't built from sorted input
        // (sorted-input insertion is a worst case / degenerate case for some
        // of these trees and would misrepresent typical shape/height).
        Collections.shuffle(keys, new Random(42));

        map = switch (mapType) {
            case "JavaTreeMap" -> new TreeMap<>();
            case "BTreeMap" -> new BTreeMap<>();
            case "BPlusTreeMap" -> new BPlusTreeMap<>();
            case "RedBlackTreeMap" -> new RedBlackTreeMap<>();
            case "AvlTreeMap" -> new AvlTreeMap<>();
            default -> throw new IllegalStateException("Unknown mapType: " + mapType);
        };

        for (Integer key : keys) {
            map.put(key, "CHAOS-" + key);
        }
        // Fail fast instead of silently benchmarking a half-populated map.
        assert map.size() == size;

        queryKeys = keys.toArray(new Integer[0]);
        // Independent seed from the insertion shuffle, so read order doesn't
        // correlate with insertion order (which could flatter/hurt caching
        // effects depending on tree internals).
        Collections.shuffle(Arrays.asList(queryKeys), new Random(84));
    }

    @Benchmark
    @OperationsPerInvocation(1000)
    public void getRandom(Blackhole bh) {
        for (int i = 0; i < 1000; i++) {
            bh.consume(map.get(queryKeys[i]));
        }
    }

    @Benchmark
    @OperationsPerInvocation(1000)
    public void containsKeyRandom(Blackhole bh) {
        for (int i = 0; i < 1000; i++) {
            bh.consume(map.containsKey(queryKeys[i]));
        }
    }

    @Benchmark
    @OperationsPerInvocation(1000)
    public void getRandomWrapped(Blackhole bh) {
        // For runs that want > 1000 samples per invocation without adding
        // iterate with modulo
        // guaranteed to be a power of two.
        for (int i = 0; i < 1000; i++) {
            bh.consume(map.get(queryKeys[i % size]));
        }
    }

    @Benchmark
    public void iterateEntries(Blackhole bh) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    @Benchmark
    public void iterateKeys(Blackhole bh) {
        for (Integer key : map.keySet()) {
            bh.consume(key);
        }
    }
}