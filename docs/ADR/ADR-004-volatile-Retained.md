# ADR-004: volatile Retained in AbstractBiTree (Rejected in AbstractNaryTree)

The state variables across our tree architectures (`root`, `size`, `modCount`) must be handled carefully regarding thread visibility. We evaluated whether to keep or remove the `volatile` keyword from these fields.

We ultimately decided to **retain** the `volatile` keyword on `root`, `size`, and `modCount` in the **Binary** implementation (`AbstractBiTree`), but **explicitly rejected** it in the **N-ary** implementation (`AbstractNaryTree`).

### Rationale: Why Binary Kept `volatile`

In the Binary family, we kept them `volatile` because we want to guarantee that simple, read-only state observations are always perfectly visible across threads without locks. Specifically:

* Checking the `root` reference
* Calling `size()`
* Fail-fast `modCount` iterator checks

If we dropped `volatile` here, a thread could theoretically cache the tree's size in its local L1 cache and continue reading that stale value forever if no other synchronization barrier forced an update. Benchmarks showed the cost of a volatile write during a Binary tree insertion is negligible compared to the balancing logic, and the guarantee of accurate read-visibility for `tree.size()` was worth it.

*(Note: This does not make the binary trees thread-safe for concurrent writes. You still must externally synchronize mutations.)*

### Rationale: Why N-ary Rejected `volatile`

In the N-ary family (B-Tree, B+Tree), we explicitly stripped `volatile` from `root`, `size`, and `modCount`. 

N-ary trees fundamentally rely on internal arrays (`Object[] keys`, `Object[] children`). In Java, marking an array reference as `volatile` only applies to the reference itself, **not** the individual elements inside the array. 

If we made `size` and `root` volatile in `AbstractNaryTree`, it would create a dangerous illusion of safety. A reader may observe a newly published root or size while simultaneously observing stale or inconsistent array contents. Since traversal algorithms depend on keyCount and the keys[] / children[] arrays remaining consistent with one another, such observations can lead to incorrect searches or runtime failures (for example, NullPointerException or IndexOutOfBoundsException, depending on the observed state).

Furthermore, N-ary trees are specifically designed with Mechanical Sympathy in mind—their contiguous array blocks (especially at small degrees like $t=16$ or $t=32$) fit perfectly into CPU L1 cache lines. Writing to a `volatile` field acts as a memory barrier, forcing the CPU to flush its store buffer and invalidating those extremely fast cache lines across cores. By stripping `volatile`, we allow the CPU to ruthlessly optimize and cache the node arrays in L1 during aggressive, single-threaded batch insertions without being constantly interrupted by memory barrier flushes.

Because reading an N-ary tree safely across threads intrinsically requires a full external lock (or specialized read-write locks) to guarantee the arrays are consistent with the tree size, keeping `volatile` on just the scalar fields was deemed useless overhead, a trap for developers, and a direct hit to L1 cache performance.

**Consequences:**  
* **Binary:** Gets consistent, immediate visibility of basic tree scale/structure without coarse-grained locks.
* **N-ary:** Forces developers to strictly manage their own external synchronization, acknowledging that array-based tree visibility cannot be solved by `volatile` alone.

## Rejected Alternatives

### Full Internal Synchronization

We rejected wrapping everything in `synchronized` blocks because it would destroy performance. It introduces massive lock contention into read-heavy workloads and makes the whole implementation unnecessarily complex.

### Atomic Wrappers

We briefly considered `AtomicLong` and `AtomicReference`, but we rejected them. We don't actually need complex atomic compound operations like `compareAndSwap`—using atomic wrappers would just bloat our memory footprint with extra object headers for no reason.


