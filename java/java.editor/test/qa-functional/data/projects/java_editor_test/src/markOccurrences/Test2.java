
package markOccurrences;

public class Test2 {

    public String method(int ... x) {
    
        return null;        
    }
    
    public String method(int x) {
        if(x!=0) method(x);
        return null;        
    }

    public Test2() {
        method(1);
    }
    
    public static void staticM() {
        new Test2().method(1);
    }
    
    String initBlock = method(2);

}
