package chaos.tree21.binary;

public final class AvlTreeSet<E> extends AbstractBinaryTreeSet<E, AvlNode<E>> {

    @Override
    public boolean add(E val) {
        if (root == null) {
            compare(val, val); // JDK Semantic: Type (and possibly null) check!
            root = new AvlNode<>(val);
            size++;
            cachedHashcode += val.hashCode();
            modCount++;
            return true;
        }
        AvlNode<E> parent = null;
        AvlNode<E> curr = root;
        int cmp = 0;
        while (curr != null) {
            parent = curr;
            cmp = compare(val, curr.getValue());
            if (cmp == 0) {
                return false;
            } else if (cmp < 0) {
                curr = curr.getLeft();
            } else {
                curr = curr.getRight();
            }
        }
        AvlNode<E> newNode = new AvlNode<>(val);
        newNode.setParent(parent);
        if (cmp < 0) {
            parent.setLeft(newNode);
        } else {
            parent.setRight(newNode);
        }
        fix_Up_from_bottom(parent);
        size++;
        modCount++;
        cachedHashcode += val.hashCode();
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (isEmpty()) return false;
        @SuppressWarnings("unchecked")
        E val = (E) o;
        AvlNode<E> x = nodeFinder(val);
        if (x == null) return false;
        //Just delete from here no tension haha LOL I feel like playing here node very much fun.
        //Just to be aware of optimization I need to add comment
        if (x.getRight() != null && x.getLeft() != null) {
            AvlNode<E> successor = x.getRight(); // Yes I do delete by successor method
            while (successor.getLeft() != null) { //becoz it's optimized for iterator purpose
                successor = successor.getLeft();
            }
            x.setValue(successor.getValue());
            x = successor; //Now guaranteed this little node in the tree will be alone to one or none hehe LOL
        }
        /*
          I need to remember this of little chaos cases in future use.
          1: if all L and R are null
          2: if L is null -> part of right node to be attached and markup of parent!
          3: if R is null -> part of left node to be attached and markup of parent!
         */
        AvlNode<E> replacement = x.getLeft() != null ? x.getLeft() : x.getRight();

        //This block is for part when case 2 and case 3 falls
        if (replacement != null) {
            replacement.setParent(x.getParent());
            if (x.getParent() == null) {//most critical one
                root = replacement;
            } else if (x == x.getParent().getLeft()) {
                x.getParent().setLeft(replacement);
            } else {
                x.getParent().setRight(replacement);
            }
            fix_Up_from_bottom(replacement.getParent());

        } else if (x.getParent() == null) { // when the little node has direct reach to root
            root = null;
        } else { // when the little node is left with no L and R
            AvlNode<E> parent = x.getParent();
            if (x == parent.getLeft()) {
                parent.setLeft(null);
            } else {
                parent.setRight(null);
            }
            //Lemme think do I
            fix_Up_from_bottom(parent);
        }

        //just clearing GC but do I need let me guess
        x.setLeft(null); //since I already removed all attachment to x to reach to x
        x.setRight(null); //JVM GC is smart enough it will collect in GC
        x.setParent(null); //Don't think JVM won't do. it will, even though it has reference
        //L,R,P becoz x has become part of garbage.
            /*
            The lesson I got here we need to do because iterator Stability
             */
        size--;
        modCount++;
        cachedHashcode -= val.hashCode();
        return true;
    }

    private void fix_Up_from_bottom(AvlNode<E> node) {
        while (node != null) {
            int oldHeight = node.getHeight();
            updateHeight(node);
            int balance = nodeHeight(node.getLeft()) - nodeHeight(node.getRight());
            if (balance > 1) {
                if (nodeHeight(node.getLeft().getLeft()) >= nodeHeight(node.getLeft().getRight())) {
                    node = rotateRightAVL(node);
                } else {
                    rotateLeftAVL(node.getLeft());
                    node = rotateRightAVL(node);
                }
            } else if (balance < -1) {
                if (nodeHeight(node.getRight().getRight()) >= nodeHeight(node.getRight().getLeft())) {
                    node = rotateLeftAVL(node);
                } else {
                    rotateRightAVL(node.getRight());
                    node = rotateLeftAVL(node);
                }
            }
            if (oldHeight == node.getHeight()) {
                break;
            }
            node = node.getParent();
        }
    }

    // --- AVL-Specific Rotation Wrappers --- These are main parts.

    private AvlNode<E> rotateRightAVL(AvlNode<E> p) {
        AvlNode<E> newRoot = p.getLeft();
        super.rotateRight(p);
        updateHeight(p);
        updateHeight(newRoot);
        return newRoot;
    }

    private AvlNode<E> rotateLeftAVL(AvlNode<E> p) {
        AvlNode<E> newRoot = p.getRight();
        super.rotateLeft(p);
        updateHeight(p);
        updateHeight(newRoot);
        return newRoot;
    }

    private void updateHeight(AvlNode<E> root) {
        root.setHeight(1 + Math.max(nodeHeight(root.getLeft()), nodeHeight(root.getRight())));
    }

    private int nodeHeight(AvlNode<E> node) {
        return node == null ? -1 : node.getHeight();
    }

    /**
     * Internal mathematical debugger. Validates all AVL structural invariants.
     * Throws IllegalStateException if any rule is broken.
     */
    void verifyInvariants() {
        if (root == null) return;
        validateAvlRules(root, null, null);
    }

    private int validateAvlRules(AvlNode<E> node, E min, E max) {
        if (node == null) return -1;

        if (min != null && compare(node.getValue(), min) <= 0) {
            throw new IllegalStateException("AVL Corruption: BST Violation. Node " + node.getValue() + " is <= min bound " + min);
        }
        if (max != null && compare(node.getValue(), max) >= 0) {
            throw new IllegalStateException("AVL Corruption: BST Violation. Node " + node.getValue() + " is >= max bound " + max);
        }

        int leftHeight = validateAvlRules(node.getLeft(), min, node.getValue());
        int rightHeight = validateAvlRules(node.getRight(), node.getValue(), max);

        // Rule 2: Height cached accurately
        int actualHeight = 1 + Math.max(leftHeight, rightHeight);
        if (node.getHeight() != actualHeight) {
            throw new IllegalStateException("AVL Corruption: Cached height of Node " + node.getValue() +
                    " is " + node.getHeight() + " but actual height is " + actualHeight);
        }

        // Rule 3: Balance Factor (-1, 0, 1)
        int balance = leftHeight - rightHeight;
        if (Math.abs(balance) > 1) {
            throw new IllegalStateException("AVL Corruption: Balance Factor Violation at Node " + node.getValue() +
                    "! Balance is " + balance);
        }

        return actualHeight;
    }
}
