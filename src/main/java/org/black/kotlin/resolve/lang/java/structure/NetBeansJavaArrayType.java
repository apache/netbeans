package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.type.TypeMirror;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaArrayType;
import org.jetbrains.kotlin.load.java.structure.JavaType;

/**
 *
 * @author Александр
 */
public class NetBeansJavaArrayType extends NetBeansJavaType<TypeMirror> implements JavaArrayType {
    
    public NetBeansJavaArrayType(@NotNull TypeMirror typeBinding){
        super(typeBinding);
    }
    
    @Override
    @NotNull
    public JavaType getComponentType(){
        return NetBeansJavaType.create(getBinding());
    }
    
}
