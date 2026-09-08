package chaos.tree.jcstress;

import chaos.tree.nary.BTreeSet;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import java.util.ConcurrentModificationException;
import java.util.Iterator;


@JCStressTest
@Outcome(id = "CME", expect = Expect.ACCEPTABLE, desc = "Properly failed fast with CME.")
@Outcome(id = "OK", expect = Expect.ACCEPTABLE, desc = "Iterated fine without detecting modification.")
@Outcome(id = "NPE", expect = Expect.FORBIDDEN, desc = "Null pointer exception thrown (internal structure corrupted without CME).")
@Outcome(id = "AIOOBE", expect = Expect.FORBIDDEN, desc = "Array index out of bounds (internal structure corrupted without CME).")
@State
public class BTreeSetFailFastTest {
    
    private final BTreeSet<Integer> set;
    
    public BTreeSetFailFastTest() {
        set = new BTreeSet<>();
        set.add(1);
        set.add(2);
        set.add(3);
    }
    
    @Actor
    public void modifier() {
        set.add(4); // Concurrent modification
    }
    
    @Actor
    public void iterator(L_Result r) {
        try {
            int sum = 0;
            for (Integer i : set) {
                sum += i;
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
