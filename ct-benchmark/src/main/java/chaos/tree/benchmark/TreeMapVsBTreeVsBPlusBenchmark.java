package chaos.tree.benchmark;

import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.naryMap.BTreeMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * This benchmark I used to get bm Sample and log, debug as well.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms4g", "-Xmx4g"})
public class TreeMapVsBTreeVsBPlusBenchmark {

    @Param({"100000", "1000000"})
    private int n;

    private static final int DEGREE = 64;

    private NavigableMap<Integer, Integer> treeMap;
    private NavigableMap<Integer, Integer> bTreeMap;
    private NavigableMap<Integer, Integer> bPlusTreeMap;

    private ArrayList<Integer> key;
    private ArrayList<Integer> value;
    private ArrayList<Integer> keyDel;

    private int cursor;

    @Setup(Level.Trial)
    public void setupTrial() {
        treeMap = new TreeMap<>();
        bTreeMap = new BTreeMap<>(DEGREE);
        bPlusTreeMap = new BPlusTreeMap<>(DEGREE);

        key = new ArrayList<>(n);
        value = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            key.add(i);
            value.add(i);
        }
        Collections.shuffle(key);
        Collections.shuffle(value);

        keyDel = new ArrayList<>(key);
        Collections.shuffle(keyDel);

