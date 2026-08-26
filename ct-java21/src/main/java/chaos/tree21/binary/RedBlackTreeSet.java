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
            fix_Up_from_bottom_Insertion(parent);
            size++;
            modCount++;
            cachedHashcode += val.hashCode();
        }
        catch (ClassCastException e){
            return false;
        }
        return false;
    }

    private void fix_Up_from_bottom_Insertion(RbtNode<E> x) {
        // I need to only care if the parent is also RED (a Red-Red violation!)
        while (x != null && x != root && x.getParent().isRed()) {
            RbtNode<E> parent = x.getParent();
            // Grandparent is mathematically guaranteed to exist because parent is RED (root is always black)
            RbtNode<E> grandParent = parent.getParent();
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
    public int height() {
        return 0;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }
}
