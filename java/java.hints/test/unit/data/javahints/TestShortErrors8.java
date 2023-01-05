package javahints;

public class TestShortErrors8 {
    
    public TestShortErrors8() {
        int y = 0; y = 0;
        TestShortErrors8 t = null; t = null;
        new Runnable() {
            public void run() {
                System.err.println(y);
                System.err.println(t.y);
            }
        };
        Runnable r = () -> {
            System.err.println(y);
            System.err.println(t.y);
        };
        Object o = null;
        switch (o) {
            case String s when s.isEmpty() == y -> {}
            case String s when s.isEmpty() == t.y -> {}
            default -> {}
        }
    }
    
    private int y;
}
