import java.util.ArrayList;

public class Way {
    private ArrayList<Node> way;
    private long id;
    private float distance;
    private int maxSpeed; //implementing this is optional
    private String name; //for road names

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Way(long id, ArrayList<Node> nodes) {
        this.id = id;
        this.way = nodes; //is a shallow copy enough?
        //distance = getDistance(way);
    }

    public ArrayList<Node> getWay() {
        return way;
    }

    private float getDistance(ArrayList<Node> way) {
        return 0;
    }
}
