# ADR-009: BiNode vs ParentBiNode Split (Parent Pointer Separation)

While I was building the Red-Black Tree (`RBT`), I hit a wall with the deletion algorithm. The fixup routine (`fixDoubleBlack`) requires me to traverse *upward*—from a node back to its parent, and then to its grandparent. 

My BST and AVL trees never needed this. They only ever descend, using the recursive call stack to implicitly track their way back up as the stack unwinds. 

But RBT can't do that. Its fixup routine needs to find the *sibling* of a node, and to find a sibling, you must know the parent. There's no clever call-stack trick that substitutes for having an explicit parent reference when you're jumping around during a non-recursive fixup. The exact same issue applied to my `Splay` tree—its zig-zig and zig-zag rotations require parent awareness.

I split ChaosTree's node hierarchy into two completely separate branches:
* `BiNode<T, N>` — No parent pointer. Used by `BST`, `AVL`, and `Treap`.
* `ParentBiNode<T, N>` — Carries an explicit parent reference. Used by `RBT` and `Splay`.

I could have just added a `parent` field to the base node and left it `null` for AVL and BST. But doing so would waste an incredible amount of memory. 

In a tree of 126 million nodes, keeping an unused 8-byte parent pointer on every single node costs about 1 GB of wasted heap space. By splitting the node hierarchy, I eliminate that waste entirely. An AVL tree pays exactly zero memory cost for a pointer it will never use.

Node byte sizes confirmed via GC profiling (`-prof gc`):

| Tree          | Bytes/Node |
|---------------|:----------:|
| BST           | 24         |
| AVL           | 32         |
| RBT           | 32         |
| Splay         | 32         |
| Treap         | 32         |
| `TreeSet` (ref) | 40       |

That 8-byte jump between BST (24 bytes) and the balanced variants (32 bytes) is exactly the object header plus their required algorithm-specific field (`height`, `color`, or `priority`). I know exactly where every single byte is going.

* **Pros:** Zero memory overhead for trees that only go down. Every single byte allocated in this library is explicitly justified by algorithm math.
* **Cons:** It forces me to duplicate some abstract logic. I have `AbstractRotateTree` and `AbstractParentRotateTree` sitting side-by-side doing almost the exact same rotations, just because one has to rewire parent pointers and the other doesn't.
* **Resolution:** That code duplication is very narrow and completely isolated. It's the explicit, one-time engineering tax I pay to save gigabytes of RAM across the entire user base.

**Consequences:**  
I have a meticulously validated hierarchy where every single field in every single node is provably load-bearing. The structural overhead is minimal and fully accounted for.
