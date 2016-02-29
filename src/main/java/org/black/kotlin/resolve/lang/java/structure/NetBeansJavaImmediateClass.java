package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaArrayType;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifier;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaTypeSubstitutor;
import org.jetbrains.kotlin.name.FqName;

/**
 *
 * @author Александр
 */
public class NetBeansJavaImmediateClass implements JavaClassifierType{
    
    private final JavaClass javaClass;
    private final JavaTypeSubstitutor substitutor;
    
    public NetBeansJavaImmediateClass(JavaClass javaClass, JavaTypeSubstitutor substitutor){
        this.javaClass = javaClass;
        this.substitutor = substitutor;
    }

    @Override
    @Nullable
    public JavaClassifier getClassifier() {
        return javaClass;
    }

    @Override
    @NotNull
    public JavaTypeSubstitutor getSubstitutor() {
        return substitutor;
    }

    @Override
    @NotNull
    public Collection<JavaClassifierType> getSupertypes() {
        return javaClass.getSupertypes();
    }

    @Override
    @NotNull
    public String getPresentableText() {
        return javaClass.getName().asString();
    }

    @Override
    public boolean isRaw() {
        return javaClass.getTypeParameters().size() > 0 && getTypeArguments().isEmpty();
    }

    @Override
    public List<JavaType> getTypeArguments() {
        List<JavaType> substitutedParameters = Lists.newArrayList();
        for (JavaTypeParameter typeParameter : javaClass.getTypeParameters()){
            JavaType substituted = substitutor.substitute(typeParameter);
            if (substituted != null){
                substitutedParameters.add(substituted);
            }
        }
        
            return substitutedParameters;
    }

    @Override
    @NotNull
    public Collection<JavaAnnotation> getAnnotations() {
        return javaClass.getAnnotations();
    }

    @Override
    @Nullable
    public JavaAnnotation findAnnotation(@NotNull FqName fqName) {
        return javaClass.findAnnotation(fqName);
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return javaClass.isDeprecatedInJavaDoc();
    }

    @Override
    public JavaArrayType createArrayType() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
}
