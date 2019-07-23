package introduceParameter;

public class Class_A_H implements Runnable{
        
    private int field = 4;
    
    public Class_A_H() {
        int x = field;
    }
       
    public void m(int param, int zz) {
        String x = "ABC";
        String y = "ABC";
        int z = zz;
    }
    
    public void run() {
        int x = 3;
    }
    
    
    class Super {
        public void m1() {
            int y = 3;
        }
    }
    
    class Sub extends Super {
        public void m1() {
            int x = 3;
            System.out.println(3 + 3);
        }
    }
    
    public void x() {
        int i = field;
    }
    
    public void y() {
        long i = System.currentTimeMillis();
    }
    
    class Generics<T> {
        public void genMethod() {
            T t = null;
        }
    }
    
    public void usage() {
        new Class_A_H();        
        m(1,2);
        new Super().m1();
        new Sub().m1();
        y();
    
    }                                             
}

