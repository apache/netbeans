package test2;

import java.util.ArrayList;
import java.util.List;

public class CCTest2 {
    
    public static void main(String[] args) {
        List<String> l; //Check the CC after <
	
	l = new ArrayList<String>(); //Check the CC after <
	
        l.add("Hello, world"); //Check the signature of the method provided by the CC
	
	l.get(0).indexOf("Hello"); //Check the methods provided after the second dot. Check the type of the variable l shown after the first dot.
    }
}
