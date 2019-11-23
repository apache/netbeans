package genericstest;

public interface MyGenericInterface<E extends Number> {
    public static final String CONST = "1.5";
    public E getElement();
    public void setElement(E element);
}
