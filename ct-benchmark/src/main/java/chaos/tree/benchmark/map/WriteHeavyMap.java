package chaos.tree.benchmark.map;

import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * If you feel this is bias I did with TreeMap with array then bring a library which is does support like this!!
 * focus on benchmark
 * Default GC-> G1CC
 */
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
public class WriteHeavyMap {

    @Param({"5000000"})
    public int size;

    @Param({"0.8f"})
    public float factor;
//    @Param({"0.5f","0.6f","0.7f","0.8f","0.9f","1f"})
// For benchmarkers just replace this benchmark and run the DragonFeed to show how density affects the node mapping.
    private Object[][] flatMatrix;
    private TreeMap<Integer, String> preBuiltSortedMap;

    @Setup(Level.Trial)
    public void setup() {
        Integer[] sortedKeys = new Integer[size];
        String[] mappedValues = new String[size];
        preBuiltSortedMap = new TreeMap<>();

        for (int i = 0; i < size; i++) {
            sortedKeys[i] = i;
            mappedValues[i] = "CHAOS-" + i;
            // Pre-build the SortedMap for the JDK to consume
            preBuiltSortedMap.put(sortedKeys[i], mappedValues[i]);
        }

        flatMatrix = new Object[2][size];
        flatMatrix[0] = sortedKeys;
        flatMatrix[1] = mappedValues;
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
    // 2. JDK O(N) BULK LOAD (Apples-to-Apples)
    @Benchmark
    public void jdkTreeMapBulkLoad(Blackhole bh) {
        // Passing a SortedMap into the constructor triggers the JDK's
        // internal O(N) buildFromSorted loop.
        TreeMap<Integer, String> map = new TreeMap<>(preBuiltSortedMap);
        bh.consume(map);
    }

    @Benchmark
    public void bPlusTreeMapBulkLoad(Blackhole bh){
        BPlusTreeMap<Integer,String> map = new BPlusTreeMap<>(preBuiltSortedMap);
        bh.consume(map);
    }

    // 3. THE DRAGON FEED (O(N) Flat Matrix Ingestion)
    @Benchmark
    public void bPlusTreeDragonFeed(Blackhole bh) {
        BTreeMap<Integer, String> map =
                BTreeMap.Builder.<Integer, String>degree(96)
                        .factor(factor)
                        .importFlatMatrix(flatMatrix)
                        .build();

        bh.consume(map);
    }
}