package chaos.tree;

import chaos.tree.binary.*;
import chaos.tree.nary.*;
import chaos.tree.core.searchtree.ISearchTree;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuzzTest {

    static class DataPacket implements Comparable<DataPacket> {
        final int id;
        final String payload;

        DataPacket(int id, String payload) {
            this.id = id;
            this.payload = payload;
        }

        @Override
        public int compareTo(@NotNull DataPacket o) {
            return Integer.compare(this.id, o.id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataPacket that = (DataPacket) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    private void runFuzzTest(ISearchTree<DataPacket> tree) {
        Random r = new Random(42);
        TreeSet<DataPacket> truth = new TreeSet<>();

        for (int i = 0; i < 50_000; i++) {
            // Constrained ID range forces compareTo() == 0 duplicates!
            int id = r.nextInt(500);
            DataPacket packet = new DataPacket(id, "Payload-" + id);

            if (r.nextBoolean()) {
                try {
                    tree.insert(packet);
                    truth.add(packet);
                } catch (IllegalArgumentException ignored) {
                    // Duplicate values should throw IllegalArgumentException
                }
            } else {
                tree.delete(packet);
                truth.remove(packet);
            }
        }
        
        assertEquals(truth.size(), tree.size());
        
        List<DataPacket> expected = new ArrayList<>(truth);
        List<DataPacket> actual = new ArrayList<>();
        tree.iterator().forEachRemaining(actual::add);
        
        assertEquals(expected, actual);
    }

    private void runSequentialStressTest(ISearchTree<Integer> tree, int max) {
        for (int i = 0; i < max; i++) {
            tree.insert(i);
        }
        assertEquals(max, tree.size());
        
        for (int i = 0; i < max; i++) {
            tree.delete(i);
        }
        assertTrue(tree.isEmpty());
    }

    @Test
    @DisplayName("Fuzz: Custom Object Routing - RBT")
    void fuzzRBT() { runFuzzTest(new RBT<>()); }

    @Test
    @DisplayName("Fuzz: Custom Object Routing - AVL")
    void fuzzAVL() { runFuzzTest(new AVL<>()); }

    @Test
    @DisplayName("Fuzz: Custom Object Routing - Treap")
    void fuzzTreap() { runFuzzTest(new Treap<>()); }

    @Test
    @DisplayName("Fuzz: Custom Object Routing - BTree (t=32)")
    void fuzzBTree() { runFuzzTest(new BTree<>(32)); }

    @Test
    @DisplayName("Fuzz: Custom Object Routing - BPlusTree (t=32)")
    void fuzzBPlusTree() { runFuzzTest(new BPlusTree<>(32)); }

    // SEQUENTIAL STRESS TESTS

    @Test
    @DisplayName("Deep Sequential: RBT (100k)")
    void sequentialRBT() { runSequentialStressTest(new RBT<>(), 100_000); }

    @Test
    @DisplayName("Deep Sequential: AVL (100k)")
    void sequentialAVL() { runSequentialStressTest(new AVL<>(), 100_000); }

    @Test
    @DisplayName("Deep Sequential: Treap (100k)")
    void sequentialTreap() { runSequentialStressTest(new Treap<>(), 100_000); }

    // N-ary trees handle sequence beautifully because of array mechanics and node saturation
    @Test
    @DisplayName("Deep Sequential: BTree (1 Million)")
    void sequentialBTree() { runSequentialStressTest(new BTree<>(32), 1_000_000); }

    @Test
    @DisplayName("Deep Sequential: BPlusTree (1 Million)")
    void sequentialBPlusTree() { runSequentialStressTest(new BPlusTree<>(32), 1_000_000); }
}
