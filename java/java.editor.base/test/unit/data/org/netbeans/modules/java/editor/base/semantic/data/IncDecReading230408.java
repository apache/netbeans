package test;

public class IncDecReading230408 {

    public void test() {
        int a = 0;
        Integer b = 0;
        int c = 0;
        
        while (++a < 10) {
            System.err.println("v" + (++b).toString() + (1 + c++));
        }
    }
    
}
