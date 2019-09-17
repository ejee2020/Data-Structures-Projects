package bearmaps;
import edu.princeton.cs.algs4.Stopwatch;
import org.junit.Test;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
public class KDTreeTest {
    /** @Source Josh Hug's walk through link
     * https://docs.google.com/presentation/d/1dsX4Q-mxKC6uLiI1YQL6J_XoIKdwHn7N
     * yjfGUAs-Ofk/preview?slide=id.g54401ae607_0_47*/
    private static Random r = new Random(500);
    @Test
    public void testNearest() {
        Point p1 = new Point(2, 3);
        Point p2 = new Point(4, 2);
        Point p3 = new Point(4, 2);
        Point p4 = new Point(4, 5);
        Point p5 = new Point(3, 3);
        Point p6 = new Point(1, 5);
        Point p7 = new Point(4, 4);
        KDTree kd = new KDTree(List.of(p1, p2, p3, p4, p5, p6, p7));
        Point goal = new Point(0, 7);
        Point actual = kd.nearest(0, 7);
        Point expected = new Point(1, 5);
        assertEquals(expected, actual);
    }

    private Point randomPoint() {
        double x = r.nextDouble();
        double y = r.nextDouble();
        return new Point(x, y);
    }
    private List<Point> randomPoints(int N) {
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < N; i += 1) {
            points.add(randomPoint());
        }
        return points;
    }

    @Test
    public void testRandomPoints() {
        List<Point> points1000 = randomPoints(1000);
        NaivePointSet nps = new NaivePointSet(points1000);
        KDTree kd = new KDTree(points1000);

        List<Point> queries200 = randomPoints(200);
        for (Point p : queries200) {
            Point expected = nps.nearest(p.getX(), p.getY());
            Point actual = kd.nearest(p.getX(), p.getY());
            assertEquals(expected, actual);
        }
    }

    @Test
    public void timeWith100000Pointsand10000Queries() {
        List<Point> points100000 = randomPoints(100000);
        NaivePointSet nps = new NaivePointSet(points100000);
        KDTree kd = new KDTree(points100000);
        Stopwatch sw = new Stopwatch();
        List<Point> queries10000 = randomPoints(10000);
        for (Point p : queries10000) {
            Point expected = nps.nearest(p.getX(), p.getY());
        }
        Stopwatch sw2 = new Stopwatch();
        for (Point p : queries10000) {
            Point expected1 = kd.nearest(p.getX(), p.getY());
        }
        System.out.println("Time elapsed for" + 100000
                + "Queries on " + 10000 + "points: " + sw.elapsedTime());
        System.out.println("Time elapsed for" + 100000
                + "Queries on " + 10000 + "points: " + sw2.elapsedTime());
    }
}
