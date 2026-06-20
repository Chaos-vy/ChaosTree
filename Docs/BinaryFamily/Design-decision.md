# Design Decisions

These are the non-obvious choices made while building ChaosTree and the
reasoning behind each one. If something in the codebase looks deliberate
but unexplained, the explanation is here.

This document covers what was built, what was rejected, and why — in the
order the decisions were actually made.

← Back to [README](README.md)

---

## 1. Architecture Decisions

### CRTP Tree Hierarchy
**Problem:** A deep hierarchy of trees requires strongly-typed node handling without continuously casting generic `Node` objects to specific implementation types (e.g., `AVLNode`).
**Decision:** We adopted a Curiously Recurring Template Pattern (CRTP) hierarchy:
`ITree -> ISearchTree -> BinaryTree -> AbstractBiTree<T, N extends BiNode<T, N>> -> Concrete Trees`.
**Advantages:** Subclasses like `AbstractRotateTree` and `AVL` inherently know their exact node type. This provides compile-time type safety for node interactions (e.g., `node.getLeft()`) without runtime casting overhead.
**Disadvantages:** Generics signatures become deeply nested and visually complex for maintainers.
**Alternatives:** Use standard generics and cast down (`(AVLNode) node.getLeft()`). Rejected because it introduces runtime performance penalties and circumvents Java's type safety.

### Iterable Integration
**Problem:** Trees must be easily traversable by users.
**Decision:** `ISearchTree<T extends Comparable<T>>` explicitly implements `Iterable<T>`, defaulting to an in-order traversal iterator.
**Advantages:** Allows idiomatic Java enhanced for-loops: `for (T element : tree)`. In-order traversal naturally yields sorted elements, which is the overwhelmingly common use case for search trees.
**Disadvantages:** Locks the default iteration order.
**Alternatives:** Require users to explicitly request an iterator via `tree.iterator(TraversalType.INORDER)`. Rejected because it creates boilerplate for the 99% use case.

### Minimal Root Interface
**Problem:** How to unify fundamentally different tree paradigms (Binary Search Trees, Tries, Spatial Trees).
**Decision:** `ITree` exclusively exposes `size()`, `isEmpty()`, and `clear()`.
**Advantages:** Prevents the pollution of search semantics into trees that do not search by a single key. For example, a spatial QuadTree queries by bounding boxes, not comparable scalar values. 
**Disadvantages:** Users dealing with `ITree` can only observe size, requiring downcasting to query.
**Alternatives:** Put `contains()` and `insert()` in `ITree`. Rejected because `contains(T)` implies an exact match which is incompatible with point-cloud or prefix-trie paradigms.

### Future Package Strategy
**Problem:** Enforcing strict interface isolation before future modules are built.
**Decision:** Packages like `spatial/` and `concurrent` exist in the 1.0.0 hierarchy despite containing no implementations.
**Advantages:** Architecturally guards the boundaries of `ITree`. It acts as a structural contract for future contributors that the core interfaces must not be biased towards binary search operations.

---

## 2. Node Design Decisions

### Parent Pointer Separation
**Problem:** Parent pointers simplify tree traversal and iterative modifications, but consume memory.
**Decision:** Created two strict hierarchies: `BiNode` (no parent pointer) and `ParentBiNode` (with parent pointer).
**Advantages:** Memory efficiency. Trees that strictly require parent traversal (Red-Black, Splay) use `ParentBiNode`. Trees that can be maintained recursively via the call stack (AVL, Treap) use `BiNode`.
**Disadvantages:** Duplicates some abstract balancing logic.
**Alternatives:** Put a parent pointer in all nodes. Rejected because it incurs an unacceptable memory penalty for AVL/Treap for no algorithmic benefit.

### Parentless AVL and Treap

AVL and Treap are built on `BiNode` rather than `ParentBiNode`.

This reduces per-node metadata and avoids storing parent references that are not required by the algorithms. The tradeoff is that upward traversal relies on recursive call stacks rather than explicit parent links.

The design prioritizes a smaller node footprint while keeping balancing logic simple.
### Color Enum Placement
**Problem:** Where to store the Red/Black invariant state.
**Decision:** `Color` is a package-private top-level enum, rather than nested inside `RBT`.
**Advantages:** Clean namespace and future-proofs the library for variants (e.g., Left-Leaning Red-Black Trees or lock-striped concurrent RBTs) that also require the coloring invariant.
**Alternatives:** Nested `RBT.Color`. Rejected as it restricts reuse and clutters the primary algorithm class.

