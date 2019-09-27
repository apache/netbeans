package test;

public class UsedInFor {
    
    public static void test() {
        for (boolean condition = true; condition;) {
            condition = false;
        }
    }
}
