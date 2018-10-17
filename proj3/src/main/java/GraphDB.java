import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.lang.reflect.Array;
import java.util.*;

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

    private HashMap<Long, Node> nodes; //connected unnamed shit..
    private HashMap<Long, Way> ways;
    private HashMap<Long, Node> locations; //nodes of locations...
    private HashMap<String, List<Node>> cleanedLocations;
    private TrieSet prefixTree;
    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        nodes = new HashMap<>();
        ways = new HashMap<>();
        locations = new HashMap<>();
        cleanedLocations = new HashMap<>();
        prefixTree = new TrieSet();

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
        clean();
        cleanLocations();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void cleanLocations() {
        Iterator<Node> i = this.locations.values().iterator();
        while (i.hasNext()) {
            Node n = i.next();
            n.setCleanedName(cleanString(n.getName()));
            addToCleanedLocations(n.getCleanedName(), n);
            prefixTree.put(n.getCleanedName());
        }
    }

    private void addToCleanedLocations(String cleanedName, Node n) {
        if (cleanedLocations.get(cleanedName) == null) {
            cleanedLocations.put(cleanedName, new ArrayList<Node>());
            cleanedLocations.get(cleanedName).add(n);
        } else {
            cleanedLocations.get(cleanedName).add(n);
        }
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
        /* This removes all unconnected nodes, even ones like intersections and buildings */
        /* 2018/9/26 deciding to not call removeNode()*/
        Iterator<Node> i = this.nodes.values().iterator();
        while (i.hasNext()) {
            if (!i.next().isConnected()) {
                i.remove();
            }
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
       ArrayList<Node> listOfNodes = new ArrayList<>(this.nodes.values());
       ArrayList<Long> ids = new ArrayList<>(listOfNodes.size());
       for (Node node : listOfNodes) {
           ids.add(node.getId());
       }
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

    /* TODO isn't that codesmelly ? */
    /** CY distance comparing nodes
     * I'm using this because I find closest by making a node that isn't part
     * of the graph. So we need to compare two nodes where one isn't in the graph
     */
    double distance(Node v, Node w) {
        return distance(v.getLon(), v.getLat(), w.getLon(), w.getLat());
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
        Node query = new Node(-1L, lon, lat);
        Node closestNode = new Node(-1L, 0,0);
        double closestDistance = 999999999;
        for (Node n : this.nodes.values()) {
            double currentDistance = distance(n, query);
            if (currentDistance < closestDistance) {
                closestNode = n;
                closestDistance = currentDistance;
            }
        }
        return closestNode.getId();
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return this.nodes.containsKey(v) ? this.nodes.get(v).getLon() : -1; //-1 means invalid input
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return this.nodes.containsKey(v) ? this.nodes.get(v).getLat() : -1;
    }


    public Node addNode(long id, double lon, double lat) {
        Node result = new Node(id, lon, lat);
        nodes.put(id, result);
        return result;
    }

    private void removeNode(Node n) {
        this.nodes.remove(n.getId());
    }

    public void addEdges(ArrayList<Node> way) {
        if (way.size() <= 1) {return;}
        for (int i = 0; i < way.size() - 1; i++) {
            way.get(i).addEdge(way.get(i + 1));
            way.get(i + 1).addEdge(way.get(i));
        }
    }

    public Way addWay(long id, ArrayList<Long> listOfLongs) {
        Way way = new Way(id);
        Node currentNode;
        for (Long ndRef : listOfLongs) {
            currentNode = this.nodes.get(ndRef);
            way.addNode(currentNode);
            currentNode.addWay(way); //adds the way to the Node's list of ways its in
        }
        this.ways.put(id, way); //saves way in hashmap
        addEdges(way.getWay());//creates edges between nodes
        return way;
    }

    public Node addLocation(Node n) {
        locations.put(n.getId(), n);
        return n;
    }

    public Node getNode(Long id) {
        return nodes.get(id);
    }

    public List<Map<String, Object>> getLocations(String locationName) {
        locationName = cleanString(locationName);
        List<Node> matchingQueries = getLocationsByName(locationName);
        List<Map<String,Object>> result = turnNodesToJson(matchingQueries);
        return result;
    }

    public List<Node> getLocationsByName(String queryName) {
        return cleanedLocations.get(queryName);
    }

    private List<Map<String, Object>> turnNodesToJson(List<Node> matchingQueries) {
        List<Map<String,Object>> result = new ArrayList<>();
        for(Node n : matchingQueries) {
            result.add(nodeToJson(n));
        }
        return result;
    }

    private Map<String, Object> nodeToJson(Node n) {
        Map<String, Object> result = new HashMap<>();
        result.put("lat", n.getLat());
        result.put("lon", n.getLon());
        result.put("name", n.getName());
        result.put("id", n.getId());
        return result;
    }

    public Way getWay(Long id) {
        return this.ways.get(id);
    }

    public int getNodeSize() {
        return this.nodes.size();
    }

    public List<String> getLocationsByPrefix(String prefix) {
//        Set<String> result = new HashSet<>();
//        int prefixLength = prefix.length();
//        Iterator<Node> i = this.locations.values().iterator();
//        while (i.hasNext()) {
//            Node n = i.next();
//            String nodeName = n.getCleanedName();
//            if(nodeName.length() < prefixLength) { continue; }
//            try {
//                if (nodeName.substring(0, prefixLength).equals(prefix)) {
//                    result.add(n.getName());
//                }
//            } catch (Exception e) {
//                System.out.println(nodeName);
//            }
//        }
//        return new ArrayList<>(result);
        prefix = cleanString(prefix);
        return prefixTree.getMatches(prefix);
    }
}
