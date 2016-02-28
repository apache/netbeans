package org.black.kotlin.resolve.lang.java.structure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.structure.JavaArrayType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeProvider;
import org.jetbrains.kotlin.load.java.structure.JavaWildcardType;

/**
 *
 * @author Александр
 */
public class NetBeansJavaImmediateWildcardType implements JavaWildcardType {
    private final JavaType bound;
    private final boolean isExtends;
    private final JavaTypeProvider typeProvider;
    
    public NetBeansJavaImmediateWildcardType(@Nullable JavaType bound, boolean isExtends,
            @NotNull JavaTypeProvider typeProvider){
        this.bound = bound;
        this.isExtends = isExtends;
        this.typeProvider = typeProvider;
    }

    @Override
    @Nullable
    public JavaType getBound() {
        return bound;
    }

    @Override
    public boolean isExtends() {
        return isExtends;
    }

    @Override
    @NotNull
    public JavaTypeProvider getTypeProvider() {
        return typeProvider;
    }

    @Override
    @NotNull
    public JavaArrayType createArrayType() {
        throw new IllegalStateException("Creating array of wildcard type");
    }
    
    
}
