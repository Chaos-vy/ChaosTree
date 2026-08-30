package chaos.tree21.nary;

public final class BTreeSet<E> extends AbstractNaryTreeSet<E, BTreeNode<E>>{


    @Override
    public boolean add(E e) {
        if (root == null) {
            root = new BTreeNode<>(degree, true);
            root.setKey(0, e);
            root.keyCount_INC1();
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
                current.keyCount_INC1();
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
            current = current.getChild(childIdx);
        }
    }

    private void splitNode(BTreeNode<E> parent, int childIdx, BTreeNode<E> child) {
        /*
        It might be internal node I might be trifling, so I made the internal node as same of child.
         */
        BTreeNode<E> sibling = new BTreeNode<>(degree, child.isLeaf());
        sibling.keyCount = degree - 1;
        // Shift right-half keys
        System.arraycopy(child.keys, degree, sibling.keys, 0, degree - 1);
        // Shift right-half children and update parent pointers
        if (!child.isLeaf()) {
            System.arraycopy(child.children, degree, sibling.children, 0, degree);
            for (int i = 0; i < degree; i++) {
                if (sibling.children[i] != null) sibling.children[i].parent = sibling;
            }
            // GC Cleanup
            for (int i = degree; i <= child.keyCount; i++) child.children[i] = null;
        }
        // GC Cleanup for keys
        // I have seen this instability during iterator.
        for (int i = degree; i < child.keyCount; i++) child.keys[i] = null;
        child.keyCount = degree - 1;

        // Shift parent arrays to make room
        System.arraycopy(parent.children, childIdx + 1, parent.children, childIdx + 2, parent.keyCount - childIdx);
        parent.setChild(childIdx + 1, sibling); // This auto-sets sibling.parent = parent!

        System.arraycopy(parent.keys, childIdx, parent.keys, childIdx + 1, parent.keyCount - childIdx);

        // Push the middle key UP and DELETE it from the child!
        parent.keys[childIdx] = child.keys[degree - 1];
        child.keys[degree - 1] = null;

        parent.keyCount_INC1();
    }

}