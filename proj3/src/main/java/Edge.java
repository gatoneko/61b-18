import java.util.ArrayList;

public class Edge {
    private ArrayList<Node> way;
    private long id;
    private float distance;
    private int maxSpeed; //implementing this is optional

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name; //for road names

    public Edge(long id, ArrayList<Node> nodes) {
        this.id = id;
        way = nodes; //is a shallow copy enough?
        //distance = getDistance(way);
    }

    private float getDistance(ArrayList<Node> way) {
        return 0;
    }
}