        for (int i = 0; i < n; i++) {
            treeMap.put(key.get(i), value.get(i));
            bTreeMap.put(key.get(i), value.get(i));
            bPlusTreeMap.put(key.get(i), value.get(i));
        }
    }

    private int nextIndex() {
        int i = cursor;
        cursor = (i + 1 == n) ? 0 : i + 1;
        return i;
    }

    @Benchmark
    public void treeMapGet(Blackhole bh) {
        bh.consume(treeMap.get(key.get(nextIndex())));
    }

    @Benchmark
    public void bTreeMapGet(Blackhole bh) {
        bh.consume(bTreeMap.get(key.get(nextIndex())));
    }

    @Benchmark
    public void bPlusTreeMapGet(Blackhole bh) {
        bh.consume(bPlusTreeMap.get(key.get(nextIndex())));
    }

    @Benchmark
    public void treeMapPutRemove(Blackhole bh) {
        Integer k = keyDel.get(nextIndex());
        Integer removed = treeMap.remove(k);
        bh.consume(treeMap.put(k, removed));
    }

    @Benchmark
    public void bTreeMapPutRemove(Blackhole bh) {
        Integer k = keyDel.get(nextIndex());
        Integer removed = bTreeMap.remove(k);
        bh.consume(bTreeMap.put(k, removed));
    }

    @Benchmark
    public void bPlusTreeMapPutRemove(Blackhole bh) {
        Integer k = keyDel.get(nextIndex());
        Integer removed = bPlusTreeMap.remove(k);
        bh.consume(bPlusTreeMap.put(k, removed));
    }
    /**
     * This data is for analyzing purpose
     * Ran : taskset -c 0-11 java -jar ct-benchmark/target/benchmarks.jar TreeMapVsBTreeVsBPlusBenchmark.bPlusTreeMapPutRemove -p n=1000000 -bm sample -f 3 -wi 3 -i 10 -jvmArgsAppend "-XX:+UseParallelGC -XX:+AlwaysPreTouch " -prof perfnorm
     * A hardware study how the shift in data changes the benchmark.
     * <pre>
     *
     Benchmark                                                  (n)    Mode      Cnt     Score     Error  Units
     bPlusTreeMapPutRemove                                  1000000  sample  2077401     0.963 ±   0.002  us/op
     bPlusTreeMapPutRemove:cpu_core/L1-dcache-loads/        1000000  sample        3   391.857 ±  18.923   #/op
     bPlusTreeMapPutRemove:cpu_core/L1-dcache-stores/       1000000  sample        3    88.604 ±   2.783   #/op
     bPlusTreeMapPutRemove:cpu_core/L1-icache-load-misses/  1000000  sample        3     0.776 ±   0.782   #/op
     bPlusTreeMapPutRemove:cpu_core/LLC-load-misses/        1000000  sample        3    12.285 ±   5.367   #/op
     bPlusTreeMapPutRemove:cpu_core/LLC-loads/              1000000  sample        3    32.547 ±   5.014   #/op
     bPlusTreeMapPutRemove:cpu_core/LLC-store-misses/       1000000  sample        3     0.001 ±   0.008   #/op
     bPlusTreeMapPutRemove:cpu_core/LLC-stores/             1000000  sample        3     0.889 ±   0.149   #/op
     bPlusTreeMapPutRemove:cpu_core/branch-misses/          1000000  sample        3    33.501 ±   6.996   #/op
     bPlusTreeMapPutRemove:cpu_core/branches/               1000000  sample        3   454.174 ±  13.171   #/op
     bPlusTreeMapPutRemove:cpu_core/cycles/                 1000000  sample        3  3137.243 ± 339.160   #/op
     bPlusTreeMapPutRemove:cpu_core/dTLB-load-misses/       1000000  sample        3    16.076 ±   5.629   #/op
     bPlusTreeMapPutRemove:cpu_core/dTLB-loads/             1000000  sample        3   393.384 ±  17.082   #/op
     bPlusTreeMapPutRemove:cpu_core/dTLB-store-misses/      1000000  sample        3     0.118 ±   0.116   #/op
     bPlusTreeMapPutRemove:cpu_core/dTLB-stores/            1000000  sample        3    88.827 ±  12.020   #/op
     bPlusTreeMapPutRemove:cpu_core/iTLB-load-misses/       1000000  sample        3     0.024 ±   0.031   #/op
     bPlusTreeMapPutRemove:cpu_core/instructions/           1000000  sample        3  1911.162 ± 576.843   #/op
     bPlusTreeMapPutRemove:p0.00                            1000000  sample              0.261            us/op
     bPlusTreeMapPutRemove:p0.50                            1000000  sample              0.891            us/op
     bPlusTreeMapPutRemove:p0.90                            1000000  sample              1.204            us/op
     bPlusTreeMapPutRemove:p0.95                            1000000  sample              1.312            us/op
     bPlusTreeMapPutRemove:p0.99                            1000000  sample              1.738            us/op
     bPlusTreeMapPutRemove:p0.999                           1000000  sample             12.880            us/op
     bPlusTreeMapPutRemove:p0.9999                          1000000  sample             18.368            us/op
     bPlusTreeMapPutRemove:p1.00                            1000000  sample            176.128            us/op
     * </pre>
     * <pre>
     *
     Benchmark                                                 (n)    Mode      Cnt     Score     Error  Units
     bPlusTreeMapPutRemove                                  100000  sample  2334958     0.435 ±   0.001  us/op
     bPlusTreeMapPutRemove:cpu_core/L1-dcache-loads/        100000  sample        3   353.578 ±   7.339   #/op
     bPlusTreeMapPutRemove:cpu_core/L1-dcache-stores/       100000  sample        3    87.346 ±   5.507   #/op
     bPlusTreeMapPutRemove:cpu_core/L1-icache-load-misses/  100000  sample        3     0.357 ±   0.466   #/op
     bPlusTreeMapPutRemove:cpu_core/LLC-load-misses/        100000  sample        3     0.068 ±   0.161   #/op
     bPlusTreeMapPutRemove:cpu_core/LLC-loads/              100000  sample        3    11.042 ±   1.541   #/op
     bPlusTreeMapPutRemove:cpu_core/LLC-store-misses/       100000  sample        3     0.001 ±   0.005   #/op
     bPlusTreeMapPutRemove:cpu_core/LLC-stores/             100000  sample        3     0.091 ±   0.221   #/op
     bPlusTreeMapPutRemove:cpu_core/branch-misses/          100000  sample        3    28.933 ±   2.181   #/op
     bPlusTreeMapPutRemove:cpu_core/branches/               100000  sample        3   404.900 ±   7.680   #/op
     bPlusTreeMapPutRemove:cpu_core/cycles/                 100000  sample        3  1521.421 ± 298.852   #/op
     bPlusTreeMapPutRemove:cpu_core/dTLB-load-misses/       100000  sample        3     0.188 ±   0.424   #/op
     bPlusTreeMapPutRemove:cpu_core/dTLB-loads/             100000  sample        3   354.132 ±   6.549   #/op
     bPlusTreeMapPutRemove:cpu_core/dTLB-store-misses/      100000  sample        3     0.004 ±   0.010   #/op
     bPlusTreeMapPutRemove:cpu_core/dTLB-stores/            100000  sample        3    87.437 ±   5.842   #/op
     bPlusTreeMapPutRemove:cpu_core/iTLB-load-misses/       100000  sample        3     0.005 ±   0.012   #/op
     bPlusTreeMapPutRemove:cpu_core/instructions/           100000  sample        3  1692.896 ± 397.534   #/op
     bPlusTreeMapPutRemove:p0.00                            100000  sample              0.170            us/op
     bPlusTreeMapPutRemove:p0.50                            100000  sample              0.418            us/op
     bPlusTreeMapPutRemove:p0.90                            100000  sample              0.490            us/op
     bPlusTreeMapPutRemove:p0.95                            100000  sample              0.515            us/op
     bPlusTreeMapPutRemove:p0.99                            100000  sample              0.607            us/op
     bPlusTreeMapPutRemove:p0.999                           100000  sample              7.736            us/op
     bPlusTreeMapPutRemove:p0.9999                          100000  sample             13.160            us/op
     bPlusTreeMapPutRemove:p1.00                            100000  sample            103.808            us/op
     * </pre>
     */
}