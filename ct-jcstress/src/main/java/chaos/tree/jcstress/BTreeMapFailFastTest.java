package chaos.tree.jcstress;

import chaos.tree.naryMap.BTreeMap;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

@JCStressTest
@Outcome(id = "CME", expect = Expect.ACCEPTABLE, desc = "Properly failed fast with CME.")
@Outcome(id = "OK", expect = Expect.ACCEPTABLE, desc = "Iterated fine without detecting modification.")
@Outcome(id = "NPE", expect = Expect.FORBIDDEN, desc = "Null pointer exception thrown (internal structure corrupted without CME).")
@Outcome(id = "AIOOBE", expect = Expect.FORBIDDEN, desc = "Array index out of bounds (internal structure corrupted without CME).")
@State
public class BTreeMapFailFastTest {
    
    private final BTreeMap<Integer, Integer> map;
    
    public BTreeMapFailFastTest() {
        map = new BTreeMap<>();
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 3);
    }
    
    @Actor
    public void modifier() {
        map.put(4, 4); // Concurrent modification
    }
    
    @Actor
    public void iterator(L_Result r) {
        try {
            int sum = 0;
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                sum += entry.getValue();
            }
            r.r1 = "OK";
        } catch (ConcurrentModificationException e) {
            r.r1 = "CME";
        } catch (NullPointerException e) {
            r.r1 = "NPE";
        } catch (IndexOutOfBoundsException e) {
            r.r1 = "AIOOBE";
        } catch (Exception e) {
            r.r1 = e.getClass().getSimpleName();
        }
    }
}
