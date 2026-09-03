package chaos.tree.binary;

import com.google.common.collect.testing.NavigableSetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSortedSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import junit.framework.Test;

import java.util.Arrays;
import java.util.SortedSet;

public class RedBlackTreeSetTest {

    // IntelliJ will recognize this as a run// IntelliJ will recognize this as a run// IntelliJ will recognize this as a runnable JUnit Test Suite!
    // IntelliJ will recognize this as a runnable JUnit Test Suite!
    public static Test suite() {
        return NavigableSetTestSuiteBuilder
                .using(new TestStringSortedSetGenerator() {
                    @Override
                    protected SortedSet<String> create(String[] elements) {
                        // This is how Guava creates fresh instances of your tree for the tests
                        RedBlackTreeSet<String> set = new RedBlackTreeSet<>();
                        set.addAll(Arrays.asList(elements));
                        return set;
                    }
                })
                .named("ChaosTree Java 21 RedBlackTreeSet Gauntlet")
                .withFeatures(
                        CollectionFeature.SUPPORTS_ADD,
                        CollectionFeature.SUPPORTS_REMOVE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionFeature.RESTRICTS_ELEMENTS, // Correct (throws NPE on nulls)
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION, // Added!
                        CollectionFeature.SERIALIZABLE, // Added!
                        CollectionSize.ANY// Tests trees with 0, 1, and many elements
                )
                .createTestSuite();
    }
}