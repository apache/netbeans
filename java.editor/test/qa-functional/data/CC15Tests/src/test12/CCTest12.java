package test12;

public class CCTest12 {
    
    public static void main(String[] args) {
        List<String> l; //Check the CC after <
	
	l = new List<String>(); //Check the CC after <
	
        l.add("Hello, world"); //Check the signature of the method provided by the CC
	
	l.get(0).indexOf("Hello"); //Check the methods provided after the second dot. Check the type of the variable l shown after the first dot.
    }

    private static class List<T> {
        public void add(T t) {
            //nothing...
        }
        
        public T get(int index) {
            return null;
        }
    }
}
