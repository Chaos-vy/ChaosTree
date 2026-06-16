package chaos.tree.benchmark.binary;

import chaos.tree.binary.avl.AVL;
import chaos.tree.binary.rbt.RBT;
import chaos.tree.binary.treap.Treap;
import chaos.tree.exception.DuplicateNodeException;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Benchmark)
@Threads(8)
public class ConcurrentBenchmark extends AbstractBinaryBenchmark {

    private final Object rbtLock = new Object();
    private final Object avlLock = new Object();
    private final Object treapLock = new Object();
    private RBT<Integer> rbt;
    private AVL<Integer> avl;
    private Treap<Integer> treap;

    @Setup(Level.Trial)
    public void setup() {
        rbt = new RBT<>();
        avl = new AVL<>();
        treap = new Treap<>();
        for (int i = 0; i < size; i++) {
            rbt.insert(i);
            avl.insert(i);
            treap.insert(i);
        }
    }

    @Benchmark
    public void rbtConcurrentInsert(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size);
        synchronized (rbtLock) {
            try {
                rbt.insert(val);
            } catch (DuplicateNodeException ignored) {
            }
        }
    }

    @Benchmark
    public void rbtConcurrentSearch(Blackhole bh) {
        synchronized (rbtLock) {
            bh.consume(rbt.contains((int) (Math.random() * size)));
        }
    }

    @Benchmark
    public void rbtConcurrentMixed(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size);
        synchronized (rbtLock) {
            if (val % 2 == 0) {
                try {
                    rbt.insert(val + size + 1);
                } catch (DuplicateNodeException ignored) {
                }
            } else {
                bh.consume(rbt.contains(val));
            }
        }
    }

    // ─── AVL ───

    @Benchmark
    public void avlConcurrentInsert(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size);
        synchronized (avlLock) {
            try {
                avl.insert(val);
            } catch (DuplicateNodeException ignored) {
            }
        }
    }

    @Benchmark
    public void avlConcurrentSearch(Blackhole bh) {
        synchronized (avlLock) {
            bh.consume(avl.contains((int) (Math.random() * size)));
        }
    }

    @Benchmark
    public void avlConcurrentMixed(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size);
        synchronized (avlLock) {
            if (val % 2 == 0) {
                try {
                    avl.insert(val + size + 1);
                } catch (DuplicateNodeException ignored) {
                }
            } else {
                bh.consume(avl.contains(val));
            }
        }
    }

    // ─── Treap ───

    @Benchmark
    public void treapConcurrentInsert(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size * 2);
        synchronized (treapLock) {
            try {
                treap.insert(val);
            } catch (DuplicateNodeException ignored) {
            }
        }
    }

    @Benchmark
    public void treapConcurrentSearch(Blackhole bh) {
        synchronized (treapLock) {
            bh.consume(treap.contains(ThreadLocalRandom.current().nextInt(size)));
        }
    }

    @Benchmark
    public void treapConcurrentMixed(Blackhole bh) {
        int val = ThreadLocalRandom.current().nextInt(size);
        synchronized (treapLock) {
            if (val % 2 == 0) {
                try {
                    treap.insert(val + size + 1);
                } catch (DuplicateNodeException ignored) {
                }
            } else {
                bh.consume(treap.contains(val));
            }
        }
    }
}