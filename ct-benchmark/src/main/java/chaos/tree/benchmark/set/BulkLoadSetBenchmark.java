package chaos.tree.benchmark.set;

import chaos.tree.nary.BTreeSet;
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
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;


/**
 * This test is lethal to TreeSet so Tree set Lover don't see the result
 * <pre>
 *
 * Benchmark                                                  (size)  Mode  Cnt   Score   Error  Units
 * BulkLoadSetBenchmark.chaosTree_DragonFeedArrayLoad        1000000  avgt   15   0.417 ± 0.010  ms/op
 * BulkLoadSetBenchmark.chaosTree_DragonFeedArrayLoad        5000000  avgt   15   2.724 ± 0.031  ms/op
 * BulkLoadSetBenchmark.chaosTree_IteratorLoad               1000000  avgt   15   2.543 ± 0.110  ms/op
 * BulkLoadSetBenchmark.chaosTree_IteratorLoad               5000000  avgt   15  12.111 ± 0.129  ms/op
 * BulkLoadSetBenchmark.chaosTree_LoadedFromTreeSetIterator  1000000  avgt   15   6.147 ± 0.014  ms/op
 * BulkLoadSetBenchmark.chaosTree_LoadedFromTreeSetIterator  5000000  avgt   15  30.333 ± 0.179  ms/op
 * BulkLoadSetBenchmark.treeSet_JdkStandard                  1000000  avgt   15   8.036 ± 0.065  ms/op
 * BulkLoadSetBenchmark.treeSet_JdkStandard                  5000000  avgt   15  46.369 ± 6.892  ms/op
 * </pre>
 * <p>
 * Speed of transmission was slow to cope up with build.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
public class BulkLoadSetBenchmark {

    @Param({"1000000", "5000000"})
    public int size;

    private Integer[] data;
    private SortedSet<Integer> sourceTreeSet;

    @Setup(Level.Trial)
    public void setup() {
        data = new Integer[size];
        for (int i = 0; i < size; i++) {
            data[i] = i;
        }
        sourceTreeSet = new TreeSet<>(Arrays.asList(data));
    }

    @Benchmark
    public SortedSet<Integer> treeSet_JdkStandard() {
        return new TreeSet<>(sourceTreeSet);
    }

    @Benchmark
    public SortedSet<Integer> chaosTree_LoadedFromTreeSetIterator() {
        return BTreeSet.Builder.<Integer>create(64).factor(0.9f)
                .importSorted(sourceTreeSet.iterator())
                .build();
    }

    @Benchmark
    public SortedSet<Integer> chaosTree_IteratorLoad() {
        CustomArrayIterator iterator = new CustomArrayIterator(data);
        return BTreeSet.Builder.<Integer>create(64).factor(0.9f)
                .importSorted(iterator)
                .build();
    }

    @Benchmark
    public SortedSet<Integer> chaosTree_DragonFeedArrayLoad() {
        return BTreeSet.Builder.<Integer>create(64).factor(0.9f)
                .importFlatArray(data)
                .build();
    }

    /**
     * The custom array-based Iterator the user requested.
     */
    private static class CustomArrayIterator implements Iterator<Integer> {
        private final Integer[] array;
        private int index = 0;

        public CustomArrayIterator(Integer[] array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return index < array.length;
        }

        @Override
        public Integer next() {
            return array[index++];
        }
    }


}
