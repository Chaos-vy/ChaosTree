# ADR-001: DeleteResult Record over boolean[]

**Context:**  
When I'm deleting nodes in both my Binary and N-ary trees, I need a way to tell the calling method what actually happened. However, the two families handle this very differently:

* **Binary Family (BST, AVL, RBT, Splay, Treap):** Deletion is **recursive**. Each recursive step has to return the updated node reference so I can rewire the tree pointer above it, AND it needs to pass back a boolean flag telling me if an element was actually deleted (so I can decrement my `size` counter). This means two distinct values have to travel back up the call stack simultaneously.
* **N-ary Family (BTree, BPlusTree):** Deletion is also **recursive**, but it operates **in-place** on sorted `Object[]` arrays inside each node. The node reference itself never changes—splits and merges mutate the existing arrays directly. So, I only need to pass one value back up: whether the deletion actually happened.

**Decision:**  
I decided to use a `DeleteResult` record for both families, but I gave them slightly different shapes:

* **Binary:** `DeleteResult<T, N>` — carries `(N node, boolean deleted)`. The updated node reference must be returned to allow the parent pointer to be rewired to the new subtree root after rotations.
* **N-ary (`AbstractNaryTree`):** `DeleteResult(boolean deleted)` — carries only the deletion status flag. Since deletion mutates node arrays in-place, the node reference itself never needs to be rewired up the call chain.

**Rationale:**  
The alternative in both cases was the classic `boolean[]` hack—passing a mutable 1-length array down the recursion chain just to carry state back up via side effects. I really didn't like this approach because:
* It relies on hidden side effects instead of clear return values.
* It hides part of the method's contract behind mutable state.
* It's just clunky and unidiomatic in modern Java.

Using a `DeleteResult` record makes ChaosTree's contract clear, immutable, and strictly type-safe. The fact that the two families use differently shaped records isn't an inconsistency—it accurately reflects exactly what each algorithm needs to talk to its caller.

**Tradeoffs:**  
* **Pros:** It keeps ChaosTree's recursive functions pure, eliminates hidden mutations, and each record tells me exactly what I need to know.
* **Cons:** It technically allocates a new `DeleteResult` record on the heap for every recursive frame during a deletion.
* **Resolution:** Modern HotSpot JVMs perform Escape Analysis and Scalar Replacement, allowing many short-lived record allocations to be optimized away. Thanks to Escape Analysis, they frequently eliminate the allocation cost of short-lived records entirely by flattening them into registers. Because of this, I chose to prioritize clean, readable code over manually dodging object creation.

**Consequences:**  
I get clean, predictable deletion signatures across the entire library. Each family gets exactly the data it needs to function—no more, no less.

## 1.2.0 Verdict

- **Removal of all record classes:** Yes, you heard it right. I am removing the record classes now and going back to POJOs.

- **Why??** Creating new nodes/results during recursive traversal showed a massive GC footprint of around **~350 B/op** under `prof-gc` at **5M elements**.
    - At 5M elements, the tree height is almost **~20**, so repeated object creation during recursive operations caused massive allocation pressure.

- **The Resolution:** I completely eradicated the record wrappers from the traversal path. Instead of allocating and returning a new object at every recursive step, I refactored the architecture to use primitive status codes such as `INSERT_SUCCESS` and `INSERT_NEEDS_SPLIT`. Structural state is now updated directly through node mutation.

- **The Result:** GC allocation collapsed from **~350 B/op → 48.0 B/op**. The structural GC overhead is now **0 B/op**. The remaining 48 B/op comes from the raw node and boxed generic value.

- Because the JVM no longer has to deal with millions of short-lived traversal objects, **GC pressure and tail-latency spikes were drastically reduced**, with B+Tree reaching around **~111 ns/op on JDK 21**.