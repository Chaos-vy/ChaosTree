package chaos.tree21.nary;

import java.util.Arrays;
import java.util.Iterator;

public final class BTreeSet<E> extends AbstractNaryTreeSet<E, BTreeNode<E>> {


    /**
     * Reference used CLRS and day-dreaming structure also took the help with Gemini and ChatGPT
     * to understand the concept.They told about wavy curve way. to read the SQL lite code.
     * So what I got is, let's make up I will also draft ADR too.
     * we just have to fill the first leaf
     * Let's take an example target to fill only is 75% in my case
     * here for example I took it as 2 as targetkey.
     * I want to insert [10, 20, 30, 40, 50, 60, 70 ....]
     *                    L   L   R   L   L   R   L
     * A pattern can now be seen
     *              [ 30 ]           <-- rightEdge[1] (Root)
     *             /      \
     *         [10, 20]  [40, 50]     <-- rightEdge[0] (Leaf)
     * A same patter will be observed for the ht too as well when the root get's full
     * 60 is pulled. It's the routing key going into the parent (rightEdge[1]).
     * 70, 80 go into the new Leaf (rightEdge[0]).
     * The tree now looks like this:
     *              [ 30 , 60 ]             <-- rightEdge[1] is now FULL!
     *             /     |     \
     *      [10, 20] [40, 50] [70, 80]    <-- rightEdge[0] is FULL!
     * as I will put 90 it willl increase ht to +1 to 2.
     * TODO: Don't forgo parent linking.
     * Supported at creation from Constructor
     * The same way for B+Tree but I need to map routing key in diff way and also provide next and prev!!
     * The point by mentioning here of B+tree I am not gonna go provide any docs there it may be in comment too.
     * I want to insert [10, 20, 30, 40, 50, 60, 70 ....]
     *                    L  LR   L  LR   L  LR   L
     * A lecture can help you visualize Jenny's Lecture.
     */
    public void buildFromSorted(Iterator<? extends E> it, float fillFactor) {
        // Calculate the future mighty chaos target (e.g., 0.75 * 63 = 47 keys per node)
        /*
        Well lemme explain it. If I banged with 100% node capacity there will a tremendous trigger of merge and split node LOL
         */
        int targetKeys = Math.max(minKeys, (int) (maxKeys * fillFactor));
        // Track the right-most path of the tree (index 0 is the Leaf layer)
        @SuppressWarnings("unchecked")
        BTreeNode<E>[] rightEdge = new BTreeNode[64]; // 64 2,3,4 trees structure rest will never touch that depth LOL

        int height = 0;
        rightEdge[0] = createNode(degree, true);
        root = rightEdge[0];

        while (it.hasNext()) {
            BTreeNode<E> rightLeaf = rightEdge[0];

            if (rightLeaf.keyCount < targetKeys) {
                // 1. FAST APPEND: Shove the data into the leaf!
                rightLeaf.keys[rightLeaf.keyCount++] = it.next();
                size++;
            } else {
                // The VERY NEXT element acts as the routing key up above!
                E routingKey = it.next();
                size++;

                // Find the lowest level on the right edge that has room for the routing key
                int level = 1;
                while (level <= height && rightEdge[level].keyCount == targetKeys) {
                    level++;
                }
                // If I ran out of height grow the tree upwards! (New Root)
                if (level > height) {
                    height++;
                    BTreeNode<E> newRoot = createNode(degree, false);
                    newRoot.child[0] = rightEdge[height - 1]; // Link old root
                    rightEdge[height - 1].parent = newRoot;      // Set parent pointer!

                    rightEdge[height] = newRoot;
                    root = newRoot;
                }
                // Insert the routing key into the target level
                BTreeNode<E> targetNode = rightEdge[level];
                targetNode.keys[targetNode.keyCount++] = routingKey;
                // 3. REBUILD DOWNWARD: Create a fresh empty path down to the leaf layer
                for (int i = level - 1; i >= 0; i--) {
                    BTreeNode<E> newNode = createNode(degree, i == 0);
                    // Link it to the parent above it
                    rightEdge[i + 1].child[rightEdge[i + 1].keyCount] = newNode;
                    newNode.parent = rightEdge[i + 1]; // Set parent pointer!
                    // Update tracking array
                    rightEdge[i] = newNode;
                }
            }
        }

        // Optional: If the very last leaf didn't reach minKeys, the B-Tree rules technically
        // allow the right-most edge to be underfull immediately after a bulk load.
        // Future inserts will naturally fix it!
    }

