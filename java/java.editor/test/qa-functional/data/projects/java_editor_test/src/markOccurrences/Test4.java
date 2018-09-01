
package markOccurrences;

public class Test4 {

    public static final int CONST = 2;
    
    public Test4() {
        System.out.println(CONST);
    }
    
    {
        int x = CONST;
    }
    
    public void method() {
        int x = CONST;
    }
    
    int a = CONST;

}
