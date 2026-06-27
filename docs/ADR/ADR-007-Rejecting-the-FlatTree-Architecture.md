# ADR-007: flatTree Retired

Very early in my research phase, I tried building a 'Flat Tree'. Instead of nodes holding actual memory pointers (`left`, `right`) to their children, the entire binary tree was mapped perfectly onto a single, contiguous 1D array (where the root is at index 0, the left child at $2i+1$, and the right child at $2i+2$).

**Decision:**  
The Flat Tree architecture was permanently retired and abandoned.

CPU hardware prefetchers absolutely love linear arrays. But binary search trees require constant balancing (rotations) to keep their math working. If the tree is stored in an array, rotating a subtree means I have to mathematically recalculate the index of every single descendant node and execute massive `System.arraycopy` shifts. 

It completely destroys the $O(1)$ rotation guarantee. A simple, fast balancing operation turns into an $O(n)$ array-shifting nightmare. On top of that, to pre-allocate an array for an AVL tree, I had to calculate the worst-case sparse tree height—meaning I had to reserve gigabytes of contiguous RAM just to hold a few thousand elements.

**Tradeoffs:**  
* **Pros:** Abandoning the flat array means I lose perfect L1 cache locality and incur the cost of pointer-chasing (cache misses) during tree traversal.
* **Cons (of Flat Tree):** Exponentially large contiguous memory allocations, $O(n)$ rotation costs, index invalidation on every mutation.
* **Resolution:** The standard pointer-based node architecture (`left`, `right`) was retained for the Binary Family to ensure mathematically perfect $O(1)$ rotations. 
* **Note:** To satisfy the desire for cache-locality, the **N-ary Family** (`BTree`, `BPlusTree`) was built. N-ary trees bridge this gap, offering the array-locality of flat trees via internal block arrays, while maintaining pointer-based $O(1)$ splits to avoid $O(n)$ shifting.

**Consequences:**  
All tree engines in the ChaosTree framework use pointer-based node structures.
