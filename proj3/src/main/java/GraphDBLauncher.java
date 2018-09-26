import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * This class provides a main method for experimenting with GraphDB construction.
 * You could also use MapServer, but this class lets you play around with
 * GraphDB in isolation from all the rest of the parts of this assignment.
 */
public class GraphDBLauncher {
    private static final String OSM_DB_PATH = "../library-sp18/data/berkeley-2018.osm.xml";

    public static void main(String[] args) {
        GraphDB g = new GraphDB(OSM_DB_PATH);

        Iterable<Long> verticesIterable = g.vertices();

        /* Convert iterable to a list. */
        List<Long> vertices = new ArrayList<Long>();
        for (long v : verticesIterable) {
            vertices.add(v);
        }

        System.out.println("There are " + vertices.size() + " vertices in the graph.");

        System.out.println("The first 10 vertices are:");
        for (int i = 0; i < 10; i += 1) {
            if (i < vertices.size()) {
                System.out.println(vertices.get(i));
            }
        }

        long v = g.closest(-122.258207, 37.875352);
        System.out.print("The vertex number closest to -122.258207, 37.875352 is " + v + ", which");
        System.out.println(" has longitude, latitude of: " + g.lon(v) + ", " + g.lat(v));

        System.out.println("To get started, uncomment print statements in GraphBuildingHandler.");
        System.out.println("Chaat Café: " + g.getNode((long)541001019).getName());
        System.out.println("Prince Street: " + g.getWay((long)6344484).getName());
        System.out.println("id4691008868's lon is -122.2648893: " + g.lon(4691008868L));
        System.out.println("id4691008868's lat is 37.8314504: " + g.lat(4691008868L));
        Iterable<Long> ids = g.vertices();
        System.out.println("---------------");
        System.out.println(g.getNodeSize());
//        g.clean();
        System.out.println(g.getNodeSize());

    }
}
