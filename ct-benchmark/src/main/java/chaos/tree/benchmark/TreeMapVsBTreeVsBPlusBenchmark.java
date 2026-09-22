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

import java.util.ArrayList;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.Random;
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
@Fork(value = 3, jvmArgsAppend = {"-Xms6g", "-Xmx6g"})
public class TreeMapVsBTreeVsBPlusBenchmark {

    private static final int DEGREE = 64;
    @Param({"100000", "1000000"})
    public int n;
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
        Collections.shuffle(key, new Random(42));
        Collections.shuffle(value, new Random(43));

        keyDel = new ArrayList<>(key);
        Collections.shuffle(keyDel, new Random(44));

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
}