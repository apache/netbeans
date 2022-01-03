struct point0 { // without "static" everything is ok
    int x;
    int y;
} p0;

static struct point1 {
    int x;
    int y;
} p1;

static class point2 {
    int x;
    int y;
} p2;

static union u {
    int i;
    long l;
} u3;
