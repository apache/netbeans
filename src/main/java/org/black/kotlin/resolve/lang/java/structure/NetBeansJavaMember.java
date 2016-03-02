package org.black.kotlin.resolve.lang.java.structure;


import com.google.common.collect.Lists;
import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.annotations;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaMember;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;

/**
 *
 * @author Александр
 */
public abstract class NetBeansJavaMember<T extends Element> 
        extends NetBeansJavaElement<T> implements JavaMember {
    
    protected NetBeansJavaMember(@NotNull T javaElement){
        super(javaElement);
    }
    
    @Override
    @NotNull
    public Collection<JavaAnnotation> getAnnotations(){
        List<Element> list = Lists.newArrayList((Element) getBinding());
        return annotations(list.toArray(new Element[list.size()]));
    }
    
    @Override
    @Nullable
    public JavaAnnotation findAnnotation(@NotNull FqName fqName){
        return NetBeansJavaElementUtil.findAnnotation(getBinding().asType(), fqName);
    }
    
    @Override
    public boolean isAbstract(){
        return Modifier.isAbstract(getBinding().getKind().getDeclaringClass().getModifiers());
    }
    
    @Override
    public boolean isStatic(){
        return Modifier.isStatic(getBinding().getKind().getDeclaringClass().getModifiers());
    }
    
    @Override
    public boolean isFinal(){
        return Modifier.isFinal(getBinding().getKind().getDeclaringClass().getModifiers());
    }
    
    @Override
    @NotNull
    public Visibility getVisibility(){
        return NetBeansJavaElementUtil.getVisibility(getBinding());
    }
    
    @Override
    @NotNull
    public Name getName(){
        return Name.guess(getBinding().getKind().getDeclaringClass().getName());
    }
    
    @Override 
    public boolean isDeprecatedInJavaDoc(){
//        return getBinding().getKind().getDeclaringClass().
        return false;
    }
    
}
