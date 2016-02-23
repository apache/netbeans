package org.black.kotlin.resolve.lang.java.structure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaLiteralAnnotationArgument;
import org.jetbrains.kotlin.name.Name;

/**
 *
 * @author Александр
 */
public class NetBeansJavaLiteralAnnotationArgument implements JavaLiteralAnnotationArgument{

    private final Object value;
    private final Name name;
    
    public NetBeansJavaLiteralAnnotationArgument(@NotNull Object value, @NotNull Name name){
        this.value = value;
        this.name = name;
    }
    
    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Name getName() {
        return name;
    }
    
}
