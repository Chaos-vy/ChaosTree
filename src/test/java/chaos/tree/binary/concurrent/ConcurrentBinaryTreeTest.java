package chaos.tree.binary.concurrent;

import chaos.tree.binary.BinaryTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
 * under heavy multi-threaded contention.
 */
public abstract class ConcurrentBinaryTreeTest<TREE extends BinaryTree<Integer>> {

    protected TREE tree;

    protected abstract TREE createTree();

    protected abstract void validateInvariants();

    @BeforeEach
    void setUp() {
        tree = createTree();
    }

    @Test
    void externalMonitorEnsuresThreadSafetyUnderHeavyContention() throws InterruptedException, ExecutionException {
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
        validateInvariants();

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
        validateInvariants();

        List<Integer> leftOver = tree.inorder();
        for (int i = 0; i < leftOver.size() - 1; i++) {
            assertTrue(leftOver.get(i) < leftOver.get(i + 1));
            assertTrue(leftOver.get(i) >= 40000);
        }
    }
}