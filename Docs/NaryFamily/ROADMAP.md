
3:34 PM
markdown
# NaryFamily Node Design: Array over ArrayList

## Decision
`BNaryNode` uses raw `Object[]` and `BNaryNode<T>[]` instead of `ArrayList<T>`
and `ArrayList<BNaryNode<T>>` for keys and children storage.

---

## The ArrayList Heap Problem

Every `ArrayList` creation results in **5 heap allocations per node**:
new BNaryNode() → alloc 1: node object
new ArrayList<>(keys) → alloc 2: ArrayList object
└── new Object[capacity] → alloc 3: backing array
new ArrayList<>(children) → alloc 4: ArrayList object
└── new Object[capacity] → alloc 5: backing array


For a BTree with 1M keys at order 5 (~200k nodes):
200,000 nodes × 5 allocs = 1,000,000 heap allocations


GC pressure scales linearly with node count. At high order and large n,
this becomes a measurable throughput bottleneck.

---

## The Growth Factor Problem

`ArrayList` default capacity is 10, growth factor is 1.5×:
10 → 15 → 22 → 33 → 49 → 73...


For BTree of order 5:
max keys = 4
max children = 5

ArrayList capacity = 10 → 6 slots wasted per keys list
5 slots wasted per children list


For BTree of order 50:
max keys = 49
ArrayList grows: 10 → 15 → 22 → 33 → 49 → 73
↑
first fit
but 73 allocated → 24 slots wasted


BTree nodes NEVER exceed `order - 1` keys before splitting.
ArrayList's growth mechanism is architecturally incompatible with
BTree's fixed-capacity node invariant — it allocates memory that
will never be used.

---

## The Pointer Overhead Problem

`ArrayList<T>` stores boxed references — every element is a pointer
to a heap object:
ArrayList<Integer> keys
→ Object[] backing array
→ [ref] → Integer(10) heap object
→ [ref] → Integer(20) heap object
→ [ref] → Integer(30) heap object


Each element access = pointer dereference = potential cache miss.

Raw `Object[]` stores references in contiguous memory:
Object[] keys = [10, 20, 30, null, null]
↑
contiguous, CPU prefetch friendly


No extra indirection. CPU loads the entire array into a cache line
in one prefetch. ArrayList adds an extra layer of indirection
(ArrayList object → backing array → elements) that raw arrays avoid.

---

## The Fix: Raw Arrays with Exact Capacity

```java
class BNaryNode<T extends Comparable<T>> {
    final Object[] keys;                  // exact: order - 1
    final BNaryNode<T>[] children;        // exact: order
    int numKeys;                          // tracks used slots
    boolean isLeaf;                       // O(1) leaf check
    BNaryNode<T> parent;

    @SuppressWarnings("unchecked")
    BNaryNode(int order) {
        this.keys     = new Object[order - 1];
        this.children = (BNaryNode<T>[]) new BNaryNode[order];
        this.numKeys  = 0;
        this.isLeaf   = true;
        this.parent   = null;
    }
}
```

Heap allocations per node:
new BNaryNode() → alloc 1: node object
new Object[] → alloc 2: keys array
new BNaryNode[] → alloc 3: children array


3 allocations instead of 5. Exact capacity — zero waste.
No growth factor. No pointer indirection layer.

---

## Shift Cost: System.arraycopy

The primary concern with arrays over ArrayList is manual shifting
on insert and delete. This is not a real cost:

```java
// insert key at index
System.arraycopy(keys, index, keys, index + 1, numKeys - index);
keys[index] = value;
numKeys++;
```

`System.arraycopy` is a JVM intrinsic — compiled to a single native
`memcpy` call. The JVM does not interpret it as a loop.
System.arraycopy ~1-2 ns single native memcpy
for loop ~5-10 ns interpreted JVM bytecode


ArrayList.add(index, element) uses System.arraycopy internally.
Shift cost is identical. The raw array removes the ArrayList wrapper
overhead with no shift cost penalty.

---

## isLeaf Flag

```java
// before — method call on every check
children.isEmpty()   // ArrayList method call

// after — direct field read  
node.isLeaf          // single boolean, zero overhead
```

Every search, insert, and delete traversal checks leaf status
at every node. A direct boolean field read outperforms a method
call on a hot path.

**Invariant:** `isLeaf` is `true` at construction.
Set to `false` when first child is assigned during split.
Never allowed to drift from actual child count.

---

## Summary

| Factor              | ArrayList         | Raw Array          |
|---------------------|-------------------|--------------------|
| Heap allocs/node    | 5                 | 3                  |
| Capacity            | 10 → 1.5x growth  | exact (order-1 / order) |
| Wasted slots        | yes               | zero               |
| Pointer indirection | 2 layers          | 1 layer            |
| Shift cost          | System.arraycopy  | System.arraycopy   |
| Leaf check          | isEmpty() call    | boolean field read |
| GC pressure         | high at scale     | minimal            |

Raw arrays are architecturally correct for BTree nodes.
The fixed-capacity invariant of BTree maps directly to
fixed-size arrays. ArrayList's dynamic growth mechanism
exists to solve a problem BTree nodes do not have.