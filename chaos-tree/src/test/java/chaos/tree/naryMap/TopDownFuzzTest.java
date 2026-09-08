package chaos.tree.naryMap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

public class TopDownFuzzTest {

    @Test
    public void testTopDownBoundaryCases() {
        int failures = 0;
        int trials = 0;

        int[] boundaryN = {0, 1, 2, 31, 32, 33, 62, 63, 64, 65, 4095, 4096, 4097, 50000, 49999};
        int[] degrees = {32, 33, 40, 63, 64, 100, 128, 256};
        float[] factors = {0.5f, 0.6f, 0.75f, 0.9f, 1.0f};

        for (int n : boundaryN) {
            for (int degree : degrees) {
                for (float f : factors) {
                    trials++;
                    if (!runOne(n, degree, f)) failures++;
                }
            }
        }
        assertEquals(0, failures, "There were " + failures + " failures in boundary cases out of " + trials + " trials.");
    }

    @Test
    public void testTopDownRandomFuzz() {
        Random rnd = new Random(42);
        int failures = 0;
        int trials = 0;

        for (int i = 0; i < 20000; i++) {
            int n = rnd.nextInt(50000);
            int degree = 32 + rnd.nextInt(225);
            float f = 0.5f + rnd.nextFloat() * 0.5f;
            trials++;
            if (!runOne(n, degree, f)) failures++;
        }
        assertEquals(0, failures, "There were " + failures + " failures in random fuzz out of " + trials + " trials.");
    }

    private boolean runOne(int n, int degree, float factor) {
        try {
            Object[] keys = new Object[n];
            Object[] values = new Object[n];
            for (int i = 0; i < n; i++) {
                keys[i] = i;
                values[i] = "v" + i;
            }
            
            BTreeMap<Integer, String> tree = BTreeMap.Builder.<Integer, String>create(degree)
                    .factor(factor)
                    .importFlatMatrix(new Object[][]{keys, values})
                    .build();


            assertEquals(n, tree.size(), "Size mismatch");
            if (n > 0) {
                assertEquals(0, tree.firstKey(), "First key mismatch");
                assertEquals(n - 1, tree.lastKey(), "Last key mismatch");

                assertEquals("v0", tree.get(0));
                assertEquals("v" + (n - 1), tree.get(n - 1));
            }
            return true;
        } catch (Throwable t) {
            System.err.println("FAIL n=" + n + " degree=" + degree + " factor=" + factor
                    + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return false;
        }
    }
}
