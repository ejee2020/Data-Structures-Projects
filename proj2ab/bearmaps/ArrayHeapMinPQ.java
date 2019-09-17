package bearmaps;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class ArrayHeapMinPQ<T> implements ExtrinsicMinPQ<T> {
    private ArrayList<PriorityNode> minheap;
    private int size = 0;
    private TreeMap<T, Integer> tree;
    private class PriorityNode {
        private T item;
        private double priority;
        PriorityNode(T item2, double priority2) {
            item = item2;
            priority = priority2;
        }
    }

    public ArrayHeapMinPQ() {
        minheap = new ArrayList<PriorityNode>();
        minheap.add(null);
        tree = new TreeMap<T, Integer>();
    }

    @Override
    public void add(T item, double priority) {
        PriorityNode temp = new PriorityNode(item, priority);
        if (this.contains(item)) {
            throw new IllegalArgumentException();
        }
        minheap.add(temp);
        tree.put(temp.item, size + 1);
        size += 1;
        swim(size);
    }

    @Override
    public boolean contains(T item) {
        return (tree.containsKey(item));
    }

    @Override
    public T getSmallest() {
        if (minheap.get(1) == null) {
            throw new NoSuchElementException();
        }
        return minheap.get(1).item;
    }

    @Override
    public T removeSmallest() {
        PriorityNode temp = minheap.get(1);
        if (minheap.get(1) == null) {
            throw new NoSuchElementException();
        }
        swap(1, size);
        minheap.set(size, null);
        tree.remove(temp.item);
        size -= 1;
        sink(1);
        return temp.item;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    /* First need to find a way to get the index of the item placed(must be log_n).
       Then, change the priority of that Node. Finally, go over if statements to
       ensure that heap is preserved.
     */
    public void changePriority(T item, double priority) {
        if (!this.contains(item)) {
            throw new NoSuchElementException();
        }
        int me = tree.get(item);
        minheap.get(me).priority = priority;
        double myPriority = priority;
        int left = leftChild(me);
        int right = rightChild(me);
        int parent = parent(me);

        /*When it is at the top */
        if (parent == 0) {
            sink(me);
            return;
        }
        /*When it is at the bottom */
        if (left > size && right > size) {
            swim(me);
            return;
        }

        /*When it has to go down */
        if (myPriority > minheap.get(left).priority || myPriority > minheap.get(left).priority) {
            sink(me);
            return;
        }

        /*When it has to go up */
        if (myPriority < minheap.get(parent).priority) {
            swim(me);
            return;
        }
    }

    private void swap(int i, int j) {
        PriorityNode temp = minheap.get(i);
        T itemToBeReplaced =  minheap.get(j).item;
        T itemTemporary = minheap.get(i).item;
        tree.replace(itemTemporary, j);
        tree.replace(itemToBeReplaced, i);
        minheap.set(i, minheap.get(j));
        minheap.set(j, temp);
    }
    /* Should do something with indexOf! Only put, get, containsKey should be used */
    private void swim(int index) {
        int parent = parent(index);
        if (minheap.get(parent) == null) {
            return;
        }
        if (minheap.get(parent).priority > minheap.get(index).priority) {
            swap(index, parent);
            swim(parent);
        }
    }

    /* Should do something with indexOf! Only put, get, containsKey should be used */
    private void sink(int index) {
        int parent = parent(index);
        int left = leftChild(index);
        int right = rightChild(index);
        double mePriority = minheap.get(index).priority;
        /* when it is at a leaf */
        if (right > size && left > size) {
            return;
        }
        /* if it is the right position*/
        if (mePriority < minheap.get(left).priority && (right > size
                || mePriority < minheap.get(right).priority)) {
            return;
        }
        if (minheap.get(right) == null) {
            swap(index, left);
            sink(left);
        } else if (minheap.get(left).priority < minheap.get(right).priority) {
            swap(index, left);
            sink(left);
        } else if (minheap.get(left).priority == minheap.get(right).priority) {
            swap(index, left);
            sink(left);
        } else {
            swap(index, right);
            sink(right);
        }
    }

    private int leftChild(double priority) {
        return (int) priority * 2;
    }

    private int rightChild(double priority) {
        return (int) priority * 2 + 1;
    }

    private int parent(double priority) {
        return (int) priority / 2;
    }
}
