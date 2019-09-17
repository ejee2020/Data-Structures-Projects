package bearmaps;
import java.util.List;
public class KDTree implements PointSet {
    /** @Source Josh Hug's walk through link
     * https://docs.google.com/presentation/d/1dsX4Q-mxKC6uLiI1YQL6J_XoIKdwHn7N
     * yjfGUAs-Ofk/preview?slide=id.g54401ae607_0_47*/
    private Node root;
    private static final boolean HORIZONTAL = false;
    private class Node {
        private Point point;
        private Node left, right;
        private boolean orientation;
        private Node(Point a, boolean orientation1) {
            point = a;
            orientation = orientation1;
        }
    }
    private Node add(Point p, Node n, boolean orientation) {
        if (n == null) {
            return new Node(p, orientation);
        }
        if (p.equals(n.point)) {
            return n;
        }
        int comp = comparePoints(p, n.point, orientation);
        if (comp < 0) {
            n.left = add(p, n.left, !orientation);
        } else if (comp >= 0) {
            n.right = add(p, n.right, !orientation);
        } else {
            return n;
        }
        return n;
    }
    public KDTree(List<Point> points) {
        for (Point p: points) {
            root = add(p, root, HORIZONTAL);
        }
    }
    @Override
    public Point nearest(double x, double y) {
        Point goal = new Point(x, y);
        Node nearestNode = nearest(root, goal, root);
        return nearestNode.point;
    }

    private Node nearest(Node n, Point goal, Node best) {
        Node goodSide = null;
        Node badSide = null;
        if (n == null) {
            return best;
        }
        if (n.point.distance(n.point, goal) < best.point.distance(best.point, goal)) {
            best = n;
        }
        double x = comparator(n, goal);
        if (x > 0) {
            goodSide = n.left;
            badSide = n.right;
        } else {
            goodSide = n.right;
            badSide = n.left;
        }
        best = nearest(goodSide, goal, best);
        Point bestBad = bestBadSidePoint(n, goal);
        if (Point.distance(bestBad, goal) < Point.distance(best.point, goal)) {
            best = nearest(badSide, goal, best);
        }
        return best;
    }

    private double comparator(Node n, Point goal) {
        if (n.orientation == HORIZONTAL) {
            return n.point.getX() - goal.getX();
        } else {
            return n.point.getY() - goal.getY();
        }
    }

    private int comparePoints(Point p, Point p2, boolean orientation) {
        if (orientation == HORIZONTAL) {
            return Double.compare(p.getX(), p2.getX());
        } else {
            return Double.compare(p.getY(), p2.getY());
        }
    }

    private Point bestBadSidePoint(Node n, Point p) {
        if (n.orientation == HORIZONTAL) {
            p = new Point(n.point.getX(), p.getY());
        } else {
            p = new Point(p.getX(), n.point.getY());
        }
        return p;
    }

    public static void main(String[] args) {
        Point p1 = new Point(2, 3);
        Point p2 = new Point(4, 2);
        Point p3 = new Point(4, 2);
        Point p4 = new Point(4, 5);
        Point p5 = new Point(3, 3);
        Point p6 = new Point(1, 5);
        Point p7 = new Point(4, 4);

        KDTree kd = new KDTree(List.of(p1, p2, p3, p4, p5, p6, p7));


    }
}
