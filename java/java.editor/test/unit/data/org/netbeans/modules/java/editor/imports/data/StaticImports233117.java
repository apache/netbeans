package org.netbeans.modules.java.editor.imports.data;

public class StaticImports233117 {
    static void foo(StaticImports233117 i) {}
    void foo() {}
    static void bar(R r) {}
    public static void main(String[] args) {
        bar(StaticImports233117::foo);
    }
    public static interface R {
        public void run(StaticImports233117 i);
    }
}
