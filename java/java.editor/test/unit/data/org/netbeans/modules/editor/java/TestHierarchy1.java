package org.netbeans.modules.editor.java;

public abstract class TestHierarchy1 implements Test1, Test2 {
    
    public void test() {
    }
    
    @Override public String toString() {
        return null;
    }
    
}

interface Test1 {
    public void test();
    public void test2();
}

interface Test2 {
    public void test();
    public void test2();
    public String toString();
}
