package bearmaps;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ArrayHeapMinPQTest {
    @Test
    public void testaddContains() {
        ArrayHeapMinPQ<Integer> test1 = new ArrayHeapMinPQ();
        test1.add(1, 1);
        assertTrue(test1.contains(1));
    }

    @Test
    public void testDuplicateItemadd() {
        ArrayHeapMinPQ<Integer> test1 = new ArrayHeapMinPQ();
        test1.add(1, 1);
        test1.add(1, 1);
    }

    @Test
    public void testmultipleadd() {
        ArrayHeapMinPQ<Integer> test1 = new ArrayHeapMinPQ();
        test1.add(1, 2);
        test1.add(2, 3);
        test1.add(3, 1);
        assertTrue(test1.getSmallest() == 3);
    }

    @Test
    public void testDuplicatePriorityadd() {
        ArrayHeapMinPQ<Integer> test1 = new ArrayHeapMinPQ();
        test1.add(1, 3);
        test1.add(2, 3);
        assertTrue(test1.getSmallest() == 1);
    }

    @Test
    public void testRemoveSmallest() {
        ArrayHeapMinPQ<Integer> test = new ArrayHeapMinPQ();
        test.add(1, 1);
        test.add(2, 2);
        test.add(3, 3);
        assertTrue(1 == test.removeSmallest());
        assertTrue(2 == test.getSmallest());
    }

    @Test
    public void testMANYadd() {
        ArrayHeapMinPQ<Integer> test = new ArrayHeapMinPQ();
        test.add(1, 12);
        test.add(2, 2);
        test.add(3, 3);
        test.add(4, 4);
        test.add(5, 5);
        test.add(6, 6);
        test.add(7, 7);
        test.add(8, 8);
        test.add(9, 9);
        test.add(10, 10);
        test.add(11, 11);
        test.add(12, 1);
        assertTrue(test.getSmallest() == 12);
    }

    @Test
    public void testSinkRemove() {
        ArrayHeapMinPQ<Integer> test = new ArrayHeapMinPQ();
        test.add(1, 12);
        test.add(2, 2);
        test.add(3, 3);
        test.add(4, 4);
        test.add(5, 5);
        test.add(6, 6);
        test.add(7, 7);
        test.add(8, 8);
        test.add(9, 9);
        test.add(10, 10);
        test.add(11, 11);
        test.add(12, 1);
        test.removeSmallest();
        assertTrue(test.getSmallest() == 2);
        test.removeSmallest();
        assertTrue(test.getSmallest() == 3);
    }

    @Test
    public void testRemoves() {
        ArrayHeapMinPQ<Integer> test = new ArrayHeapMinPQ();
        test.add(1, 3);
        test.add(2, 4);
        test.add(3, 4);
        test.add(4, 6);
        test.removeSmallest();
        assertTrue(test.getSmallest() == 2);
    }

    @Test
    public void testTree() {
        ArrayHeapMinPQ<Integer> test = new ArrayHeapMinPQ();
        test.add(1, 1);
        test.add(2, 4);
        test.add(3, 2);
        test.add(4, 8);
        test.add(5, 5);
        test.add(6, 8);
        test.add(7, 8);
        test.add(8, 7);
        test.add(9, 12);
        test.add(10, 9);
        test.add(11, 10);
        test.add(12, 11);
        test.add(13, 6);
        test.changePriority(5, 3);
        assertTrue(test.getSmallest() == 1);
    }
}
