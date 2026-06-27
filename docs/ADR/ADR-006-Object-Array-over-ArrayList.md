# ADR-006: Object[] over ArrayList in N-ary Engine

Every N-ary tree node (`BTree` and `BPlusTree`) stores its keys and child references in fixed-capacity contiguous arrays inside it to hold its data elements and child references. This collection has to be able to maintain sorted order by shifting contiguous elements during insertions and deletions.

I completely avoided using Java's standard `java.util.ArrayList`. Instead, I dropped down to using raw `Object[]` arrays with exact bounds, relying heavily on `System.arraycopy` to move memory around.

Using `ArrayList` would have made my code much easier to write because it handles array shifting automatically. But `ArrayList` uses a dynamic growth strategy—when it gets full, it allocates an array 1.5x the size. 

In an N-ary tree, nodes are already mathematically required to be up to 50% empty. If I wrapped my internal data in an `ArrayList`, I would be compounding the B-Tree's mathematically required empty space with `ArrayList`'s hidden spare capacity. At scale (like 500 million elements), that compounds into literally gigabytes of wasted RAM. Using a raw `Object[]` array sized perfectly to `2t` gives me rigid, predictable memory boundaries.

**Tradeoffs:**  
* **Pros:** Unmatched heap density. I get complete control over memory alignment, and I get hardware-accelerated memory shifting thanks to `System.arraycopy`.
* **Cons:** The internal code is visually complex and hard to maintain. I have to manually track index boundaries, wrestle with Java's generic type erasure (`(T) elements[i]`), and remember to manually `null` out trailing array slots after a deletion so I don't cause memory leaks.
* **Resolution:** I accepted the additional implementation complexity in exchange for predictable memory usage and complete control over node layout. This decision was later validated by my heap saturation and benchmark results, which demonstrated substantially higher memory density for the N-ary engine than the Binary family.

**Consequences:**  
N-ary trees achieve absolute theoretical memory efficiency at the cost of dense, manual index-manipulation code.
