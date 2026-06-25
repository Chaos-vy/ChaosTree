## Extreme Memory Limits & Saturation Profiling

To find out exactly where the ChaosTree framework breaks, we threw our data structures into a bare-metal heap saturation test (we call it the "Chaos Engine") against a strictly capped JVM.

← Back to [README](README.md)

---
## Hardware & Environment
* **CPU:** i5 13450HX 16 Cores (Intel Hybrid Architecture)
* **RAM:** 24 GB DDR5 (4800 MT/s)
* **JVM Allocation:** 5,808 MB Maximum Heap (`-Xmx`)
* **JVM Flags:** `-XX:+UseCompressedOops`
---

## 1. Absolute Capacity Limits & JVM Interoperability

The `ChaosTree` collections utilize a standard 32-bit signed `volatile int` for internal `size` tracking, establishing a hard structural capacity ceiling of **$2,147,483,647$ elements** ($2^{31}-1$).

**Why do we use an `int` instead of a `long`?**
This limitation is a highly intentional, intentional JVM interoperability design choice driven entirely by the limitations of the Java Virtual Machine (JVM).

1. **JVM Array Boundaries:** The entire java.util.Collection and java.util.stream ecosystem ultimately relies on Java arrays, whose indices are limited to 32-bit signed integers. While a tree implementation could theoretically track its logical size using a long, any operation that materializes the contents into a contiguous array—such as toArray(), Stream.toArray(), Collectors.toList() (internally backed by arrays), or similar APIs—must allocate a Java array. Because the JVM restricts array lengths to approximately Integer.MAX_VALUE (with an implementation-dependent safety margin, typically around Integer.MAX_VALUE - 8 on HotSpot), attempting to allocate a larger array immediately fails with:
`java.lang.OutOfMemoryError: Requested array size exceeds VM limit`
Consequently, supporting collections larger than the JVM's maximum array size would require an entirely different ecosystem of APIs and data structures rather than the standard Java Collections Framework.
2. **The Memory Reality:** Based on the validated node footprint of ≈48 bytes (via compressed OOPs), reaching the 32-bit integer ceiling would require approximately 103 GB of heap memory. In practice, heap exhaustion occurs long before the theoretical size limit is approached.
3. **Collection Framework Compatibility:** Maintaining a 32-bit `volatile int` aligns with the capacity model used throughout the Java Collections Framework. Since interoperability ultimately depends on JVM-backed arrays and collection APIs, tracking capacities beyond `Integer.MAX_VALUE` provides little practical value.
---

## 2. Stack Depth & Recursion Limits

Binary trees rely on vertical traversal paths. The depth of these paths dictates whether the collection is limited by the JVM's Heap space or the Thread Execution Stack.

* **Unbalanced Trees (BST):** Bounded by the JVM Stack. In bare-metal testing, a sequentially inserted `BST` degenerated into a linked list and triggered a `StackOverflowError` at exactly **19,654 nodes**.
* **Balanced Trees (AVL, RBT, Treap, Splay):** Bounded by JVM Heap. Because their height is mathematically restricted to `O(log n)`, their depth grows logarithmically and remained negligible compared to heap limits throughout saturation testing. They successfully scale to **126.7+ Million nodes** before triggering an `OutOfMemoryError` on a 5.8 GB heap limit.

| Tree      | Runtime Error | Allocated nodes | Description                                  |
|-----------|---------------|-----------------|----------------------------------------------|
| **BST**   | SOF           | ~19654          | (unbounded recursion on sorted input)        |
| **Splay** | OOM           | ~126.8M         |                                              |
| **AVL**   | OOM           | ~126.8M         | (height-bounded, recursion depth negligible) |
| **RBT**   | OOM           | ~126.8M         |                                              |
| **Treap** | OOM           | ~126.8M         |                                              |

---

## 3. Thread Safety Limits

We deliberately built the Binary Family to be **not natively thread-safe** (except for our dedicated Concurrent RBT). You can technically wrap them in external synchronization (`synchronized` blocks or `ReadWriteLock`), but you will hit severe micro-architectural bottlenecks under heavy contention:

* **The Splay Exclusion Zone:** `Splay` trees are poorly suited to `ReadWriteLock-style` concurrency because `contains()` performs structural modification rather than acting as a read-only operation.
* **The Monitor Lock Tax:** When we ran an 8-thread write-heavy benchmark, applying external monitor locks caused our execution latency to spike to an awful **~34,000 ns/op**. This is an Operating System constraint—coarse-grained locking forces the OS to continuously park and context-switch threads, completely starving the CPU pipeline. For massive concurrent write throughput, wait for the lock-free B-Tree paging implementations.
* **Treap Seed Contention:** Instantiating a `Treap` with a shared `java.util.Random` seed under concurrent writes will cause severe atomic contention. Always use the default constructor, which utilizes independent `ThreadLocalRandom` pipelines to bypass seed locking.

---

## 4. Iterator & Modification Limits

The ChaosTree iterators and stream spliterators are strictly **fail-fast**.

