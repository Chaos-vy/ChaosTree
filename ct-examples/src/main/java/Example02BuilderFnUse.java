import chaos.tree.nary.BTreeSet;
import chaos.tree.naryMap.BTreeMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Set;

/**
 * The builder fn usecase, please do Read the static class for more such combination.
 */
public class Example02BuilderFnUse {
    public static void main(String[] args) {
        
        List<Integer> initialData = Arrays.asList(10, 20, 30, 40, 50, 60, 70, 80);
        
        Set<Integer> set = BTreeSet.Builder.<Integer>create(32)
                .factor(0.85f)
                .comparator(Comparator.reverseOrder())
                .importCollection(initialData)
                .build();
        
        System.out.println("Built BTreeSet from collection:");
        System.out.println(set);
        
        Map<Integer, String> initialMap = new HashMap<>();
        initialMap.put(1, "One");
        initialMap.put(2, "Two");
        initialMap.put(3, "Three");
        initialMap.put(4, "Four");
        initialMap.put(5, "Five");
        
        BTreeMap<Integer, String> map = BTreeMap.Builder.<Integer, String>create(64)
                .factor(0.75f)
                .importMap(initialMap)
                .build();
        
        System.out.println("Built BTreeMap from map:");
        System.out.println(map.display());
    }
}
