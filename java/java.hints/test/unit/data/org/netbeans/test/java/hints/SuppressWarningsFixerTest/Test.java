package org.netbeans.test.java.hints;

import java.util.ArrayList;
import java.util.List;

public class Test {
    
    private List<String> l = new ArrayList<>();
    
    {
        l = new ArrayList<>();
    }
    
    private void test1() {
        List l = null;
        l.add("");
    }
    
    @SuppressWarnings("deprecation")
    private void test2() {
        List l = null;
        l.add("");
    }
    
    @SuppressWarnings({"deprecation"})
    private void test3() {
        List l = null;
        l.add("");
    
    }
    
    @Deprecated
    private void test4() {
        List l = null;
        l.add("");
    }
    
}
