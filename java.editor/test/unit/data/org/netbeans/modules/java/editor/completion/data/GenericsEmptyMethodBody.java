package test;

public class Test<X extends Number, Y extends RuntimeException> extends java.util.AbstractList<X> {

    private X singleton;

    public X get(int index) {
        return singleton;
    }

    public int size() {
        return 1;
    }

    public static void main(String... args) {

    }
}
