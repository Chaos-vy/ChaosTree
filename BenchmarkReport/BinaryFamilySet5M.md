# Binary Tree Benchmark Report

This is the analysis report for the **ChaosTree Binary Family** (`BST`, `AVL`, `RBT`, `Treap`, `TreeSet`) at a scale of 5 Million nodes, collected after the `DeleteState` Zero-Allocation Refactor (v1.1.0).

---

## Test Architecture
- **Framework**: Java Microbenchmark Harness (JMH)
- **Workload**: `InsertDeleteBenchmark` Insert+Delete Fisher-Yates Shuffle
- **Scale**: 5,000,000 Elements 
- **JVMs Tested**: JDK 11 vs JDK 21 

---

## `bm sample` (Tail Latency Profile)

### JDK 11 (Latency in ns)
| Tree Type   | Avg (ns/op) | p50 | p90 | p99 | p99.9 | pMax    |
|:------------|:------------|:----|:----|:----|:------|:--------|
| **BST**     | 85.7        | 74  | 78  | 109 | 2,604 | 51,776  |
| **TreeSet** | 146.6       | 132 | 134 | 182 | 2,819 | 29,760  |
| **AVL**     | 329.3       | 301 | 322 | 598 | 7,010 | 145,152 |
| **RBT**     | 334.0       | 313 | 319 | 468 | 6,977 | 90,240  |
| **Treap**   | 379.8       | 348 | 363 | 564 | 7,587 | 656,384 |

### JDK 21 (Latency in ns)
| Tree Type   | Avg (ns/op) | p50 | p90 | p99   | p99.9 | pMax    |
|:------------|:------------|:----|:----|:------|:------|:--------|
| **BST**     | 78.1        | 72  | 74  | 96    | 1,308 | 38,400  |
| **TreeSet** | 119.0       | 104 | 107 | 144   | 3,305 | 49,664  |
| **RBT**     | 292.3       | 267 | 274 | 1,236 | 6,120 | 117,888 |
| **Treap**   | 357.2       | 312 | 335 | 1,412 | 7,585 | 72,064  |
| **AVL**     | 359.1       | 307 | 337 | 1,484 | 7,694 | 113,408 |

---

## `prof gc` (Garbage Collection Allocation)

The recursive insert and delete for AVL and Treap were not following tail recursion were optimized by mutable DeleteState.

| Tree Type   | JDK 11 (B/op) | JDK 21 (B/op) | Allocation Cost Identity                    |
|:------------|:--------------|:--------------|:--------------------------------------------|
| **BST**     | 56            | 56            | `new BSTNode()`                             |
| **RBT**     | 64            | 64            | `new RBTNode()` (Color field overhead)      |
| **TreeSet** | 72            | 72            | JDK baseline (`TreeMap.Entry`)              |
| **AVL**     | 80            | 80            | `new AVLNode()` (Height field overhead)     |
| **Treap**   | 80            | 80            | `new TreapNode()` (Priority field overhead) |

---

## `prof perfnorm` (Execution Throughput)

| Tree Type   | JDK 11 (ns/op) | JDK 21 (ns/op) |
|:------------|:---------------|:---------------|
| **BST**     | 85.71          | 78.19          |
| **TreeSet** | 146.67         | 119.00         |
| **RBT**     | 334.01         | 292.33         |
| **AVL**     | 329.38         | 359.19         |
| **Treap**   | 379.80         | 357.26         |

---

## `prof perf` (Hardware Cache Telemetry)

L1 Data Cache miss rates and execution cycles on JDK 21:

| Tree Type   | L1 Miss Rate | Instructions    | Cycles          | L1 Loads (per op) |
|:------------|:-------------|:----------------|:----------------|:------------------|
| **BST**     | 10.63%       | 360,272,848,991 | 209,070,642,158 | 269               |
| **TreeSet** | 17.44%       | 351,894,976,845 | 221,285,977,932 | 399               |
| **RBT**     | 20.17%       | 338,556,213,277 | 211,344,565,105 | 918               |
| **AVL**     | 19.91%       | 414,804,228,634 | 222,043,493,310 | 1,460             |
| **Treap**   | 10.23%       | 281,248,221,371 | 209,844,788,006 | 891               |

---

## Conclusion

1. **GC Zero-Allocation Achieved**: There was Garbage Collection leak around ~568 B/op for AVL and ~664 B/op for Treap which came into existence during the benchmark done with `bm sample`. So I removed the `DeleteResult` class with the `DeleteState` only containing boolean. The binary family now achieves minimum allocation costs exactly matching the node object size assuming Mechanical Sympathy architecture.
2. **Hardware Locality Limits**: The binary family fundamentally suffers from high LLC (L3) cache misses at scale ($O(\log_2 N)$ depth). Additionally,  AVL tree executes 1,460 L1 Cache Loads *per operation* compared to BST's 269, heavy bounding maximum throughput (300-360 ns/op) regardless of the JDK version.

**Benchmark files are present in the Reports folder**
* [Test on Jdk 11](Reports/binaryJMH_jdk11_5M.txt)
* [Test on jdk 21](Reports/binaryJMH_jdk21_5M.txt)