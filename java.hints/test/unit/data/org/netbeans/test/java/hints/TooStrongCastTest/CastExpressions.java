/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.test.java.hints.TooStrongCastTest;

/**
 *
 * @author sdedic
 */
public class CastExpressions {
    public void castPrimitive() {
        int a = 2;
        long b;
        byte c = 1;
        
        b = (int)c + a;
    }
    
    public void castPrimitiveStrict() {
        int a = 2;
        long b = 1;
        byte c = 1;
        
        a = (byte)b + c;
    }
    
    public void castPrimitiveOK() {
        int a = 2;
        long b = 1;
        byte c = 1;
        
        a = (int)b + c;
    }

    public void castStringExpr() {
        String result; 
        Object a = null;   
        String b = null;  
        
        result = b + (String)a; 
    }

    public void castInSwitch() {
        byte a = 0;
        
        switch ((int)a) {
            case 1:
        }
    }

}
