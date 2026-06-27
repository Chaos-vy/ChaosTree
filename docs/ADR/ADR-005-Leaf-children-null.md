# ADR-005: Leaf children = null (N-ary Node Memory Paradox)

When I started building my high-performance N-ary trees (`BTree`, `BPlusTree`), I ran into a massive architectural paradox. B-Tree nodes are huge—they often hold hundreds of keys. Leaf nodes sit at the bottom of the tree and, by definition, have absolutely zero children. 

I found ourselves in a three-way standoff between pure Object-Oriented Programming (OOP), CPU cache speed, and heap memory efficiency.

## The Decision

I explicitly chose to go with **Pseudo-Polymorphism via Lazy Allocation**. If a node is a leaf, I just set `this.children = null` and pass an `isLeaf` boolean into the constructor.

**Rationale:**  
I looked at three different ways to build these nodes:

1. **Path A: True OOP Polymorphism (The "Clean" Code):** I could create an `NaryNode` interface and implement separate `InternalNode` and `LeafNode` classes.
   * *Why I rejected it:* Having two totally different node hierarchies means my tree algorithms would have to constantly figure out which type of node they are looking at. It breaks the uniform, compact memory layout I need for speed.
2. **Path B: The Monolithic Node (The "Database" Tax):** I could use one single class and just allocate a massive `new NaryNode[maxChildren]` array for *every* node.
   * *Why I rejected it:* In a B-Tree, the vast majority of nodes live at the leaf level. If I allocated child arrays for leaves that will never use them, I'd be wasting gigabytes of RAM.
3. **Path C: Pseudo-Polymorphism (The Winner):** I use a single monolithic class, but an `isLeaf` boolean dictates whether the children array is actually allocated, or just left as an 8-byte `null` reference. I get the lightning-fast logic pipeline of Path B, with the strict memory footprint of Path A.

*(Side note: Real C++ databases like InnoDB and Postgres allocate raw 4KB memory blocks. Internal nodes use that space for child pointers, while leaf nodes ignore the pointers and just pack in more data. While Java object layouts are very different from raw database pages, my lazy allocation follows the exact same principle: never reserve memory for something a leaf node will never use.)*

**Tradeoffs:**  
* **Pros:** Massive heap memory savings. 
* **Cons:** Every traversal algorithm must explicitly check `if (!node.isLeaf)` before touching the `children` array. I intentionally violated pure OOP principles by forcing a class to hold a field it might never use.
* **Resolution:** The tiny CPU cost of one `if` branch is absolutely worth the gigabytes of memory I save by dropping those unused arrays.

**Consequences:**  
My N-ary nodes are incredibly memory-dense. ChaosTree can pack millions of elements into surprisingly small RAM footprints.

## Rejected Alternatives

### Separate LeafNode and InternalNode Hierarchies

As mentioned in Path A, I could have built separate classes for leaf and internal nodes. I rejected this because it just inflates my implementation complexity. I'd still have to write special logic to handle leaves anyway, so splitting the classes didn't actually buy me anything.

### Child Array Allocation

I rejected this because allocating arrays that are mathematically guaranteed to remain empty is just reckless memory management.

### Unsafe or Off-Heap Memory Layouts

I briefly thought about dropping down into `Unsafe`, `MemorySegment`, or raw off-heap memory pages. I decided against it because it falls outside the scope of ChaosTree's design goals. I want this library to be portable, safe, and maintainable standard Java.

