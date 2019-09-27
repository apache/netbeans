package test7;

public class CCTest7aiv {
    
    public void test(String permanent, String ... variable) {
        permanent.indexOf("Hello"); //Check the methods provided after the first dot.
	
	int dummy = variable.length; //Check that after the dot the CC is equal to CC content for an array.
	
	variable[0].indexOf("Hello"); //Check the CC after the dot. It should match the CC for String.
    }

    public static void testStatic(String permanent, String ... variable) {

	

	

    }
}
