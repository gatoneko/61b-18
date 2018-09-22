import java.util.LinkedList;

public class Edge {
    private LinkedList<Node> way;
    private long id;
    private float distance;

    public Edge(long id, LinkedList<Node> nodes) {
        this.id = id;
        way = nodes; //is a shallow copy enough?
        //distance = getDistance(way);
    }

    private float getDistance(LinkedList<Node> way) {
        return 0;
    }
}
