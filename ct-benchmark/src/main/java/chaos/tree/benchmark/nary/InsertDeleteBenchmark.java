package chaos.tree.benchmark.nary;

import chaos.tree.nary.BTree;
import chaos.tree.nary.BPlusTree;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

/**
 * Measures combined insert + delete latency for N-ary tree implementations.
 *
 * <p>Uses the even/odd key distribution strategy to ensure mutations are
 * properly distributed across the entire tree span rather than hammered
 * into a single cache-hot edge.</p>
 */
public class InsertDeleteBenchmark extends AbstractNaryBenchmark {

    private BTree<Integer> btree;
    private BPlusTree<Integer> bplus;

    private int[] mutationPool;
    private int poolMask;

    private int btreeCursor;
    private int bplusCursor;

    @Setup(Level.Trial)
    public void buildStaticTrees() {
        btree = new BTree<>(degree);
        bplus = new BPlusTree<>(degree);

        int[] evenData = new int[size];
        for (int i = 0; i < size; i++) {
            evenData[i] = 2 * i;
        }
        shuffleInPlace(evenData, 999L);

        for (int v : evenData) {
            btree.insert(v);
            bplus.insert(v);
        }

        int poolSize = Integer.highestOneBit(Math.max(size, 2));
        if (poolSize < size) poolSize <<= 1;
        poolMask = poolSize - 1;

        mutationPool = new int[poolSize];
        for (int i = 0; i < poolSize; i++) {
            mutationPool[i] = 2 * i + 1;
        }
        shuffleInPlace(mutationPool, 12345L);

        btreeCursor = 0;
        bplusCursor = 0;
    }

    private static void shuffleInPlace(int[] arr, long seed) {
        Random rnd = new Random(seed);
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    @Benchmark
    public void bTreeInsertDelete(Blackhole bh) {
        int key = mutationPool[btreeCursor++ & poolMask];
        btree.insert(key);
        btree.delete(key);
        bh.consume(key);
    }

    @Benchmark
    public void bPlusTreeInsertDelete(Blackhole bh) {
        int key = mutationPool[bplusCursor++ & poolMask];
        bplus.insert(key);
        bplus.delete(key);
        bh.consume(key);
    }
}
