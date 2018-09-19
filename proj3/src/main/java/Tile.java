public class Tile {

    private int depth;
    private int xVal;
    private int yVal;
    private double ullon;
    private double ullat;
    private double lrlon;
    private double lrlat;


    public Tile(int depth, int xVal, int yVal) {
        this.depth = depth;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public double getUllon() {
        return ullon;
    }

    public void setUllon(double ullon) {
        this.ullon = ullon;
    }

    public double getUllat() {
        return ullat;
    }

    public void setUllat(double ullat) {
        this.ullat = ullat;
    }

    public double getLrlon() {
        return lrlon;
    }

    public void setLrlon(double lrlon) {
        this.lrlon = lrlon;
    }

    public double getLrlat() {
        return lrlat;
    }

    public void setLrlat(double lrlat) {
        this.lrlat = lrlat;
    }



    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getxVal() {
        return xVal;
    }

    public void setxVal(int xVal) {
        this.xVal = xVal;
    }

    public int getyVal() {
        return yVal;
    }

    public void setyVal(int yVal) {
        this.yVal = yVal;
    }

    @Override
    public String toString() {
        return "" +
                "d" + depth +
                "_x" + xVal +
                "_y" + yVal +
                ".png";
    }
}