    @Override
    public boolean add(E e) {
        if (root == null) {
            root = new BTreeNode<>(degree, true);
            root.keys[0] = e;
            root.keyCount++;
            size++;
            modCount++;
            return true;
        }

        BTreeNode<E> current = root;

        while (true) {
            int idx = searchNode(current, e);

            // B-TREE RULE: Any exact match, anywhere in the tree, is a duplicate!
            if (idx >= 0) {
                return false;
            }
            int childIdx = ~idx;

            if (current.isLeaf()) {
                /*
                 1. INLINED LEAF INSERTION
                 No checking the leaf is full prior to it
                 */
                System.arraycopy(current.keys, childIdx, current.keys, childIdx + 1, current.keyCount - childIdx);
                current.keys[childIdx] = e;
                current.keyCount++;
                size++;
                modCount++;
                /*
                 2. ITERATIVE BUBBLE-UP
                 Now here I designed to catch the overflow
                 */
                while (current.keyCount > maxKeys) {
                    if (current == root) {
                        BTreeNode<E> newRoot = new BTreeNode<>(degree, false);
                        newRoot.setChild(0, root);
                        splitNode(newRoot, 0, root);
                        root = newRoot;
                        break;
                    }
                    BTreeNode<E> parent = current.parent;
                    /*
                    Since I have completely made the new node I need to also map with the parent node
                     */
                    @SuppressWarnings("unchecked")
                    E eval = (E) current.keys[0];
                    int pIdx = ~searchNode(parent, eval);
                    splitNode(parent, pIdx, current);
                    current = parent; // Move UP
                }
                return true;
            }
            current = current.child[childIdx];
        }
    }

    private void splitNode(BTreeNode<E> parent, int childIdx, BTreeNode<E> child) {
        BTreeNode<E> sibling = new BTreeNode<>(degree, child.isLeaf());
        sibling.keyCount = degree - 1;
        // Shift right-half keys
        System.arraycopy(child.keys, degree, sibling.keys, 0, degree - 1);
        // Shift right-half child and update parent pointers
        if (!child.isLeaf()) {
            System.arraycopy(child.child, degree, sibling.child, 0, degree);
            for (int i = 0; i < degree; i++) {
                if (sibling.child[i] != null) sibling.child[i].parent = sibling;
            }
            // GC Cleanup
            Arrays.fill(child.child, degree, child.keyCount + 1, null);
        }
        Arrays.fill(child.keys, degree, child.keyCount, null);
        child.keyCount = degree - 1;

        // Shift parent arrays to make room
        System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
        parent.setChild(childIdx + 1, sibling); // This auto-sets sibling.parent = parent!

        System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);

        // Push the middle key UP and DELETE it from the child!
        parent.keys[childIdx] = child.keys[degree - 1];
        child.keys[degree - 1] = null;

