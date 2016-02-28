package org.black.kotlin.resolve.lang.java.structure;


import com.google.common.collect.Lists;
import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.types;
import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.classifierTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifier;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaTypeSubstitutor;
import org.jetbrains.kotlin.load.java.structure.impl.JavaTypeSubstitutorImpl;

/**
 *
 * @author Александр
 */
public class NetBeansJavaClassifierType extends NetBeansJavaType<TypeMirror> implements JavaClassifierType {
    
    private Element typeBinding;
    
    public NetBeansJavaClassifierType(Element typeBinding){
        super(typeBinding.asType());
        this.typeBinding = typeBinding;
    }

    @Override
    public JavaClassifier getClassifier() {
        return NetBeansJavaClassifier.create(typeBinding);
    }

    @Override
    public JavaTypeSubstitutor getSubstitutor() {
        JavaClassifier resolvedType = getClassifier();
//        if (resolvedType instanceof JavaClass && getBinding() instanceof TypeParameterElement){
        if (resolvedType instanceof JavaClass && typeBinding.getKind() == ElementKind.TYPE_PARAMETER){
            JavaClass javaClass = (JavaClass) resolvedType;
            List<JavaType> substitutedTypeArguments = getTypeArguments();
            
            int i = 0;
            Map<JavaTypeParameter, JavaType> substitutionMap = 
                    new HashMap<JavaTypeParameter, JavaType>();
            boolean isThisRawType = isRaw();
            for (JavaTypeParameter typeParameter : javaClass.getTypeParameters()){
                substitutionMap.put(typeParameter, !isThisRawType ? substitutedTypeArguments.get(i) : null);
                i++;
            }
            
            return new JavaTypeSubstitutorImpl(substitutionMap);
        }
        
        return JavaTypeSubstitutor.EMPTY;
    }

    @Override
    public Collection<JavaClassifierType> getSupertypes() {
        return classifierTypes(NetBeansJavaElementUtil.getSuperTypesWithObject(typeBinding));
    }

    @Override
    public String getPresentableText() {
        return getBinding().getKind().getDeclaringClass().getCanonicalName();
    }

    @Override
    public boolean isRaw() {
        return typeBinding instanceof ReferenceType;
    }

    @Override
    public List<JavaType> getTypeArguments() {
        List<TypeMirror> typeArgs = Lists.newArrayList();
        
//        if (getBinding() instanceof TypeElement){
        if (typeBinding.getKind().isClass() || typeBinding.getKind().isInterface()){
            for (TypeParameterElement elem : ((TypeElement) typeBinding).getTypeParameters()){
                typeArgs.add(elem.asType());
            }
            return types(typeArgs.toArray(new TypeMirror[typeArgs.size()]));
        }
        return Collections.EMPTY_LIST;
    }
    
}
