package org.black.kotlin.resolve.lang.java.structure;

import java.util.Collection;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner;
import org.jetbrains.kotlin.load.java.structure.JavaArrayType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.name.FqName;

/**
 *
 * @author Александр
 */
public class NetBeansJavaType<T extends TypeMirror> implements JavaType, JavaAnnotationOwner {

    private final T binding;
    
    public NetBeansJavaType(@NotNull T binding){
        this.binding = binding;
    }
    
    public static NetBeansJavaType<?> create(@NotNull TypeMirror typeBinding){
        if (typeBinding.getKind().getDeclaringClass().isPrimitive()){
            return new NetBeansJavaPrimitiveType(typeBinding);
        } else {
            throw new UnsupportedOperationException("Unsupported NetBeans type: " + typeBinding);
        }
        
    }
    
    @Override
    public JavaArrayType createArrayType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @NotNull
    public T getBinding(){
        return binding;
    }
    
}
