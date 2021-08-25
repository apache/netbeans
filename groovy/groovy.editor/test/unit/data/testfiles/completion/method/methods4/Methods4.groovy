class GroovyClass1 {
    def method1() {}
    def method2() {}
    def method3() {}
    
    interface ISuper {
        def methodSuper();
    }
    
    interface I1 extends ISuper {
        def methodA();
    }
}

class GroovyClass2 {
    def m2() {
        GroovyClass1.I1 iface;
        iface.meth

    }
}
