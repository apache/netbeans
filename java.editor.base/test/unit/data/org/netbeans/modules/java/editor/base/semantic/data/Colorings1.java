package test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;

public class Colorings1 {
    
    private List list1;
    private static List list2;
    private static final List list3 = null;
    
    /**@deprecated*/
    private List list4;
    
    @Deprecated
    public List list5;
    
           List list6;
    
    protected List list7;
    
    public static void main(String[] args) {
        List list8 = null;
        
        list2.add("");
    }
    
    private void test1() {
        Test1 test1;
        Test2 test2;
        Test3 test3;
        Test4 test4;
        Test5 test5;
    }

    private static void test2() {
        
    }
    
    /**@deprecated*/
    public final void test3() {
        
    }
    
    @Deprecated
    private static void test4() {
        
    }
    
    private static class Test1 {
        
    }

    /**@deprecated*/
    private static final class Test2 {
        
    }

    static class Test3 {
        
    }

    class Test4 {
        
    }

    @Deprecated
    public class Test5 {
        
    }
    
}
