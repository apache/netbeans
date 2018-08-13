package test;

public class Test {

    interface Foo {
        int op (String s);
    }

    private void test(Foo f) {
    }

    public static void main(String[] args) {
	Test t = new Test();
	t.test(s -> s.length());
	t.test((s) -> s.length());
	t.test((String s) -> {return s.length();});
	t.test(s -> {return s.length();});
    }
}
