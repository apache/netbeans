package org.netbeans.modules.java.editor.semantic.data;

public class ClassUseNewInstance {
    
    public static void main(String[] args) {
        Object o;
        
        o = new Test();
    }
    
    private static final class Test {
        
    }
    
}
