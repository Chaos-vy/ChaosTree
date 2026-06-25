# ADR-010: SearchResult & NodeSearchResult — Single-Pass Traversal Records

In both our Binary and N-ary trees, we often need multiple derived values from a single traversal. 

The naive way to do this is to execute one traversal per query—one for `contains()`, a second for `floor()`, a third for `ceil()`. Each traversal costs us an $O(\log n)$ pointer-chase. If we call all three independently, we literally triple our cache misses for absolutely zero algorithmic gain.

---

## Binary Family: `SearchResult<T>(boolean contains, T floor, T ceil)`

We run a single top-down pass through `searchHelper()` which yields a `SearchResult<T>` carrying all three values at once: whether we found the exact value, the floor candidate, and the ceiling candidate.

**Rationale:**  
When you think about a standard BST descent, we are making a left or right decision at every single node. Every time we turn left, the current node is larger than our target, making it a potential ceiling. Every time we turn right, the current node is smaller than our target, making it a potential floor. 

We just track these candidates as local variables during our single pass and capture them when we terminate. This information is completely free—it costs us literally zero extra comparisons.

```java
while (node != null) {
    int cp = value.compareTo(node.getValue());
    if (cp == 0) return new SearchResult<>(true, node.getValue(), node.getValue());
    if (cp > 0) { floor = node.getValue(); node = node.getRight(); }
    else        { ceil  = node.getValue(); node = node.getLeft();  }
}
return new SearchResult<>(false, floor, ceil);
```

`contains()`, `floor()`, and `ceil()` all call `search()` and each reads the relevant field from the single result — one traversal, three derived answers.

**Tradeoffs:**
* **Pros:** It totally eliminates redundant traversals. We get our floor/ceil candidates completely for free just by observing which direction we're descending.
* **Cons:** Technically, we're allocating a `SearchResult` record per query. And callers who only want `contains()` are still paying the tiny CPU cost of tracking floor/ceil variables.
* **Resolution:** Again, the JVM's Escape Analysis swoops in and scalar-replaces that short-lived `SearchResult` record straight into CPU registers. The tracking variables (`floor`, `ceil`) just live on the stack during the traversal—they cost us absolutely nothing.

---

## N-ary Family: `NodeSearchResult(boolean found, int index)`

**Decision:**  
Inside our N-ary nodes, keys are stored in a sorted `Object[]` array. By running a single binary search pass through `searchNode()`, we return a `NodeSearchResult(boolean found, int index)` that answers two questions at once.

**Rationale — The Binary Search Invariant:**  
We built this record to exploit a really cool mathematical invariant of binary search termination. When the `while` loop finishes:
* **If it found the key:** `index` is the exact array position of the key.
* **If it didn't find the key:** `index` is the final resting value of the `left` pointer. By mathematical guarantee, that `left` pointer is ALWAYS the exact child routing index we need to descend into on the next level.

```java
while (left <= right) {
    int mid = left + (right - left) / 2;
    int cmp = key.compareTo(node.getKey(mid));
    if (cmp == 0) return new NodeSearchResult(true, mid);
    if (cmp > 0) left = mid + 1;
    else right = mid - 1;
}
return new NodeSearchResult(false, left);  // left IS the child routing index
```

This means a **single binary search simultaneously answers two questions:**
1. *"Is this key inside this node?"* → `found`
2. *"Which child do I descend into if it is not?"* → `index` (the `left` pointer invariant)

By returning both the search outcome and the final insertion/routing index together, every operation can reuse the result of a single binary search rather than repeating the search for the child index.

**Tradeoffs:**
* **Pros:** It instantly halves the number of internal binary searches across every operation in the N-ary engine.
* **Cons:** The dual meaning of the `index` variable (found-position vs. routing-index) can be really confusing for someone who isn't familiar with binary search's `left` pointer invariant. A future maintainer might look at `found=false` and wonder why we're returning an `index` at all.
* **Resolution:** We explicitly documented this mathematical invariant in the Javadoc for `searchNode()`.

**Consequences:**  
Our N-ary engine calls `searchNode()` exactly once per level, period. Every single derived index we need flows perfectly out of that single binary search.
