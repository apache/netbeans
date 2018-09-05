package genericstest;

public class MyGenericsTestii<X extends Number, E extends RuntimeException> extends MyGenericClass<X> {
    
    public X op(X param) throws E {
        return param;
    }
    
    public static void main(String... args) {        

        MyGenericClass mc;
        MyGenericClass<Long> mcl;

        MyGenericClass.GenericInner mcgi;
        MyGenericClass<Short>.Inner mcsi;
        genericstest.MyGenericClass<Double>.GenericInner<String> mcdgi;
        
    }
}
