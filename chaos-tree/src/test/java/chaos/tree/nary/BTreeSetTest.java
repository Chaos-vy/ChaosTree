package chaos.tree.nary;

import com.google.common.collect.testing.NavigableSetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSortedSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import junit.framework.Test;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.SortedSet;

public class BTreeSetTest extends TestCase {
    public static Test suite() {
        return NavigableSetTestSuiteBuilder
                .using(new TestStringSortedSetGenerator() {
                    @Override
                    protected SortedSet<String> create(String[] elements) {
                        BTreeSet<String> set = new BTreeSet<>();
                        set.addAll(Arrays.asList(elements)); // Uses your O(M) fast-path if sorted!
                        return set;
                    }
                })
                .named("ChaosTree B-Tree Gauntlet")
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
                        CollectionSize.ANY
                )
                .createTestSuite();
    }

}