### Treap Priority Guard
**Problem:** Unbounded priority assignment in Treaps can cause entropy failure.
**Decision:** Explicit priority bound validation constraints.
**Advantages:** Deterministic priority generation is supported for testing and benchmarking
through seeded constructors and custom Random providers.
---

## 3. API Design Decisions

### Public root() Removal
**Problem:** Exposing the tree's entry point.
**Decision:** `root()` was completely removed from the public API. 
**Advantages:** Strict encapsulation. Exposing the root permanently leaks the `Node` class hierarchy, tying the public API to internal memory layout and preventing future optimizations (like flattening or array-backing).
**Alternatives:** Provide `tree.toList(TraversalType.LEVEL_ORDER).getFirst()`. This provides the semantic root value without leaking the node reference.

### DeleteResult Record
**Problem:** Java methods cannot return multiple values (e.g., the new root node and a boolean indicating if deletion occurred).
**Decision:** Introduced a private `DeleteResult` record.
**Advantages:** Immutable, semantic clarity, zero-boilerplate.
**Alternatives:** `boolean[]` hacks or mutable holder objects. Rejected due to escape-analysis failure overhead and poor readability.

### SearchResult Record
**Problem:** Executing `floor()`, `ceil()`, and `contains()` traditionally requires separate tree traversals.
**Decision:** A single top-down search yields a `SearchResult<T>` containing all three.
**Advantages:** Eliminates redundant $O(\log n)$ pointer-chasing for complex positional queries.
**Disadvantages:** Minor allocation overhead, mitigated by JVM Escape Analysis.

### Null Validation Strategy
**Problem:** Handling `null` insertions.
**Decision:** Native `Objects.requireNonNull` enforces non-nullability at the API boundary.
**Advantages:** Fails fast, aligns with standard `java.util` Collections semantics, and allows the compiler to optimize out internal null checks via static analysis.

### DuplicateNodeException
**Problem:** How to handle existing keys.
**Decision:** `DuplicateNodeException` extends `RuntimeException`.
**Advantages:** Unchecked exceptions align with Java Collection Framework idioms (e.g., `IllegalArgumentException`). Checked exceptions would pollute stream operations and lambda implementations across the library.

---

## 4. Thread Safety Decisions

### Volatile Tree Metadata
**Problem:** State visibility across threads.

**Decision:** `root`, `size`, and `modCount` are marked `volatile` to ensure
state changes are immediately visible across threads. `modCount` is declared
as `volatile long` — not `int` — to prevent silent overflow in long-lived
caches where an `int` wraps after 2.1 billion modifications; a `long` takes
292,000 years at 1 million writes per second.

**Advantages:** External weakly-consistent reads like polling `size()` never
observe stale CPU-cached values. `volatile` establishes a happens-before edge
between a writing thread and any thread that subsequently reads the field —
without requiring a lock acquisition on the read side.

**Limitation:** `volatile` guarantees visibility, not atomicity. Compound
operations like `modCount++` are still read-modify-write sequences that require
external synchronization under concurrent writes. `volatile` alone does not
make the trees thread-safe.

**Deep Volatility Fallacy:** Declaring `root` as `volatile` only guarantees
visibility of the root reference itself — not the node graph beneath it.
`volatile` is shallow. The internal `left`, `right`, and `parent` pointers
inside each node carry no visibility guarantee of their own. A thread reading
`root` after a write sees the new root reference, but the subtree it points
into may still reflect a partially committed mutation from another thread —
stale child pointers, torn node state, or an invariant mid-rotation. Lock-free
traversal during active structural mutations will encounter data races and
corrupted object graphs. External synchronization must cover the entire
operation, not just the entry point.
### Splay Read-Operation Mutation
**Problem:** `contains()` modifies the Splay tree.

**Decision:** We strictly enforce that search triggers a splay to the root, preventing `ReadWriteLock` separation.

**Advantages:** Preserves the core Splay mathematical guarantee: Amortized $O(\log n)$ performance.

**Alternatives:** Read-only `contains()`. Rejected. A read-only Splay search degrades to $O(n)$ worst-case on adversarial patterns with zero amortized recovery.

### Treap Random Source
**Problem:** Generating priority bounds concurrently.

**Decision:** `ThreadLocalRandom.current()` is the default source.

