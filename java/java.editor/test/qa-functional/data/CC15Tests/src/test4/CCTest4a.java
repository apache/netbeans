package test4;
import java.util.ArrayList;

import java.util.List;

public class CCTest4a {
    
    public static final int TEST_FIELD = 1;
    
    public static final List<String> testMethod() {
        List<String> result = new ArrayList<String>();
	
	result.add("Hello, world.");
        
        return result;
    }
    
    public class Inner {
    }
}
