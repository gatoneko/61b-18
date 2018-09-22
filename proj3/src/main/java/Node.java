import java.util.LinkedList;
import java.util.List;

public class Node {
    private long id;
    private double lon;
    private double lat;
    private String name;
//    private List<Edge> partOfEdges;
    private List<Node> adjacentNodes;

    public boolean isConnected() {
        return !adjacentNodes.isEmpty();
    }

    public Node(long id, double lon, double lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
