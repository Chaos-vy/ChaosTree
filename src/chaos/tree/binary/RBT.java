package chaos.tree.binary;

import chaos.tree.core.AbstractRotateTree;
import chaos.tree.exception.DuplicateNodeException;

public class RBT<T extends Comparable<T>> extends AbstractRotateTree<T,RBTNode<T>> {

    private static final boolean RED = false;
    private static final boolean BLACK = true;

    @Override
    protected RBTNode<T> createNode(T value) {
        return new RBTNode<>(value);
    }

    @Override
    protected String nodeText(RBTNode<T> node) {
        return node.getValue()
                + (node.getColor() ? "(B)" : "(R)");
    }
    @Override
    protected RBTNode<T> leftRotate(RBTNode<T> node) {
        RBTNode<T> parent = node.getParent();
        RBTNode<T> newNode = super.leftRotate(node);
        newNode.getLeft().setParent(newNode);
        if(parent==null){
            root = newNode;
        }
        else {
            if(parent.getLeft()==node){
                parent.setLeft(newNode);
            }
            else parent.setRight(newNode);
        }
        newNode.setParent(parent);
        return newNode;
    }

    @Override
    protected RBTNode<T> rightRotate(RBTNode<T> node) {

        RBTNode<T> parent = node.getParent();
        RBTNode<T> newNode = super.rightRotate(node);
        newNode.getRight().setParent(newNode);
        if(parent==null){
            root = newNode;
        }
        else {
            if(parent.getLeft()==node){
                parent.setLeft(newNode);
            }
            else parent.setRight(newNode);
        }
        newNode.setParent(parent);
        return newNode;
    }

    private boolean isRed(RBTNode<T> node) {
        return node != null && node.getColor() == RED;
    }

    private void setColor(RBTNode<T> node, boolean color) {
        if (node != null) node.setColor(color);
    }
    @Override
    public void insert(T value) {
        root = insert(root, value);
        setColor(root, BLACK);
        size++;
    }
    @Override
    protected RBTNode<T> insert(RBTNode<T> node, T value) {
        if (node == null) {
            return createNode(value);
        }
        int cmp = compare(value, node);

        if (cmp == 0) {
            throw new DuplicateNodeException("Value already present in tree");
        }
        if (cmp > 0) {
            node.setRight(insert(node.getRight(), value));
            node.getRight().setParent(node);
        } else {
            node.setLeft(insert(node.getLeft(), value));
            node.getLeft().setParent(node);
        }
        return afterInsert(node);
    }

    private void refactorUncleRed(RBTNode<T> node) {
        setColor(node, RED);
        setColor(node.getLeft(), BLACK);
        setColor(node.getRight(), BLACK);
    }
    private void refactorUncleBlack(RBTNode<T> node, RBTNode<T> child){
        setColor(node,BLACK);
        setColor(child,RED);
    }
    @Override
    protected RBTNode<T> afterInsert(RBTNode<T> node) {

        RBTNode<T> left = node.getLeft();
        RBTNode<T> right = node.getRight();

        if (isRed(left) && isRed(left.getLeft())) {
            if(!isRed(right)){
                node = rightRotate(node);
                refactorUncleBlack(node,node.getRight());
            }
            else {
                refactorUncleRed(node);
            }
        } else if (isRed(left) && isRed(left.getRight())) {
            if(!isRed(right)){
                node.setLeft(leftRotate(left));
                node=rightRotate(node);
                refactorUncleBlack(node,node.getRight());
            }
            else{
                refactorUncleRed(node);
            }
        } else if (isRed(right) && isRed(right.getRight())) {
            if(!isRed(left)){
                node = leftRotate(node);
                refactorUncleBlack(node,node.getLeft());
            }
            else {
                refactorUncleRed(node);
            }
        } else if (isRed(right) && isRed(right.getLeft())) {
            if(!isRed(left)){
                node.setRight(rightRotate(right));
                node=leftRotate(node);
                refactorUncleBlack(node,node.getLeft());
            }
            else {
                refactorUncleRed(node);
            }
        }
        return node;
    }


    private void rewire(RBTNode<T> parent, RBTNode<T> node,RBTNode<T> replacement){
        boolean isLeftChild = parent.getLeft()==node;
        if(replacement==null){
            if(isLeftChild){
                parent.setLeft(null);
            }
            else{
                parent.setRight(null);
            }
        }
        else {
            if(isLeftChild){
                parent.setLeft(replacement);
                replacement.setParent(parent);
            }
            else{
                parent.setRight(replacement);
                replacement.setParent(parent);
            }
        }
    }

    @Override
    protected RBTNode<T> delete(RBTNode<T> node, T value) {
        if (node == null) return null;

        int cmp = compare(value, node);

        if (cmp > 0) {
            delete(node.getRight(), value);

        } else if (cmp < 0) {
            delete(node.getLeft(), value);

        } else
        {
            if(node.getLeft()!=null && node.getRight()!=null) {
                RBTNode<T> successor = getMinNode(node.getRight());
                node.setValue(successor.getValue());
                delete(node.getRight(), successor.getValue());
                return node;
            }
            if(isRed(node)){
                rewire(node.getParent(),node,null);
                return null;
            }
            RBTNode<T> descendantNode = node.getLeft()==null?node.getRight():node.getLeft();
            if(isRed(descendantNode)){
                descendantNode.setColor(BLACK);
                rewire(node.getParent(),node,descendantNode);
                return null;
            }

            fixDoubleBlack(node);
            rewire(node.getParent(),node,null);
            return null;
        }
        return node;
    }

    private void fixDoubleBlack(RBTNode<T> node) {

        if (node == null) return;
        if (node == root) return;

        RBTNode<T> parent = node.getParent();
        boolean isLeftChild = parent.getLeft() == node;
        RBTNode<T> sibling = isLeftChild ? parent.getRight() : parent.getLeft();
        if (sibling == null) {
            fixDoubleBlack(parent);
            return;
        }
        if (isRed(sibling)) {
            sibling.setColor(BLACK);
            parent.setColor(RED);
            if (isLeftChild) {
                leftRotate(parent);
            } else {
                rightRotate(parent);
            }
            fixDoubleBlack(node);
            return;
        }
        boolean siblingHasRedChild = isRed(sibling.getLeft()) ||isRed(sibling.getRight());
        if (!siblingHasRedChild) {
            sibling.setColor(RED);
            if (isRed(parent)) {
                parent.setColor(BLACK);
            } else {
                fixDoubleBlack(parent);
            }
            return;
        }
        if (isLeftChild) {
            if (isRed(sibling.getRight())) {
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                sibling.getRight().setColor(BLACK);
                leftRotate(parent);
            }
            else {
                sibling.getLeft().setColor(BLACK);
                rightRotate(sibling);
                sibling = parent.getRight();
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                leftRotate(parent);
            }
        } else {
            if (isRed(sibling.getLeft())) {
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                sibling.getLeft().setColor(BLACK);
                rightRotate(parent);
            }
            else {
                sibling.getRight().setColor(BLACK);
                leftRotate(sibling);
                sibling = parent.getLeft();
                sibling.setColor(parent.getColor());
                parent.setColor(BLACK);
                rightRotate(parent);
            }
        }
    }
    public boolean isBalanced() {
        return blackHeight(root) != -1;
    }
    private int blackHeight(RBTNode<T> node) {
        if (node == null) {
            return 1;
        }
        int left = blackHeight(node.getLeft());
        int right = blackHeight(node.getRight());
        if (left == -1 || right == -1 || left != right) {
            return -1;
        }
        return left + (node.getColor() == BLACK ? 1 : 0);
    }
}
