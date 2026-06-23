package chaos.tree.nary.concurrent;

import chaos.tree.nary.NaryTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates structural integrity and external monitor synchronization
 * under heavy multithreaded contention for N-ary trees.
 */
public abstract class ConcurrentNaryTreeTest<NARY extends NaryTree<Integer>> {

    protected abstract NARY createTree(int degree);

    protected abstract void validateInvariants(NARY tree);

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 10, 32})
    @DisplayName("External monitor ensures thread safety under heavy contention")
    void externalMonitorEnsuresThreadSafetyUnderHeavyContention(int degree) throws InterruptedException, ExecutionException {
        NARY tree = createTree(degree);
        Object monitor = new Object();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<?>> tasks = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            final int threadOffset = i * 20000;
            tasks.add(executor.submit(() -> {
                for (int j = 0; j < 20000; j++) {
                    synchronized (monitor) {
                        tree.insert(threadOffset + j);
                    }
                }
            }));
        }

        for (Future<?> task : tasks) task.get();
        assertEquals(80000, tree.size());
        validateInvariants(tree);

        tasks.clear();

        for (int i = 0; i < 2; i++) {
            final int threadOffset = i * 20000;
            tasks.add(executor.submit(() -> {
                for (int j = 0; j < 20000; j++) {
                    synchronized (monitor) {
                        tree.delete(threadOffset + j);
                    }
                }
            }));
        }
        for (int i = 2; i < 4; i++) {
            final int threadOffset = i * 20000;
            tasks.add(executor.submit(() -> {
                for (int j = 0; j < 20000; j++) {
                    synchronized (monitor) {
                        assertTrue(tree.contains(threadOffset + j));
                    }
                }
            }));
        }

        for (Future<?> task : tasks) task.get();
        executor.shutdown();

        assertEquals(40000, tree.size());
        validateInvariants(tree);

        List<Integer> leftOver = tree.toList();
        for (int i = 0; i < leftOver.size() - 1; i++) {
            assertTrue(leftOver.get(i) < leftOver.get(i + 1), "Tree elements must remain strictly sorted");
            assertTrue(leftOver.get(i) >= 40000, "Deleted elements should not be present in the tree");
        }
    }
}