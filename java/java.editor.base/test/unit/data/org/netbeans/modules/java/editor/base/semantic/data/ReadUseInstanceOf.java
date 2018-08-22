package test;

import java.util.List;
import java.util.LinkedList;

public class ReadUseInstanceOf {

    public void test() {
        List l = null;
        
        if (l instanceof LinkedList) {
            //nothing;
        }
    }
    
}
