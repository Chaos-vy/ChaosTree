package chaos.tree.benchmark;

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

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@State(Scope.Thread)
public class ChaosTreeMapMixedBenchmark {

    @Param({"JavaTreeMap", "BPlusTreeMap", "BTreeMap"})
    public String mapType;

    @Param({"10000", "100000"})
    public int size;

    @Param({"42"})
    public long seed;

    @Param({"70,20,10","50,25,25","10,70,20","20,60,20","34,33,33","20,10,70"})
    public String mix;

    private static final byte OP_GET = 0, OP_PUT = 1, OP_REMOVE = 2;

    private Supplier<NavigableMap<Integer, Integer>> baseSupplier;
    private NavigableMap<Integer, Integer> template;

    private byte[] opType;
    private int[] opKey;

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

        String[] parts = mix.split(",");
        int getPct = Integer.parseInt(parts[0]);
        int putPct = Integer.parseInt(parts[1]);

        Random rnd = new Random(seed);
        opType = new byte[size];
        opKey = new int[size];
        for (int i = 0; i < size; i++) {
            int roll = rnd.nextInt(100);
            opType[i] = roll < getPct ? OP_GET
                    : roll < getPct + putPct ? OP_PUT
                    : OP_REMOVE;
            opKey[i] = rnd.nextInt(size);
        }
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        workingMap = baseSupplier.get();
        workingMap.putAll(template);
    }

    public void baselineSetupOnly(Blackhole bh) {
        bh.consume(workingMap);
    }

    @Benchmark
    public void mixedWorkload(Blackhole bh) {
        int n = opType.length;
        for (int i = 0; i < n; i++) {
            int key = opKey[i];
            switch (opType[i]) {
                case OP_GET -> bh.consume(workingMap.get(key));
                case OP_PUT -> bh.consume(workingMap.put(key, key));
                case OP_REMOVE -> bh.consume(workingMap.remove(key));
            }
        }
    }
}