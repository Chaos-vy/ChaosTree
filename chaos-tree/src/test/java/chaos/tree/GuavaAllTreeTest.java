package chaos.tree;

import com.google.common.collect.testing.*;
import com.google.common.collect.testing.features.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;
import chaos.tree.binaryMap.*;
import chaos.tree.naryMap.*;
import chaos.tree.binary.*;
import chaos.tree.nary.*;
import java.util.*;

@RunWith(AllTests.class)
public class GuavaAllTreeTest {
    public static Test suite() {
        TestSuite suite = new TestSuite("ChaosTree Master Guava Testlib Gauntlet");

        suite.addTest(NavigableMapTestSuiteBuilder
                .using(new TestStringSortedMapGenerator() {
                    @Override
                    protected SortedMap<String, String> create(Map.Entry<String, String>[] entries) {
                        BPlusTreeMap<String, String> map = new BPlusTreeMap<>();
                        for (Map.Entry<String, String> entry : entries) { map.put(entry.getKey(), entry.getValue()); }
                        return map;
                    }
                })
                .named("BPlusTreeMap Guava test")
                .withFeatures(MapFeature.SUPPORTS_PUT, MapFeature.SUPPORTS_REMOVE, MapFeature.ALLOWS_NULL_VALUES, MapFeature.RESTRICTS_KEYS, CollectionFeature.SUPPORTS_ITERATOR_REMOVE, CollectionFeature.KNOWN_ORDER, CollectionFeature.SUBSET_VIEW, CollectionFeature.DESCENDING_VIEW, CollectionSize.ANY, MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
                .createTestSuite());

        suite.addTest(NavigableMapTestSuiteBuilder
                .using(new TestStringSortedMapGenerator() {
                    @Override
                    protected SortedMap<String, String> create(Map.Entry<String, String>[] entries) {
                        BTreeMap<String, String> map = new BTreeMap<>();
                        for (Map.Entry<String, String> entry : entries) { map.put(entry.getKey(), entry.getValue()); }
                        return map;
                    }
                })
                .named("BTreeMap Guava test")
                .withFeatures(MapFeature.SUPPORTS_PUT, MapFeature.SUPPORTS_REMOVE, MapFeature.ALLOWS_NULL_VALUES, MapFeature.RESTRICTS_KEYS, CollectionFeature.SUPPORTS_ITERATOR_REMOVE, CollectionFeature.KNOWN_ORDER, CollectionFeature.SUBSET_VIEW, CollectionFeature.DESCENDING_VIEW, CollectionSize.ANY, MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
                .createTestSuite());

        suite.addTest(NavigableMapTestSuiteBuilder
                .using(new TestStringSortedMapGenerator() {
                    @Override
                    protected SortedMap<String, String> create(Map.Entry<String, String>[] entries) {
                        RedBlackTreeMap<String, String> map = new RedBlackTreeMap<>();
                        for (Map.Entry<String, String> entry : entries) { map.put(entry.getKey(), entry.getValue()); }
                        return map;
                    }
                })
                .named("RedBlackTreeMap Guava test")
                .withFeatures(MapFeature.SUPPORTS_PUT, MapFeature.SUPPORTS_REMOVE, MapFeature.ALLOWS_NULL_VALUES, MapFeature.RESTRICTS_KEYS, CollectionFeature.SUPPORTS_ITERATOR_REMOVE, CollectionFeature.KNOWN_ORDER, CollectionFeature.SUBSET_VIEW, CollectionFeature.DESCENDING_VIEW, CollectionSize.ANY, MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
                .createTestSuite());

        suite.addTest(NavigableMapTestSuiteBuilder
                .using(new TestStringSortedMapGenerator() {
                    @Override
                    protected SortedMap<String, String> create(Map.Entry<String, String>[] entries) {
                        AvlTreeMap<String, String> map = new AvlTreeMap<>();
                        for (Map.Entry<String, String> entry : entries) { map.put(entry.getKey(), entry.getValue()); }
                        return map;
                    }
                })
                .named("AvlTreeMap Guava test")
                .withFeatures(MapFeature.SUPPORTS_PUT, MapFeature.SUPPORTS_REMOVE, MapFeature.ALLOWS_NULL_VALUES, MapFeature.RESTRICTS_KEYS, CollectionFeature.SUPPORTS_ITERATOR_REMOVE, CollectionFeature.KNOWN_ORDER, CollectionFeature.SUBSET_VIEW, CollectionFeature.DESCENDING_VIEW, CollectionSize.ANY, MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
                .createTestSuite());

        suite.addTest(NavigableSetTestSuiteBuilder
                .using(new TestStringSortedSetGenerator() {
                    @Override
                    protected SortedSet<String> create(String[] elements) {
                        BPlusTreeSet<String> set = new BPlusTreeSet<>();
                        set.addAll(Arrays.asList(elements));
                        return set;
                    }
                })
                .named("BPlusTreeSet Guava test")
                .withFeatures(CollectionSize.ANY, CollectionFeature.KNOWN_ORDER, CollectionFeature.ALLOWS_NULL_VALUES, CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION, CollectionFeature.SUPPORTS_ADD, CollectionFeature.SUPPORTS_REMOVE, CollectionFeature.SUPPORTS_ITERATOR_REMOVE)
                .createTestSuite());

        suite.addTest(NavigableSetTestSuiteBuilder
                .using(new TestStringSortedSetGenerator() {
                    @Override
                    protected SortedSet<String> create(String[] elements) {
                        BTreeSet<String> set = new BTreeSet<>();
                        set.addAll(Arrays.asList(elements));
                        return set;
                    }
                })
                .named("BTreeSet Guava test")
                .withFeatures(CollectionSize.ANY, CollectionFeature.KNOWN_ORDER, CollectionFeature.ALLOWS_NULL_VALUES, CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION, CollectionFeature.SUPPORTS_ADD, CollectionFeature.SUPPORTS_REMOVE, CollectionFeature.SUPPORTS_ITERATOR_REMOVE)
                .createTestSuite());

        suite.addTest(NavigableSetTestSuiteBuilder
                .using(new TestStringSortedSetGenerator() {
                    @Override
                    protected SortedSet<String> create(String[] elements) {
                        RedBlackTreeSet<String> set = new RedBlackTreeSet<>();
                        set.addAll(Arrays.asList(elements));
                        return set;
                    }
                })
                .named("RedBlackTreeSet Guava test")
                .withFeatures(CollectionSize.ANY, CollectionFeature.KNOWN_ORDER, CollectionFeature.ALLOWS_NULL_VALUES, CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION, CollectionFeature.SUPPORTS_ADD, CollectionFeature.SUPPORTS_REMOVE, CollectionFeature.SUPPORTS_ITERATOR_REMOVE)
                .createTestSuite());

        suite.addTest(NavigableSetTestSuiteBuilder
                .using(new TestStringSortedSetGenerator() {
                    @Override
                    protected SortedSet<String> create(String[] elements) {
                        AvlTreeSet<String> set = new AvlTreeSet<>();
                        set.addAll(Arrays.asList(elements));
                        return set;
                    }
                })
                .named("AvlTreeSet Guava test")
                .withFeatures(CollectionSize.ANY, CollectionFeature.KNOWN_ORDER, CollectionFeature.ALLOWS_NULL_VALUES, CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION, CollectionFeature.SUPPORTS_ADD, CollectionFeature.SUPPORTS_REMOVE, CollectionFeature.SUPPORTS_ITERATOR_REMOVE)
                .createTestSuite());

        return suite;
    }
}
