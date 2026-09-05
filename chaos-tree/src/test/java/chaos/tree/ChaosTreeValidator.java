package chaos.tree;

import chaos.tree.naryMap.BPlusTreeMap;

import java.util.*;

public class ChaosTreeValidator {

    public static void main(String[] args) {
        int size = 1_000_000;
        System.out.println("Starting ChaosTree Validation for " + size + " elements");
        //sorted data is prepared firstly because it's chunk so it's testing on sorted array
        Integer[] keys = new Integer[size];
        String[] values = new String[size];
        TreeMap<Integer, String> truthMap = new TreeMap<>();

        for (int i = 0; i < size; i++) {
            keys[i] = i;
            values[i] = "Chaos-" + i;
            truthMap.put(keys[i], values[i]);
        }

        Object[][] flatMatrix = new Object[][]{keys, values};
        BPlusTreeMap<Integer, String> chaosTree =
                BPlusTreeMap.Builder.<Integer, String>degree(64)
                        .factor(1.0f) //full packing is used 1f 100% node are filled except last one.
                        .importFlatMatrix(flatMatrix)
                        .build();

        boolean passed = true;

        passed &= verifySize(truthMap, chaosTree);
        passed &= verifyExactGets(truthMap, chaosTree, keys);
        passed &= verifyIteration(truthMap, chaosTree);
        passed &= verifyRandomDeletionGauntlet(chaosTree,keys);

        if (passed) {
            System.out.println("SUCCESS: ChaosTree is structurally sound and mathematically identical to JDK TreeMap.");
        } else {
            System.err.println("Doomed by the way: ChaosTree has data corruption or invariant violations thanks by the way :(");
        }
    }

    private static boolean verifySize(Map<Integer, String> truth, Map<Integer, String> chaos) {
        if (truth.size() != chaos.size()) {
            System.err.println("Size mismatch! JDK: " + truth.size() + ", Chaos: " + chaos.size());
            return false;
        }
        System.out.println("Size matches (" + chaos.size() + ")");
        return true;
    }

    private static boolean verifyExactGets(Map<Integer, String> truth, Map<Integer, String> chaos, Integer[] keys) {
        // shuffle data to see it's holds true it's very nervous thing too..
        List<Integer> shuffledKeys = new ArrayList<>(Arrays.asList(keys));
        Collections.shuffle(shuffledKeys, new Random(42));

        for (Integer key : shuffledKeys) {
            String expected = truth.get(key);
            String actual = chaos.get(key);
            if (!Objects.equals(expected, actual)) {
                System.err.println("Lookup failed for key " + key + "! Expected: " + expected + ", Got: " + actual);
                return false;
            }
        }
        System.out.println("1,000,000 Random lookups match perfectly.");
        return true;
    }

    private static boolean verifyIteration(Map<Integer, String> truth, Map<Integer, String> chaos) {
        Iterator<Map.Entry<Integer, String>> truthIter = truth.entrySet().iterator();
        Iterator<Map.Entry<Integer, String>> chaosIter = chaos.entrySet().iterator();

        int count = 0;
        while (truthIter.hasNext() && chaosIter.hasNext()) {
            Map.Entry<Integer, String> t = truthIter.next();
            Map.Entry<Integer, String> c = chaosIter.next();

            if (!t.getKey().equals(c.getKey()) || !t.getValue().equals(c.getValue())) {
                System.err.println("Iteration mismatch at step " + count + "!");
                System.err.println("Expected: " + t.getKey() + "=" + t.getValue());
                System.err.println("Got: " + c.getKey() + "=" + c.getValue());
                return false;
            }
            count++;
        }

        if (truthIter.hasNext() || chaosIter.hasNext()) {
            System.err.println("Iteration lengths do not match!");
            return false;
        }

        System.out.println("Forward iteration sequence matches perfectly. But It did not matches speed with dragon speed");
        return true;
    }

    private static boolean verifyRandomDeletionGauntlet(Map<Integer, String> chaosTree, Integer[] keys) {
        System.out.println("Randomized Test (Deleting " + keys.length + " elements)...");

        // Shuffled the keys to ensure chaotic deletion patterns
        List<Integer> keysToRemove = new ArrayList<>(Arrays.asList(keys));
        Collections.shuffle(keysToRemove, new Random(99));

        int expectedSize = chaosTree.size();
        for (int i = 0; i < keysToRemove.size(); i++) {
            Integer key = keysToRemove.get(i);

            String removedValue = chaosTree.remove(key);
            expectedSize--;

            if (removedValue == null) {
                System.err.println("Deletion Failed! remove() returned null for key: " + key + " at step " + i);
                return false;
            }

            if (chaosTree.size() != expectedSize) {
                System.err.println("Size Tracking Failed! Expected: " + expectedSize + ", Actual: " + chaosTree.size());
                return false;
            }

            if (chaosTree.containsKey(key)) {
                System.err.println("Ghost Key! remove() executed, but containsKey() is still true for: " + key);
                return false;
            }
        }

        if (!chaosTree.isEmpty() || chaosTree.size() != 0) {
            System.err.println("Memory Leak! Tree should be empty but size is: " + chaosTree.size());
            return false;
        }

        System.out.println("SUCCESS: chaosTree from " + keys.length + " to 0 with zero invariant violations.");
        System.out.println("ChaosTree passes the test made by chaos-vy LOL!");
        return true;
    }
}
