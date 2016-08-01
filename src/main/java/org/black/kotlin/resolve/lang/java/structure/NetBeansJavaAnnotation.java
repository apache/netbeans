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

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class NetBeansJavaAnnotation implements JavaAnnotation, JavaElement{

    private final Project kotlinProject;
    private final AnnotationMirror binding;
    
    protected NetBeansJavaAnnotation(AnnotationMirror javaAnnotation){
        this.binding = javaAnnotation;
        this.kotlinProject = NetBeansJavaProjectElementUtils.getProject(binding.getAnnotationType().asElement());
    }
    
    public JavaAnnotationArgument findArgument(@NotNull Name name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                getBinding().getElementValues().entrySet()){
            if (name.asString().equals(entry.getKey().getSimpleName().toString())){
                return NetBeansJavaAnnotationArgument.create(entry.getValue().getValue(),
                        name,
                        kotlinProject);
            }
        }
        
        return null;
    }

    @Override
    public Collection<JavaAnnotationArgument> getArguments() {
        List<JavaAnnotationArgument> arguments = Lists.newArrayList();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                getBinding().getElementValues().entrySet()){
            arguments.add(NetBeansJavaAnnotationArgument.create(entry.getValue().getValue(), 
                    Name.identifier(entry.getKey().getSimpleName().toString()), 
                    kotlinProject));
        }
        return arguments;
    }

    @Override
    public ClassId getClassId() {
        DeclaredType annotationType = getBinding().getAnnotationType();
        return annotationType != null ? 
                NetBeansJavaElementUtil.computeClassId((TypeElement) annotationType.asElement()) : null;
    }

    @Override
    @Nullable
    public JavaClass resolve() {
        DeclaredType annotationType = getBinding().getAnnotationType();
        return annotationType != null ? 
                new NetBeansJavaClass((TypeElement) annotationType.asElement()) : null;
    }
    
    @NotNull
    public AnnotationMirror getBinding(){
        return binding;
    }
    
    @Override
    public int hashCode(){
        return getBinding().hashCode();
    }
    
    @Override
    public boolean equals(Object obj){
        return obj instanceof NetBeansJavaAnnotation && getBinding().equals(((NetBeansJavaAnnotation)obj).getBinding());
    }
    
}
