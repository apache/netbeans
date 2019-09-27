package genericstest;

public class MyGenericClass<T extends Number> implements MyGenericInterface<T> {

    public T[] elements;
    
    public T getElement() {
        return null;
    }
    
    public void setElement(T element) {
    }
    
    public MyGenericClass<T> instance() {
        return this;
    }
    
    public class Inner {
        public T get(int i) {
            return elements[i];
        }
    }
    
    public class GenericInner<P> {
        public P processElement(T element) {
            return null;
        }
    }
}
