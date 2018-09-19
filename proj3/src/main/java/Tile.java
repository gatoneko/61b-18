public class Tile {
    private int depth;
    private int xVal;
    private int yVal;


    public Tile(int depth, int xVal, int yVal) {
        this.depth = depth;
        this.xVal = xVal;
        this.yVal = yVal;
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
                "_y" + yVal;
    }
}
