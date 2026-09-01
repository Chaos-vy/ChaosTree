package chaos.tree21.naryMap;

import chaos.tree21.core.SearchTreeMap;

public sealed abstract class AbstractNaryTreeMap<K, V> implements SearchTreeMap<K, V> permits BTreeMap, BPlusTreeMap {
}
