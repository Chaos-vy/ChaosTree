package chaos.tree.benchmark.nary;

import chaos.tree.exception.DuplicateNodeException;
import chaos.tree.nary.BTree;
import chaos.tree.nary.BPlusTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Tests heavy lock contention and thread safety overhead on the N-ary family.
 * Runs 8 concurrent threads hammering synchronized blocks around the trees.
 */
@State(Scope.Benchmark)
@Threads(8)
public class ConcurrentBenchmark extends AbstractNaryBenchmark {

    private final Object btreeLock = new Object();
    private final Object bplusLock = new Object();

    private BTree<Integer> btree;
    private BPlusTree<Integer> bplus;

    @Setup(Level.Trial)
    public void setup() {
        btree = new BTree<>(degree);
        bplus = new BPlusTree<>(degree);
        
        for (int i = 0; i < size; i++) {
            btree.insert(i);
            bplus.insert(i);
        }
    }

    // ─── B-Tree ───

    @Benchmark
    public void bTreeConcurrentInsert(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size);
        synchronized (btreeLock) {
            try {
                btree.insert(val);
            } catch (DuplicateNodeException ignored) {
            }
        }
    }

    @Benchmark
    public void bTreeConcurrentSearch(Blackhole bh) {
        synchronized (btreeLock) {
            bh.consume(btree.contains(ThreadLocalRandom.current().nextInt(size)));
        }
    }

    @Benchmark
    public void bTreeConcurrentMixed(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size);
        synchronized (btreeLock) {
            if (val % 2 == 0) {
                try {
                    btree.insert(val + size + 1);
                } catch (DuplicateNodeException ignored) {
                }
            } else {
                bh.consume(btree.contains(val));
            }
        }
    }

    // ─── B+Tree ───

    @Benchmark
    public void bPlusTreeConcurrentInsert(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size);
        synchronized (bplusLock) {
            try {
                bplus.insert(val);
            } catch (DuplicateNodeException ignored) {
            }
        }
    }

    @Benchmark
    public void bPlusTreeConcurrentSearch(Blackhole bh) {
        synchronized (bplusLock) {
            bh.consume(bplus.contains(ThreadLocalRandom.current().nextInt(size)));
        }
    }

    @Benchmark
    public void bPlusTreeConcurrentMixed(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size);
        synchronized (bplusLock) {
            if (val % 2 == 0) {
                try {
                    bplus.insert(val + size + 1);
                } catch (DuplicateNodeException ignored) {
                }
            } else {
                bh.consume(bplus.contains(val));
            }
        }
    }
}
