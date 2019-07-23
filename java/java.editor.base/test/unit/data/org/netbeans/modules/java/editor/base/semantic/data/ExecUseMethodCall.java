package org.netbeans.modules.java.editor.semantic.data;

public class ExecUseMethodCall {
    
    public static void main(String[] args) {
        Object o;
        
        o = test();
    }
    
    private static Object test() {
        return null;
    }
}
