package test5;

import static test4.CCTest4a.*;

public class CCTest5b {
    
    public static void main(String[] args) {
        int x = TEST_FIELD; //Check wheher the CC provides TEST_FIELD after the =
	
	testMethod().get(0).indexOf("Hello"); //Check whether the CC provides "testMethod". Check the methods provided after the dot.
    }
}
