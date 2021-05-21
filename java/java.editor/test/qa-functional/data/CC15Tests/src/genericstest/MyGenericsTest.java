package genericstest;

public class MyGenericsTest<X extends Number, E extends RuntimeException> extends MyGenericClass<X> {
    
    public X op(X param) throws E {
        return param;
    }
    
    public static void main(String... args) {        
        MyGenericsTest<Integer, ArithmeticException> mgt;
        MyGenericClass mc;
        MyGenericClass<Long> mcl;
        genericstest.MyGenericClass<java.lang.Integer> mci;
        MyGenericClass.GenericInner mcgi;
        MyGenericClass<Short>.Inner mcsi;
        genericstest.MyGenericClass<Double>.GenericInner<String> mcdgi;
        
    }
}
