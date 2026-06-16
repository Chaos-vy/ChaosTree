package chaos.tree.benchmark.binary;

import chaos.tree.binary.splay.Splay;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class SplayInsertDeleteBenchmark extends AbstractBinaryBenchmark {

    private final int POOL_SIZE = 2_000_000;
    private Splay<Integer> tree;
    private int[] activeElements;
    private int[] incomingElements;
    private int step = 0;

    @Setup(Level.Trial)
    public void setup() {
        tree = new Splay<>();
        activeElements = new int[size];
        incomingElements = new int[POOL_SIZE];

        Random rng = new Random(42);

        Set<Integer> unique = new HashSet<>();
        while (unique.size() < size + POOL_SIZE) {
            unique.add(rng.nextInt());
        }

        Iterator<Integer> it = unique.iterator();

        for (int i = 0; i < size; i++) {
            int val = it.next();
            tree.insert(val);
            activeElements[i] = val;
        }

        for (int i = 0; i < POOL_SIZE; i++) {
            incomingElements[i] = it.next();
        }
    }

    @Benchmark
    public void splayInsertDelete() {
        int activeIdx = step % size;
        int poolIdx = step % POOL_SIZE;
        int oldTarget = activeElements[activeIdx];
        tree.delete(oldTarget);

        int newTarget = incomingElements[poolIdx];
        tree.insert(newTarget);

        activeElements[activeIdx] = newTarget;

        step++;
    }
}