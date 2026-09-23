![Supported JVM Versions](https://img.shields.io/badge/JVM-21+-brightgreen.svg?&logo=openjdk)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.chaos-vy/chaos-tree.svg?label=maven%20central)](https://search.maven.org/artifact/io.github.chaos-vy/chaos-tree)
[![GitHub release](https://img.shields.io/github/v/release/Chaos-vy/ChaosTree)](https://github.com/Chaos-vy/ChaosTree/releases)
[![License](https://img.shields.io/github/license/Chaos-vy/ChaosTree)](LICENSE)
[![Coverage](https://raw.githubusercontent.com/Chaos-vy/ChaosTree/badges/.github/badges/jacoco.svg)](https://github.com/Chaos-vy/ChaosTree/actions)
[![codecov](https://codecov.io/gh/Chaos-vy/ChaosTree/graph/badge.svg?token=V6LKTLXZA2)](https://codecov.io/gh/Chaos-vy/ChaosTree)

## What is ChaosTree?

**ChaosTree is a Java Sorted Set/Map library built around multiple search-tree data structures, including AVL Trees,
Red-Black Trees, B-Trees, and B+ Trees.**

The library provides both **Set and Map implementations**, with APIs designed around the semantics of the JDK's
`NavigableSet`, `NavigableMap`, `SequencedSet`, and `SequencedMap` contracts.

In addition to the standard collection APIs, ChaosTree provides specialized construction APIs for users who want direct
control over the initial structure of N-ary trees, Do read

* `buildFromSorted(Iterator, factor)`
* `importFlatMatrix(Object[][], factor)`

These APIs allow users to control the target node occupancy through a configurable `factor` in the supported range
**[0.5, 1.0]**, while maintaining the structural invariants required by the underlying B-Tree/B+Tree design.

### Correctness & Validation

ChaosTree is validated through multiple layers of testing:

* **Guava Testlib** compatibility testing
* **jqwik** property-based testing
* Randomized differential testing against reference collections
* White-box structural validation of tree nodes
* Direct validation of B-Tree/B+Tree structural invariants
* Exception and iterator-contract testing
* Serialization and cloning tests

The structural tests inspect the internal tree representation rather than relying solely on externally observable
behavior. This provides an additional layer of validation for node occupancy, ordering, topology, and balancing
invariants.

Performance claims are backed by reproducible JMH benchmark configurations. If a referenced benchmark source is missing
from the repository due to project cleanup, it can be restored or replaced with an updated benchmark.

### Why ChaosTree?

* **Cache-Locality First:** The N-ary engine packs data tightly into pre-allocated exact-capacity arrays, drastically
  improving L1/L2 CPU cache hit rates and memory load stalls by nearly 40% during large range scans.
* **Strictly Compatible:** Leverages the new JDK 21 `SequencedCollection`, `SequencedSet`, and `SequencedMap`
  interfaces. It passes the Guava Testlib (214,000+ tests) to enforce identical semantics to `java.util.TreeMap` and
  `TreeSet`.
* **Public Bulk Load:** I do explicitly provide two powerful API through which user is allowed to build the N-ary tree
  family, It only works at empty tree. Need sorted data. Verified tested.
* **Serializable & Cloneable** Each tree supports Serialization **(Bulk load O (N))** as well as Cloneable.

## Requirements

- **Minimum JDK: 0xCAFEBABE 0000 0041 | JDK 21+**
- **Build Tool: Maven 3.8+**

**Details about ChaosTree:** https://chaos-vy.github.io/ChaosTree/index.html

*(Note: As strictly sorted structures, `addFirst()` and `addLast()` are unsupported and fail-fast).*

## Getting ChaosTree

### Maven coordinates

```xml
<dependency>
    <groupId>io.github.chaos-vy</groupId>
    <artifactId>chaos-tree</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle

```kotlin
implementation("io.github.chaos-vy:chaos-tree:2.0.0")
```

## Quick start

Create a highly-optimized `BPlusTreeMap` to leverage the N-ary engine:

```java
import chaos.tree.naryMap.BPlusTreeMap;
import java.util.NavigableMap;

public class Main {
    public static void main(String[] args) {
        // Degree 64 B+Tree Map
        NavigableMap<Integer, String> map = new BPlusTreeMap<>();

        map.put(1, "Chaos");
        map.put(2, "Tree");
        map.put(3, "Performance");

        // Instant range scans traversing the contiguous leaf-linked list
        NavigableMap<Integer, String> subMap = map.subMap(1, true, 3, true);
        System.out.println(subMap);
    }
}
```

## Data Structures and Architecture

ChaosTree is split into two foundational engines:

* **The N-ary Family (Sets & Maps):** `BTree`, `BPlusTree`. Built for maximum read throughput, large-scale range scans,
  and zero GC churn. The `BPlusTree` pushes all real data to a contiguous double linked-list at the bottom layer,
  allowing high read through put.
* **The Binary Family (Sets):** `AVL`, `RBT`, . Built for fast point-queries and everyday data storage where the extreme
  caching of the N-ary engine is not required.

## Testing & Thread-Safety

I wanted ChaosTree to be correct just as much as I wanted it to be fast. It is validated by these following testing
suite:

* **Guava Testlib:** ChaosTree passes 214,000+ generated test cases validating exact `java.util.NavigableMap` and
  `NavigableSet` for all tree.
* **The Fuzz Test:** Trees are subjected to hundreds of thousands of completely randomized property tests via `jqwik` to
  verify structural invariants against a source-of-truth (`java.util.TreeMap`). Due to Nary API node structure of 32 the
  new node never got created in Guava So I explicitly designed the verify API which verify explicitly for that.
* **Strict Contracts:** Enforces fail-fast `ConcurrentModificationException` iterator semantics, exact size counting,
  and strict Null-Pointer guards on custom Comparators.

## Benchmark highlights

### N-ary Tree (B-Tree/B+Tree)

> JMH · degree=64 · i5-13450HX (16c) · G1GC · default heap · 3 forks × 10 warmup/10 measurement iters

>Factor define the minimum node occupancy for 64 degree B+tree CLRS maximum key = 127
> - 127 x 0.5  = 63  minKey
> - 127 x 0.75 = 95  minKey
> - 127 x 1.0  = 127 minKey == maxKey

### The Truth (168x Sequential / 2.07x Random)

> Benchmark environment: Java 21.0.12 (OpenJDK), JMH 1.37, -Xms4g -Xmx4g -XX:+UseParallelGC -XX:+AlwaysPreTouch.
> 3 forks, 5 measurement iterations, 5,000,000 integer keys, single run per table.

**5,000,000 Element Insertion — Sequential Key Order**

| Benchmark (Strategy)                      | Complexity | Score (ms/op) | Allocated (Bytes/op) | GC Time (ms) |
|:------------------------------------------|:----------:|--------------:|---------------------:|-------------:|
| **B+Tree.dragonFeed (Native, factor 0.75f)** | O(N)       | **5.451**     | **58,058,576**       | **649**      |
| B+Tree.bulkLoad (Iterator)                | O(N)       | 41.202        | 58,298,864           | 197          |
| JDK TreeMap.bulkLoad (SortedMap)          | O(N)       | 80.988        | 200,000,311          | 1,022        |
| B+Tree.iterativePut (Sequential)          | O(N log N) | 300.806       | 86,994,836           | 673          |
| JDK TreeMap.iterativePut (Sequential)     | O(N log N) | 917.328       | 200,001,987          | 717          |

*Result: `dragonFeed` is **168x faster** than standard JDK sequential insertion, **14.9x faster** than the JDK's own bulk loader, and allocates **71% less heap** than either JDK path.*

**5,000,000 Element Insertion — Random Key Order**

| Benchmark (Strategy)  | Score (ms/op) | Allocated (Bytes/op) | GC Time (ms) |
|:----------------------|--------------:|---------------------:|-------------:|
| **B+Tree.randomPut**  | **3,535.158** | **62,784,682**       | **620**      |
| JDK TreeMap.randomPut | 7,314.133     | 200,005,846          | 342          |

*Result: Under random insertion, ChaosTree is **2.07x faster** than the JDK and allocates **3.2x less heap**.*

### Bulk Import (`importFlatMatrix`, arraycopy-based)

|  Factor  |        10K |        100K |          1M |         10M |
|:--------:|-----------:|------------:|------------:|------------:|
|   0.5f   |    11.5 µs |    145.7 µs |     1.47 ms |    16.73 ms |
|  0.75f   |     9.7 µs |     98.8 µs |     1.02 ms |    13.01 ms |
| **1.0f** | **7.3 µs** | **75.2 µs** | **0.79 ms** | **9.73 ms** |

### Sorted Build (`buildFromSorted`, iterator-driven)

| Factor |     10K |     100K |      1M |     10M |
|:------:|--------:|---------:|--------:|--------:|
|  0.5f  | 74.3 µs | 700.3 µs | 7.64 ms | 82.8 ms |
| 0.75f  | 72.6 µs | 705.4 µs | 7.60 ms | 84.0 ms |
|  1.0f  | 73.8 µs | 689.4 µs | 7.62 ms | 84.4 ms |

*Factor is a non-factor here — cost is dominated by per-entry comparator/split overhead, not node packing.*

### Memory Footprint @ 1M entries

|  Factor  |        Time |  Tree Size |
|:--------:|------------:|-----------:|
|   0.5f   |     1.48 ms |    16.7 MB |
|  0.75f   |     1.02 ms |    12.0 MB |
| **1.0f** | **0.78 ms** | **8.3 MB** |

**Rule of thumb:** `0.75f` (default) — headroom for future writes. `1.0f` — read-only/snapshot data, fastest + smallest, but first write after load forces a split.
Read more in detailed [DragonFeed]()

##  Tail Latency (p1.00)

> JMH · `-bm sample` · n=1K→10M · degree=64 · Xms4g/Xmx4g · `-XX:+UseParallelGC -XX:+AlwaysPreTouch`

Average time hides rare expensive operations. Sample mode keeps every individual invocation, so a real GC pause or rebalance cascade shows up at p1.00 instead of being averaged into invisibility.

|    n | TreeMap p1.00 | B+Tree p1.00 | B-Tree p1.00 |
|-----:|--------------:|-------------:|-------------:|
|   1K |        899 µs |       139 µs |       176 µs |
|  10K |      1,169 µs |       159 µs |       145 µs |
| 100K |      2,363 µs |       208 µs |       172 µs |
|   1M |     22,086 µs |       221 µs |       187 µs |
|  10M |    130,286 µs |       234 µs |       218 µs |

**TreeMap's worst case grows ~145x** over this range (899µs → 130ms) — confirmed against `-Xlog:gc` as real Stop-The-World pauses, not benchmark noise. **B+Tree and B-Tree grow ~1.3–1.5x** over the same 10,000x increase in data size — zero GC pauses logged at any n, on any fork.

Root cause: packed-array nodes (ChaosTree) vs. one heap-allocated `Entry` object per key (`TreeMap`) — fewer, larger allocations instead of millions of small ones means far less GC pressure under churn.

Full percentile breakdown (p50–p99.99) and methodology: [tail-latency-report](./tail-latency-report.html)

### Iteration Performance: BPlusTreeMap vs TreeMap vs ArrayList

Benchmarked `entrySet()` iteration cost (JMH, avgt, `-prof perfnorm`) across sizes from 1K to 1M elements.

| Structure                  | Cost @ small N | Cost @ 1M elements |
|----------------------------|----------------|--------------------|
| `BPlusTreeMap` (ChaosTree) | ~1.6 ns/elem   | **2.57 ns/elem**   |
| `ArrayList<Entry>`         | ~0.37 ns/elem  | 3.09 ns/elem       |
| `java.util.TreeMap`        | ~6.1 ns/elem   | 21.1 ns/elem       |

**BPlusTreeMap overtakes a flat `ArrayList` at scale (~280K elements and above)**, despite executing ~4.6x more
instructions per element. The reason is memory locality, not raw compute: keys and values live directly in packed leaf
arrays, avoiding the extra `Map.Entry` indirection a reference-based collection pays for every element. At 1M elements,
hardware counters confirm this directly — `ArrayList`'s LLC cache-miss ratio climbs to 86.8% (nearly every access is a
full round-trip to main memory) versus 38% for BPlusTreeMap, which also makes ~21x fewer LLC accesses per element
overall. The payoff shows up as sustained IPC: 3.72 for BPlusTreeMap vs 0.88 for ArrayList at 1M — ArrayList's pipeline
is mostly stalled waiting on memory, not doing useful work.

`TreeMap` is 5-10x slower than both across every size tested — its successor-pointer traversal touches far more
scattered memory per step (parent/left/right node pointers) than either alternative.

**Takeaway:** below ~280K entries, a flat array beats any tree structure for pure iteration. Past that point,
BPlusTreeMap's cache-friendly leaf layout wins, and the gap widens with scale.

### For documentation do use : [https://chaos-vy.github.io/ChaosTree/](https://chaos-vy.github.io/ChaosTree/)

## Support and contributions

* **Bugs and features:** GitHub Issues
* **Discussion:** GitHub Discussions

Pull requests and well-scoped issue reports for compatibility, correctness, and maintenance work are welcome!


---

