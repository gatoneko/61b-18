import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    private static final double STARTING_PATH = -999.9;
    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     * @param g The graph to use.
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        HashMap<Long, Double> distTo = new HashMap<>();
        HashMap<Long, Long> edgeTo = new HashMap<>();
        HashMap<Long, Boolean> marked = new HashMap<>();
        PriorityQueue<PQNode> fringe = new PriorityQueue<>();
        PQNode startNode = new PQNode(g.getNode(g.closest(stlon, stlat)));
        PQNode destinationNode = new PQNode(g.getNode(g.closest(destlon, destlat)));
        LinkedList<Long> result = new LinkedList<>();
        if (startNode.getNode() == null || destinationNode.getNode() == null) { //path to nowhere gets empty list
            return result;
        }
        /*todo this can all be broken into methods i think*/
        fringe.add(startNode);
        distTo.put(startNode.getID(), 0.0);
        edgeTo.put(startNode.getID(), null);
        while (!fringe.isEmpty()) {
//            evaluateTop()
            PQNode top = fringe.poll();
            if (marked.get(top.getID()) != null) {
                continue;
            }
            if (top.getID() == destinationNode.getID()) {
                break;
            }
            List<Node> adjacentNodes = top.getAdjacentNodes();
            for (Node node: adjacentNodes) {
//                relaxEdge(top, node, distTo, edgeTo, fringe, startNode);
                double distanceNodeToStart = top.getDistance() + g.distance(top.getID(), node.getId());
                if (distTo.get(node.getId()) == null || distanceNodeToStart < distTo.get(node.getId())) {
                    distTo.put(node.getId(), distanceNodeToStart);
                    edgeTo.put(node.getId(), top.getID());
                }
                fringe.add(new PQNode(node, distanceNodeToStart));
            }
            marked.put(top.getID(), true);
        }
        Long currentNode = destinationNode.getID();
        result.addFirst(currentNode);
        while (currentNode != null && currentNode != startNode.getID()) {
            result.addFirst(edgeTo.get(currentNode));
            currentNode = edgeTo.get(currentNode);
        }
        return result; // FIXME
    }


    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        List<NavigationDirection> result = new ArrayList<>();

        Node n = g.getNode(route.get(0));
        Node next = g.getNode(route.get(1));
        Way currentWay = getCommonWay(n, next);
        double distance = 0.0;
        double startingBearing = g.bearing(n.getId(), next.getId());
        double bearing = STARTING_PATH;
        for (int i = 0; i < route.size() - 1; i++) {
            n = g.getNode(route.get(i));
            next = g.getNode(route.get(i + 1));
            if (!hasCurrentWay(next, currentWay)) {
                result.add(new NavigationDirection(bearingToDirection(bearing), currentWay.getName(), distance));
                currentWay = getCommonWay(n, next);
                bearing = bearingDifference(startingBearing, g.bearing(n.getId(), next.getId()));
                startingBearing = g.bearing(n.getId(), next.getId()); //absolute baring
                distance = 0.0;
            } else {
                distance += (g.distance(n, next));
            }
        }
        System.out.println("Before: " + result);
        System.out.println();
        result = consolidateDirections(result);
        System.out.println("After: " + result);
        return result;
    }

    private static List<NavigationDirection> consolidateDirections(List<NavigationDirection> input) {
        Iterator<NavigationDirection> iter = input.iterator();
        List<NavigationDirection> result = new ArrayList<>();
        result.add(iter.next()); // off by 1?

        while (iter.hasNext()) {
            NavigationDirection currentNode = iter.next();
            NavigationDirection resultNode = result.get(result.size() - 1);
            if (resultNode != null
                    && matchDirections(currentNode, resultNode)
                    && currentNode.way.equals(resultNode.way)) {
                resultNode.distance += currentNode.distance;
            } else {
                result.add(currentNode);
            }
        }
        return result;
    }



    /** compiled from:
     * https://stackoverflow.com/questions/7570808/how-do-i-calculate-the-difference-of-two-angle-measures
     * @param startingBearing
     * @param newBearing
     * @return difference of the two angles
     */
    public static double bearingDifference(double startingBearing, double newBearing) {
        double phi = Math.abs(newBearing - startingBearing) % 360;
        double difference = phi > 180 ? 360 - phi : phi; //absolute value only
        int sign = (newBearing - startingBearing >= 0 && newBearing - startingBearing <= 180 )
//                || newBearing - startingBearing <= -180 && newBearing - startingBearing >= -360)
                ? 1 : -1;// -1 turn left, +1 turn right
        return sign*difference;
    }

    public static int bearingToDirection(double bearing) {
        if (bearing == STARTING_PATH) return 0;
        if (bearing >= -15 && bearing <= 15) return 1;
        if (bearing < -15 && bearing >= -30) return 2; //sl
        if (bearing > 15 && bearing <= 30) return 3; //sr

        if (bearing > 30 && bearing <= 100) return 4; //r
        if (bearing < -30 && bearing >= -100) return 5; //l
        if (bearing < -100) return 6; //sh l
        if (bearing > 100) return 7; // sh r
        else return 0;
    }

    public static boolean matchDirections(NavigationDirection a, NavigationDirection b) {
        if ((a.direction == 0 || a.direction == 1 || a.direction == 2 || a.direction == 3)
                && (b.direction == 0 || b.direction == 1 || b.direction == 2 || b.direction == 3)) {
            return true;
        } else {
            return false;
        }
    }

    private static Way getCommonWay(Node n, Node next) {
        List<Way> nWay = n.getPartOfWays();
        List<Way> nextWay = next.getPartOfWays();
        for (Way w : nWay) {
            for (Way v : nextWay) {
                if (w == v) {
                    return w;
                }
            }
        }
        return null;

    }

    private static boolean hasCurrentWay(Node next, Way currentWay) {
        List<Way> nextWay = next.getPartOfWays();
        for (Way w : nextWay) {
            if (w == currentWay) { return true; }
        }
        return false;
    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way. */
        public static final String UNKNOWN_ROAD = "unknown road";
        
        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction a given NavigationDirection represents.*/
        int direction;
        /** The name of the way I represent. */
        String way;
        /** The distance along this way I represent. */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        /**
         * Create a NavigationDirection with 3 importants filled in
         * @author Conrad
         * @return
         */
        public NavigationDirection(int direction, String way, double distance) {
            this.direction = direction;
            if (way == null) {this.way = UNKNOWN_ROAD;}
            else { this.way = way; }
            this.distance = distance;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                    && way.equals(((NavigationDirection) o).way)
                    && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
