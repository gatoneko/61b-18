import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Node {
    private long id;
    private double lon;
    private double lat;
    private String name;
    private List<Edge> partOfEdges;


    private List<Way> partOfWays;
    private List<Node> adjacentNodes;

    public boolean isConnected() {
        return !adjacentNodes.isEmpty();
    }

    public Node(long id, double lon, double lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.partOfEdges = new ArrayList<>();
        this.partOfWays = new ArrayList<>();
        this.adjacentNodes = new ArrayList<>();
    }

    public void addEdge(Node n) {
        adjacentNodes.add(n);
    }

    public void addWay(Way w) {
        partOfWays.add(w);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public long getId() {
        return id;
    }

    public List<Way> getPartOfWays() {
        return partOfWays;
    }

    public List<Node> getAdjacentNodes() {
        return adjacentNodes;
    }
}
