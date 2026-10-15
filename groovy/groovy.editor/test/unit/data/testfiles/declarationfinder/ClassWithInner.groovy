class ClassWithInner {

    String x = "A"
    String y = "B"

    class Inner {

        Point x = new Point();

        def method() {
            this.x = null
            y.isEmpty()
        }
    }

    def method() {
        this.x.isNumber()
    }

    class Point {
        int x, y
    }
}
