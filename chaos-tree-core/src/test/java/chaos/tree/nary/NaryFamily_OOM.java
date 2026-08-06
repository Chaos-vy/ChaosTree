package chaos.tree.nary;

import chaos.tree.core.searchtree.SearchTree;
import org.junit.jupiter.api.Disabled;

import java.util.ArrayList;
import java.util.List;

/**
 * All the test of OOM are default heap my RAM: 24 GB -> 6GB heap for JVM
 */
@Disabled("This is a manual OOM stress test. Do not run during mvn package.")
public class NaryFamily_OOM {
    public static void main(String[] args) {
        System.out.println("=".repeat(20) + "B-Tree" + "=".repeat(20));
        int a = 0, b = 1;
        SearchTree<Integer> btree = new BTree<>(1024); //1023<-->2047 keys
        int count = 0;
        try {
            while (true) {
                btree.insert(b++);
                count++;
            }
        } catch (OutOfMemoryError e) {
            //.err not used because it's work on different thread
            btree = null;
            System.gc();
            System.out.println("OOM hit for sequential at " + count);
        }
        btree = new BTree<>(1024);
        count=0;
        b = 1;
        try {
            while (a < 350_000_000) {
                btree.insert(a);
                a = a + 2;
                count++;
            }
            while (true) {
                btree.insert(b);
                b = b + 2;
                count++;
            }
        } catch (OutOfMemoryError e) {
            //.err not used because it's work on different thread
            btree = null;
            System.gc();
            System.out.println("OOM hit for variant at " + count);
        }
        System.out.println("\n" + "=".repeat(20) + "B+Tree" + "=".repeat(20));
        a = 0;
        b = 1;
        SearchTree<Integer> bplustree = new BPlusTree<>(1024); //1023<-->2047 keys
        count = 0;
        try {
            while (true) {
                bplustree.insert(b++);
                count++;
            }
        } catch (OutOfMemoryError e) {
            //.err not used because it's work on different thread
            bplustree = null;
            System.gc();
            System.out.println("OOM hit for sequential at " + count);
        }
        b = 1;
        bplustree = new BPlusTree<>(1024);
        count=0;
        try {
            while (a < 350_000_000) {
                bplustree.insert(a);
                a = a + 2;
                count++;
            }
            while (true) {
                bplustree.insert(b);
                b = b + 2;
                count++;
            }
        } catch (OutOfMemoryError e) {
            //.err not used because it's work on different thread
            bplustree = null;
            System.gc();
            System.out.println("OOM hit for variant at " + count);
        }
        System.out.println("\n" + "=".repeat(20) + " ArrayList " + "=".repeat(20));
        //There is no variant input here because there is no balancing here
        List<Integer> list = new ArrayList<>();
        count = 0;
        //Just checking if it can because a resize can cause massive gc count and slow the program.
        // Resize will trigger massive GC and RAM usage.
        try {
            while (true){
                list.add(a++);
            }
        }
        catch (OutOfMemoryError e){
            list = null;
            System.gc();
            System.out.println("OOM hit at "+(a-1));
        }
    }
}
