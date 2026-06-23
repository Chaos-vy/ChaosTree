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

    // Cache Boundary Testing:
    // 1000    : Fits comfortably inside L1 Cache (32KB)
    // 50000   : Spills into L2 Cache (256KB - 1MB)
    // 1000000 : Spills into L3 Cache (8MB - 32MB)
    // 5000000 : Complete L3 Cache Miss, forces Main RAM fetch
    @Param({"1000", "50000", "1000000", "5000000"})
    public int size;

    // 4 = Deep trees (like binary)
    // 32 = The "Sweet Spot" for 64-byte L1 Cache lines
    // 128 = Shallow trees, heavy array shifting
    @Param({"4", "32", "128"})
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
