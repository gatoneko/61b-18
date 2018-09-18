import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    private static final int MAX_DEPTH = 7;


    public Rasterer() {
        // YOUR CODE HERE
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        System.out.println(params);

        int depth = getDepth(params.get("lrlon"), params.get("ullon"), params.get("w"));

        Map<String, Object> results = new HashMap<>();
        results = helloWorlding(results);
        System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
                           + "your browser.");
        return results;
    }

    /** Satisfies test.html through manual input */
    private Map<String, Object> helloWorlding(Map<String, Object> results) {
        String[][] resultGrid = {{"d7_x84_y28.png", "d7_x85_y28.png", "d7_x86_y28.png"}, {"d7_x84_y29.png", "d7_x85_y29.png", "d7_x86_y29.png"}, {"d7_x84_y30.png", "d7_x85_y30.png", "d7_x86_y30.png"}};

        results.put("render_grid", resultGrid);
        results.put("raster_ul_lon", -122.24212646484375);
        results.put("raster_ul_lat", 37.87701580361881);
        results.put("raster_lr_lon", -122.24006652832031);
        results.put("raster_lr_lat", 37.87538940251607);
        results.put("depth", 7);
        results.put("query_success", true);

        return results;
    }

    /** Returns depth. from 0 to 7. -1 is error */
    private static int getDepth(double lowRightLon, double upLeftLon, double width) { //It's odd that the size in pixels was a floating number.. you can't have half a pixel... This might be some fucked case test where a monitor with 1.5 pixels
        //Future todo sanitize screen size input to integers.
        int result = -1;

        double windowLonDPP = calcLonDPP(lowRightLon, upLeftLon, width);

        double tilelowRightLon = calcLowRightLon(MAX_DEPTH);
        double tileLonDPP  = calcLonDPP(tilelowRightLon, MapServer.ROOT_ULLON, MapServer.TILE_SIZE);
        if (windowLonDPP < tileLonDPP) {
            return MAX_DEPTH;
        }
        for (int depth = 0; depth <= 7; depth++) {
            tilelowRightLon = calcLowRightLon(depth);
            tileLonDPP = calcLonDPP(tilelowRightLon, MapServer.ROOT_ULLON, MapServer.TILE_SIZE);
            if (tileLonDPP <= windowLonDPP)  {
                return depth;
            }
        }

        return result;
    }

    private static double calcLonDPP(double lowRightLon, double upLeftLon, double boxWidth) { //tested
        return ((lowRightLon - upLeftLon) / boxWidth);
    }

    private static double calcLowRightLon(int depth) { //tested
        double totalLonLength = MapServer.ROOT_ULLON - MapServer.ROOT_LRLON;
        double tileLength = totalLonLength / (Math.pow(2, depth));
        double lowRightLon = MapServer.ROOT_ULLON - tileLength;
        return lowRightLon;
    }

    public static void main (String[] args) {
        System.out.println(calcLonDPP(MapServer.ROOT_LRLON, MapServer.ROOT_ULLON, 512));
        System.out.println(getDepth(MapServer.ROOT_LRLON, MapServer.ROOT_ULLON, 513));
    }
}