        parent.keyCount++;
    }

    private void borrowLeft(BTreeNode<E> parent, int childIdx, BTreeNode<E> sibling, BTreeNode<E> starving) {
        // Shift starving node's keys & child RIGHT by 1 to make space at index 0
        System.arraycopy(starving.keys, 0, starving.keys, 1, starving.keyCount);
        if (!starving.isLeaf()) {
            System.arraycopy(starving.child, 0, starving.child, 1, starving.keyCount + 1);
        }

        // Pull the Parent's routing key DOWN into the starving node's 0 index
        starving.keys[0] = parent.keys[childIdx - 1];

        // Move the Sibling's largest child over to the starving node
        if (!starving.isLeaf()) {
            starving.child[0] = sibling.child[sibling.keyCount];
            if (starving.child[0] != null) {
                starving.child[0].parent = starving;
            }
            sibling.child[sibling.keyCount] = null; // GC
        }

        // Push the Sibling's largest key UP to the parent
        parent.keys[childIdx - 1] = sibling.keys[sibling.keyCount - 1];
        sibling.keys[sibling.keyCount - 1] = null; // GC

        //  Update counts
        sibling.keyCount--;
        starving.keyCount++;
    }

    private void borrowRight(BTreeNode<E> parent, int childIdx, BTreeNode<E> starving, BTreeNode<E> sibling) {
        //  Pull the Parent's routing key DOWN into the end of the starving node
        starving.keys[starving.keyCount] = parent.keys[childIdx];

        //  Move the Sibling's smallest child over to the end of the starving node
        if (!starving.isLeaf()) {
            starving.child[starving.keyCount + 1] = sibling.child[0];
            if (starving.child[starving.keyCount + 1] != null) {
                starving.child[starving.keyCount + 1].parent = starving;
            }
        }

        // Push the Sibling's smallest key UP to the parent
        parent.keys[childIdx] = sibling.keys[0];

        // Shift sibling's keys & child LEFT by 1 to fill the gap
        System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyCount - 1);
        sibling.keys[sibling.keyCount - 1] = null; // GC

        if (!sibling.isLeaf()) {
            System.arraycopy(sibling.child, 1, sibling.child, 0, sibling.keyCount);
            sibling.child[sibling.keyCount] = null; // GC
        }

        // Update counts
        starving.keyCount++;
        sibling.keyCount--;
    }

    private BTreeNode<E> getPredecessorLeaf(BTreeNode<E> node, int childIdx) {
        BTreeNode<E> current = node.child[childIdx]; // Go left once
        while (!current.isLeaf()) {
            current = current.child[current.keyCount]; // Go right all the way down
        }
        return current;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (root == null) return false;

        BTreeNode<E> current = root;
        int idx = -1;

        E e = (E) o;
        while (true) {
            idx = searchNode(current, e);
            if (idx >= 0) break; // Found it!

            if (current.isLeaf()) return false; // Key does not exist
            current = current.child[~idx];
        }

        if (!current.isLeaf()) {
            BTreeNode<E> predLeaf = getPredecessorLeaf(current, idx);

            E predKey = (E) predLeaf.keys[predLeaf.keyCount - 1];
            current.keys[idx] = predKey;

            current = predLeaf;
            idx = current.keyCount - 1;
        }
        System.arraycopy(current.keys, idx + 1, current.keys, idx, current.keyCount - idx - 1);
        current.keys[current.keyCount - 1] = null; // GC Cleanup
        current.keyCount--;
        size--;
        modCount++;
        while (current != root && current.keyCount < minKeys) {
            BTreeNode<E> parent = current.parent;

            // Find which child index 'current' represents in the parent
            int childIdx = 0;
            while (childIdx <= parent.keyCount && parent.child[childIdx] != current) {
                childIdx++;
            }

            BTreeNode<E> leftSibling = (childIdx > 0) ? parent.child[childIdx - 1] : null;
            BTreeNode<E> rightSibling = (childIdx < parent.keyCount) ? parent.child[childIdx + 1] : null;

            // Trying the  borrowing first!
            if (leftSibling != null && leftSibling.keyCount > minKeys) {
                borrowLeft(parent, childIdx, leftSibling, current);
                break; // Rebalance complete!
            } else if (rightSibling != null && rightSibling.keyCount > minKeys) {
                borrowRight(parent, childIdx, current, rightSibling);
                break; // Rebalance complete!
            } else {
                // Must Merge (Smasher)
                if (leftSibling != null) {
                    mergeNodes(parent, childIdx - 1, leftSibling, current);
                } else {
                    mergeNodes(parent, childIdx, current, rightSibling);
                    // The parent lost a key, so underflow bubbles UP!
                    current = parent;
                }
            }
        }
        if (root.keyCount == 0) {
            if (root.isLeaf()) {
                root = null; // The tree is now completely empty
            } else {
                root = root.child[0];
                root.parent = null; // Drop the old root to the GC
            }
        }

        return true;
    }

    private void mergeNodes(BTreeNode<E> parent, int childIdx, BTreeNode<E> left, BTreeNode<E> right) {
        // Pull the parent's routing key DOWN into the middle of the left node
        left.keys[left.keyCount++] = parent.keys[childIdx];

        // merge the right node's keys into the left node
        System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);

        // merge the right node's children into the left node only for non-leaf
        if (!left.isLeaf()) {
            System.arraycopy(right.child, 0, left.child, left.keyCount, right.keyCount + 1);
            // Update the parent pointers for the children I just moved!
            for (int i = 0; i <= right.keyCount; i++) {
                if (right.child[i] != null) right.child[i].parent = left;
            }
        }

        left.keyCount += right.keyCount;

        // Shifting the parent's keys and children LEFT by 1 to close the gap left by the routing key
        System.arraycopy(parent.keys, childIdx + 1, parent.keys, childIdx, parent.keyCount - childIdx - 1);
        parent.keys[parent.keyCount - 1] = null; // Clean up GC

        System.arraycopy(parent.child, childIdx + 2, parent.child, childIdx + 1, parent.keyCount - childIdx - 1);
        parent.child[parent.keyCount] = null; // Clean up GC

        parent.keyCount--;
    }
}