package javahints;

public abstract class AbstractError1 {
    public abstract void x();
}

@SuppressWarnings("anything")
class X extends AbstractError1 {
}
