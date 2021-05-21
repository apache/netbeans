package test;

import java.util.List;

public abstract class CommentedGenerics {

    public abstract List/*<String>*/ getList();

    private List/*<String>*/ field;
}
