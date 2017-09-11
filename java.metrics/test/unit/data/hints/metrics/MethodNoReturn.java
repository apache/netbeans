/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

/**
 *
 * @author sdedic
 */
public class MethodNoReturn {
    boolean b;
    
    public boolean methodReturn(Object o) {
        if (o == this) {
            b = false;
            return true;
        }
//        if (!(o instanceof MethodNoReturn)) {
//            b = true;
//            return false;
//        }
        return true;
    }
  
    public boolean methodGuardedReturns(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MethodNoReturn)) {
            return false;
        }
        if (!b) {
            return false;
        }
        return true;
    }
    
    public boolean equals(Object o) {
        if (o == this) {
            b = false;
            return true;
        }
        if (!(o instanceof MethodNoReturn)) {
            b = true;
            return false;
        }
        if (!b) {
            b = false;
            return false;
        }
        return true;
    }
}
