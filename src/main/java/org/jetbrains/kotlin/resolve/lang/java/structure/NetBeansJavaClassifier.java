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
package org.jetbrains.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import org.jetbrains.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner;
import org.jetbrains.kotlin.load.java.structure.JavaClassifier;
import org.jetbrains.kotlin.name.FqName;

/**
 *
 * @author Александр
 */
public abstract class NetBeansJavaClassifier<T extends Element> extends
        NetBeansJavaElement<T> implements JavaClassifier, JavaAnnotationOwner {
    
    public NetBeansJavaClassifier(T javaType) {
        super(javaType);
    }
    
    public static JavaClassifier create(Element element){
        if (element.asType().getKind() == TypeKind.TYPEVAR){
            return new NetBeansJavaTypeParameter((TypeParameterElement) element);
        }
        
        if (element.getKind().isClass() || element.getKind().isInterface() 
                || element.getKind() == ElementKind.ENUM){
            return new NetBeansJavaClass((TypeElement) element);
        }
        else
            throw new IllegalArgumentException("Element" + element.getSimpleName().toString() + "is not JavaClassifier");
    }
    
    @Override
    public Collection<JavaAnnotation> getAnnotations(){
        List<JavaAnnotation> annotations = Lists.newArrayList();
        for ( AnnotationMirror annotation : getBinding().getAnnotationMirrors()){
            annotations.add(new NetBeansJavaAnnotation(annotation));
        }
        return annotations;
    }
    
    @Override 
    public JavaAnnotation findAnnotation(FqName fqName){
        return NetBeansJavaElementUtil.findAnnotation(getBinding().getAnnotationMirrors(), fqName);
    }
    
    @Override
    public boolean isDeprecatedInJavaDoc(){
        return NetBeansJavaProjectElementUtils.isDeprecated(getBinding());
    }
}