* **ConcurrentModificationException:** All topological modifications (insertions, deletions, and Splay searches) increment a `volatile modCount`. If an active iterator detects a mismatch between its expected state and the tree's global state, it will immediately throw a `ConcurrentModificationException`.
* **Visibility Guarantee:** Because `modCount` and `size` are declared as `volatile`, multithreaded modifications are instantly visible across core boundaries without requiring a lock acquisition. However, `volatile` does not prevent race conditions on compound operations (e.g., `modCount++`), meaning un-synchronized concurrent writes may silently drop fail-fast visibility.

---

## 5. Performance Degradation Thresholds

Through micro-architectural CPU testing (`perf`), the following degradation thresholds were identified:

* **Splay Tree Branch Misses:** On purely random search workloads, Splay trees suffer massive CPU pipeline degradation. Because every access rewrites the tree structure, the hardware branch predictor fails repeatedly, resulting in **29+ branch misses per operation** and pushing latency to ~525 ns/op. Use Splay ONLY for workloads with intense temporal locality (e.g., caching recently used IDs).
* **AVL Insertion Overhead:** While AVL provides the fastest single-threaded search bounds (~31 ns/op), it enforces strict geometric flatness (height difference <= 1). Under massive write volume, `AVL` performs additional balancing work per mutation compared to `Red-Black Trees` due to its stricter height invariant and more aggressive rotation policy.
* **Cache Line Thrashing:** Any external synchronization wrapper applied to these structures causes severe L1/L2 cache invalidation on the lock metadata block, drastically lowering the core Instructions-Per-Cycle (IPC) ratio.

---
### The 50% Density Advantage
The ChaosTree architecture was tested directly against Java's native `java.util.TreeSet` to compare memory layout efficiency under maximum saturation.

| Data Structure       | Max Elements Retained | Total Footprint per Element | Failure Mode                 |
|----------------------|-----------------------|-----------------------------|------------------------------|
| **ChaosTree RBT**    | **126,777,335**       | $\approx 48$ bytes          | `OutOfMemoryError` (Heap)    |
| **ChaosTree Treap**  | **126,775,226**       | $\approx 48$ bytes          | `OutOfMemoryError` (Heap)    |
| **ChaosTree AVL**    | **126,752,322**       | $\approx 48$ bytes          | `OutOfMemoryError` (Heap)    |
| **ChaosTree Splay**  | **126,727,081**       | $\approx 48$ bytes          | `OutOfMemoryError` (Heap)    |
| **Native `TreeSet`** | 84,523,757            | $\approx 72$ bytes          | `OutOfMemoryError` (Heap)    |
| **ChaosTree BST**    | 19,654                | $\approx 40$ bytes          | `StackOverflowError` (Stack) |

| Tree    | Structural Overhead (Excluding Payload) |
|---------|-----------------------------------------|
| BST     | ~24 B                                   |
| AVL     | ~32 B                                   |
| RBT     | ~32 B                                   |
| Splay   | ~32 B                                   |
| Treap   | ~32 B                                   |
| TreeSet | ~40 B                                   |

### Engineering Takeaways
1. **Memory Efficiency:** The ChaosTree base nodes pack into 48-byte cache-line friendly footprints. Compared to the heavy `Map.Entry` objects utilized by the standard Java library, ChaosTree successfully houses **50% more nodes in the exact same memory footprint**.
2. **Uniform Object Padding:** The balanced variants (AVL, RBT, Treap, Splay) all trigger heap exhaustion within a 0.04% margin. This indicates the JVM perfectly pads their distinct tracking variables (`color`, `height`, `priority`) into uniform byte boundaries with zero memory waste.
3. **The Unbalanced Danger:** The standard BST failed via `StackOverflowError` at $\approx 19,654$ nodes. Without rotational balancing, sequential insertions degraded the tree into a linked list, destroying the thread execution stack long before the heap was threatened. All balanced family variants avoid this failure mode under the tested conditions.

---
<details>
<summary><b>Click to expand raw Chaos Engine Saturation Logs</b></summary>

```text
=================================================
        ENVIRONMENT & HARDWARE SIGNATURE         
=================================================
OS Distribution : Ubuntu 26.04 LTS
Max Allowed Heap (-Xmx) : 5,808 MB
Available CPU Cores     : 16
Java Vendor / Version   : Oracle Corporation 26.0.1
=================================================

[Chaos Engine] Initiating Stack Depth Degradation via BST...
>> SUCCESS: StackOverflowError caught at BST size: 19654

[Chaos Engine] Initiating Heap Saturation via AVL...
>> SUCCESS: OutOfMemoryError caught at allocation count: 126,752,322

[Chaos Engine] Initiating Heap Saturation via RBT...
>> SUCCESS: OutOfMemoryError caught at allocation count: 126,777,335

[Chaos Engine] Initiating Heap Saturation via Splay...
>> SUCCESS: OutOfMemoryError caught at allocation count: 126,727,081

[Chaos Engine] Initiating Heap Saturation via Treap...
>> SUCCESS: OutOfMemoryError caught at allocation count: 126,775,226

[Chaos Engine] Initiating Heap Saturation via Java TreeMap...
>> SUCCESS: OutOfMemoryError caught at allocation count: 84,523,757
==================================================
           CHAOS TEST SEQUENCE COMPLETED          
==================================================

```
</details>