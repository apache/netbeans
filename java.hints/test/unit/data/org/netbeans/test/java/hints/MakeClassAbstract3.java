package org.netbeans.test.java.hints;

@Test1 @Test2(test="uuu") class MakeClassAbstract3 {
    
    public MakeClassAbstract3() {
    }
    
    public abstract void test();
    
}

@interface Test1 {}

@interface Test2 {
    public String test();
}
