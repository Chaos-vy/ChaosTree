package chaos.tree.benchmark;

import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@State(Scope.Thread)
public class ChaosTreeMapReadBenchmark {

    @Param({"JavaTreeMap", "BPlusTreeMap", "BTreeMap"})
    public String mapType;

    @Param({"TreeMap", "descendingMap", "subMap"})
    public String mode;

    @Param({"100000"})
    public int size;

    @Param({"42"})
    public long seed;

    private NavigableMap<Integer, Integer> map;

    private Integer[] hitKeys;

    private Integer[] missKeys;

    @Setup(Level.Trial)
    public void setUp() {
        Supplier<NavigableMap<Integer, Integer>> baseSupplier = switch (mapType) {
            case "JavaTreeMap" -> () -> new TreeMap<>(Comparator.reverseOrder());
            case "BTreeMap" -> () -> new BTreeMap<>(Comparator.reverseOrder());
            case "BPlusTreeMap" -> () -> new BPlusTreeMap<>(Comparator.reverseOrder());
            default -> throw new IllegalStateException(mapType);
        };

        NavigableMap<Integer, Integer> base = baseSupplier.get();
        for (int i = 0; i < size; i++) {
            base.put(i, i);
        }

        UnaryOperator<NavigableMap<Integer, Integer>> transformer = switch (mode) {
            case "TreeMap" -> m -> m;
            case "descendingMap" -> NavigableMap::descendingMap;
            case "subMap" -> m -> m.tailMap(size - 1, true);
            default -> throw new IllegalStateException(mode);
        };
        map = transformer.apply(base);

        hitKeys = IntStream.range(0, size).boxed().toArray(Integer[]::new);

        missKeys = new Integer[size];
        int half = size / 2;
        for (int i = 0; i < half; i++) {
            missKeys[i] = -(i + 1);
        }
        for (int i = half; i < size; i++) {
            missKeys[i] = size + (i - half);
        }

        Random rnd = new Random(seed);
        Collections.shuffle(Arrays.asList(hitKeys), rnd);
        Collections.shuffle(Arrays.asList(missKeys), rnd);
    }

    @Benchmark
    @OperationsPerInvocation(100000)
    public void get(Blackhole bh) {
        for (Integer key : hitKeys) {
            bh.consume(map.get(key));
        }
    }

    @Benchmark
    @OperationsPerInvocation(100000)
    public void getMiss(Blackhole bh) {
        for (Integer key : missKeys) {
            bh.consume(map.get(key));
        }
    }

    @Benchmark
    @OperationsPerInvocation(100000)
    public void containsKey(Blackhole bh) {
        for (Integer key : hitKeys) {
            bh.consume(map.containsKey(key));
        }
    }

    @Benchmark
    @OperationsPerInvocation(100000)
    public void floorKey(Blackhole bh) {
        for (Integer key : hitKeys) {
            bh.consume(map.floorKey(key));
        }
    }

    @Benchmark
    @OperationsPerInvocation(100000)
    public void ceilingKey(Blackhole bh) {
        for (Integer key : hitKeys) {
            bh.consume(map.ceilingKey(key));
        }
    }

    @Benchmark
    public void firstAndLast(Blackhole bh) {
        bh.consume(map.firstEntry());
        bh.consume(map.lastEntry());
    }

    @Benchmark
    @OperationsPerInvocation(100000)
    public void iterate(Blackhole bh) {
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            bh.consume(e.getValue());
        }
    }
}