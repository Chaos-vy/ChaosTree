package chaos.tree.benchmark.chaosArena;

import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntSortedMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@State(Scope.Thread)
public class ArenaPrimitive {

    private static final int OPS = 100_000;
    private static final int OP_GET = 0, OP_PUT = 1, OP_REMOVE = 2;
    private static final int OP_SHIFT = 30;
    private static final int KEY_MASK = (1 << OP_SHIFT) - 1;
    private static final int POOL_SIZE = 1 << 21;
    private static final int POOL_MASK = POOL_SIZE - 1;

    @Param({"FastutilAVLIntNative", "FastutilRBTIntNative"})
    public String mapType;

    @Param({"1000", "10000", "100000", "1000000"})
    public int size;

    @Param({"42"})
    public long seed;

    @Param({"RANDOM", "SEQUENTIAL"})
    public String buildOrder;

    @Param({"50,25,25", "20,40,40", "80,10,10", "70,30,0", "50,50,0", "30,70,0"})
    public String mix;

    private Int2IntSortedMap map;
    private int[] pool;
    private int cursor;

    private Int2IntSortedMap newMap() {
        switch (mapType) {
            case "FastutilAVLIntNative": return new Int2IntAVLTreeMap();
            case "FastutilRBTIntNative": return new Int2IntRBTreeMap();
            default: throw new IllegalStateException(mapType);
        }
    }

    @Setup(Level.Trial)
    public void setupTrial() {
        final int universe = size * 2;

        map = newMap();
        if ("SEQUENTIAL".equals(buildOrder)) {
            for (int i = 0; i < size; i++) map.put(i, i);
        } else {
            Random rnd = new Random(seed);
            int[] order = new int[size];
            for (int i = 0; i < size; i++) order[i] = i;
            for (int i = size - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                int t = order[i]; order[i] = order[j]; order[j] = t;
            }
            for (int k : order) map.put(k, k);
        }

        // pre-drawn op stream, separate RNG so it is identical across mapType/buildOrder
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
    public void mixedWorkloadNative(Blackhole bh) {
        final Int2IntSortedMap m = map;
        final int[] p = pool;
        int c = cursor;

        for (int i = 0; i < OPS; i++) {
            int packed = p[c++ & POOL_MASK];
            int key = packed & KEY_MASK;
            switch (packed >>> OP_SHIFT) {
                case OP_GET -> bh.consume(m.get(key));
                case OP_PUT -> bh.consume(m.put(key, key));
                default     -> bh.consume(m.remove(key));
            }
        }

        cursor = c;
    }
}