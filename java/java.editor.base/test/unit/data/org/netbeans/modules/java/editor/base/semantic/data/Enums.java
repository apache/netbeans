package test;

public class Enums {
    
    public void test() {
        Enum e = Enum.A;
        
        switch (e) {
            case A:
            case B:
            case C:
        }
    }
    
    public enum Enum {
        A, B {}, C() {};
    }
}
