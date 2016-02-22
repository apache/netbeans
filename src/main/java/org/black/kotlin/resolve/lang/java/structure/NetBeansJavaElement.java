package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.element.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaElement;
/**
 *
 * @author Александр
 */
public abstract class NetBeansJavaElement<T extends Element> implements JavaElement {
    
    private final T binding;
    
    protected NetBeansJavaElement(@NotNull T binding){
        this.binding = binding;
    }
    
    @NotNull
    public T getBinding(){
        return binding;
    }
    
    @Override
    public int hashCode(){
        return getBinding().hashCode();
    }
    
    @Override
    public boolean equals(Object obj){
        return obj instanceof NetBeansJavaElement && getBinding().equals(((NetBeansJavaElement<?>)obj).getBinding());
    }
    
    
}
