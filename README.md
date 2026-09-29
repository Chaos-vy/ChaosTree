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
    <version>2.0.2</version>
</dependency>
```

**Gradle (Kotlin DSL)**
```kotlin
implementation("io.github.chaos-vy:chaos-tree:2.0.2")
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

- **N-ary family** (`BTree`, `BPlusTree`) — maximum read throughput and large-scale range scans, zero GC churn. `BPlusTree` pushes all data into a contiguous doubly-linked (SoA) leaf layer for fast sequential reads.
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

Everything below is measured with [JMH](https://github.com/openjdk/jmh) on one machine, with `Integer` keys and values, and compared against `java.util.TreeMap`. Numbers are means; the linked reports carry the full ± errors, raw JMH output, and every command.

| | |
| --- | --- |
| **CPU** | Intel Core i5-13450HX (16 cores) — L1 864 KiB, L2 9.5 MiB, L3 20 MiB |
| **RAM / OS** | 24 GB, Ubuntu (Linux x86_64) |
| **JVM** | OpenJDK 64-Bit Server VM 21.0.12 |
| **N-ary config** | degree 64 unless noted; fill factor 0.75f unless noted |

> **How to read this page.** Each section below was run with its own JMH settings (listed under the section heading), heap size, and GC. Compare rows **within** a table, not across sections. Ratios are `TreeMap time ÷ ChaosTree time`, so above 1.0x means ChaosTree is faster and below 1.0x means it is slower.

### At a glance

| Workload | ChaosTree B+Tree vs. `TreeMap` |
| --- | --- |
| Bulk load, flat sorted array (`importFlatMatrix`) | **6.1–8.2x** faster at f = 0.75, **7.9–10.7x** at f = 1.0 |
| Bulk load, sorted iterator (`buildFromSorted`) | 1.08–1.15x faster |
| Sequential `put` | 1.5x (1K keys) → 2.8x (1M keys) faster |
| Random `put` | **slower** below ~25K keys (0.66x at 1K, 0.89x at 10K), 1.24x at 100K, 1.69x at 1M |
| Mixed get/put/remove | 1.14x (10K), 1.56x (100K), 2.12x (1M) |
| Point `get` | tie at 10K, ~1.4x faster at 100K |
| Full iteration, 1M entries | ~8x faster |
| Heap, 1M entries (incl. boxed `Integer`s) | −31% (built by `put`), −44% (bulk loaded at 1.0f) |
| Put/remove churn, p99.99 latency at 1M | ~4x lower |

The gains come from memory behaviour, not from doing less work per operation. The wins grow once the working set no longer fits in cache, and they shrink or reverse for small trees (see [Where ChaosTree does not win](#where-chaostree-does-not-win)).
 

---

### Bulk loading

`importFlatMatrix` copies a pre-sorted `Object[][]` into node arrays with `System.arraycopy`. `buildFromSorted` consumes any sorted iterator. Both are O(N) and only work on an empty tree.

> `WriteHeavyMap`, JMH `avgt`, 30 warmup × 500 ms, 30 measurement × 500 ms, 3 forks (90 samples per cell), `-Xms8G -Xmx8G -XX:+AlwaysPreTouch`. The f = 1.0 row comes from a separate 10-warmup run of the same benchmark.

| Benchmark | 1,000 | 10,000 | 100,000 | 1,000,000 |
| --- | --- | --- | --- | --- |
| B+Tree `importFlatMatrix` (f = 0.75) | 1.187 µs | 9.477 µs | 96.573 µs | 0.994 ms |
| B+Tree `importFlatMatrix` (f = 1.0) | 0.914 µs | 7.258 µs | 75.121 µs | 0.808 ms |
| B+Tree `buildFromSorted` (iterator) | 6.545 µs | 71.279 µs | 0.685 ms | 7.214 ms |
| `TreeMap` (sorted-map constructor) | 7.247 µs | 77.559 µs | 0.790 ms | 7.811 ms |
| **Speedup, `importFlatMatrix` f = 0.75** | 6.11x | 8.18x | 8.18x | 7.86x |
| **Speedup, `importFlatMatrix` f = 1.0** | 7.93x | 10.69x | 10.52x | 9.67x |
| **Speedup, `buildFromSorted`** | 1.11x | 1.09x | 1.15x | 1.08x |

The iterator path is only marginally faster than `TreeMap`'s own loader: both do the same linear, memory-bound work and the per-element iterator cost dominates. The large gain comes from handing the engine a flat array so it can skip the per-element iterator, comparisons, and rotations entirely.

**Larger sizes and fill factors** (10 warmup × 1 s, 10 measurement × 1 s, 3 forks, default heap, G1):

| `importFlatMatrix` | 10,000 | 100,000 | 1,000,000 | 10,000,000 |
| --- | --- | --- | --- | --- |
| f = 0.5 | 11.5 µs | 145.7 µs | 1.47 ms | 16.73 ms |
| f = 0.75 | 9.7 µs | 98.8 µs | 1.02 ms | 13.01 ms |
| f = 1.0 | 7.3 µs | 75.2 µs | 0.79 ms | 9.73 ms |

| `buildFromSorted` | 10,000 | 100,000 | 1,000,000 | 10,000,000 |
| --- | --- | --- | --- | --- |
| f = 0.5 | 74.3 µs | 700.3 µs | 7.64 ms | 82.8 ms |
| f = 0.75 | 72.6 µs | 705.4 µs | 7.60 ms | 84.0 ms |
| f = 1.0 | 73.8 µs | 689.4 µs | 7.62 ms | 84.4 ms |

For `importFlatMatrix` the fill factor is a first-order effect (f = 1.0 is roughly 35–42% faster than f = 0.5). For `buildFromSorted` all three factors are within each other's error bars, because per-entry cost dominates.

> **A note on the biggest ratios.** Against `TreeMap` filled with 1,000,000 sequential `put`s (117.5 ms), `importFlatMatrix` is 118x (f = 0.75) to 145x (f = 1.0) faster, and it is 43x faster than B+Tree's own sequential `put` (42.6 ms). Those ratios compare a bulk path with one-at-a-time insertion. The like-for-like comparison against `TreeMap`'s bulk loader is the 6–11x row above.

Choosing between the two paths, and the degree and fill factor: [Tree tuning guide](https://chaos-vy.github.io/ChaosTree/utils/build/Tree-tuning.html) · Full report: [The Dragon Feed](https://chaos-vy.github.io/ChaosTree/benchmark/dragon-feed.html)
 

---

### Insertion, one key at a time

> Same run as the bulk-load table above (`WriteHeavyMap`, 30 × 500 ms warmup and measurement, 3 forks, 8 GB heap).

| Benchmark | 1,000 | 10,000 | 100,000 | 1,000,000 |
| --- | --- | --- | --- | --- |
| B+Tree sequential `put` | 26.268 µs | 0.253 ms | 3.283 ms | 42.647 ms |
| `TreeMap` sequential `put` | 39.507 µs | 0.586 ms | 8.191 ms | 117.514 ms |
| **Speedup** | 1.50x | 2.31x | 2.49x | 2.76x |
| B+Tree random `put` | 82.826 µs | 1.458 ms | 19.968 ms | 440.925 ms |
| `TreeMap` random `put` | 54.558 µs | 1.292 ms | 24.722 ms | 743.592 ms |
| **Speedup** | **0.66x** | **0.89x** | 1.24x | 1.69x |

Random insertion is the honest worst case. While the data sits in L2 cache, `TreeMap` only links one new node whereas the B+Tree shifts array slots inside a leaf, so `TreeMap` wins. Once the working set spills to L3 and beyond, the wide nodes need far fewer pointer hops per operation and the B+Tree pulls ahead. Zoom runs put the crossover near 25,000 keys, where the two are tied within error (20K: 3.183 vs 3.127 ms; 25K: 4.055 vs 4.029 ms; 30K: 4.989 vs 5.204 ms).
 

---

### Mixed get / put / remove

Compared with other sorted-map libraries in a single repeatable workload.

> `ArenaObject` / `ArenaPrimitive`. Each invocation runs 100,000 operations from a pre-generated pool; keys are uniform over `0 … 2×size−1`; the map evolves between invocations (no rebuild). Six get/put/remove mixes: `50/25/25`, `20/40/40`, `80/10/10`, `70/30/0`, `50/50/0`, `30/70/0`. Single-threaded, `-Xms4g -Xmx4g -XX:+AlwaysPreTouch -XX:+UseParallelGC`, 3 forks, 10 × 1 s warmup, 10 × 1 s measurement. The mixes with 0% remove run on a saturated tree.

Mean time per operation across the six mixes (**ns/op, lower is better**):

| Map | 10K | 100K | 1M |
| --- | --- | --- | --- |
| **ChaosTree B+Tree** | 127.3 | **213.2** | **531.4** |
| **ChaosTree B-Tree** | 127.7 | 213.8 | 534.9 |
| JDK `TreeMap` | 145.5 | 333.6 | 1124.4 |
| ChaosTree AVL | 154.6 | 352.2 | 999.6 |
| ChaosTree Red-Black | 155.4 | 351.7 | 1111.0 |
| Fastutil AVL (boxed keys) | 157.2 | 348.6 | 1111.5 |
| Eclipse `TreeSortedMap` | 146.0 | 365.1 | 1105.1 |
| *Fastutil `Int2Int` AVL (primitive, no boxing)* | *123.0* | *246.7* | *578.7* |
| **B+Tree speedup vs. `TreeMap`** | 1.14x | 1.56x | 2.12x |

- The primitive-key Fastutil map avoids boxing and `Comparable` calls entirely, so it is a **ceiling reference**, not a like-for-like competitor. It is fastest at 10K; the B+Tree and B-Tree overtake it at 100K and 1M even with boxed keys.
- The binary family (AVL, Red-Black) performs roughly on par with `TreeMap`. It is slower at 10K and 100K and about 12% faster (AVL) or on par (Red-Black) at 1M. It is there for API parity and workload choice, not for speed.
- `ConcurrentSkipListMap` is in the full report as a single-thread reference (1911 ns/op mean at 1M).
  **Remove-dominant caveat.** In a separate mixed-workload report (`TreeMap` vs. B-Tree/B+Tree only, 2 GB heap, default GC), a `20/10/70` get/put/remove mix at 10K keys is a loss: B+Tree 1.369 ms vs. `TreeMap` 1.295 ms per invocation (0.95x), B-Tree 0.91x. The same mix flips to 1.31x / 1.29x at 100K and 1.82x / 1.79x at 1M.

Full reports: [Arena benchmark](https://chaos-vy.github.io/ChaosTree/benchmark/Chaos-tree-Arena.html) · [Mix-ratio sweep](https://chaos-vy.github.io/ChaosTree/benchmark/nary-mixed-workload.html)
 

---

### Reads

> `ChaosTreeMapReadBenchmark`, `avgt` in ns/op, 10 × 500 ms warmup, 10 × 500 ms measurement, tree pre-filled, seed 42.

| Operation    | Size | `TreeMap` | B-Tree | B+Tree |
|--------------|------|-----------|--------|--------|
| `get`        | 10K  | 106.8     | 100.1  | 104.2  |
| `get`        | 100K | 259.0     | 183.6  | 190.1  |
| `ceilingKey` | 10K  | 104.1     | 106.3  | 104.8  |
| `ceilingKey` | 100K | 220.8     | 191.2  | 175.2  |

At 10K everything is a statistical tie; at 100K the N-ary trees are ~1.2–1.4x faster.

**Full iteration.** B+Tree's `forEach` walks the linked leaf layer directly, with no iterator object and no tree descent. Iterating 1,000,000 entries (JMH, ms/op):

| Structure              | Time              | Allocated |
|------------------------|-------------------|-----------|
| B+Tree `forEach`       | 2.540 ± 0.132 ms  | 8.7 B/op  |
| `ArrayList` (raw loop) | 3.090 ± 0.213 ms  | 10.6 B/op |
| `TreeMap`              | 20.650 ± 3.691 ms | 70.6 B/op |

This is a single-size measurement (1M) against a flat array; it does not claim that the B+Tree beats `ArrayList` at every size.

Full report: [Read-heavy benchmark](https://chaos-vy.github.io/ChaosTree/benchmark/nary-read-heavy.html)
 

---

### Memory footprint

Exact object sizes from [JOL](https://github.com/openjdk/jol) (compressed oops), 1,000,000 `Integer` → `Integer` entries.

| Structure | Tree structure only | Total incl. boxed `Integer`s | Total vs. `TreeMap` |
| --- | --- | --- | --- |
| JDK `TreeMap` | 40,000,048 B (40.0 B/entry) | 71,998,000 B (72.0 B/entry) | — |
| B-Tree (built by `put`) | 17,402,824 B (17.4 B/entry) | 49,400,776 B (49.4 B/entry) | −31.4% |
| B+Tree (built by `put`) | 17,398,864 B (17.4 B/entry) | 49,396,816 B (49.4 B/entry) | −31.4% |
| B+Tree (`importFlatMatrix`, f = 1.0) | 8,701,720 B (8.7 B/entry) | 40,699,672 B (40.7 B/entry) | **−43.5%** |

The tree structure itself is 2.3x smaller (built by `put`) to 4.6x smaller (bulk loaded at 1.0f) than `TreeMap`'s. The 32 MB of boxed `Integer`s is identical for every structure, so it dilutes the total: it is 78.6% of the bulk-loaded B+Tree's footprint. With primitive or pre-interned keys the relative saving would be larger.

**Fill factor vs. size** — `importFlatMatrix`, 1,000,000 entries, `-prof gc`:

| Factor | Time | Tree size |
| --- | --- | --- |
| 0.5f | 1.483 ms | 16.73 MB |
| 0.6f | 1.241 ms | 13.91 MB |
| 0.7f | 1.097 ms | 12.04 MB |
| 0.8f | 0.954 ms | 10.44 MB |
| 0.9f | 0.845 ms | 9.22 MB |
| **1.0f** | **0.777 ms** | **8.30 MB** |

**Rule of thumb:** `0.75f` leaves headroom for future writes. `1.0f` is for read-only or snapshot data — fastest and smallest, but the first write after load forces a split.

Full derivation: [N-ary Tree Architecture](https://chaos-vy.github.io/ChaosTree/utils/Nary-Tree-Architecture.html)


---

### Tail latency

A remove-then-reinsert churn workload (`PutRemove`) holds the map at constant size while turning over nodes and boxed keys — the pattern that exposes GC-driven latency. Values in **µs**.

> `TreeMapVsBTreeVsBPlusBenchmark`, degree 64, fixed seeds, `-Xms6g -Xmx6g`, 3 forks, 10 warmup + 10 measurement iterations × 500 ms.

| n    | Map       | mean          | p99   | p99.99  |
|------|-----------|---------------|-------|---------|
| 10K  | `TreeMap` | 0.355         | 1.415 | 12.161  |
|      | B-Tree    | 0.343         | 0.620 | 7.148   |
|      | B+Tree    | 0.340         | 0.634 | 5.876   |
| 100K | `TreeMap` | 0.617         | 1.902 | 14.113  |
|      | B-Tree    | 0.457         | 0.865 | 8.417   |
|      | B+Tree    | 0.452         | 0.821 | 7.421   |
| 1M   | `TreeMap` | 1.722 ± 0.424 | 5.656 | 100.241 |
|      | B-Tree    | 1.010 ± 0.004 | 4.280 | 22.628  |
|      | B+Tree    | 1.007 ± 0.004 | 4.128 | 25.506  |

At 1M keys the mean is ~1.7x lower and p99.99 is about 4x lower. Absolute worst-case (p1.00) values are single samples and vary run to run, so they are in the [full report](https://chaos-vy.github.io/ChaosTree/benchmark/tail-latency.html) but are deliberately not used as a headline.
 
--
-

### Where ChaosTree does not win

- **Random single-key inserts below ~25K keys[on my machine L2/L3 cache cliff]** The whole tree fits in cache and `TreeMap`'s one-pointer link beats shifting array slots (0.66x at 1K, 0.89x at 10K).
- **Remove-dominant workloads on small trees** (~10K keys or fewer): 0.91–0.95x in the `20/10/70` mix above.
- **Cold, empty-tree updates at low N.** A cold-start study found ~2.4x more instructions per insert than `TreeMap` at 1K keys, converging by 1M. See [Cold Start at Low N](https://chaos-vy.github.io/ChaosTree/utils/Slow-at-low-dataset.html).
- **Primitive-key workloads.** Specialised primitive maps such as Fastutil's `Int2Int*` avoid boxing entirely and are faster at 10K–100K.
- **Binary family (AVL, Red-Black).** Roughly equal to `TreeMap`, not faster.
  If your maps stay under ~10K entries, `TreeMap` is a perfectly good choice. ChaosTree is aimed at large trees, sorted bulk ingestion, and tail-latency-sensitive workloads.

### Reproducing the results

The benchmark module is `ct-benchmark`. Examples:

```bash
# Bulk load + insertion (table sections 1–2)
java -jar ct-benchmark/target/benchmarks.jar WriteHeavyMap \
    -wi 30 -w 500ms -i 30 -r 500ms -f 3 \
    -p size=1000,10000,100000,1000000 \
    -jvmArgs "-Xms8G -Xmx8G -XX:+AlwaysPreTouch" -tu us
 
# Bulk load across fill factors and up to 10M keys
java -jar ct-benchmark/target/benchmarks.jar WriteHeavyMap.importFlatMatrixBPlusTree \
    -p size=10000,100000,1000000,10000000 -p factor=0.5f,0.75f,1.0f \
    -f 3 -wi 10 -w 1 -r 1 -i 10 -tu us
 
# Mixed get/put/remove against other libraries
java -jar ct-benchmark/target/benchmarks.jar ArenaObject \
    -p size=10000,100000,1000000 -f 3 -wi 10 -w 1s -i 10 -r 1s \
    -jvmArgsAppend "-Xms4g -Xmx4g -XX:+AlwaysPreTouch -XX:+UseParallelGC"
```

Results depend on CPU cache sizes, JVM version, GC choice, and key type. Please benchmark your own payload on your own hardware, and open an issue if you see numbers that disagree.

**All reports:** [Bulk load / Dragon Feed](https://chaos-vy.github.io/ChaosTree/benchmark/dragon-feed.html) · [Insert-heavy](https://chaos-vy.github.io/ChaosTree/benchmark/nary-insert-heavy.html) · [Read-heavy](https://chaos-vy.github.io/ChaosTree/benchmark/nary-read-heavy.html) · [Mixed workload](https://chaos-vy.github.io/ChaosTree/benchmark/nary-mixed-workload.html) · [Arena vs. other libraries](https://chaos-vy.github.io/ChaosTree/benchmark/Chaos-tree-Arena.html) · [Tail latency](https://chaos-vy.github.io/ChaosTree/benchmark/tail-latency.html) · [Cold start at low N](https://chaos-vy.github.io/ChaosTree/utils/Slow-at-low-dataset.html)
 

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
 