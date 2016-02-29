package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeProvider;
import org.jetbrains.kotlin.load.java.structure.JavaWildcardType;
import org.netbeans.api.project.ui.OpenProjects;

/**
 *
 * @author Александр
 */
public class NetBeansJavaWildcardType extends NetBeansJavaType<TypeMirror> implements JavaWildcardType {
    
    public NetBeansJavaWildcardType(@NotNull TypeMirror typeBinding){
        super(typeBinding);
    }

    @Override
    public JavaType getBound() {
        TypeMirror bound = ((WildcardType) getBinding()).getSuperBound();
        return bound != null ? NetBeansJavaType.create(bound) : null;//temp
    }

    @Override
    public boolean isExtends() {
//        ((WildcardType) getBinding()).getExtendsBound();
        return false;
    }

    @Override
    public JavaTypeProvider getTypeProvider() {
        return new NetBeansJavaTypeProvider(OpenProjects.getDefault().getOpenProjects()[0]);
    }
    
    
    
}
