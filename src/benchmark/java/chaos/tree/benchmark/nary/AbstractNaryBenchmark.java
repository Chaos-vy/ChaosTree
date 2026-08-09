package chaos.tree.benchmark.nary;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Base benchmark class for the N-ary tree family (B-Tree, B+Tree).
 * Sets up standard JMH annotations, identical to the Binary family harness,
 * but adds the 'degree' parameter to test cache line boundaries.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public abstract class AbstractNaryBenchmark {

    @Param({"1000", "50000", "1000000", "5000000"})
    public int size;

    @Param({"8", "32", "64", "128"})
    public int degree;

    protected static int[] getShuffledInts(int n) {
        return getShuffledInts(n, 42L);
    }

    protected static int[] getShuffledInts(int n, long seed) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = i;
        Random rng = new Random(seed);
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        return arr;
    }
}
