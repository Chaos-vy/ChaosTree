package chaos.tree.naryMap;

import com.google.common.collect.testing.NavigableMapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSortedMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.Test;
import junit.framework.TestCase;

import java.util.Map;
import java.util.SortedMap;

public class BTreeMapTest extends TestCase {
    public static Test suite() {
        return NavigableMapTestSuiteBuilder
                .using(new TestStringSortedMapGenerator() {
                    @Override
                    protected SortedMap<String, String> create(Map.Entry<String, String>[] entries) {
                        BTreeMap<String, String> map = new BTreeMap<>();
                        for (Map.Entry<String, String> entry : entries) {
                            map.put(entry.getKey(), entry.getValue());
                        }
                        return map;
                    }
                })
                .named("ChaosTree Java 21 B+TreeMap Navigable Gauntlet")
                .withFeatures(
                        MapFeature.SUPPORTS_PUT,
                        MapFeature.SUPPORTS_REMOVE,
                        MapFeature.ALLOWS_NULL_VALUES,
                        MapFeature.RESTRICTS_KEYS,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.KNOWN_ORDER,
                        CollectionSize.ANY,
                        CollectionFeature.SERIALIZABLE,
                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION
                )
                .createTestSuite();
    }
}