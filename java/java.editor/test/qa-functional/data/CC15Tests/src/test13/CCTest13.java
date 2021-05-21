package test13;

public class CCTest13 {
    private static List<String> l; //Check the CC after <
    
    public static void main(String[] args) {
        l = new List<String>(); //Check the CC after <
	
        l.add("Hello, world"); //Check the signature of the method provided by the CC
	
	l.get(0).indexOf("Hello"); //Check the methods provided after the second dot. Check the type of the variable l shown after the first dot.
    }
    
}
