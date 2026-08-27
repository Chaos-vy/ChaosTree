package chaos.tree21.binary;

public final class RedBlackTreeSet<E> extends AbstractBinaryTreeSet<E, RbtNode<E>> {

    /*
     * <ol>
     *     <li>The root is BLACK</li>
     *     <li>Node can be either BLACK or RED</li>
     *     <li>All leaf node must be null/Black</li>
     *     <li>A RED node cannot have a red children</li>
     *     <li>Every path from a node to any descendant leaves contains the same no of BLACK node</li>
     * </ol>
     */
    @Override
    public boolean add(Object o) {
        if(o == null) throw new NullPointerException();
        try {
            @SuppressWarnings("unchecked")
            E val = (E) o;
            if(root == null){
                root = new RbtNode<>(val);
                root.setBlack();//A RBT rule must be Obeyed So I put the definition of RBT for not to forget.
                size++;
                cachedHashcode += val.hashCode();
                modCount++;
                return true;
            }
            RbtNode<E> parent = null;
            RbtNode<E> curr = root;
            int cmp = 0;
            while (curr != null) {
                parent = curr;
                cmp = compare(val, curr.getValue());
                if (cmp == 0) return false;
                else if (cmp < 0) curr = curr.getLeft();
                else curr = curr.getRight();
            }
            //Remember every node in RBT I made its default color is RED... true.... RED.....
            RbtNode<E> newNode = new RbtNode<>(val);
            newNode.setParent(parent);
            if (cmp < 0) {
                parent.setLeft(newNode);
            } else {
                parent.setRight(newNode);
            }
            fix_Up_from_bottom_Insertion(newNode);
            size++;
            modCount++;
            cachedHashcode += val.hashCode();
            return true;
        }
        catch (ClassCastException e){
            return false;
        }
    }

    private void fix_Up_from_bottom_Insertion(RbtNode<E> x) {
        // I need to only care if the parent is also RED (a Red-Red violation!)
        while (x != null && x != root && x.getParent().isRed()) {
            RbtNode<E> parent = x.getParent();
            // Grandparent is mathematically guaranteed to exist because parent is RED (root is always black)
            RbtNode<E> grandParent = parent.getParent();
            //Left Symmetry
            if (parent == grandParent.getLeft()) {
                RbtNode<E> uncle = grandParent.getRight();
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
                RbtNode<E> uncle = grandParent.getLeft();

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

    @Override
    public boolean remove(Object o) {
        if (o == null) throw new NullPointerException();
        //I am approaching the same way as of AVL tree delete. Reference: CLRS or take the AVL tree.
        try {
            if (isEmpty()) return false;
            @SuppressWarnings("unchecked")
            E val = (E) o;
            RbtNode<E> x = nodeFinder(val);
            if (x == null) return false;

            if (x.getLeft() != null && x.getRight() != null) {
                RbtNode<E> successor = x.getRight();
                while (successor.getLeft() != null) {
                    successor = successor.getLeft();
                }
                x.setValue(successor.getValue());
                x = successor;
            }

            //Guaranteed one child or none
            RbtNode<E> node_replacer = x.getLeft() != null ? x.getLeft() : x.getRight();
            boolean deletedNodeWasBlack = x.isBlack(); //This must be stored.

            if (node_replacer != null) {
                node_replacer.setParent(x.getParent());
                if (x.getParent() == null) {
                    root = node_replacer;
                } else if (x == x.getParent().getLeft()) {
                    x.getParent().setLeft(node_replacer);
                } else {
                    x.getParent().setRight(node_replacer);
                }

                // If the deleted node was Black, the tree lost a black weight. Fix it!
                if (deletedNodeWasBlack) {
                    fixDoubleBlack(node_replacer);
                }
            } else if (x.getParent() == null) {
                root = null; // The tree is now empty
            } else {
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

            //just clearing GC but do I need let me guess
            x.setLeft(null); //since I already removed all attachment to x to reach to x
            x.setRight(null); //JVM GC is smart enough it will collect in GC
            x.setParent(null); //Don't think JVM won't do. it will, even though it has reference
            //L,R,P becoz x has become part of garbage.
            /*
            The lesson I got here we need to do because of iterator Stability
            */
            size--;
            modCount++;
            cachedHashcode -= val.hashCode();
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
    private boolean isBlack(RbtNode<E> node) {
        return node == null || node.isBlack();
    }

    private boolean isRed(RbtNode<E> node) {
        return node != null && node.isRed();
    }

    private void fixDoubleBlack(RbtNode<E> x) {
        // Bubble the "Phantom Black" weight up until we hit a Red node or the Root
        while (x != root && isBlack(x)) {
            RbtNode<E> parent = x.getParent();

            if (x == parent.getLeft()) {
                RbtNode<E> sibling = parent.getRight();

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
                RbtNode<E> sibling = parent.getLeft();

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

    @Override
    public int height() {
        return calculateHeight(root);
    }
    private int calculateHeight(RbtNode<E> node) {
        // Base case: null nodes have a mathematical height of -1
        if (node == null) {
            return -1;
        }
        // Recursively find the deepest path
        return 1 + Math.max(calculateHeight(node.getLeft()), calculateHeight(node.getRight()));
        //This operation cost thread stack. maximum: not more than ~60
    }

    /**
     * Internal mathematical debugger. Validates all Red-Black structural invariants.
     * Throws IllegalStateException if any rule is broken.
     * These test are made to make endure the tree remains stable
     */
    void verifyInvariants() {
        if (root == null) return;
        if (root.isRed()) throw new IllegalStateException("RBT Corruption: Root is RED");
        validateRbtRules(root, null, null);
    }

    private int validateRbtRules(RbtNode<E> node, E min, E max) {
        if (node == null) return 1; // Null leaves have black height of 1

        // Rule 1: Binary Search Tree Property
        if (min != null && compare(node.getValue(),min) <= 0) {
            throw new IllegalStateException("RBT Corruption: BST Violation. Node " + node.getValue() + " is <= min bound " + min);
        }
        if (max != null && compare(node.getValue(),max) >= 0) {
            throw new IllegalStateException("RBT Corruption: BST Violation. Node " + node.getValue() + " is >= max bound " + max);
        }

        // Rule 2: Double Red Violation
        if (node.isRed()) {
            if (node.getLeft() != null && node.getLeft().isRed()) {
                throw new IllegalStateException("RBT Corruption: Double Red! Node " + node.getValue() + " and Left Child " + node.getLeft().getValue() + " are both RED.");
            }
            if (node.getRight() != null && node.getRight().isRed()) {
                throw new IllegalStateException("RBT Corruption: Double Red! Node " + node.getValue() + " and Right Child " + node.getRight().getValue() + " are both RED.");
            }
        }

        int leftBlackHeight = validateRbtRules(node.getLeft(), min, node.getValue());
        int rightBlackHeight = validateRbtRules(node.getRight(), node.getValue(), max);

        // Rule 3: Perfect Black Height
        if (leftBlackHeight != rightBlackHeight) {
            throw new IllegalStateException("RBT Corruption: Black Height Mismatch at Node " + node.getValue() + 
                "! Left BH=" + leftBlackHeight + ", Right BH=" + rightBlackHeight);
        }

        return leftBlackHeight + (node.isRed() ? 0 : 1);
    }

}
