package chaos.tree21.nary;

import java.util.Arrays;
/*
I prioritize sometime DOD over OOD
 */
public final class BPlusTreeSet<E> extends AbstractNaryTreeSet<E, BPlusTreeNode<E>>{

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
            current = current.getChild(childIdx);
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
            System.arraycopy(parent.children, childIdx + 1, parent.children, childIdx + 2, parent.keyCount - childIdx);
            parent.setChild(childIdx + 1, sibling);
            System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);
            // Push a COPY of the sibling's first key up as the Routing Key!
            parent.keys[childIdx] = sibling.keys[0];

        } else {
            sibling.keyCount = degree - 1;
            System.arraycopy(child.keys, degree, sibling.keys, 0, degree - 1);

            System.arraycopy(child.children, degree, sibling.children, 0, degree);
            for (int i = 0; i < degree; i++) {
                if (sibling.children[i] != null) sibling.children[i].parent = sibling;
            }
            //clearing GC!!
            Arrays.fill(child.children, degree, child.keyCount + 1, null);
            Arrays.fill(child.keys, degree, child.keyCount, null);

            child.keyCount = degree - 1;

            System.arraycopy(parent.children, childIdx + 1, parent.children, childIdx + 2, parent.keyCount - childIdx);
            parent.setChild(childIdx + 1, sibling);

            System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);
            // Push middle key UP and DELETE it from the child!
            parent.keys[childIdx] = child.keys[degree - 1];
            child.keys[degree - 1] = null;

        }
        parent.keyCount++;
    }
}
