package org.netbeans.test.java.hints;

import java.util.List;

public class TypevarsAndErrors<T> {
    
    public TypevarsAndErrors() {
    }
    
    public void test(T c1, Class<T> c2, E e1, List<E> e2) {
        this.c1 = c1;
        this.c2 = c2;
        this.e1 = e1;
        this.e2 = e2;
    }
}
