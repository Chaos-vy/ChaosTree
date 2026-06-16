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

    @Param({"1000", "10000", "100000"})
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
