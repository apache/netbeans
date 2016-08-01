/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.black.kotlin.resolve.lang.java.structure;

import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.annotations;

import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner;
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
    
    @Override
    public int hashCode(){
        return getBinding().hashCode();
    }
    
    @Override
    public boolean equals(Object obj){
        return obj instanceof NetBeansJavaType && getBinding().equals(((NetBeansJavaType<?>) obj).getBinding());
    }
    
    public static NetBeansJavaType<?> create(@NotNull TypeMirror typeBinding){
        if (typeBinding.getKind().isPrimitive() || typeBinding.toString().equals("void")){
            return new NetBeansJavaPrimitiveType(typeBinding);
        } else if (typeBinding.getKind() == TypeKind.ARRAY){
            return new NetBeansJavaArrayType((ArrayType) typeBinding);
        } else if (typeBinding.getKind() == TypeKind.DECLARED || 
                typeBinding.getKind() == TypeKind.TYPEVAR){
            return new NetBeansJavaClassifierType(typeBinding);
        } else if (typeBinding.getKind() == TypeKind.WILDCARD){
            return new NetBeansJavaWildcardType((WildcardType) typeBinding);
        } 
        else {
            throw new UnsupportedOperationException("Unsupported NetBeans type: " + typeBinding);
        }
        
    }

    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        List<? extends AnnotationMirror> annotations = getBinding().getAnnotationMirrors();
        return annotations(annotations.toArray(new AnnotationMirror[annotations.size()]));
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqName) {
        return NetBeansJavaElementUtil.findAnnotation(binding.getAnnotationMirrors(), fqName);
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return false;
    }
    
    @NotNull
    public T getBinding(){
        return binding;
    }
    
}
