/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Collections;

/**
 *
 * @author sdedic
 */
public class MethodLimits {
    int val;
    
    public static <T> void methodWithExceptions() throws 
            IllegalArgumentException, 
            IllegalStateException,  
            NullPointerException,
            ArrayIndexOutOfBoundsException {
    }

    /* no modifiers, no template params */ void methodWithExceptions2(int param1, int param2) throws 
            IllegalArgumentException, 
            IllegalStateException,  
            NullPointerException,
            ArrayIndexOutOfBoundsException {
        System.err.println(""); // some statement
    }

    public static <T> void methodWithParameters(
            int param1, int param2,
            int param3, int param4,
            int param5, int param6,
            int param7, int param8,
            int param9, int param10,
            int param11, int param12 
            ) {
        
    }

    /** some exception thrown */
    void methodWithParameters2(
            int param1, int param2,
            int param3, int param4,
            int param5, int param6,
            int param7, int param8,
            int param9, int param10,
            int param11, int param12 
            )  throws NullPointerException {
    }
    
    public static <T> void methodWithNegations() {
        boolean  input = false;
        boolean neg1 = !input;
        boolean neg2 = !neg1;
        boolean neg3 = neg1 != neg2;
        
        if (!neg3) {
            
        }
    }

    int methodWithNegations(int param1) throws IllegalStateException {
        boolean  input = false;
        boolean neg1 = !input;
        boolean neg2 = !neg1;
        boolean neg3 = neg1 != neg2;
        
        if (!neg3) {
            
        }
        return 0;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof MethodLimits)) {
            return false;
        }
        if (o != this) {
            MethodLimits other = (MethodLimits)o;
            if (other.val != this.val) {
                return this.val != other.val;
            }
        }
        return false;
    }
    
    int negationsWithAssert(Object o) {
        assert !(o instanceof MethodLimits);
        assert o != this;
        assert ((MethodLimits)o).val != val;
        assert val != 0;
        return 1;
    }
    
    public static <T> void methodWithLoops() {
        for (int i = 0; i < 3; i++) {
            
        }
        for (Object o : Collections.emptyList()) {
            
        }
        while (true) {
            break;
        }
        do {
            
        } while (false);
    }

    int methodWithLoops2(int param1) throws IllegalStateException {
        for (int i = 0; i < 3; i++) {
            
        }
        for (Object o : Collections.emptyList()) {
            
        }
        while (true) {
            break;
        }
        do {
            
        } while (false);
        return 0;
    }
}
