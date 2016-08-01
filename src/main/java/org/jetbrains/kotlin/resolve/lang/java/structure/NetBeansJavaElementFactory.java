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
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;

/**
 *
 * @author Александр
 */
public class NetBeansJavaElementFactory {

    private NetBeansJavaElementFactory(){}
    
    private interface Factory<Binding, Java> {
        @NotNull
        Java create(@NotNull Binding binding);
    }
    
    private static class Factories {
        private static final Factory<AnnotationMirror, JavaAnnotation> ANNOTATIONS =
                new Factory<AnnotationMirror, JavaAnnotation>() {
            @Override
            public JavaAnnotation create(AnnotationMirror binding) {
                return new NetBeansJavaAnnotation(binding);
            }
        };
        
        private static final Factory<TypeMirror, JavaType> TYPES = 
                new Factory<TypeMirror, JavaType>() {
            @Override
            public JavaType create(TypeMirror binding) {
                return NetBeansJavaType.create(binding);
            }
        };
        
        private static final Factory<TypeMirror, JavaClassifierType> CLASSIFIER_TYPES =
                new Factory<TypeMirror, JavaClassifierType>() {
            @Override
            public JavaClassifierType create(TypeMirror binding) {
                return new NetBeansJavaClassifierType(binding);
            }
        };
        
        private static final Factory<ExecutableElement, JavaMethod> METHODS =
                new Factory<ExecutableElement, JavaMethod>() {
            @Override
            public JavaMethod create(ExecutableElement binding) {
                return new NetBeansJavaMethod(binding);
            }
        };
        
        private static final Factory<VariableElement, JavaField> FIELDS =
                new Factory<VariableElement, JavaField>() {
            @Override
            public JavaField create(VariableElement binding) {
                return new NetBeansJavaField(binding);
            }
        };
        
        private static final Factory<TypeParameterElement, JavaTypeParameter> TYPE_PARAMETERS =
                new Factory<TypeParameterElement, JavaTypeParameter>() {
            @Override
            public JavaTypeParameter create(TypeParameterElement binding) {
                return new NetBeansJavaTypeParameter(binding);
            }
        };
        
    }

    @NotNull
    private static <Binding, Java> List<Java> convert(@NotNull Binding[] elements, 
            @NotNull Factory<Binding, Java> factory){
        if (elements.length == 0)
            return Collections.emptyList();
        List<Java> result = Lists.newArrayList();
        for (Binding element : elements){
            result.add(factory.create(element));
        }
        return result;
    }
    
    
    @NotNull
    public static List<JavaAnnotation> annotations(@NotNull AnnotationMirror[] annotations){
        return convert(annotations, Factories.ANNOTATIONS);
    }
    
    @NotNull
    public static List<JavaType> types(@NotNull TypeMirror[] types) {
        return convert(types, Factories.TYPES);
    }
    
    @NotNull
    public static List<JavaClassifierType> classifierTypes(@NotNull TypeMirror[] classTypes){
        return convert(classTypes, Factories.CLASSIFIER_TYPES);
    }
    
    @NotNull
    public static List<JavaMethod> methods(@NotNull ExecutableElement[] methods){
        return convert(methods, Factories.METHODS);
    }
    
    @NotNull
    public static List<JavaField> fields(@NotNull VariableElement[] variables){
        return convert(variables, Factories.FIELDS);
    }
    
    @NotNull
    public static List<JavaTypeParameter> typeParameters(@NotNull TypeParameterElement[] typeParameters){
        return convert(typeParameters, Factories.TYPE_PARAMETERS);
    }
    
}
