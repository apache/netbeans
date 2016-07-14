package org.black.kotlin.resolve.lang.java.structure;


import com.google.common.collect.Lists;
import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.types;
import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.classifierTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifier;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;

/**
 *
 * @author Александр
 */
public class NetBeansJavaClassifierType extends NetBeansJavaType<TypeMirror> implements JavaClassifierType {
    
    public NetBeansJavaClassifierType(TypeMirror typeBinding){
        super(typeBinding);
    }

    @Override
    public JavaClassifier getClassifier() {
        if (getBinding().getKind() == TypeKind.DECLARED){
            return NetBeansJavaClassifier.create(((DeclaredType)getBinding()).asElement());
        } else 
            return null;
    }

    @Override
    public String getPresentableText() {
        return getBinding().toString();
    }

    @Override
    public boolean isRaw() {
        return getBinding() instanceof ReferenceType;
    }

    @Override
    public List<JavaType> getTypeArguments() {
        List<TypeMirror> typeArgs = Lists.newArrayList();
        
        if (getBinding().getKind() == TypeKind.DECLARED){
            
            for (TypeMirror elem : ((DeclaredType) getBinding()).getTypeArguments()){
                typeArgs.add(elem);
            }
            return types(typeArgs.toArray(new TypeMirror[typeArgs.size()]));
        }
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public String getCanonicalText() {
        return getBinding().toString();
    }
    
}
