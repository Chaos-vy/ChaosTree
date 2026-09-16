package chaos.tree.benchmark.map;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * If you feel this is bias I did with TreeMap with array then bring a library which is does support like this!!
 * focus on benchmark
 * Default GC
 */

/**
 * <pre>
 *
 * Benchmark                                  (factor)   (size)  Mode  Cnt    Score    Error  Units
 * WriteHeavyMapx01.bPlusTreeDragonFeed           0.8f  5000000  avgt   15    5.548 ±  0.024  ms/op
 * WriteHeavyMapx01.bPlusTreeMapBulkLoad          0.8f  5000000  avgt   15   37.379 ±  0.422  ms/op
 * WriteHeavyMapx01.bPlusTreeMapIterativePut      0.8f  5000000  avgt   15  211.627 ±  1.998  ms/op
 * WriteHeavyMapx01.jdkTreeMapBulkLoad            0.8f  5000000  avgt   15   44.202 ±  2.487  ms/op
 * WriteHeavyMapx01.jdkTreeMapIterativePut        0.8f  5000000  avgt   15  778.044 ± 18.889  ms/op
 * </pre>
 * //do ignore that factor tag I used that factor tag to jsut build the benchmark of
 * factor x memory
 * more the factor less the memory and vice-versa.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(value = 3, jvmArgs = {
        "-Xms4g",
        "-Xmx4g",
        "-XX:+UseParallelGC",
        "-XX:+AlwaysPreTouch"
})
public class WriteHeavyMapx01 {

    @Param({"5000000"})
    public int size;

    @Param({"0.75f"})
    public float factor;
    //    @Param({"0.5f","0.6f","0.7f","0.8f","0.9f","1f"})
// For benchmarkers just replace this benchmark and run the DragonFeed to show how density affects the node mapping.
    private Object[][] flatMatrix;
    private TreeMap<Integer, String> preBuiltSortedMap;
    private Integer[] shuffledKeys;
    private String[] shuffledValues;
    private String[] mappedValues;
    @Setup(Level.Trial)
    public void setup() {
        Integer[] sortedKeys = new Integer[size];
        Integer[] shuffledKeys = new Integer[size];
        mappedValues = new String[size];
        preBuiltSortedMap = new TreeMap<>();

        for (int i = 0; i < size; i++) {
            sortedKeys[i] = i;
            mappedValues[i] = "CHAOS-" + i;
            preBuiltSortedMap.put(sortedKeys[i], mappedValues[i]);
        }

        // Randomized key order — same key set, shuffled insertion sequence
        shuffledKeys = sortedKeys.clone();
        Collections.shuffle(Arrays.asList(shuffledKeys), new Random(42)); // fixed seed for reproducibility

        flatMatrix = new Object[2][size];
        flatMatrix[0] = sortedKeys;
        flatMatrix[1] = mappedValues;

        this.shuffledKeys = shuffledKeys; // store for random-order benchmarks
    }

    // Random insertion: not If I do decrease to L1 cache level it's TreeMap win.
    @Benchmark
    public void jdkTreeMapRandomPut(Blackhole bh) {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            Integer key = shuffledKeys[i];
            map.put(key, mappedValues[key]);   // key doubles as the index
        }
        bh.consume(map);
    }

    @Benchmark
    public void bPlusTreeMapRandomPut(Blackhole bh) {
        BPlusTreeMap<Integer, String> map = new BPlusTreeMap<>();
        for (int i = 0; i < size; i++) {
            Integer key = shuffledKeys[i];
            map.put(key, mappedValues[key]);
        }
        bh.consume(map);
    }

    // 1. ITERATIVE BASELINE (O(N log N))
    @Benchmark
    public void jdkTreeMapIterativePut(Blackhole bh) {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            map.put((Integer) flatMatrix[0][i], (String) flatMatrix[1][i]);
        }
        bh.consume(map);
    }

    @Benchmark
    public void bPlusTreeMapIterativePut(Blackhole bh) {
        BPlusTreeMap<Integer, String> map = new BPlusTreeMap<>();
        for (int i = 0; i < size; i++) {
            map.put((Integer) flatMatrix[0][i], (String) flatMatrix[1][i]);
        }
        bh.consume(map);
    }

    @Benchmark
    public void jdkTreeMapBulkLoad(Blackhole bh) {
        TreeMap<Integer, String> map = new TreeMap<>(preBuiltSortedMap);
        bh.consume(map);
    }

    @Benchmark
    public void bPlusTreeMapBulkLoad(Blackhole bh) {
        BPlusTreeMap<Integer, String> map = new BPlusTreeMap<>(preBuiltSortedMap);
        bh.consume(map);
    }

    // 3. THE DRAGON FEED (O(N) Flat Matrix Ingestion)
    @Benchmark
    public void bPlusTreeDragonFeed(Blackhole bh) {
        BPlusTreeMap<Integer, String> map =
                BPlusTreeMap.Builder.<Integer, String>create(64)
                        .factor(factor)
                        .importFlatMatrix(flatMatrix)
                        .build();

        bh.consume(map);
    }
}