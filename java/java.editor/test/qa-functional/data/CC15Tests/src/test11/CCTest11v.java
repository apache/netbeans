package test11;

public class CCTest11v {
    private static List<String> l; //Check the CC after <
    
    public static void main(String[] args) {
        l = new List<String>(); //Check the CC after <
	
        l.add("Hello, world"); //Check the signature of the method provided by the CC

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
