package accesscontroltest.points;

public class Point {
    
    protected int x, y;
    void move(int dx, int dy) { x += dx; y += dy; }
    public void moveAlso(int dx, int dy) { move(dx, dy); }
    void warp(accesscontroltest.morepoints.Point3d a) {
        a.
    }
}

class PointList {
    Point next, prev;
}
