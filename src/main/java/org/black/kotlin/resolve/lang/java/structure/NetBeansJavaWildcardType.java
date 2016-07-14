package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaWildcardType;

/**
 *
 * @author Александр
 */
public class NetBeansJavaWildcardType extends NetBeansJavaType<WildcardType> implements JavaWildcardType {
    
    public NetBeansJavaWildcardType(@NotNull WildcardType typeBinding){
        super(typeBinding);
    }

    @Override
    public JavaType getBound() {
        TypeMirror bound = getBinding().getSuperBound();
        return bound != null ? NetBeansJavaType.create(bound) : null;//temp
    }

    @Override
    public boolean isExtends() {
        return getBinding().getExtendsBound() != null;
    }
    
    
}
