package chaos.tree.benchmark;

import chaos.tree.naryMap.BPlusTreeMap;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *     Benchmark                                                             (size)  Mode  Cnt   Score    Error   Units
 * ChaosTreeMapIterateBenchmark.iterateChaosTree                        1000000  avgt    9   2.540 ±  0.132   ms/op
 * ChaosTreeMapIterateBenchmark.iterateChaosTree:gc.alloc.rate          1000000  avgt    9   0.003 ±  0.001  MB/sec
 * ChaosTreeMapIterateBenchmark.iterateChaosTree:gc.alloc.rate.norm     1000000  avgt    9   8.719 ±  0.461    B/op
 * ChaosTreeMapIterateBenchmark.iterateChaosTree:gc.count               1000000  avgt    9     ≈ 0           counts
 * ChaosTreeMapIterateBenchmark.iterateJavaTreeMap                      1000000  avgt    9  20.650 ±  3.691   ms/op
 * ChaosTreeMapIterateBenchmark.iterateJavaTreeMap:gc.alloc.rate        1000000  avgt    9   0.003 ±  0.001  MB/sec
 * ChaosTreeMapIterateBenchmark.iterateJavaTreeMap:gc.alloc.rate.norm   1000000  avgt    9  70.686 ± 12.486    B/op
 * ChaosTreeMapIterateBenchmark.iterateJavaTreeMap:gc.count             1000000  avgt    9     ≈ 0           counts
 * ChaosTreeMapIterateBenchmark.iterateRawArrayList                     1000000  avgt    9   3.090 ±  0.213   ms/op
 * ChaosTreeMapIterateBenchmark.iterateRawArrayList:gc.alloc.rate       1000000  avgt    9   0.003 ±  0.001  MB/sec
 * ChaosTreeMapIterateBenchmark.iterateRawArrayList:gc.alloc.rate.norm  1000000  avgt    9  10.604 ±  0.745    B/op
 * ChaosTreeMapIterateBenchmark.iterateRawArrayList:gc.count            1000000  avgt    9     ≈ 0           counts
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 3, time = 2)
@Fork(3)
public class ChaosTreeMapIterateBenchmark {

    @Param({"1000000"})
    public int size;

    private TreeMap<Integer, String> javaTreeMap;
    private BPlusTreeMap<Integer, String> chaosTree;
    private ArrayList<String> arrayList;

    @Setup(Level.Trial)
    public void setup() {
        javaTreeMap = new TreeMap<>();
        chaosTree = new BPlusTreeMap<>();
        arrayList = new ArrayList<>(size);

        List<Integer> keys = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keys.add(i);
        }
        Collections.shuffle(keys, new Random(42));

        for (Integer key : keys) {
            String val = "CHAOS-" + key;
            javaTreeMap.put(key, val);
            chaosTree.put(key, val);
        }

        for (int i = 0; i < size; i++) {
            arrayList.add("CHAOS-" + i);
        }
    }

    @Benchmark
    public void iterateJavaTreeMap(Blackhole bh) {
        for (Map.Entry<Integer, String> entry : javaTreeMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    @Benchmark
    public void iterateChaosTree(Blackhole bh) {
        for (Map.Entry<Integer, String> entry : chaosTree.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    @Benchmark
    public void iterateRawArrayList(Blackhole bh) {
        for (String s: arrayList) {
            bh.consume(s);
        }
    }
}
