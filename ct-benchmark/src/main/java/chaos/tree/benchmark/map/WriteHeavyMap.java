package chaos.tree.benchmark.map;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(value = 3)
public class WriteHeavyMap {

    private static final long SEED = 42L;

    @Param({"10000", "100000", "1000000", "10000000"})
    public int size;

    @Param("64")
    public int degree;

    @Param("0.75")
    public float factor;

    private Integer[] sortedKeys;
    private String[] sortedValues;
    private Integer[] randomKeys;
    private String[] randomValues;
    private TreeMap<Integer, String> sortedTreeMap;
    private Object[][] flatMatrix;

    @Setup(Level.Trial)
    public void setup() {
        sortedKeys = new Integer[size];
        sortedValues = new String[size];
        sortedTreeMap = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            sortedKeys[i] = i;
            sortedValues[i] = "CHAOS-" + i;
            sortedTreeMap.put(sortedKeys[i], sortedValues[i]);
        }

        randomKeys = sortedKeys.clone();
        Collections.shuffle(Arrays.asList(randomKeys), new Random(SEED));
        randomValues = new String[size];
        for (int i = 0; i < size; i++) {
            randomValues[i] = sortedValues[randomKeys[i]];
        }

        flatMatrix = new Object[][]{sortedKeys, sortedValues};
    }

    @Benchmark
    public Map<Integer, String> sequentialPutJdkTreeMap() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            map.put(sortedKeys[i], sortedValues[i]);
        }
        return map;
    }

    @Benchmark
    public Map<Integer, String> sequentialPutBPlusTree() {
        BPlusTreeMap<Integer, String> map = new BPlusTreeMap<>(degree);
        for (int i = 0; i < size; i++) {
            map.put(sortedKeys[i], sortedValues[i]);
        }
        return map;
    }

    @Benchmark
    public Map<Integer, String> randomPutJdkTreeMap() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            map.put(randomKeys[i], randomValues[i]);
        }
        return map;
    }

    @Benchmark
    public Map<Integer, String> randomPutBPlusTree() {
        BPlusTreeMap<Integer, String> map = new BPlusTreeMap<>(degree);
        for (int i = 0; i < size; i++) {
            map.put(randomKeys[i], randomValues[i]);
        }
        return map;
    }

    @Benchmark
    public Map<Integer, String> bulkLoadJdkTreeMap() {
        return new TreeMap<>(sortedTreeMap);
    }

    @Benchmark
    public Map<Integer, String> bulkLoadBPlusTree() {
        return new BPlusTreeMap<>(sortedTreeMap);
    }

    @Benchmark
    public Map<Integer, String> importFlatMatrixBPlusTree() {
        return BPlusTreeMap.Builder.<Integer, String>create(degree)
                .factor(factor)
                .importFlatMatrix(flatMatrix)
                .build();
    }
}
