package bearmaps;

/**
 * Created by hug. Demonstrates how you can use either
 * System.currentTimeMillis or the Princeton Stopwatch
 * class to time code.
 */
public class TimingTestDemo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ArrayHeapMinPQ<Integer> test = new ArrayHeapMinPQ();
        for (int i = 0; i < 100000; i++) {
            test.add(i, i + 1);
        }
        for (int k = 0; k < 99999; k++) {
            test.changePriority(k, k - 1);
        }
        long end = System.currentTimeMillis();
        System.out.println("Total time elapsed: " + (end - start) / 1000.0 +  " seconds.");

        long start1 = System.currentTimeMillis();
        NaiveMinPQ<Integer> test2 = new NaiveMinPQ<Integer>();
        for (int j = 0; j < 100000; j++) {
            test2.add(j, j + 1);
        }
        for (int k = 0; k < 99999; k++) {
            test2.changePriority(k, k - 1);
        }
        long end1 = System.currentTimeMillis();
        System.out.println("Total time elapsed: " + (end1 - start1) / 1000.0 +  " seconds.");
    }
}
