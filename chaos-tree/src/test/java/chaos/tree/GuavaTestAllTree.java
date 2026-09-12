package chaos.tree;

import com.google.common.collect.testing.NavigableMapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSortedMapGenerator;
import com.google.common.collect.testing.NavigableSetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSortedSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import chaos.tree.binaryMap.AvlTreeMap;
import chaos.tree.binaryMap.RedBlackTreeMap;
import chaos.tree.naryMap.BTreeMap;
import chaos.tree.naryMap.BPlusTreeMap;
import chaos.tree.binary.AvlTreeSet;
import chaos.tree.binary.RedBlackTreeSet;
import chaos.tree.nary.BTreeSet;
import chaos.tree.nary.BPlusTreeSet;

@RunWith(AllTests.class)
public class GuavaTestAllTree extends TestCase {

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
                .withFeatures(MapFeature.SUPPORTS_PUT, 
                        MapFeature.SUPPORTS_REMOVE, 
                        MapFeature.ALLOWS_NULL_VALUES, 
                        MapFeature.RESTRICTS_KEYS, 
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE, 
                        CollectionFeature.KNOWN_ORDER, 
                        CollectionFeature.SUBSET_VIEW, 
                        CollectionFeature.DESCENDING_VIEW, 
                        CollectionSize.ANY, 
                        CollectionFeature.SERIALIZABLE, 
                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
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
                .withFeatures(MapFeature.SUPPORTS_PUT,
                        MapFeature.SUPPORTS_REMOVE,
                        MapFeature.ALLOWS_NULL_VALUES,
                        MapFeature.RESTRICTS_KEYS,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionSize.ANY,
                        CollectionFeature.SERIALIZABLE,
                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
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
                .withFeatures(MapFeature.SUPPORTS_PUT,
                        MapFeature.SUPPORTS_REMOVE,
                        MapFeature.ALLOWS_NULL_VALUES,
                        MapFeature.RESTRICTS_KEYS,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionSize.ANY,
                        CollectionFeature.SERIALIZABLE,
                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
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
                .withFeatures(MapFeature.SUPPORTS_PUT,
                        MapFeature.SUPPORTS_REMOVE,
                        MapFeature.ALLOWS_NULL_VALUES,
                        MapFeature.RESTRICTS_KEYS,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionSize.ANY,
                        CollectionFeature.SERIALIZABLE,
                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
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
                .withFeatures(CollectionFeature.SUPPORTS_ADD,
                        CollectionFeature.SUPPORTS_REMOVE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionFeature.RESTRICTS_ELEMENTS,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SERIALIZABLE,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionSize.ANY)
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
                .withFeatures(CollectionFeature.SUPPORTS_ADD,
                        CollectionFeature.SUPPORTS_REMOVE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionFeature.RESTRICTS_ELEMENTS,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SERIALIZABLE,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionSize.ANY)
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
                .withFeatures(CollectionFeature.SUPPORTS_ADD,
                        CollectionFeature.SUPPORTS_REMOVE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionFeature.SUBSET_VIEW,
                        CollectionFeature.DESCENDING_VIEW,
                        CollectionFeature.RESTRICTS_ELEMENTS,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SERIALIZABLE,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionSize.ANY)
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
                .withFeatures(CollectionFeature.SUPPORTS_ADD, 
                        CollectionFeature.SUPPORTS_REMOVE, 
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE, 
                        CollectionFeature.KNOWN_ORDER, 
                        CollectionFeature.SUBSET_VIEW, 
                        CollectionFeature.DESCENDING_VIEW, 
                        CollectionFeature.RESTRICTS_ELEMENTS, 
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION, 
                        CollectionFeature.SERIALIZABLE, 
                        CollectionFeature.ALLOWS_NULL_QUERIES, 
                        CollectionSize.ANY)
                .createTestSuite());

        return suite;
    }
}
