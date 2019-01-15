package test;

public class Test {

    interface Foo {
        int op (String s);
    }

    interface Bar {
	String name();
    }

    private void test(Foo f, int i) {
    }

    private void test(Bar b, String s) {
    }

    public static void main(String[] args) {
	Test t = new Test();
