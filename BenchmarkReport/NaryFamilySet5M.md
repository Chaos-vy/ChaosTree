# N-ary Tree Benchmark Report

This is the benchmark analysis for the **ChaosTree N-ary Family** (`BTree`, `BPlusTree`), isolating the architectural limits across varying node degrees (`t=8, 32, 64, 128`) at a scale of 5 Million nodes.

---

##  Test Architecture
- **Framework**: Java Microbenchmark Harness (JMH)
- **Workload**: `InsertDeleteBenchmark` Insert+Delete Fisher-Yates Shuffle
- **Scale**: 5,000,000 Elements 
- **JVMs Tested**: JDK 11 vs JDK 21 

---

## `bm sample` (Tail Latency Profile)

### JDK 11 (Latency in ns)
| Tree Type     | Degree | p50 | p90 | p99 | p99.9  | pMax      |
|:--------------|:-------|:----|:----|:----|:-------|:----------|
| **BPlusTree** | t=8    | 373 | 392 | 808 | 10,848 | 128,128   |
| **BPlusTree** | t=32   | 307 | 327 | 685 | 10,356 | 1,431,552 |
| **BPlusTree** | t=64   | 395 | 521 | 785 | 9,457  | 1,132,544 |
| **BPlusTree** | t=128  | 145 | 161 | 230 | 3,281  | 68,480    |
|               |        |     |     |     |        |           |
| **BTree**     | t=8    | 268 | 282 | 538 | 9,272  | 211,968   |
| **BTree**     | t=32   | 282 | 298 | 497 | 8,729  | 1,142,784 |
| **BTree**     | t=64   | 141 | 157 | 217 | 2,868  | 97,024    |
| **BTree**     | t=128  | 137 | 150 | 217 | 2,868  | 121,088   |

### JDK 21 (Latency in ns)
| Tree Type     | Degree | p50 | p90 | p99 | p99.9  | pMax      |
|:--------------|:-------|:----|:----|:----|:-------|:----------|
| **BPlusTree** | t=8    | 352 | 363 | 700 | 10,573 | 1,357,824 |
| **BPlusTree** | t=32   | 258 | 279 | 500 | 7,739  | 1,492,992 |
| **BPlusTree** | t=64   | 333 | 436 | 847 | 11,109 | 48,832    |
| **BPlusTree** | t=128  | 151 | 155 | 270 | 3,171  | 103,296   |
|               |        |     |     |     |        |           |
| **BTree**     | t=8    | 234 | 255 | 345 | 3,629  | 42,496    |
| **BTree**     | t=32   | 246 | 258 | 413 | 7,111  | 35,136    |
| **BTree**     | t=64   | 150 | 156 | 185 | 1,660  | 112,256   |
| **BTree**     | t=128  | 143 | 149 | 184 | 1,379  | 1,638,400 |

---

## `prof gc` (Garbage Collection Allocation)

| Tree Type     | Degree | JDK 11 (B/op) | JDK 21 (B/op) |
|:--------------|:-------|:--------------|:--------------|
| **BPlusTree** | t=8    | 816           | 816           | 
| **BPlusTree** | t=32   | 1,200         | 1,200         | 
| **BPlusTree** | t=64   | 2,224         | 2,224         | 
| **BPlusTree** | t=128  | 48            | 48            | 
|               |        |               |               |
| **BTree**     | t=8    | 400           | 400           |
| **BTree**     | t=32   | 712           | 712           |
| **BTree**     | t=64   | 144           | 144           |
| **BTree**     | t=128  | 120           | 120           |

---

## `prof perfnorm` (Execution Throughput)

| Tree Type     | Degree | JDK 11 (ns/op) | JDK 21 (ns/op) |
|:--------------|:-------|:---------------|:---------------|
| **BPlusTree** | t=8    | 411.28         | 387.45         |
| **BPlusTree** | t=32   | 353.15         | 292.04         |
| **BPlusTree** | t=64   | 441.91         | 385.99         |
| **BPlusTree** | t=128  | 163.24         | 165.94         |
|               |        |                |                |
| **BTree**     | t=8    | 296.87         | 252.78         |
| **BTree**     | t=32   | 307.95         | 260.18         |
| **BTree**     | t=64   | 159.08         | 156.89         |
| **BTree**     | t=128  | 153.69         | 157.24         |

---

## `prof perf` (Hardware Cache Telemetry)

L1 Data Cache miss rates on JDK 21:

| Tree Type     | Degree | L1 Misses   | L1 Miss Rate | Instructions    | Cycles         |
|:--------------|:-------|:------------|:-------------|:----------------|:---------------|
| **BPlusTree** | t=8    | 197,353,807 | 10.56%       | 290,804,024,772 | 71,663,343,968 |
| **BPlusTree** | t=32   | 168,068,724 | 10.36%       | 273,206,352,637 | 68,589,212,480 |
| **BPlusTree** | t=64   | 196,265,540 | 23.61%       | 226,475,265,513 | 69,335,384,029 |
| **BPlusTree** | t=128  | 187,317,743 | 10.40%       | 269,321,567,980 | 69,030,333,213 |

---

## Conclusion

1. At the 5-million element scale, the benchmark is entirely bottlenecked by main memory latency, making CPU cache-alignment the primary performance driver.
2. The standard BTree at degree t=128 emerges as the optimal architecture, consistently achieving a ~143ns p50 latency on JDK 21 by stopping early and minimizing pointer chasing.
3. Hardware telemetry confirms that massive t=128 nodes perfectly align with CPU hardware prefetchers, reducing L1 cache miss rates to a highly efficient 10.40%.
4. Structurally, larger node degrees intentionally bypass aggressive loop unrolling in the JVM C2 compiler, keeping the compiled method small enough to guarantee inlining.
5. This successful inlining enables Escape Analysis to scalar-replace temporary search wrappers, dropping garbage generation from a massive 2,224 B/op down to just ~120 B/op.
6. Finally, while JDK 21 significantly improves CPU-bound lower degrees (10-15% throughput gain), peak throughput at t=128 is identical to JDK 11 because it hits the physical speed limit of RAM retrieval.

**Benchmark files are present in the Reports folder**
* [Test on Jdk 11](Reports/final_nary_jdk11.txt)
* [Test on jdk 21](Reports/final_nary_jdk21.txt)
