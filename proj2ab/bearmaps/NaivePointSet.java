package bearmaps;
import java.util.List;
import java.util.ArrayList;
public class NaivePointSet implements PointSet {
    /** @Source Josh Hug's walk through link
     * https://docs.google.com/presentation/d/1dsX4Q-mxKC6uLiI1YQL6J_XoIKdwHn7N
     * yjfGUAs-Ofk/preview?slide=id.g54401ae607_0_47*/
    private List<Point> myPoints;
    public NaivePointSet(List<Point> points) {
        myPoints = new ArrayList();
        for (Point P: points) {
            myPoints.add(P);
        }
    }
    @Override
    public Point nearest(double x, double y) {
        Point best = myPoints.get(0);
        Point target = new Point(x, y);
        for (Point P: myPoints) {
            double currentBest = Point.distance(best, target);
            double currentDistance = Point.distance(P, target);
            if (currentDistance < currentBest) {
                best = P;
            }
        }
        return best;
    }
    public static void main(String[] args) {
        Point p1 = new Point(1, 2);
        Point p2 = new Point(-3, 4);
        Point p3 = new Point(-3, 4);

        NaivePointSet nn = new NaivePointSet(List.of(p1, p2, p3));
        Point ret = nn.nearest(3.0, 4.0);
        ret.getX();
        ret.getY();
    }
}
