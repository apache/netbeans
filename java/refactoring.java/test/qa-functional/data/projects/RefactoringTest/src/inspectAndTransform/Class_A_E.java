package inspectAndTransform;

import java.util.ArrayList;
import java.util.List;

public class Class_A_E {

    protected void finalize() throws Throwable {
        List<Integer> ls = new ArrayList<Integer>();
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o); //To change body of generated methods, choose Tools | Templates.
    }

    public int hashCode() {
        return super.hashCode(); //To change body of generated methods, choose Tools | Templates.
    }

    public void m() {
        try {
            System.out.println("");
        } catch (IllegalArgumentException e1) {
            someCode();
        } catch (SecurityException e2) {
            someCode();
        }
    }
    
    public void someCode(){
        
    }
}
