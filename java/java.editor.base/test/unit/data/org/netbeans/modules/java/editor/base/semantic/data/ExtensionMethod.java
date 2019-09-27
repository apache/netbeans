package org.netbeans.modules.java.editor.semantic.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface ExtensionMethod<T> extends List<T> {
    
    public default void sort(Comparator<? super T> c) {
        Collections.<T>sort(this, c);
    }

}
