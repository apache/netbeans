package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.type.ArrayType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaArrayType;
import org.jetbrains.kotlin.load.java.structure.JavaType;

/**
 *
 * @author Александр
 */
public class NetBeansJavaArrayType extends NetBeansJavaType<ArrayType> implements JavaArrayType {
    
    public NetBeansJavaArrayType(@NotNull ArrayType typeBinding){
        super(typeBinding);
    }
    
    @Override
    @NotNull
    public JavaType getComponentType(){
        return NetBeansJavaType.create(getBinding().getComponentType());
    }
    
}
