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
        System.out.println(params);

        int depth = getDepth(params.get("lrlon"), params.get("ullon"), params.get("w"));
        double upLeftLon = params.get("ullon");
        double upLeftLat = params.get("ullat");
        double lowRightLon = params.get("lrlon");
        double lowRightLat = params.get("lrlat");


        double tileWidth = sizeOfTile(ROOT_WIDTH, depth);
        double tileHeight = sizeOfTile(ROOT_HEIGHT, depth);


        //todo you can refactor this to be just half the methods if you make all of your searching from the top left corner.
        //todo put all of the functionality to get these in a separate class
        //todo: It would make more sense to have a tile object and pass it all around


        // you're getting wrong because you're feeding the query coordinates
        Tile topLeftTile = getTopLeftTile(depth, tileHeight, tileWidth, upLeftLon, upLeftLat);
        Tile bottomRightTile = getBottomRightTile(depth, tileHeight, tileWidth, lowRightLon, lowRightLat);

        Tile[][] queryGrid = getQueryGrid(depth, topLeftTile, bottomRightTile);



        String[][] tileNames = convertTilestoStrings(queryGrid);

        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", tileNames);
        results.put("raster_ul_lon", topLeftTile.getUllon());
        results.put("raster_ul_lat", topLeftTile.getUllat());
        results.put("raster_lr_lon", bottomRightTile.getLrlon());
        results.put("raster_lr_lat", bottomRightTile.getLrlat());
        results.put("depth", depth);
        results.put("query_success", true);
//        results = helloWorlding(results);
        System.out.println(results);
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

    private static Tile[][] getQueryGrid(int depth, Tile topLeftTile, Tile bottomRightTile) {
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

    private static Tile getTopLeftTile(int depth, double tileHeight, double tileWidth, double upLeftLon, double upLeftLat) {
        int topLeftY = getTopLeftY(depth, tileHeight, upLeftLat);
        int topLeftX = getTopLeftX(depth, tileWidth, upLeftLon);
        Tile result = new Tile(depth, topLeftX, topLeftY);
        result.setUllon(MapServer.ROOT_ULLON + (tileWidth * topLeftX));
        result.setUllat(MapServer.ROOT_ULLAT - (tileHeight * topLeftY));
        return result;
    }


    private static int getTopLeftX(int depth, double tileWidth, double upLeftLon) {
        int numOfTiles = (int) Math.pow(2,depth);
        double currentBottomCorner = MapServer.ROOT_ULLON + tileWidth;
        for (int tileIndex = 0; tileIndex < numOfTiles ; tileIndex++) {
            if (currentBottomCorner > upLeftLon) { //todo does it matter if this is >= or > ??? Calc bottom right needed it.
                return tileIndex;
            }
            currentBottomCorner += tileWidth;
        }
        return -1;
    }

    private static int getTopLeftY(int depth, double tileHeight, double upLeftLat) {
        int numOfTiles = (int) Math.pow(2,depth);
        double currentBottomCorner = MapServer.ROOT_ULLAT - tileHeight;
        for (int tileIndex = 0; tileIndex < numOfTiles; tileIndex++) {
            if (currentBottomCorner < upLeftLat) { //todo does it matter if this is >= or > ??? Calc bottom right needed it.
                return tileIndex;
            }
            currentBottomCorner -= tileHeight;
        }
        return -1;
    }

    private static Tile getBottomRightTile(int depth, double tileHeight, double tileWidth, double lowRightLon, double lowRightLat) {
        int botLeftY = getBotLeftY(depth, tileHeight, lowRightLat);
        int botLeftX = getBotLeftX(depth, tileWidth, lowRightLon);
        Tile result = new Tile(depth, botLeftX, botLeftY);
        result.setLrlon(MapServer.ROOT_ULLON + (tileWidth * (botLeftX + 1)));
        result.setLrlat(MapServer.ROOT_ULLAT - (tileHeight * (botLeftY + 1)));
        return result;
    }

    private static int getBotLeftX(int depth, double tileWidth, double lowRightLon) {
        int numOfTiles = (int) Math.pow(2,depth);
        double currentBottomCorner = MapServer.ROOT_LRLON - tileWidth;
        for (int tileIndex = numOfTiles - 1; tileIndex >= 0; tileIndex--) {
            if (currentBottomCorner <= lowRightLon) {
                return tileIndex;
            }
            currentBottomCorner -= tileWidth;
        }
        return -1;
    }

    private static int getBotLeftY(int depth, double tileHeight, double lowRightLat) {
        int numOfTiles = (int) Math.pow(2,depth);
        double currentBottomCorner = MapServer.ROOT_LRLAT + tileHeight;
        for (int tileIndex = numOfTiles - 1; tileIndex >= 0; tileIndex--) {
            if (currentBottomCorner >= lowRightLat) {
                return tileIndex;
            }
            currentBottomCorner += tileHeight;
        }
        return -1;
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

    private static double calcLonDPP(double lowRightLon, double upLeftLon, double boxWidth) { //tested
        return ((lowRightLon - upLeftLon) / boxWidth);
    }

    private static double calcLowRightLon(int depth) { //tested
        double totalLonLength = MapServer.ROOT_ULLON - MapServer.ROOT_LRLON;
        double tileLength = totalLonLength / (Math.pow(2, depth));
        double lowRightLon = MapServer.ROOT_ULLON - tileLength;
        return lowRightLon;
    }

    private static int calcNumberOfTiles(int depth, double windowUpLeftLon, double windowlowRightLon) {
        return 0;
    }

    /** returns longitudinal or latitudinal length/width of tile based on zoom level
     * Maybe I don't understand this. Maybe I'm never supposed to calculate
     * the latitude
     * distance of latitude is different depending where you are above/below the
     * sekidou
     * */
    private static double sizeOfTile(double distance, int depth) {
        return distance / Math.pow(2,depth);
    }

    public static void main (String[] args) {
        int depth = 0;
        double tileWidth = sizeOfTile(ROOT_WIDTH, depth);
        double tileHeight = sizeOfTile(ROOT_HEIGHT, depth);
        System.out.println(getTopLeftTile(depth,tileHeight, tileWidth, MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT ));
        System.out.println(getBottomRightTile(depth,tileHeight, tileWidth, MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT ));

        depth = 7;
        tileWidth = sizeOfTile(ROOT_WIDTH, depth);
        tileHeight = sizeOfTile(ROOT_HEIGHT, depth);
        System.out.println(getTopLeftTile(depth,tileHeight, tileWidth, -122.241632, 37.87655));
        System.out.println(getBottomRightTile(depth,tileHeight, tileWidth, -122.24054, 37.87548));

        depth = 7;
        tileWidth = sizeOfTile(ROOT_WIDTH, depth);
        tileHeight = sizeOfTile(ROOT_HEIGHT, depth);
        System.out.println(getTopLeftTile(depth,tileHeight, tileWidth, -122.21260070800781, 37.82334456722848));
        System.out.println(getBottomRightTile(depth,tileHeight, tileWidth, -122.2119140625, 38.82280243352756));

//        System.out.println(sizeOfTile(ROOT_WIDTH, 0));
//        System.out.println(calcLonDPP(MapServer.ROOT_LRLON, MapServer.ROOT_ULLON, 512));
//        System.out.println(getDepth(MapServer.ROOT_LRLON, MapServer.ROOT_ULLON, 513));
    }
}
