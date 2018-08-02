/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.test.java.hints.TooStrongCastTest;

public class ArrayAccess {
    public void castInArrayAccess() throws Exception {
        Integer idx = Integer.valueOf(10);
        Object[] ar = new Object[100];
        long idx2 = 10;
        
        ar[(int)idx] = 0;
        ar[(int)idx2] = 0;
        ar[(byte)idx2] = 0;
        ar[(byte)((int)idx)] = 1;
        ar[(byte)((int)idx)] = (float)1;
        
        double[] ar2 = new double[5];
        ar2[idx] = (float)1;
    }
}
