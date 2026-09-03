package chaos.tree.binary;

import com.google.common.collect.testing.NavigableSetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSortedSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import junit.framework.Test;

import java.util.Arrays;
import java.util.SortedSet;

public class AvlTreeSetTest {

    public static Test suite() {
        return NavigableSetTestSuiteBuilder
                .using(new TestStringSortedSetGenerator() {
                    @Override
                    protected SortedSet<String> create(String[] elements) {
                        AvlTreeSet<String> set = new AvlTreeSet<>();
                        set.addAll(Arrays.asList(elements));
                        return set;
                    }
                })
                .named("ChaosTree Java 21 AvlTreeSet Gauntlet")
                .withFeatures(
                        CollectionFeature.SUPPORTS_ADD,
                        CollectionFeature.SUPPORTS_REMOVE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionFeature.RESTRICTS_ELEMENTS,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SERIALIZABLE,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionSize.ANY
                )
                .createTestSuite();
    }
}