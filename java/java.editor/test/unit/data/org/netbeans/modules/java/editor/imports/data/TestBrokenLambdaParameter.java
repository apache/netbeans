package org.netbeans.modules.java.editor.imports.data;

public class TestBrokenLambdaParameter {
    public Test() {
        Object o = (str -> {System.err.println(str);});
    }
    List l;
}
