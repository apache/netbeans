package javahints;

public class TestShortErrors9 {
    
    public TestShortErrors9() {
        final int y;
        final TestShortErrors9 t;
        new Runnable() {
            public void run() {
                System.err.println(y);
                System.err.println(t.y);
            }
        };
    }
    
    private int y;
}
