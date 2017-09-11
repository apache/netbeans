package test;

import java.util.List;
import java.util.LinkedList;

public class ReadUseTypeCast {

    public void test() {
        List l = null;
        LinkedList l2 = (LinkedList) l;
    }
    
}
