package chaos.tree21.nary;

import java.util.Arrays;
/*
I prioritize sometime DOD over OOD
 */
public final class BPlusTreeSet<E> extends AbstractNaryTreeSet<E, BPlusTreeNode<E>>{

    /*
    Yeah, a self varName. As the name suggest this Compaction count only works during deletion
    case. Only and Only if the key was found to be in route else no!!
     */
    private transient int chaosCompaction = 0; // Tracks ghost routing keys

    public float getCompactionRatio() {
        if (size == 0) return 0.0f;
        return (float) chaosCompaction / size;
    }

    @Override
    public boolean add(E e) {
        if (root == null) {
            root = createNode(degree, true);
            root.keys[0] = e;
            root.keyCount = 1;
            size++;
            modCount++;
            return true;
        }

        BPlusTreeNode<E> current = root;

        while (true) {
            int idx = searchNode(current, e);

            if (current.isLeaf()) {
                // B+TREE RULE: Only reject duplicates if we are physically at the Leaf!
                if (idx >= 0) return false;

                int insertIdx = ~idx;
                System.arraycopy(current.keys, insertIdx, current.keys, insertIdx + 1, current.keyCount - insertIdx);
                current.keys[insertIdx] = e;
                current.keyCount++;
                size++;
                modCount++;

                while (current.keyCount > maxKeys) {
                    if (current == root) {
                        BPlusTreeNode<E> newRoot = createNode(degree, false);
                        newRoot.setChild(0, root);
                        splitNode(newRoot, 0, root);
                        root = newRoot;
                        break;
                    }

                    BPlusTreeNode<E> parent = current.parent;
                    @SuppressWarnings("unchecked")
                    int pIdx = ~searchNode(parent, (E) current.keys[0]);
                    splitNode(parent, pIdx, current);
                    current = parent; // Move UP
                }
                return true;
            }

            // ROUTE DOWN
            // If it's an exact match in an internal node (idx >= 0), follow the right child (idx + 1)
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            current = current.child[childIdx];
        }
    }

