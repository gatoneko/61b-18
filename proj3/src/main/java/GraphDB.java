import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Way, etc. */
    private HashMap<Long, Node> nodes;
    private HashMap<Long, Way> ways;
    private LinkedList<Node> graph;

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        nodes = new HashMap<>();
        ways = new HashMap<>();
        graph = new LinkedList<>();
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        populateGraph();
        convertWaystoGraph();
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        /** This removes all unconnected nodes, even ones like intersections and buildings */

        Iterator<Node> i = graph.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            if (!n.isConnected()) {
                i.remove();
            }
        }
        System.out.println("cleaned!");
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
       ArrayList<Node> n = new ArrayList<Node>(nodes.values());
       ArrayList<Long> ids = new ArrayList<>(n.size());
       for (Node node : n) {
           ids.add(node.getId());
       }
//        ArrayList<Long> a = new ArrayList<>();
       return ids;
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        ArrayList<Node> adjacentNodes = new ArrayList<>(this.nodes.get(v).getAdjacentNodes());
        ArrayList<Long> result = new ArrayList<>();
        for (Node node : adjacentNodes) {
            result.add(node.getId());
        }
        return result;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        return 0;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return nodes.containsKey(v) ? nodes.get(v).getLon() : 0; //0 means invalid input
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return nodes.containsKey(v) ? nodes.get(v).getLat() : 0;
    }

    public Node addNode(long id, double lon, double lat) {
        Node result = new Node(id, lon, lat);
        nodes.put(id, result);
        return result;
    }

    public void removeNode(long id) {};

//    public Way addWay(Long id, Way way) {
//        ways.put(id, way);
//        return way;
//    }

    public void populateGraph() {
        for (Node n : nodes.values()) {
            graph.add(n);
        }
    }

    public void convertWaystoGraph() {
        for (Way way: this.ways.values()) {
            ArrayList<Node> listOfNodes = way.getWay();
            addNeighbors(listOfNodes);
        }
    }

    public void addNeighbors(ArrayList<Node> way) {
        if (way.size() <= 1) {return;}
        for (int i = 0; i < way.size() - 1; i++) {
            way.get(i).addNeighbor(way.get(i + 1));
            way.get(i + 1).addNeighbor(way.get(i));
        }

    }

    public Way addWay(long id, ArrayList<Long> listOfLongs) {
        ArrayList<Node> provisionalNodes = new ArrayList<>();
        for (Long ndRef : listOfLongs) {
            provisionalNodes.add(nodes.get(ndRef));
        }
        return addWayWithNodes(id, provisionalNodes);
    }

    private Way addWayWithNodes(long id, ArrayList<Node> listOfNodes) {
        Way way = new Way(id, listOfNodes);
        this.ways.put(id, way);
        for(Node node : listOfNodes) {
            node.addWay(way);
        }
        return way;
    }

    public Node getNode(Long id) {
        return nodes.get(id);
    }

    public Way getWay(Long id) {
        return this.ways.get(id);
    }

    public int getNodeSize() {
        return this.graph.size();
    }
}
