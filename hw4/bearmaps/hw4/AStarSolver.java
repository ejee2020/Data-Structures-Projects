package bearmaps.hw4;
import bearmaps.proj2ab.DoubleMapPQ;
import edu.princeton.cs.algs4.Stopwatch;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import static bearmaps.hw4.SolverOutcome.SOLVED;
public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {
    private SolverOutcome outcome;
    private double timeSpent;
    private DoubleMapPQ pq;
    private HashMap<Vertex, Double> distTo;
    private HashMap<Vertex, Vertex> edgeTo;
    private HashMap<Vertex, Double> heuristic;
    private Vertex target;
    private Vertex starter;
    private int times;

    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex end, double timeout) {
        Stopwatch sw = new Stopwatch();
        this.target = end;
        this.starter = start;
        pq = new DoubleMapPQ();
        pq.add(start, 0 + input.estimatedDistanceToGoal(start, end));
        heuristic = new HashMap<Vertex, Double>();
        distTo = new HashMap<Vertex, Double>();
        edgeTo = new HashMap<Vertex, Vertex>();
        distTo.put(start, (double) 0);
        edgeTo.put(start, null);
        heuristic.put(start, input.estimatedDistanceToGoal(start, end));
        while (pq.size() != 0 && !pq.getSmallest().equals(end) && timeSpent <= timeout) {
            Vertex p = (Vertex) pq.removeSmallest();
            times += 1;
            timeSpent = sw.elapsedTime();
            List<WeightedEdge<Vertex>> neighborEdges = input.neighbors(p);
            for (WeightedEdge<Vertex> e : neighborEdges) {
                heuristic.put(e.to(), input.estimatedDistanceToGoal(e.to(), end));
                relax(e);
            }
        }
        if (timeSpent > timeout) {
            outcome = SolverOutcome.TIMEOUT;
        } else if (pq.size() == 0 && !edgeTo.containsKey(end)) {
            outcome = SolverOutcome.UNSOLVABLE;
        } else {
            outcome = SolverOutcome.SOLVED;
        }
    }
    @Override
    public SolverOutcome outcome() {
        return outcome;
    }

    @Override
    /** A list of vertices corresponding to the answer*/
    public List<Vertex> solution() {
        if (this.outcome != SOLVED) {
            return new ArrayList<Vertex>();
        }
        Vertex end1 = this.target;
        Vertex start = this.starter;
        List realSolution = new ArrayList<Vertex>();
        realSolution.add(end1);
        while (!end1.equals(start)) {
            realSolution.add(edgeTo.get(end1));
            end1 = edgeTo.get(end1);
        }
        Collections.reverse(realSolution);
        return realSolution;
    }
    @Override
    /** returns a total weight of the solution*/
    public double solutionWeight() {
        if (this.outcome != SOLVED) {
            return 0;
        }
        return (double) distTo.get(this.target);
    }

    /** The total number of priority queue dequeue operations */
    @Override
    public int numStatesExplored() {
        return times;
    }

    /**The total time spent in seconds by the constructor */
    @Override
    public double explorationTime() {
        return timeSpent;
    }

    private void relax(WeightedEdge e) {
        Vertex p = (Vertex) e.from();
        Vertex q = (Vertex) e.to();
        double w = e.weight();
        if (!distTo.containsKey(q)) {
            distTo.put(q, (double) Integer.MAX_VALUE);
        }
        if (distTo.get(p) + w <= distTo.get(q)) {
            distTo.put(q, (double) distTo.get(p) + w);
            edgeTo.put(q, p);
            if (pq.contains(q)) {
                pq.changePriority(q, (double) distTo.get(q) + (double) heuristic.get(q));
            } else {
                pq.add(q, (double) distTo.get(q) + (double) heuristic.get(q));
            }
        }
    }
}
