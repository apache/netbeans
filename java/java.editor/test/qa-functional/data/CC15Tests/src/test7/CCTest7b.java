package test7;

import static test7.CCTest7a.testStatic;

public class CCTest7b {
    
    public static void main(String[] args) {
        CCTest7a t = new CCTest7a();
	
	t.test("Hello", "Hello", "Hello"); //Check the signature of the test method.
	testStatic("Hello", "Hello", "Hello"); //Check that the testStatic method is provided in the CC. Check its signature.
    }
    
}
