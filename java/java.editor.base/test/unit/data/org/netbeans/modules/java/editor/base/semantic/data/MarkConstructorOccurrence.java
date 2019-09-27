package org.netbeans.modules.java.editor.semantic.data;

public class MarkConstructorOccurrence {

    public MarkConstructorOccurrence() {}
    public MarkConstructorOccurrence(int i) {}

    public static void main(String[] args) {
        new MarkConstructorOccurrence();
        new MarkConstructorOccurrence(1);
    }
    
}
