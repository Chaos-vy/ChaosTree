package chaos.tree.benchmark.binary;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public abstract class AbstractBinaryBenchmark {

    // Cache Boundary Testing:
    // 1000    : Fits comfortably inside L1 Cache (32KB)
    // 50000   : Spills into L2 Cache (256KB - 1MB)
    // 1000000 : Spills into L3 Cache (8MB - 32MB)
    // 5000000 : Complete L3 Cache Miss, forces Main RAM fetch
    @Param({"1000", "50000", "1000000", "5000000"})
    public int size;

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