**Advantages:** Eliminates thread contention on the PRNG engine, avoiding the catastrophic CAS-loop CPU spin caused by `Math.random()` or shared `java.util.Random` in high-throughput environments.

### Concurrent Splay Tree Rejection
**Problem:** Implementing a thread-safe Splay tree.

**Decision:** Explicitly rejected from the concurrent roadmap.

**Why:** Structural mutation on reads means an exclusive lock is required for every search. This destroys read concurrency entirely, defeating the purpose of a concurrent data structure. 

### Long modCount

**Problem:** Iterator fail-fast mechanisms track structural modifications via an integer.

**Decision:** `modCount` is a `long`.

**Advantages:** An `int` overflows after 2.1 billion modifications. In a system doing 1 million inserts/sec, `int` overflows in 35 minutes, silently breaking the fail-fast iterator contract. A `long` takes 292,000 years to overflow.

---

## 5. Benchmarking Decisions

### Splay Setup Strategy

**Problem:** Splay trees dynamically restructure based on query history.

**Decision:** Splay benchmarks use `@Setup(Level.Iteration)` to rebuild the tree from scratch.

**Why:** Continuous querying in JMH without resetting the tree pushes Splay into a degenerate chain or tightly clustered root mass, rendering latency measurements for uniform-random searches statistically invalid compared to static trees.

### Dedicated Splay Benchmark Suite

**Problem:** Shared benchmark state.

**Decision:** Splay Insert/Delete is segregated from the standard Binary benchmark.

**Why:** Including Splay in the shared JMH state contaminates the heap geometry and GC allocation rates for AVL and RBT, skewing their latency profiles.

### Mutation Pool Size

**Problem:** How many elements to insert/delete during JMH churn.

**Decision:** `mutationPool = treeSize`.

**Why:** Using a small mutation pool (e.g., 10%) ensures the hot dataset fits entirely in the CPU L1 cache, presenting an artificially fast, marketing-friendly number. Using 100% of the tree size forces real-world Last Level Cache (LLC) misses.

### Concurrent Benchmark Exclusions

**Problem:** Unbounded or mutative structures in multi-threaded tests.

**Decision:** BST and Splay are excluded from concurrent testing.

**Why:** Concurrent BST searches easily degrade to $O(n)$ pathological latency spikes. Concurrent Splay requires strict `clone-per-thread` isolation to avoid instant pointer corruption.

### Reference Dataset Size
**Problem:** Standardizing visual benchmark outputs.

**Decision:** $n = 10,000$ was selected as the reference target.

**Why:** 10K nodes is large enough to expose memory-hierarchy effects on typical
modern CPUs while keeping benchmark execution times manageable.
---

## 6. Versioning Decisions

### FlatTree Rejected implementation
**Array-backing was rejected because contiguous memory and rebalancing are
architecturally incompatible.**

Rebalancing trees (AVL, RBT) require continuous node rotations. Rotating nodes
inside a flat array necessitates cascading `System.arraycopy` shifts, destroying
the `O(log n)` insertion guarantee entirely — rotation becomes `O(n)`.

Maintaining strict height bounds compounds the problem further. AVL enforces a
height ceiling of `1.44 * log₂(n)`, meaning the array must accommodate a
precisely bounded but unpredictable structural layout. Every rotation invalidates
the index mapping of every descendant node, forcing a full subtree reindex on
each structural change.

AVL enforces a height ceiling of `1.44 * log₂(n)`. A heap-mapped flat array
requires a single contiguous block of `2^(h+1) - 1` slots to cover every
possible node position across all levels. For 1 million elements, ``AVL
height ≤ 29, requiring `2³⁰ - 1 ≈ 1.07 billion` contiguous slots — roughly
`8.6 GB` as a single memory block before storing a single value. RBT's bound
of `2 * log₂(n)` pushes this to `2⁴¹ - 1 ≈ 2.2 trillion` slots — a
physically impossible single allocation on any real hardware. The required
block size is not linear in n — it grows exponentially with log(n),
making pre-allocation architecturally unsolvable.

Pointer-based nodes eliminate all of these constraints. Rotation is a reference
rewire — `O(1)` pointer reassignment with zero memory movement. The tree grows
into heap space without pre-allocation, without index invalidation, and without
any upper bound imposed by contiguous memory requirements.

### Future Version (BinaryFamily)

