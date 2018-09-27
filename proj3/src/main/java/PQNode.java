import java.util.List;

public class PQNode implements Comparable<PQNode> {
    private Node node;
    private double distance;

    public PQNode(Node node) {
        this.node = node;
    }

    public PQNode(Node node, double distance) {
    this.node = node;
    this.distance = distance;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getID() {
        return this.node.getId();
    }

    public List<Node> getAdjacentNodes() {
        return this.node.getAdjacentNodes();
    }

    @Override
    public int compareTo(PQNode o) {
        if (this.distance < o.getDistance()) { return -1;} //we are less than... right!?
        if (this.distance > o.getDistance()) { return 1;}
        else { return 0;}
    }
}
