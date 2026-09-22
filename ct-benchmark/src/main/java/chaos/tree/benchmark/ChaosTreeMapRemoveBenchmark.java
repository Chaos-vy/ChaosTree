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

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/*
@OperationsPerInvocation(100000) must be changed.
So this made me recompile and rerun it
java -jar ct-benchmark/target/benchmarks.jar ChaosTreeMapRemoveBenchmark.remove -w 1000ms -r 1000ms  -wi 10 -i 10 -p size=100000 -prof gc -prof perfnorm -prof jfr
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 15)
@Measurement(iterations = 25)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@State(Scope.Thread)
public class ChaosTreeMapRemoveBenchmark {

    @Param({"JavaTreeMap", "BPlusTreeMap", "BTreeMap"})
    public String mapType;

    @Param({"100000"})
    public int size;

    @Param({"42"})
    public long seed;

    private Supplier<NavigableMap<Integer, Integer>> baseSupplier;
    private NavigableMap<Integer, Integer> template;
    private Integer[] removalOrder;
    private Set<Map.Entry<Integer, Integer>> evenEntries;

    private NavigableMap<Integer, Integer> workingMap;

    @Setup(Level.Trial)
    public void setupTrial() {
        baseSupplier = switch (mapType) {
            case "JavaTreeMap" -> TreeMap::new;
            case "BTreeMap" -> BTreeMap::new;
            case "BPlusTreeMap" -> BPlusTreeMap::new;
            default -> throw new IllegalStateException(mapType);
        };

        template = baseSupplier.get();
        for (int i = 0; i < size; i++) {
            template.put(i, i);
        }

        removalOrder = IntStream.range(0, size).boxed().toArray(Integer[]::new);
        Random rnd = new Random(seed);
        Collections.shuffle(Arrays.asList(removalOrder), rnd);

        evenEntries = new HashSet<>();
        for (int i = 0; i < size; i += 2) {
            evenEntries.add(new AbstractMap.SimpleImmutableEntry<>(i, i));
        }
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        workingMap = baseSupplier.get();
        workingMap.putAll(template);
    }

    @Benchmark
    public void baselineSetupOnly(Blackhole bh) {
        bh.consume(workingMap);
    }

    @Benchmark
    @OperationsPerInvocation(100000)
    public void remove(Blackhole bh) {
        for (Integer key : removalOrder) {
            bh.consume(workingMap.remove(key));
        }
    }

    @Benchmark
    @OperationsPerInvocation(100000)
    public void iteratorRemove(Blackhole bh) {
        Iterator<Map.Entry<Integer, Integer>> it = workingMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Integer> e = it.next();
            bh.consume(e.getValue());
            it.remove();
        }
    }

    @Benchmark
    @OperationsPerInvocation(50000)
    public void removeIfHalf(Blackhole bh) {
        workingMap.entrySet().removeIf(e -> e.getKey() % 2 == 0);
        bh.consume(workingMap);
    }

    @Benchmark
    @OperationsPerInvocation(50000)
    public void removeAllHalf(Blackhole bh) {
        workingMap.entrySet().removeAll(evenEntries);
        bh.consume(workingMap);
    }

}
/*
# JFR copy
jfr view allocation-by-site *.jfr
jfr view jdk.GarbageCollection *.jfr
jfr view hot-methods *.jfr
jfr view memory-leaks-by-site *.jfr
 */