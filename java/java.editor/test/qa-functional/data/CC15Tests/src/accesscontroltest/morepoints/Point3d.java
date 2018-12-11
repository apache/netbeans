package accesscontroltest.morepoints;

import accesscontroltest.points.Point;

public class Point3d extends Point {
    protected int z;
    public void delta(Point p) {

    }
    public void delta3d(Point3d q) {

    }
    private class Inner {
        public void delta(Point3d r) {

        }
    }

    public Point3d() {

    }
}
