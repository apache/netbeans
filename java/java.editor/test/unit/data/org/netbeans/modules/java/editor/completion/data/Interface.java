package test;

interface Test {

    default String name() {
        return "Test";
    }

    static int length(String s) {
        return s.length();
    }
}
