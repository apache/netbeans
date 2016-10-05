/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.resolve.lang.java;

import com.google.common.collect.Lists;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaType;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaTypeParameter;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaValueParameter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class ExecutableSearchers {
    
    public static class ReturnTypeSearcher implements Task<CompilationController> {

        private final ElemHandle<ExecutableElement> handle;
        private final Project project;
        private JavaType returnType = null;
        
        public ReturnTypeSearcher(ElemHandle<ExecutableElement> handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.ELEMENTS_RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            TypeMirrorHandle typeHandle = TypeMirrorHandle.create(((ExecutableElement) elem).getReturnType());
            returnType = NetBeansJavaType.create(typeHandle, project);
        }
        
        public JavaType getReturnType() {
            return returnType;
        }
        
    }
    
    public static class HasAnnotationParameterDefaultValueSearcher implements Task<CompilationController> {

        private final ElemHandle<ExecutableElement> handle;
        private boolean hasAnnotationParameterDefaultValue = false;
        
        public HasAnnotationParameterDefaultValueSearcher(ElemHandle<ExecutableElement> handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.ELEMENTS_RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            hasAnnotationParameterDefaultValue = ((ExecutableElement) elem).getDefaultValue() != null;
        }
        
        public boolean hasAnnotationParameterDefaultValue() {
            return hasAnnotationParameterDefaultValue;
        }
        
    }
    
    public static class TypeParametersSearcher implements Task<CompilationController> {

        private final ElemHandle<ExecutableElement> handle;
        private final Project project;
        private final List<JavaTypeParameter> typeParameters = Lists.newArrayList();
        
        public TypeParametersSearcher(ElemHandle<ExecutableElement> handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.ELEMENTS_RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            List<? extends TypeParameterElement> typeParams = ((ExecutableElement) elem).getTypeParameters();
            for (TypeParameterElement typeParameter : typeParams) {
                ElemHandle elemHandle = ElemHandle.create(typeParameter, project);
                typeParameters.add(new NetBeansJavaTypeParameter(elemHandle, project));
            }
        }
        
        public List<JavaTypeParameter> getTypeParameters() {
            return typeParameters;
        }
        
    }
    
    public static class ValueParametersSearcher implements Task<CompilationController> {

        private final ElemHandle<ExecutableElement> handle;
        private final Project project;
        private final List<JavaValueParameter> valueParameters = Lists.newArrayList();
        
        public ValueParametersSearcher(ElemHandle<ExecutableElement> handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.ELEMENTS_RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            List<? extends VariableElement> valueParams =  ((ExecutableElement) elem).getParameters();
            
            int parameterTypesCount = valueParams.size();
            for (int i = 0; i < parameterTypesCount; i++){
                boolean isLastParameter = i == parameterTypesCount-1;
                String parameterName = valueParams.get(i).getSimpleName().toString();
                ElemHandle elemHandle = ElemHandle.create(valueParams.get(i), project);
                JavaValueParameter valueParameter = new NetBeansJavaValueParameter(
                        elemHandle, 
                        project, parameterName, 
                        isLastParameter ? ((ExecutableElement) elem).isVarArgs() : false);
                valueParameters.add(valueParameter);
            }
        }
        
        public List<JavaValueParameter> getValueParameters() {
            return valueParameters;
        }
        
    }
    
}
