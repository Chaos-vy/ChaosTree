import chaos.tree.nary.BTreeSet;
import chaos.tree.naryMap.BTreeMap;
import java.util.Arrays;

public class Example03ArrayBasedSerialDeserial {
    public static void main(String[] args) {
        
        BTreeSet<Integer> originalSet = new BTreeSet<>();
        for (int i = 1; i <= 20; i++) {
            originalSet.add(i);
        }
        
        Object[] serializedArray = originalSet.toArray();
        System.out.println(originalSet.display());
        System.out.println("\nSerialized Set Array: " + Arrays.toString(serializedArray));
        
        BTreeSet<Integer> deserializedSet = BTreeSet.Builder.<Integer>create(32)
                .factor(0.75f) // this sets the node packing density to 32*3/2 = 48 min keys 75% occupancy
                .importFlatArray(serializedArray) //I call its name as DragonFeed minimum requirement is degree >=32
                .build(); //necessary
                
        System.out.println("\nDeserialized Set size: " + deserializedSet.size());
        System.out.println(deserializedSet.display());
        
        BTreeMap<Integer, String> originalMap = new BTreeMap<>();
        for (int i = 1; i <= 80; i++) {
            originalMap.put(i, "Value" + i);
        }
        
        Object[][] serializedMatrix = originalMap.exportFlatMatrix();
        System.out.println("\nSerialized Map Keys: " + Arrays.toString(serializedMatrix[0]));
        System.out.println("\nSerialized Map Vals: " + Arrays.toString(serializedMatrix[1]));
        
        BTreeMap<Integer, String> deserializedMap = BTreeMap.Builder.<Integer, String>create(64)
                .importFlatMatrix(serializedMatrix)
                .build();
                
        System.out.println("Deserialized Map size: " + deserializedMap.size());
        // Another way
        BTreeMap<Integer,String> btree= new BTreeMap<>(48,(e1,e2)->e2-e1);
        btree.putAll(originalMap);
        Object[][] flat = btree.exportFlatMatrix();
        System.out.println("Display use here");
        System.out.println(btree.display());
        // here comes the twist the comparator definition must be added prior!!
        BTreeMap<Integer,String> btree0 = new BTreeMap<>(32,(e1,e2)->e2-e1);
        btree0.importFlatMatrix(flat, 0.89f); //you can use any degree here 89% occupancy.
        System.out.println("Display use here");
        System.out.println(btree0.display());

    }
}
