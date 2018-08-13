package test;

public class ReadUse132342 {

    public static String test1() {
        String p = "b";
        return p += "a";
    }
    
    public static void test2() {
        String p = "b";
        System.err.println(p += "a");
    }
    
    public static String test3() {
        String p = "b";
        String a = p += "a";
        return a;
    }
    
    public static String test4() {
        String p = "b";
        String a = "c";
        
        a += p += "a";
        return a;
    }
    
    public static String test5() {
        String p = "b";
        
        return (p += "a");
    }
    
    public static void test6() {
        String p = "b";
        
        assert (p += "a") != null;
    }
    
    public static String test7() {
        String p = "b";
        String a = "c";
        
        a = p += "a";
        
        return a;
    }
    
}
