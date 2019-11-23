package org.netbeans.modules.java.editor.semantic.data;

public class ArrayAccess {
    
    public static void main(String[] args) {
        int g = get()[0];
        int[] h = null;
        
        g = h[0];
    }
    
    public static int[] get() {return null;}
}
