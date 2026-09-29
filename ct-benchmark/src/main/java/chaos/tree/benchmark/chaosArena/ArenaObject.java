package chaos.tree.benchmark.chaosArena;

import chaos.tree.binaryMap.AvlTreeMap;
import chaos.tree.binaryMap.RedBlackTreeMap;
import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
/*
I don't like to put dirt here so my CLI remains clean.
-wi 10 -5 1 -i 10 -r 1 -f 1 -j -jvmArgsAppend "-Xms4g -Xmx4g -XX:+AlwaysPreTouch -XX:+UseParallelGC"
(well these can be overridden but Jvm flags won't)
 these are flag used for benchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@State(Scope.Thread)
public class ArenaObject {

    private static final int OPS = 100_000;
    //operations are fixed so the benchmarked will have 10K OperationPerInvocation(10K)
    private static final int OP_GET = 0, OP_PUT = 1, OP_REMOVE = 2;
    private static final int OP_SHIFT = 30;
    private static final int KEY_MASK = (1 << OP_SHIFT) - 1;
    private static final int POOL_SIZE = 1 << 21;
    private static final int POOL_MASK = POOL_SIZE - 1;

    @Param({
            "ChaosBPlusTree", "ChaosBTree", "ChaosAVL", "ChaosRBT",
            "JDKTreeMap", "JDKConcurrentSkipListMap",
            "FastutilAVLObj", "FastutilRBTObj", "EclipseRBT"
    })
    public String mapType;

    @Param({"10000", "100000", "1000000"})
    public int size;

    @Param({"42"})
    public long seed;

    //This argument manipulated in CLI is broken. Must alter the split from file itself.
    // Make sure enough to recompile it.
    @Param({"50,25,25", "20,40,40", "80,10,10", "70,30,0", "50,50,0", "30,70,0"})
    public String mix;

    private Map<Integer, Integer> map;
    private Integer[] boxed;
    private int[] pool;
    private int cursor;

    private Map<Integer, Integer> newMap() {
        switch (mapType) {
            case "ChaosBPlusTree": return new BPlusTreeMap<>(64);
            case "ChaosBTree": return new BTreeMap<>(64);
            case "ChaosAVL": return new AvlTreeMap<>();
            case "ChaosRBT": return new RedBlackTreeMap<>();
            case "JDKTreeMap": return new TreeMap<>();
            case "JDKConcurrentSkipListMap": return new ConcurrentSkipListMap<>();
            case "FastutilAVLObj": return new Object2ObjectAVLTreeMap<>();
            case "FastutilRBTObj": return new Object2ObjectRBTreeMap<>();
            case "EclipseRBT": return new TreeSortedMap<>();
            default: throw new IllegalStateException(mapType);
        }
    }


    @Setup(Level.Trial)
    public void setupTrial() {
        final int universe = size * 2;

        boxed = new Integer[universe];
        for (int i = 0; i < universe; i++) boxed[i] = i;

        map = newMap();
        Random rnd = new Random(seed);
        int[] order = new int[size];
        for (int i = 0; i < size; i++) order[i] = i;
        for (int i = size - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = order[i]; order[i] = order[j]; order[j] = t;
        }
        for (int k : order) map.put(boxed[k], boxed[k]);

        String[] parts = mix.split(",");
        int getPct = Integer.parseInt(parts[0]);
        int putPct = Integer.parseInt(parts[1]);

        Random opRnd = new Random(seed ^ 0x5DEECE66DL);
        pool = new int[POOL_SIZE];
        for (int i = 0; i < POOL_SIZE; i++) {
            int roll = opRnd.nextInt(100);
            int op = roll < getPct ? OP_GET : roll < getPct + putPct ? OP_PUT : OP_REMOVE;
            pool[i] = (op << OP_SHIFT) | opRnd.nextInt(universe);
        }
        cursor = 0;
    }

    @Benchmark
    @OperationsPerInvocation(OPS)
    public void mixedWorkload(Blackhole bh) {
        final Map<Integer, Integer> m = map;
        final Integer[] b = boxed;
        final int[] p = pool;
        int c = cursor;

        for (int i = 0; i < OPS; i++) {
            int packed = p[c++ & POOL_MASK];
            Integer k = b[packed & KEY_MASK];
            switch (packed >>> OP_SHIFT) {
                case OP_GET -> bh.consume(m.get(k));
                case OP_PUT -> bh.consume(m.put(k, k));
                default -> bh.consume(m.remove(k));
            }
        }

        cursor = c;
    }
}