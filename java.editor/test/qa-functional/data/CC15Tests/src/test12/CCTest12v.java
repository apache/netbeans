package test12;

public class CCTest12v {
    
    public static void main(String[] args) {
        List<String> l; //Check the CC after <
	
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
