package chaos.tree.nary;

import org.openjdk.jol.info.ClassLayout;

public class NaryFamilyJOLTest {
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
        testBPlusTreeNodeMemoryFootprint();
        testBTreeNodeMemoryFootprint();
    }
}
