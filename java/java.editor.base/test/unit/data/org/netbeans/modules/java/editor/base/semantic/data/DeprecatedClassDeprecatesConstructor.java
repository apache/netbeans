package org.netbeans.modules.java.editor.semantic.data;

public class DeprecatedClassDeprecatesConstructor {
    
    public static void main(String[] args) {
        Depr p = new Depr();
    }

    @Deprecated
    public static class Depr {
        public Depr() {
            
        }
    }
}
