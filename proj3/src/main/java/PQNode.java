public class PQNode implements Comparable<PQNode> {
    Node node;
    double distance;

    public PQNode(Node node) {
        this.node = node;
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

    @Override
    public int compareTo(PQNode o) {
        if (this.distance < o.getDistance()) { return -1;} //we are less than... right!?
        if (this.distance > o.getDistance()) { return 1;}
        else { return 0;}
    }
}
