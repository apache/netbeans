package test3;

import java.util.ArrayList;
import java.util.List;

public class CCTest3 {
    
    public static void main(String[] args) {
        List<String> l;
	
	l = new ArrayList<>();
	
        l.add("Hello, world");
	
	for (String s : l) {
	    s.indexOf("Hello"); //Check the methods provided after the dot
	}
    }
}
