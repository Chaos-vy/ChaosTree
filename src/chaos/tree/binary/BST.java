package chaos.tree.binary;
import chaos.tree.core.AbstractBiTree;
import chaos.tree.core.BiNode;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

class BSTNode<T> extends BiNode<T> {
    public BSTNode(T value) {
        super(value);
    }
}
public class BST<T extends Comparable<T>> extends AbstractBiTree<T> {

    private BSTNode<T> root;
    private int size=0;
    @Override
    public void insert(T value) {
        if(root == null) {
            root = new BSTNode<>(value);
            size++;
            return;
        }
        insertHelper(root, value);
        size++;
    }

    private void insertHelper(BSTNode<T> curr, T value) {
        if(value.compareTo(curr.getValue()) > 0) {
            if(curr.getRight() == null) {
                curr.setRight(new BSTNode<>(value));
            } else {
                insertHelper((BSTNode<T>) curr.getRight(), value);
            }
        } else {
            if(curr.getLeft() == null) {
                curr.setLeft(new BSTNode<>(value));
            } else {
                insertHelper((BSTNode<T>) curr.getLeft(), value);
            }
        }
    }

    @Override
    public boolean search(T value) {
        if(root==null)return false;
        return searchHelper(root,value);
    }

    private boolean searchHelper(BSTNode<T> curr, T value) {
        if(curr==null) return false;
        if(value.compareTo(curr.getValue())==0)return true;

        if(value.compareTo(curr.getValue())>0){
           return searchHelper((BSTNode<T>) curr.getRight(),value);
        }
        else{
            return searchHelper((BSTNode<T>)curr.getLeft(),value);
        }
    }
    private boolean deleteFlag =false;
    @Override
    public void delete(T value) {
        if(root==null) return;
        deleteFlag=false;
        root = deleteHelper(root,value);
        if(deleteFlag)size--;
    }

    private BSTNode<T> deleteHelper(BSTNode<T> curr, T value) {
        if (curr == null) return null;

        if (value.compareTo(curr.getValue())<0) {
            curr.setLeft(deleteHelper((BSTNode<T>) curr.getLeft(), value));

        } else if (value.compareTo(curr.getValue())>0) {
            curr.setRight(deleteHelper((BSTNode<T>) curr.getRight(), value));
        }
        else {
            deleteFlag = true;
            if(curr.getLeft()==null && curr.getRight()==null){
                return null;
            }
            if (curr.getLeft() == null) {
                return (BSTNode<T>) curr.getRight();
            }
            if (curr.getRight() == null) {
                return (BSTNode<T>) curr.getLeft();
            }
            BSTNode<T> successor = (BSTNode<T>)curr.getRight();
            while (successor.getLeft() != null) {
                successor = (BSTNode<T>) successor.getLeft();
            }
            curr.setValue(successor.getValue());
            curr.setRight(deleteHelper((BSTNode<T>) curr.getRight(), successor.getValue()));
        }
        return curr;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int height() {
        return heightHelper(root);
    }

    private int heightHelper(BSTNode<T> curr) {
        if(curr == null) return -1;

        int leftHeight = heightHelper((BSTNode<T>) curr.getLeft());
        int rightHeight = heightHelper((BSTNode<T>) curr.getRight());

        return Math.max(leftHeight, rightHeight) + 1;
    }

    @Override
    public boolean isEmpty() {
        return root==null;
    }

    @Override
    public void clear() {
        root=null;
        size=0;
    }



    @Override
    public List<T> inorder() {
        List<T> list = new ArrayList<>();
        inorderHelper(root,list);
        return list;
    }

    private void inorderHelper(BSTNode<T> curr,List<T> list) {
        if(curr==null)return;
        inorderHelper((BSTNode<T>) curr.getLeft(),list);
        list.add(curr.getValue());
        inorderHelper((BSTNode<T>) curr.getRight(),list);
    }

    @Override
    public List<T> preorder() {
        List<T> list = new ArrayList<>();
        preorderHelper(root,list);
        return list;
    }

    private void preorderHelper(BSTNode<T> curr, List<T> list) {
        if(curr==null)return;
        list.add(curr.getValue());
        preorderHelper((BSTNode<T>) curr.getLeft(),list);
        preorderHelper((BSTNode<T>) curr.getRight(),list);
    }

    @Override
    public List<T> postorder() {
        List<T> list = new ArrayList<>();
        postorderHelper(root,list);
        return list;
    }

    private void postorderHelper(BSTNode<T> curr, List<T> list){
        if(curr==null)return;
        postorderHelper((BSTNode<T>) curr.getLeft(),list);
        postorderHelper((BSTNode<T>) curr.getRight(),list);
        list.add(curr.getValue());
    }

    @Override
    public T getMin() {
        if(root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        BSTNode<T> curr =root;
        while (curr.getLeft()!=null){
            curr = (BSTNode<T>) curr.getLeft();
        }
        return curr.getValue();
    }


    @Override
    public T getMax() {
        if(root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        BSTNode<T> curr =root;
        while (curr.getRight()!=null){
            curr = (BSTNode<T>) curr.getRight();
        }
        return curr.getValue();
    }
}
