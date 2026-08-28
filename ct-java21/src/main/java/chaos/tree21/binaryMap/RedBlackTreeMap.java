package chaos.tree21.binaryMap;

public final class RedBlackTreeMap<K, V> extends AbstractBinaryTreeMap<K, V, RbtMapNode<K, V>> {

    @Override
    protected RbtMapNode<K, V> createNode(K key, V value) {
        return new RbtMapNode<>(key, value);
    }

    @Override
    protected void afterInsert(RbtMapNode<K, V> x) {
        // I need to only care if the parent is also RED (a Red-Red violation!)
        while (x != null && x != root && x.getParent().isRed()) {
            RbtMapNode<K, V> parent = x.getParent();
            // Grandparent is mathematically guaranteed to exist because parent is RED (root is always black)
            RbtMapNode<K, V> grandParent = parent.getParent();
            //Left Symmetry
            if (parent == grandParent.getLeft()) {
                RbtMapNode<K, V> uncle = grandParent.getRight();
                // Case 1: Uncle is RED (The Recolor Case)
                if (uncle != null && uncle.isRed()) {
                    parent.setBlack();
                    uncle.setBlack();
                    grandParent.setRed();
                    x = grandParent; // Push the red violation up the tree and loop again!
                } else {
                    // Case 2: Uncle is BLACK (The Triangle Case)
                    if (x == parent.getRight()) {
                        x = parent;
                        super.rotateLeft(x);
                        parent = x.getParent();
                    }
                    // Case 3: Uncle is BLACK (The Line Case)
                    parent.setBlack();
                    grandParent.setRed();
                    super.rotateRight(grandParent);
                    break;
                }
            } else {
                // Symmetrical cases for the Right side
                RbtMapNode<K, V> uncle = grandParent.getLeft();

                if (uncle != null && uncle.isRed()) {
                    parent.setBlack();
                    uncle.setBlack();
                    grandParent.setRed();
                    x = grandParent;
                } else {
                    if (x == parent.getLeft()) {
                        x = parent;
                        super.rotateRight(x);
                        parent = x.getParent();
                    }
                    parent.setBlack();
                    grandParent.setRed();
                    super.rotateLeft(grandParent);
                    break;
                }
            }
        }
        //Everything ends with this guy.
        root.setBlack();
    }

    private boolean isBlack(RbtMapNode<K, V> node) {
        return node == null || node.isBlack();
    }

    private boolean isRed(RbtMapNode<K, V> node) {
        return node != null && node.isRed();
    }

    @Override
    public V remove(Object o) {
        if (isEmpty()) return null;
        @SuppressWarnings("unchecked")
        K key = (K) o;

        RbtMapNode<K, V> x = nodeFinder(key);
        if (x == null) return null; //No key, no operation LOL it's look like a dialogue.
        V oldValue = x.getValue();
        cachedHashcode -= x.hashCode();
        if (x.getLeft() != null && x.getRight() != null) {
            RbtMapNode<K, V> successor = x.getRight();
            while (successor.getLeft() != null) {
                successor = successor.getLeft();
            }
            x.setPair(successor.getKey(), successor.getValue());
            x = successor;
        }
        RbtMapNode<K, V> node_replacer = x.getLeft() != null ? x.getLeft() : x.getRight();
        boolean deletedNodeWasBlack = x.isBlack();
        if (node_replacer != null) {
            node_replacer.setParent(x.getParent());
            if (x.getParent() == null) {
                root = node_replacer;
            } else if (x == x.getParent().getLeft()) {
                x.getParent().setLeft(node_replacer);
            } else {
                x.getParent().setRight(node_replacer);
            }
            if (deletedNodeWasBlack) {
                fixDoubleBlack(node_replacer);
            }
        } else if (x.getParent() == null) {
            root = null; // The tree is now empty
        } else {
            // Leaf Node Deletion: We must fix the black weight BEFORE unlinking!
            if (deletedNodeWasBlack) {
                fixDoubleBlack(x);
            }

            if (x == x.getParent().getLeft()) {
                x.getParent().setLeft(null);
            } else {
                x.getParent().setRight(null);
            }
            x.setParent(null);
        }

        // Clearing GC references for Iterator stability
        x.setLeft(null);
        x.setRight(null);
        x.setParent(null);

        size--;
        modCount++;
        return oldValue;
    }

    private void fixDoubleBlack(RbtMapNode<K, V> x) {
        // Bubble the "Phantom Black" weight up until we hit a Red node or the Root
        while (x != root && isBlack(x)) {
            RbtMapNode<K, V> parent = x.getParent();

            if (x == parent.getLeft()) {
                RbtMapNode<K, V> sibling = parent.getRight();

                // Case 1: Sibling is RED
                // We rotate to force the sibling to be BLACK, which pushes us into Case 2, 3, or 4
                if (isRed(sibling)) {
                    sibling.setBlack();
                    parent.setRed();
                    super.rotateLeft(parent);
                    sibling = parent.getRight(); // Update sibling after rotation
                }

                // Case 2: Both of the sibling's children (nephews) are BLACK
                if (isBlack(sibling.getLeft()) && isBlack(sibling.getRight())) {
                    sibling.setRed();
                    x = parent; // Push the Double-Black weight up to the parent!
                } else {
                    // Case 3: Sibling is BLACK, Right nephew is BLACK (Left nephew is RED)
                    if (isBlack(sibling.getRight())) {
                        if (sibling.getLeft() != null) sibling.getLeft().setBlack();
                        sibling.setRed();
                        super.rotateRight(sibling);
                        sibling = parent.getRight(); // Update sibling
                    }

                    // Case 4: Sibling is BLACK, Right nephew is RED
                    // This is the TERMINAL CASE. We need to fix the tree and instantly BREAK!
                    if (parent.isRed()) sibling.setRed();
                    else sibling.setBlack();

                    parent.setBlack();
                    if (sibling.getRight() != null) sibling.getRight().setBlack();
                    super.rotateLeft(parent);

                    break; // EARLY EXIT!
                }
            } else {
                // Symmetrical cases for when 'x' is the Right child
                RbtMapNode<K, V> sibling = parent.getLeft();

                if (isRed(sibling)) {
                    sibling.setBlack();
                    parent.setRed();
                    super.rotateRight(parent);
                    sibling = parent.getLeft();
                }

                if (isBlack(sibling.getRight()) && isBlack(sibling.getLeft())) {
                    sibling.setRed();
                    x = parent;
                } else {
                    if (isBlack(sibling.getLeft())) {
                        if (sibling.getRight() != null) sibling.getRight().setBlack();
                        sibling.setRed();
                        super.rotateLeft(sibling);
                        sibling = parent.getLeft();
                    }

                    if (parent.isRed()) sibling.setRed();
                    else sibling.setBlack();

                    parent.setBlack();
                    if (sibling.getLeft() != null) sibling.getLeft().setBlack();
                    super.rotateRight(parent);

                    break; // EARLY EXIT!
                }
            }
        }
        if (x != null) {
            x.setBlack();
        }
    }
}
