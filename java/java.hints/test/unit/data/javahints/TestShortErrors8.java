package javahints;

public class TestShortErrors8 {
    
    public TestShortErrors8() {
        int y = 0;
        TestShortErrors8 t = null;
        new Runnable() {
            public void run() {
                System.err.println(y);
                System.err.println(t.y);
            }
        };
    }
    
    private int y;
}
