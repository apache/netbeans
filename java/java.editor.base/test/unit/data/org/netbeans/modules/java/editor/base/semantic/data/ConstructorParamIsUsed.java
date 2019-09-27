package org.netbeans.modules.java.editor.semantic.data;

public class ConstructorParamIsUsed {
    public static void main(String[] args) {
        int i = 0;
        StringBuilder x = new StringBuilder(i++);
        StringBuilder y = new StringBuilder(i++);
    }
}
