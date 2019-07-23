package test6;

public class CCTest6i {
    
    public static void main(String[] args) {
        CCTest6 t = new CCTest6();
	

    }
    
    public void test(String permanent, String ... variable) {
        permanent.indexOf("Hello"); //Check the methods provided after the first dot.
	
        int dummy = variable.length; //Check that after the dot the CC is equal to CC content for an array.
	
	variable[0].indexOf("Hello"); //Check the CC after the dot. It should match the CC for String.
    }
}
