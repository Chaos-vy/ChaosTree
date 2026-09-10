[![Maven Central](https://img.shields.io/maven-central/v/io.github.chaos-vy/chaos-tree.svg?label=maven%20central)](https://search.maven.org/artifact/io.github.chaos-vy/chaos-tree)
[![GitHub release](https://img.shields.io/github/v/release/Chaos-vy/ChaosTree)](https://github.com/Chaos-vy/ChaosTree/releases)
[![License](https://img.shields.io/github/license/Chaos-vy/ChaosTree)](LICENSE)

ChaosTree is a from-scratch, high-performance data structure library for Java 21+. 
It provides highly optimized Binary and especially N-ary search trees (including B-Trees and B+Trees) 
designed to minimize memory overhead, maximize L1/L2 CPU cache locality, and completely minimize the G Stop the world C.. pauses that problems standard Java collections at scale.

## Why ChaosTree?

* **GC Zero-Trash Guarantee:** Eliminates `Map.Entry` object churn entirely. At 1 million elements, JDK `TreeMap` suffers 82ms "Stop-The-World" GC pauses; `BPlusTreeMap` maxes out at a 200µs JVM safepoint sync. **Note: It was caught in 1/30 measurement rest were all okay**
* **Cache-Locality First:** The N-ary engine packs data tightly into pre-allocated exact-capacity arrays, drastically improving L1/L2 CPU cache hit rates and memory load stalls by nearly 40% during large range scans.
* **Strictly Compatible:** Leverages the new JDK 21 `SequencedCollection`, `SequencedSet`, and `SequencedMap` interfaces. It passes the Guava Testlib (214,000+ tests) to enforce identical semantics to `java.util.TreeMap` and `TreeSet`.
* **Public Bulk Load:** I do explicitly provide two powerful API through which user is allowed to build the N-ary tree family, It only works at empty tree. Need sorted data. Verified tested.
* **Serializable & Cloneable** Each tree supports Serialization **(Bulk load O(N))** as well as Cloneable.

### Project status

The latest release is **ChaosTree 2.0.0** (Java 21 baseline). 

ChaosTree 2.0.0 is available from Maven Central and GitHub Releases.

See the [`CHANGELOG.md`](CHANGELOG.md) for the release details and compatibility changes.

### Requirements

- **Minimum JDK: 0xCAFEBABE 0000 0041 | JDK 21+**
- **Build Tool: Maven 3.8+** 

**Do Read from website:** https://chaos-vy.github.io/ChaosTree/index.html

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

* **The N-ary Family (Sets & Maps):** `BTree`, `BPlusTree`. Built for maximum read throughput, large-scale range scans, and zero GC churn. The `BPlusTree` pushes all real data to a contiguous linked-list at the bottom layer, allowing the hardware pre-fetcher to anticipate memory accesses perfectly.
* **The Binary Family (Sets):** `AVL`, `RBT`, . Built for fast point-queries and everyday data storage where the extreme caching of the N-ary engine is not required.

## Testing & Thread-Safety

I wanted ChaosTree to be correct just as much as I wanted it to be fast. It is validated by a relentless testing suite:

* **Guava Testlib:** ChaosTree passes 214,000+ generated test cases validating exact `java.util.NavigableMap` and `NavigableSet` conformance.
* **The Fuzz Test:** Trees are subjected to hundreds of thousands of completely randomized property tests via `jqwik` to verify structural invariants against a source-of-truth (`java.util.TreeMap`).
* **Strict Contracts:** Enforces fail-fast `ConcurrentModificationException` iterator semantics, exact size counting, and strict Null-Pointer guards on custom Comparators.

## Documentation

* **Architecture Decision Records:** [`docs/ADR.html`](docs/ADR.html)
* **JMH GC Profiling & The 82ms Pause:** [`docs/utils/JMH-Report.html`](docs/utils/JMH-Report.html)
* **Throughput & CPU Benchmarks:** [`docs/Benchmark_Analysis.html`](docs/Benchmark_Analysis.html)
* **The Testing Journey:** [`docs/Test_Journey.html`](docs/Test_Journey.html)
* **Release history:** [`CHANGELOG.md`](CHANGELOG.md)
* **Contributing guide:** [`CONTRIBUTING.md`](CONTRIBUTING.md)

## Support and contributions

* **Bugs and features:** GitHub Issues
* **Discussion:** GitHub Discussions

Pull requests and well-scoped issue reports for compatibility, correctness, and maintenance work are welcome! 


---

