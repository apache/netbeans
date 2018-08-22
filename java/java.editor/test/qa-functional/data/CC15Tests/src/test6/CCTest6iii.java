package test6;

public class CCTest6iii {
    
    public static void main(String[] args) {
        CCTest6 t = new CCTest6();
	
	t.test("Hello", "Hello", "Hello"); //Check the signature of the test method.
    }
    
    public void test(String permanent, String ... variable) {
        permanent.indexOf("Hello"); //Check the methods provided after the first dot.
	

	

    }
}
