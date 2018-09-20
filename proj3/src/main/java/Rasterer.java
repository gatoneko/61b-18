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

    private static final double ROOT_HEIGHT = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;
    private static final double ROOT_WIDTH = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON;

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

        int depth = getDepth(params.get("lrlon"), params.get("ullon"), params.get("w"));
        double upLeftLon = params.get("ullon");
        double upLeftLat = params.get("ullat");
        double lowRightLon = params.get("lrlon");
        double lowRightLat = params.get("lrlat");


        double tileWidth = sizeOfTile(ROOT_WIDTH, depth);
        double tileHeight = sizeOfTile(ROOT_HEIGHT, depth);



        Tile topLeftTile        = getCornerTile(depth, tileHeight, tileWidth, upLeftLon, upLeftLat);
        Tile bottomRightTile    = getCornerTile(depth, tileHeight, tileWidth, lowRightLon, lowRightLat);

        Tile[][] queryGrid = getQueryGrid(depth, topLeftTile, bottomRightTile);

        //todo make a method that sanitizes request. if invalid request just return a query_success = false


        String[][] tileNames = convertTilestoStrings(queryGrid);
        Map<String, Object> results = buildResults(tileNames, topLeftTile.getUllon(), topLeftTile.getUllat(), bottomRightTile.getLrlon(), bottomRightTile.getLrlat(), depth);

        System.out.println("Paramaters: " + params);
        System.out.println("Results: " + results);

        return results;
    }

    private Map<String, Object> buildResults(String[][] tileNames, double ullon, double ullat, double lrlon, double lrlat, int depth) {
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", tileNames);
        results.put("raster_ul_lon", ullon);
        results.put("raster_ul_lat", ullat);
        results.put("raster_lr_lon", lrlon);
        results.put("raster_lr_lat", lrlat);
        results.put("depth", depth);
        results.put("query_success", true);
        return results;
    }

    private String[][] convertTilestoStrings(Tile[][] queryGrid) {

        int ylength = queryGrid.length;
        int xlength = queryGrid[0].length; //array should never be jagged

        String[][] result = new String[ylength][xlength];

        for (int i = 0; i < ylength; i++) {
            for (int j = 0; j < xlength; j++) {
                result[i][j] = queryGrid[i][j].toString();
            }
        }
        return result;
    }

    private Tile[][] getQueryGrid(int depth, Tile topLeftTile, Tile bottomRightTile) {
        int width =  bottomRightTile.getxVal() - topLeftTile.getxVal() + 1; //plus one for inclusive width/height
        int height=  bottomRightTile.getyVal() - topLeftTile.getyVal() + 1;

        int startX = topLeftTile.getxVal();
        int startY = topLeftTile.getyVal();

        int currentX = startX;
        Tile[][] queryGrid = new Tile[height][width];

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++) {
                queryGrid[i][j] = new Tile(depth, currentX, startY);
                currentX++;
            }
            startY++;
            currentX = startX;
        }
        return queryGrid;
    }

    private Tile getCornerTile(int depth, double tileHeight, double tileWidth, double queryLon, double queryLat) {
        int tileLrlon = getTileLrlon(depth, tileWidth, queryLon);
        int tileLrlat = getTileLrlat(depth, tileHeight, queryLat);
        Tile result = new Tile(depth, tileLrlon, tileLrlat);
        result.setUllon(MapServer.ROOT_ULLON + (tileWidth * tileLrlon));
        result.setUllat(MapServer.ROOT_ULLAT - (tileHeight * tileLrlat));
        result.setLrlon(MapServer.ROOT_ULLON + (tileWidth * (tileLrlon + 1)));
        result.setLrlat(MapServer.ROOT_ULLAT - (tileHeight * (tileLrlat + 1)));
        return result;
    }

    private int getTileLrlon(int depth, double tileWidth, double queryPoint) {
        int numOfTiles = (int) Math.pow(2,depth);
        double currentBottomCorner = MapServer.ROOT_ULLON + tileWidth;
        for (int tileIndex = 0; tileIndex < numOfTiles ; tileIndex++) {
            if (currentBottomCorner > queryPoint) {
                return tileIndex;
            }
            currentBottomCorner += tileWidth;
        }
        return (numOfTiles - 1);
    }

    private int getTileLrlat(int depth, double tileHeight, double queryPoint) {
        int numOfTiles = (int) Math.pow(2,depth);
        double currentBottomCorner = MapServer.ROOT_ULLAT - tileHeight;
        for (int tileIndex = 0; tileIndex < numOfTiles; tileIndex++) {
            if (currentBottomCorner < queryPoint) {
                return tileIndex;
            }
            currentBottomCorner -= tileHeight;
        }
        return (numOfTiles - 1);
    }


    /** Returns depth. from 0 to 7. -1 is error */
    private int getDepth(double lowRightLon, double upLeftLon, double width) { //It's odd that the size in pixels was a floating number.. you can't have half a pixel... This might be some fucked case test where a monitor with 1.5 pixels
        //Future todo sanitize screen size input to integers.

        double windowLonDPP = calcLonDPP(lowRightLon, upLeftLon, width);

        double tilelowRightLon = calcLowRightLon(MAX_DEPTH);
        double tileLonDPP  = calcLonDPP(tilelowRightLon, MapServer.ROOT_ULLON, MapServer.TILE_SIZE);

        if (windowLonDPP < tileLonDPP) {
            return MAX_DEPTH;
        }
        for (int depth = 0; depth <= MAX_DEPTH; depth++) {
            tilelowRightLon = calcLowRightLon(depth);
            tileLonDPP = calcLonDPP(tilelowRightLon, MapServer.ROOT_ULLON, MapServer.TILE_SIZE);
            if (tileLonDPP <= windowLonDPP)  {
                return depth;
            }
        }

        return -1;
    }

    private double calcLonDPP(double lowRightLon, double upLeftLon, double boxWidth) { //tested
        return ((lowRightLon - upLeftLon) / boxWidth);
    }

    private double calcLowRightLon(int depth) { //tested
        double totalLonLength = MapServer.ROOT_ULLON - MapServer.ROOT_LRLON;
        double tileLength = totalLonLength / (Math.pow(2, depth));
        double lowRightLon = MapServer.ROOT_ULLON - tileLength;
        return lowRightLon;
    }


    /** returns longitudinal or latitudinal length/width of tile based on zoom level
     * Maybe I don't understand this. Maybe I'm never supposed to calculate
     * the latitude
     * distance of latitude is different depending where you are above/below the
     * sekidou
     * */
    private  double sizeOfTile(double distance, int depth) {
        return distance / Math.pow(2,depth);
    }

    public static void main (String[] args) {

    }
}
