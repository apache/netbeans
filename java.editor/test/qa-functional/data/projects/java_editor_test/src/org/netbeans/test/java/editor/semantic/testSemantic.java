package org.netbeans.test.java.editor.semantic;

public class testSemantic {
    private int unusedField;
    static int staticField;
    
    private void unusedMethod() {
        int unusedVariable;        
    }

    static void staticMethod() {
        staticField = 3;        
    }

}
