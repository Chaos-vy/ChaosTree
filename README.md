<div align="center">

# 🌲 ChaosTree
**A high-performance, cache-aware Java NavigableMap & NavigableSet engineered for the extreme.**

[![Supported JVM Versions](https://img.shields.io/badge/JVM-21+-brightgreen.svg?&logo=openjdk)](#)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.chaos-vy/chaos-tree.svg?label=maven%20central)](https://search.maven.org/artifact/io.github.chaos-vy/chaos-tree)
[![GitHub release](https://img.shields.io/github/v/release/Chaos-vy/ChaosTree)](https://github.com/Chaos-vy/ChaosTree/releases)
[![License](https://img.shields.io/github/license/Chaos-vy/ChaosTree)](LICENSE)
[![Coverage](https://raw.githubusercontent.com/Chaos-vy/ChaosTree/badges/.github/badges/jacoco.svg)](https://github.com/Chaos-vy/ChaosTree/actions)
[![codecov](https://codecov.io/gh/Chaos-vy/ChaosTree/graph/badge.svg?token=V6LKTLXZA2)](https://codecov.io/gh/Chaos-vy/ChaosTree)

*ChaosTree is a zero-dependency Java library featuring AVL Trees, Red-Black Trees, B-Trees, and B+ Trees. <br> Fully compliant with JDK 21 `SequencedCollection`*

[Documentation Hub](https://chaos-vy.github.io/ChaosTree/index.html) • [Benchmarks](#-benchmark-highlights) • [Getting Started](#-getting-started)

</div>

---

## Why ChaosTree

|                              |                                                                                                                                                                  |
|------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Cache-locality first**     | N-ary nodes pack into pre-allocated, exact-capacity arrays — up to ~40% fewer memory stalls on large range scans vs. pointer-chasing structures.                 |
| **O(N) bulk loading**        | `buildFromSorted` and `importFlatMatrix` construct trees directly from sorted data, with a tunable fill factor `[0.5, 1.0]`.                                     |
| **Strictly compatible**      | Built on JDK 21 `SequencedCollection` / `SequencedSet` / `SequencedMap`. Validated against 214,000+ Guava Testlib cases for exact `TreeMap`/`TreeSet` semantics. |
| **Serializable & Cloneable** | Native `O(N)` bulk-load restoration on deserialize; full `Cloneable` support.                                                                                    |
| **Zero dependencies**        | Nothing else on the classpath.                                                                                                                                   |
 

---
## Getting Started

**Maven**
```xml
<dependency>
    <groupId>io.github.chaos-vy</groupId>
    <artifactId>chaos-tree</artifactId>
    <version>2.0.1</version>
</dependency>
```

**Gradle (Kotlin DSL)**
```kotlin
implementation("io.github.chaos-vy:chaos-tree:2.0.1")
```

## Quick Start

```java
import chaos.tree.naryMap.BPlusTreeMap;
import java.util.NavigableMap;
 
public class Main {
    public static void main(String[] args) {
        // Degree-64 B+Tree, backed by a contiguous leaf-linked list
        NavigableMap<Integer, String> map = new BPlusTreeMap<>();
 
        map.put(1, "Chaos");
        map.put(2, "Tree");
        map.put(3, "Performance");
 
        // Range scans walk the linked leaf layer directly — no tree descent
        NavigableMap<Integer, String> subMap = map.subMap(1, true, 3, true);
        System.out.println(subMap);
    }
}
```

> `addFirst()` / `addLast()` are unsupported and fail-fast — these are strictly sorted structures.
---

## Architecture

Two engines, chosen by workload:

- **N-ary family** (`BTree`, `BPlusTree`) — maximum read throughput and large-scale range scans, zero GC churn. `BPlusTree` pushes all data into a contiguous doubly-linked leaf layer for fast sequential reads.
- **Binary family** (`AVL`, `RBT`) — fast point queries and everyday storage where N-ary's extreme cache optimization isn't needed.
  Deep dives: [Architecture Decision Records](https://chaos-vy.github.io/ChaosTree/utils/ADR.html) · [N-ary Tree Architecture](https://chaos-vy.github.io/ChaosTree/utils/Nary-Tree-Architecture.html) · [N-ary Complexity Map](https://chaos-vy.github.io/ChaosTree/utils/nary-complexity-map.html) · [Math Behind Bulk Load](https://chaos-vy.github.io/ChaosTree/utils/build/array-build-proof.html)

---

## Correctness & Testing

ChaosTree is validated through several independent layers:

- **Guava Testlib** — 214,680 generated test permutations enforcing exact `NavigableMap`/`NavigableSet` semantics against `java.util`.
- **Jqwik property-based fuzzing** — hundreds of thousands of randomized structural invariant checks against `java.util.TreeMap` as a source of truth. (N-ary's degree-32 node floor meant Guava's suite never exercised it directly, so a dedicated invariant-verification API was built to cover that gap.)
- **Randomized differential testing** against reference collections.
- **White-box structural validation** of B-Tree/B+Tree node invariants.
- **Contract tests** — fail-fast `ConcurrentModificationException` semantics, exact size counting, strict null-guards on custom comparators.
  [The Testing Journey](https://chaos-vy.github.io/ChaosTree/utils/build/Test_Journey.html)

---

## Benchmarks

All results: JMH, 3 forks, i5-13450HX (16c) unless noted otherwise. Full methodology and raw logs are linked per section.

### Sequential vs. random insertion — 5M keys

> Java 21.0.12, `-Xms4g -Xmx4g -XX:+UseParallelGC -XX:+AlwaysPreTouch`, 5 measurement iterations.

**Sequential key order**

| Benchmark | Complexity | Score (ms/op) | Allocated (B/op) | GC Time (ms) |
|---|:---:|---:|---:|---:|
| **`dragonFeed` (native, factor 0.75f)** | O(N) | **5.451** | **58,058,576** | 649 |
| `bulkLoad` (iterator) | O(N) | 41.202 | 58,298,864 | 197 |
| `TreeMap.bulkLoad` (JDK) | O(N) | 80.988 | 200,000,311 | 1,022 |
| `iterativePut` (ChaosTree) | O(N log N) | 300.806 | 86,994,836 | 673 |
| `iterativePut` (JDK `TreeMap`) | O(N log N) | 917.328 | 200,001,987 | 717 |

`dragonFeed` is **168x faster** than JDK sequential insertion, **14.9x faster** than the JDK's own bulk loader, and allocates **71% less heap**.

**Random key order** (bulk-loading bypasses don't apply here)

| Benchmark | Score (ms/op) | Allocated (B/op) | GC Time (ms) |
|---|---:|---:|---:|
| **`randomPut` (ChaosTree)** | **3,535.158 ± 399.494** | **62,784,682** | 620 |
| `randomPut` (JDK `TreeMap`) | 7,314.133 ± 483.405 | 200,005,846 | 342 |

Random insertion is the honest worst case — cache-locality gains collapse for both structures. ChaosTree still comes out **2.07x faster** with **3.2x less** allocation.

### Bulk import (`importFlatMatrix`, arraycopy-based)

| Factor | 10K | 100K | 1M | 10M |
|:---:|---:|---:|---:|---:|
| 0.5f | 11.5 µs | 145.7 µs | 1.47 ms | 16.73 ms |
| 0.75f | 9.7 µs | 98.8 µs | 1.02 ms | 13.01 ms |
| **1.0f** | **7.3 µs** | **75.2 µs** | **0.79 ms** | **9.73 ms** |

### Sorted build (`buildFromSorted`, iterator-driven)

| Factor | 10K | 100K | 1M | 10M |
|:---:|---:|---:|---:|---:|
| 0.5f | 74.3 µs | 700.3 µs | 7.64 ms | 82.8 ms |
| 0.75f | 72.6 µs | 705.4 µs | 7.60 ms | 84.0 ms |
| 1.0f | 73.8 µs | 689.4 µs | 7.62 ms | 84.4 ms |

Factor is a non-factor here — every value sits within error bars of the others. Cost is dominated by per-entry comparator/split overhead, not node packing. (Contrast with `importFlatMatrix` above, where factor is a first-order effect.)

### Memory footprint @ 1M entries

| Factor | Time | Tree Size |
|:---:|---:|---:|
| 0.5f | 1.48 ms | 16.7 MB |
| 0.75f | 1.02 ms | 12.0 MB |
| **1.0f** | **0.78 ms** | **8.3 MB** |

**Rule of thumb:** `0.75f` (default) leaves headroom for future writes. `1.0f` is for read-only/snapshot data — fastest and smallest, but the first write after load forces a split.

[Full Dragon Feed report](https://chaos-vy.github.io/ChaosTree/benchmark/dragon-feed.html)

### Tail latency (p1.00)

> JMH `-bm sample`, n = 1K→10M, degree 64, `-Xms4g -Xmx4g -XX:+UseParallelGC -XX:+AlwaysPreTouch`. Sample mode keeps every invocation, so GC pauses and rebalance cascades show up at p1.00 instead of averaging out.

| n | `TreeMap` p1.00 | `B+Tree` p1.00 | `B-Tree` p1.00 |
|---:|---:|---:|---:|
| 1K | 899 µs | 139 µs | 176 µs |
| 10K | 1,169 µs | 159 µs | 145 µs |
| 100K | 2,363 µs | 208 µs | 172 µs |
| 1M | 22,086 µs | 221 µs | 187 µs |
| 10M | 130,286 µs | 234 µs | 218 µs |

Over a 10,000x increase in data size, `TreeMap`'s worst case grows **~145x** (899 µs → 130 ms) — confirmed against `-Xlog:gc` as real stop-the-world pauses. ChaosTree's B+Tree and B-Tree grow **~1.3–1.5x**, with zero logged GC pauses at any size, on any fork.

**Root cause:** packed-array nodes (ChaosTree) vs. one heap-allocated `Entry` object per key (`TreeMap`) — fewer, larger allocations beat millions of small ones under churn.

[Full tail-latency report](https://chaos-vy.github.io/ChaosTree/benchmark/tail-latency.html)

### Iteration: `BPlusTreeMap` vs. `TreeMap` vs. `ArrayList`

> JMH `avgt`, `-prof perfnorm`, `entrySet()` iteration, 1K–1M elements.

| Structure | Cost @ small N | Cost @ 1M |
|---|---:|---:|
| `BPlusTreeMap` | ~1.6 ns/elem | **2.57 ns/elem** |
| `ArrayList<Entry>` | ~0.37 ns/elem | 3.09 ns/elem |
| `TreeMap` | ~6.1 ns/elem | 21.1 ns/elem |

`BPlusTreeMap` overtakes a flat `ArrayList` past ~280K elements, despite executing ~4.6x more instructions per element — the win is memory locality, not compute. At 1M elements, LLC cache-miss ratio is 38% for `BPlusTreeMap` vs. 86.8% for `ArrayList` (nearly every access round-trips to main memory), with ~21x fewer LLC accesses per element and sustained IPC of 3.72 vs. 0.88.

`TreeMap` is 5–10x slower than both at every size — its parent/left/right pointer traversal touches far more scattered memory per step.

**Takeaway:** below ~280K entries, a flat array wins on pure iteration. Past that, `BPlusTreeMap`'s cache-friendly leaf layout takes over, and the gap widens with scale.

More reports: [Insert-heavy](https://chaos-vy.github.io/ChaosTree/benchmark/nary-insert-heavy.html) · [Read-heavy](https://chaos-vy.github.io/ChaosTree/benchmark/nary-read-heavy.html) · [Mixed workload](https://chaos-vy.github.io/ChaosTree/benchmark/nary-mixed-workload.html) · [The Amortization Quirk](https://chaos-vy.github.io/ChaosTree/utils/Slow-at-low-dataset.html)

---

## Documentation

Full docs, ADRs, and benchmark methodology live on the [Documentation Hub](https://chaos-vy.github.io/ChaosTree/).

- [Release history (`CHANGELOG.md`)](CHANGELOG.md)
- [Contributing guide (`CONTRIBUTING.md`)](CONTRIBUTING.md)
- [Security policy (`SECURITY.md`)](SECURITY.md)

---

## Support & Contributions

- **Bugs & features:** [GitHub Issues](https://github.com/Chaos-vy/ChaosTree/issues)
- **Discussion:** [GitHub Discussions](https://github.com/Chaos-vy/ChaosTree/discussions)
  Pull requests and well-scoped issue reports for compatibility, correctness, and maintenance work are welcome.
 