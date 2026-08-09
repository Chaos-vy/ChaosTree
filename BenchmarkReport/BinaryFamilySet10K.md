# Binary Tree Benchmark Report (10K Dataset)

This is the analysis report for the **ChaosTree Binary Family** (`BST`, `AVL`, `RBT`, `Treap`, `TreeSet`) at a micro-scale of **10,000 nodes**. 

By restricting the dataset to 10K, the entire tree fits perfectly into the L1 and L2 CPU caches, completely eliminating the  Main Memory latency penalty observed in the 5M dataset.

---



## Test Architecture
- **Framework**: Java Microbenchmark Harness (JMH)
- **Workload**: `InsertDeleteBenchmark` Insert+Delete Fisher-Yates Shuffle
- **Scale**: 10,000 Elements (Fit in L1)
- **JVMs Tested**: JDK 11 vs JDK 21 

---

## `bm sample` (Tail Latency Profile)

### JDK 11 (Latency in ns)
| Tree Type   | Avg (ns/op) | p50 | p90 | p99 | p99.9 | pMax      |
|:------------|:------------|:----|:----|:----|:------|:----------|
| **BST**     | 64.8        | 56  | 58  | 77  | 1,429 | 114,048   |
| **TreeSet** | 76.5        | 69  | 83  | 87  | 1,226 | 15,056    |
| **Treap**   | 116.5       | 102 | 117 | 145 | 3,016 | 108,928   |
| **RBT**     | 152.0       | 139 | 143 | 147 | 1,390 | 1,908,736 |
| **AVL**     | 156.0       | 142 | 159 | 178 | 4,363 | 104,576   |

### JDK 21 (Latency in ns)
| Tree Type   | Avg (ns/op) | p50 | p90 | p99 | p99.9 | pMax    |
|:------------|:------------|:----|:----|:----|:------|:--------|
| **BST**     | 58.4        | 54  | 58  | 67  | 240   | 14,944  |
| **TreeSet** | 78.4        | 63  | 83  | 89  | 1,141 | 11,782  |
| **Treap**   | 68.4        | 58  | 74  | 95  | 1,214 | 42,048  |
| **RBT**     | 129.7       | 117 | 137 | 143 | 4,832 | 123,136 |
| **AVL**     | 147.5       | 140 | 146 | 161 | 1,363 | 87,552  |

---

## `prof gc` (Garbage Collection Allocation)

Because the L1 dataset executes iterations faster, the allocation size per operation exactly mirrors the sizes derived previously, but validates the absolute minimum boundaries.

| Tree Type   | JDK 11 (B/op) | JDK 21 (B/op) | Allocation Cost Identity                                          |
|:------------|:--------------|:--------------|:------------------------------------------------------------------|
| **BST**     | 56.0          | 56.0          | `new BSTNode()` (32B) + `Integer` (24B)                           |
| **RBT**     | 64.0          | 64.0          | `new RBTNode()` (40B) + `Integer` (24B)                           |
| **TreeSet** | 72.0          | 72.0          | JDK baseline (`TreeMap.Entry`)                                    |
| **AVL**     | 80.0          | 80.0          | `new AVLNode()` (40B) + `Integer` (24B) + Partial EA Fail (16B)   |
| **Treap**   | 80.0          | 80.0          | `new TreapNode()` (40B) + `Integer` (24B) + Partial EA Fail (16B) |

---

## `prof perfnorm` (Execution Throughput)

With Main Memory latency removed from the equation, we observe the pure algorithmic speed limit of the rotators.

| Tree Type   | JDK 11 (ns/op) | JDK 21 (ns/op) |
|:------------|:---------------|:---------------|
| **BST**     | 67.08          | 58.48          |
| **TreeSet** | 80.77          | 78.47          |
| **Treap**   | 137.95         | 68.44          |
| **RBT**     | 141.36         | 129.77         |
| **AVL**     | 160.66         | 147.53         |

---

## `prof perf` (Hardware Cache Telemetry)

When isolated to the L1 Cache on JDK 21, the CPU Pipeline bounds (instructions and cycles per operation) are exposed perfectly without RAM stall cycles.

| Tree Type   | Instructions/op | Cycles/op | L1 Loads/op |
|:------------|:----------------|:----------|:------------|
| **BST**     | 633             | 127       | 196         |
| **TreeSet** | 1,150           | 238       | 362         |
| **Treap**   | 1,038           | 161       | 274         |
| **RBT**     | 1,978           | 363       | 501         |
| **AVL**     | 3,046           | 472       | 753         |

---

## Conclusion

1. The standard, unbalanced BST dominates performance (58.4 ns/op) simply because it executes far fewer instructions per operation (633 vs. AVL's 3,046), proving that maintaining strict invariants is mathematically expensive.
2. The JDK 21 C2 compiler demonstrates a massive optimization for the Treap (dropping from 137ns to 68ns), successfully devirtualizing random number generation and tightly loop-unrolling the heap-priority rotations.
3. The AVL tree is confirmed as the most computationally expensive architecture (147.5 ns/op), executing nearly 5x the instructions of a BST to constantly measure depths and rotate paths.
4. In memory allocation (prof gc), the data validates that apart from unavoidable Integer auto-boxing (24B) and structural node creation (32B–40B), Escape Analysis perfectly scalar-replaces temporary search wrappers on BST and RBT.
5. The minor 16B Escape Analysis failure in AVL and Treap implies that specific rotation logic or depth-calculation metadata within those classes slightly exceeded C2's inlining thresholds during heavy mutation.

**Benchmark files are present in the Reports folder**
* [Test on Jdk 11](Reports/binaryJMH_jdk11_10K.txt)
* [Test on jdk 21](Reports/binaryJMH_jdk21_10K.txt)