**Decision:** Version 1.1.0 targets **Concurrent RBT only**. No other
BinaryFamily tree receives a concurrent implementation.

**Reasoning from benchmark data:** The `ConcurrentBenchmark` results
(8 threads, external monitor) establish RBT as the only tree worth the
engineering cost of fine-grained locking:

- **Search at scale:** RBT leads at 10k (1,462 ns/op) and 100k (2,195 ns/op)
- **Mixed workload:** RBT delivers the lowest latency (17,447 ns/op) and lowest
  variance at 100k
- **Insert:** All three trees are statistically indistinguishable under coarse
  synchronization — monitor acquisition dominates, not tree structure

>AVL was not selected despite competitive single-thread performance. Its
stricter balancing invariant increases mutation complexity under contention,
making concurrent implementation less attractive than RBT.

>Treap was excluded from the concurrent roadmap because priority assignment is
implementation-defined and may vary with tree state. Since rotations are driven
by runtime priority relationships rather than deterministic balancing rules,
the mutation path cannot be known in advance. This significantly complicates
fine-grained lock acquisition, invariant verification, and deadlock avoidance
compared to Red-Black Trees, whose balancing behavior follows a fixed set of
structural cases.

>Splay was excluded because every search operation is also a structural
modification, greatly limiting the practical benefit of read concurrency.

---

## 7. Explicitly Rejected Designs

### Concurrent Splay Tree
1. **Idea:** A multithreaded Splay tree utilizing `ReadWriteLock`.
2. **Advantages:** Zero.
3. **Considered because:** Developers frequently ask for thread-safe caching layers.
4. **Reason for Rejection:** `contains()` requires splaying the node to the root, which mutates pointers. A read lock is therefore semantically invalid, and an exclusive lock obliterates concurrency. 

### FlatTree Array Backing
1. **Idea:** Store the binary tree in a contiguous flat array.
2. **Advantages:** Perfect cache locality.
3. **Considered because:** Hardware prefetchers excel at linear memory access.
4. **Reason for Rejection:** Rotations. Moving a subtree in an array requires $O(n)$ memory shifting, violating the strict $O(\log n)$ insertion bounds of AVL and RBT.

### Public root()
1. **Idea:** Provide `tree.root()` to return the top node.
2. **Advantages:** Trivializes custom user algorithms.
3. **Considered because:** standard Java practice often leans toward wide-open APIs.
4. **Reason for Rejection:** Completely breaks encapsulation. It permanently ties the library to a specific node implementation, preventing future structural optimizations.

### boolean[] Delete State
1. **Idea:** Pass a `boolean[]` down the recursive delete chain to track if an element was removed.
2. **Advantages:** Avoids object allocation.
3. **Considered because:** Standard C-style memory trick.
4. **Reason for Rejection:** Semantically unclear, unidiomatic for Java, and unnecessary given that JDK 14+ Escape Analysis flawlessly scalar-replaces short-lived `DeleteResult` records.

### int modCount
1. **Idea:** Use a standard `int` for modification tracking.
2. **Advantages:** Saves 4 bytes per tree.
3. **Considered because:** `java.util.ArrayList` uses an `int`.
4. **Reason for Rejection:** Silent overflow after 2.1 billion modifications. In modern distributed systems, this is a fatal flaw for long-lived application caches.

---

# Long-Term Architectural Philosophy

The ChaosTree library operates on a strict set of guiding principles established during the 1.0.0 lifecycle:

1. **Encapsulation over Convenience:** We do not leak internal layout (`root()`, `Node` classes) just to make custom extensions easier. The API contract is paramount.
2. **Performance Through Measurement, Not Assumptions:** We rely heavily on JMH and hardware profiling. Algorithmic theory is validated against L1 cache geometry, not just Big-O notation.
3. **Memory Footprint Awareness:** Object headers and padding matter. `BiNode` was specifically isolated from `ParentBiNode` to aggressively cull memory overhead where parent pointers were algorithmically unnecessary.
4. **Collection-Framework Consistency:** `modCount` fail-fast semantics, unchecked exceptions, and `Iterable` integration ensure that ChaosTree behaves predictably to any Java veteran.
5. **Explicit Tradeoff Documentation:** Every tree in this library has a critical juncture where it fails. We document these limits explicitly rather than relying on one-size-fits-all marketing.

As ChaosTree moves beyond 1.0.0, future contributions must adhere to this empirical, benchmark-driven design strategy.
