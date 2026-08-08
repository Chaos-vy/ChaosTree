package chaos.tree.jol;

import chaos.tree.binary.*;
import chaos.tree.nary.BPlusTreeNode;
import chaos.tree.nary.BTreeNode;
import org.openjdk.jol.info.ClassLayout;

public class JOLTest {

    public static void testAVLNodeMemoryFootprint() {
        System.out.println("AVL Tree node:");
        System.out.println(ClassLayout.parseClass(AVLNode.class).toPrintable());
        System.out.println("ExactByte : "+ClassLayout.parseClass(AVLNode.class).instanceSize());
        System.out.println("=".repeat(50)+"\n\n");
    }
    public static void testRBTNodeMemoryFootprint() {
        System.out.println("RBT Tree node:");
        System.out.println(ClassLayout.parseClass(RBTNode.class).toPrintable());
        System.out.println("ExactByte : "+ClassLayout.parseClass(RBTNode.class).instanceSize());
        System.out.println("=".repeat(50)+"\n\n");
    }

    public static void testBSTNodeMemoryFootprint() {
        System.out.println("BST Tree node:");
        System.out.println(ClassLayout.parseClass(BSTNode.class).toPrintable());
        System.out.println("ExactByte : "+ClassLayout.parseClass(BSTNode.class).instanceSize());
        System.out.println("=".repeat(50)+"\n\n");
    }
    public static void testTreapNodeMemoryFootprint() {
        System.out.println("Treap Tree node:");
        System.out.println(ClassLayout.parseClass(TreapNode.class).toPrintable());
        System.out.println("ExactByte : "+ClassLayout.parseClass(TreapNode.class).instanceSize());
        System.out.println("=".repeat(50)+"\n\n");
    }
    public static void testSplayNodeMemoryFootprint() {
        System.out.println("RBT Tree node:");
        System.out.println(ClassLayout.parseClass(SplayNode.class).toPrintable());
        System.out.println("ExactByte : "+ClassLayout.parseClass(SplayNode.class).instanceSize());
        System.out.println("=".repeat(50)+"\n\n");
    }
    public static void testBTreeNodeMemoryFootprint() {
        System.out.println("\nB-Tree node:\n");
        System.out.println(ClassLayout.parseClass(BTreeNode.class).toPrintable());
        System.out.println("ExactByte : "+ClassLayout.parseClass(BTreeNode.class).instanceSize());
        System.out.println("\n"+"=".repeat(20)+" B-Tree "+"=".repeat(20)+"\n");
    }
    public static void testBPlusTreeNodeMemoryFootprint() {
        System.out.println("\nB+Tree node:\n");
        System.out.println(ClassLayout.parseClass(BPlusTreeNode.class).toPrintable());
        System.out.println("ExactByte : "+ClassLayout.parseClass(BPlusTreeNode.class).instanceSize());
        System.out.println("\n"+"=".repeat(20)+" B+Tree "+"=".repeat(20)+"\n");
    }
    public static void main(String[] args) {
        testAVLNodeMemoryFootprint();
        testRBTNodeMemoryFootprint();
        testBSTNodeMemoryFootprint();
        testSplayNodeMemoryFootprint();
        testTreapNodeMemoryFootprint();
        testBPlusTreeNodeMemoryFootprint();
        testBTreeNodeMemoryFootprint();
    }
}
