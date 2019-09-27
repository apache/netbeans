package org.netbeans.test.java.hints;

public class ArrayAccess {
    
    public ArrayAccess() {
    }
    
    public static void main(String[] args) {
        int a = x[0];
        a = x[0];
        
        ArrayAccess aa = null;
        
        int b = aa.x[0];
        b = aa.x[0];

        int[][] x = null;
	
        a = x[0][u];
        b = x[0][aa.u];
        a = x[u][0];
        b = x[aa.u][0];
        
        assert true : aa.s[0][1];
    }
}