    private void splitNode(BPlusTreeNode<E> parent, int childIdx, BPlusTreeNode<E> child) {
        BPlusTreeNode<E> sibling = createNode(degree, child.isLeaf());
        if (child.isLeaf()) {
            sibling.keyCount = degree;
            // Shift right-half keys (degree keys) to sibling
            System.arraycopy(child.keys, degree, sibling.keys, 0, degree);
            // GC Cleanup TODO: in Btree set: done
            Arrays.fill(child.keys, degree, child.keyCount, null);
            child.keyCount = degree;
            //  Wire up the next and prev pointer!!
            BPlusTreeNode<E> childNext = child.getNext();
            sibling.setNext(childNext);
            if (childNext != null) {
                childNext.setPrev(sibling);
            }
            sibling.setPrev(child); // Sibling points back to child
            child.setNext(sibling); // Child points forward to sibling
            //  Shift parent arrays
            System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
            parent.setChild(childIdx + 1, sibling);
            System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);
            // Push a COPY of the sibling's first key up as the Routing Key!
            parent.keys[childIdx] = sibling.keys[0];

        } else {
            sibling.keyCount = degree - 1;
            System.arraycopy(child.keys, degree, sibling.keys, 0, degree - 1);

            System.arraycopy(child.child, degree, sibling.child, 0, degree);
            for (int i = 0; i < degree; i++) {
                if (sibling.child[i] != null) sibling.child[i].parent = sibling;
            }
            //clearing GC!!
            Arrays.fill(child.child, degree, child.keyCount + 1, null);
            Arrays.fill(child.keys, degree, child.keyCount, null);

            child.keyCount = degree - 1;

            System.arraycopy(parent.child, childIdx + 1, parent.child, childIdx + 2, parent.keyCount - childIdx);
            parent.setChild(childIdx + 1, sibling);

            System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);
            // Push middle key UP and DELETE it from the child!
            parent.keys[childIdx] = child.keys[degree - 1];
            child.keys[degree - 1] = null;

        }
        parent.keyCount++;
    }
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (root == null) return false;

        BPlusTreeNode<E> current = root;
        E e = (E) o;
        boolean createsGhost = false;
        while (!current.isLeaf()) {
            int idx = searchNode(current, e);
            createsGhost = idx >= 0;
            int childIdx = (idx >= 0) ? idx + 1 : ~idx;
            current = current.child[childIdx];
        }

        //SEARCH THE LEAF
        int idx = searchNode(current, e);
        if (idx < 0) return false; // Key does not exist

        /*
        GHOST DELETE IN THE LEAF
        means I do not go up traversing deleting the route key.
         */
        System.arraycopy(current.keys, idx + 1, current.keys, idx, current.keyCount - idx - 1);
        current.keys[current.keyCount - 1] = null;
        current.keyCount--;
        size--;
        modCount++;

        // If I deleted the 0th index, it was likely acting as a routing key higher up!
        // We leave the routing key alone (Ghost Delete) but flag the compaction engine!
        if (createsGhost) chaosCompaction++;


        // 4. REBALANCE PHASE (Bottom-Up)
        while (current != root && current.keyCount < minKeys) {
            BPlusTreeNode<E> parent = current.parent;

            int childIdx = 0;
            while (childIdx <= parent.keyCount && parent.child[childIdx] != current) {
                childIdx++;
            }

            BPlusTreeNode<E> leftSibling = (childIdx > 0) ? parent.child[childIdx - 1] : null;
            BPlusTreeNode<E> rightSibling = (childIdx < parent.keyCount) ? parent.child[childIdx + 1] : null;

            if (leftSibling != null && leftSibling.keyCount > minKeys) {
                borrowLeft(parent, childIdx, leftSibling, current);
                break;
            }
            else if (rightSibling != null && rightSibling.keyCount > minKeys) {
                borrowRight(parent, childIdx, current, rightSibling);
                break;
            }
            else {
                if (leftSibling != null) {
                    mergeNodes(parent, childIdx - 1, leftSibling, current);
                    current = parent;
                } else {
                    mergeNodes(parent, childIdx, current, rightSibling);
                    current = parent;
                }
            }
        }

        if (root.keyCount == 0) {
            if (root.isLeaf()) root = null;
            else {
                root = root.child[0);
                root.parent = null;
            }
        }

        return true;
    }
    private void mergeNodes(BPlusTreeNode<E> parent, int childIdx, BPlusTreeNode<E> left, BPlusTreeNode<E> right) {
        if (left.isLeaf()) {
            // LEAF MERGE
            System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);
            left.keyCount += right.keyCount;

            // SAFELY REPAIR THE DOUBLY-LINKED LIST!
            BPlusTreeNode<E> rightNext = right.getNext();
            left.setNext(rightNext);
            if (rightNext != null) {
                rightNext.setPrev(left);
            }

            // Shift parent arrays left to delete the routing key
            System.arraycopy(parent.keys, childIdx + 1, parent.keys, childIdx, parent.keyCount - childIdx - 1);
            parent.keys[parent.keyCount - 1] = null;

            System.arraycopy(parent.child, childIdx + 2, parent.child, childIdx + 1, parent.keyCount - childIdx - 1);
            parent.child[parent.keyCount] = null;

            parent.keyCount--;
        }
        else {
            // INTERNAL NODE MERGE (Exactly like B-Tree)
            left.keys[left.keyCount] = parent.keys[childIdx];
            left.keyCount++;

            System.arraycopy(right.keys, 0, left.keys, left.keyCount, right.keyCount);
            System.arraycopy(right.child, 0, left.child, left.keyCount, right.keyCount + 1);
            for (int i = 0; i <= right.keyCount; i++) {
                if (right.child[i] != null) right.child[i].parent = left;
            }
            left.keyCount += right.keyCount;

            System.arraycopy(parent.keys, childIdx + 1, parent.keys, childIdx, parent.keyCount - childIdx - 1);
            parent.keys[parent.keyCount - 1] = null;
            System.arraycopy(parent.child, childIdx + 2, parent.child, childIdx + 1, parent.keyCount - childIdx - 1);
            parent.child[parent.keyCount] = null;
            parent.keyCount--;
        }
    }

    private void borrowLeft(BPlusTreeNode<E> parent, int childIdx, BPlusTreeNode<E> sibling, BPlusTreeNode<E> starving) {
        if (starving.isLeaf()) {
            System.arraycopy(starving.keys, 0, starving.keys, 1, starving.keyCount);
            starving.keys[0] = sibling.keys[sibling.keyCount - 1];
            sibling.keys[sibling.keyCount - 1] = null;
            parent.keys[childIdx - 1] = starving.keys[0];

            sibling.keyCount--;
            starving.keyCount++;
        }
        else {
            System.arraycopy(starving.keys, 0, starving.keys, 1, starving.keyCount);
            System.arraycopy(starving.child, 0, starving.child, 1, starving.keyCount + 1);

            starving.keys[0] = parent.keys[childIdx - 1];

            starving.child[0] = sibling.child[sibling.keyCount];
            if (starving.child[0] != null) starving.child[0].parent = starving;
            sibling.child[sibling.keyCount] = null;

            parent.keys[childIdx - 1] = sibling.keys[sibling.keyCount - 1];
            sibling.keys[sibling.keyCount - 1] = null;

            sibling.keyCount--;
            starving.keyCount++;
        }
    }
    private void borrowRight(BPlusTreeNode<E> parent, int childIdx, BPlusTreeNode<E> starving, BPlusTreeNode<E> sibling) {
        if (starving.isLeaf()) {
            starving.keys[starving.keyCount] = sibling.keys[0];
            System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyCount - 1);
            sibling.keys[sibling.keyCount - 1] = null;
            parent.keys[childIdx] = sibling.keys[0];

            starving.keyCount++;
            sibling.keyCount--;
        }
        else {
            starving.keys[starving.keyCount] = parent.keys[childIdx];

            starving.child[starving.keyCount + 1] = sibling.child[0];
            if (starving.child[starving.keyCount + 1] != null) {
                starving.child[starving.keyCount + 1].parent = starving;
            }

            parent.keys[childIdx] = sibling.keys[0];

            System.arraycopy(sibling.keys, 1, sibling.keys, 0, sibling.keyCount - 1);
            sibling.keys[sibling.keyCount - 1] = null;

            System.arraycopy(sibling.child, 1, sibling.child, 0, sibling.keyCount);
            sibling.child[sibling.keyCount] = null;

            starving.keyCount++;
            sibling.keyCount--;
        }
    }
}
