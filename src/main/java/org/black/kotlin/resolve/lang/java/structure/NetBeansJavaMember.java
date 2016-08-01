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
import javax.lang.model.element.Element;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
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
        List<? extends AnnotationMirror> annotations = getBinding().getAnnotationMirrors();
        return annotations(annotations.toArray(new AnnotationMirror[annotations.size()]));
    }
    
    @Override
    @Nullable
    public JavaAnnotation findAnnotation(@NotNull FqName fqName){
        return NetBeansJavaElementUtil.findAnnotation(getBinding().getAnnotationMirrors(), fqName);
    }
    
    @Override
    public boolean isAbstract(){
        return NetBeansJavaElementUtil.isAbstract(getBinding().getModifiers());
    }
    
    @Override
    public boolean isStatic(){
        return NetBeansJavaElementUtil.isStatic(getBinding().getModifiers());
    }
    
    @Override
    public boolean isFinal(){
        return NetBeansJavaElementUtil.isFinal(getBinding().getModifiers());
    }
    
    @Override
    @NotNull
    public Visibility getVisibility(){
        return NetBeansJavaElementUtil.getVisibility(getBinding());
    }
    
    @Override
    @NotNull
    public Name getName(){
        return Name.identifier(getBinding().getSimpleName().toString());
    }
    
    @Override 
    public boolean isDeprecatedInJavaDoc(){
        return NetBeansJavaProjectElementUtils.isDeprecated(getBinding());
    }
    
}
