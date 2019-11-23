package test;

import java.util.List;

public class Usages {
    
    private List l;
    private List l1;
    
    private void test() {
        List l = null;
        List l2 = null;
        
        l.add(null);
        l1.add(null);
        l2.add(null);
        
        while (l1 == null) {
            List l1 = null;
            
            l.add(null);
            l1.add(null);
            l2.add(null);
        }
        
        l.add(null);
        l1.add(null);
        l2.add(null);
        
        test();
        
        Usages u = null;
    }
}
