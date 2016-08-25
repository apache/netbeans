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
package org.jetbrains.kotlin.resolve.lang.java2;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaAnnotation;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaAnnotationAsAnnotationArgument;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaClassObjectAnnotationArgument;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaLiteralAnnotationArgument;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaReferenceAnnotationArgument;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class AnnotationSearchers {
    
    
    public static class AnnotationSearcher implements Task<CompilationController> {

        private final Collection<JavaAnnotation> annotations = Lists.newArrayList();
        private final ElementHandle handle;
        private final Project project;
        
        public AnnotationSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        private JavaAnnotationArgument create(Object value, Name name, CompilationController info) {
            if (value instanceof AnnotationMirror){
                TypeMirrorHandle typeHandle = TypeMirrorHandle.create(
                        ((AnnotationMirror) value).getAnnotationType());
                return new NetBeansJavaAnnotationAsAnnotationArgument(project, name, typeHandle, 
                        getMirrorArguments((AnnotationMirror) value, info));
            } else if (value instanceof VariableElement){
                return new NetBeansJavaReferenceAnnotationArgument(ElementHandle.create(((VariableElement) value)), project);
            } else if (value instanceof String){
                return new NetBeansJavaLiteralAnnotationArgument(value, name);
            } else if (value instanceof Class<?>){
                return new NetBeansJavaClassObjectAnnotationArgument((Class) value, name, project);
            } else if (value instanceof Collection<?>){
                
            } else if (value instanceof AnnotationValue){
                return create(((AnnotationValue) value).getValue(), name, info);
            } else return null; 
        }
        
        private Collection<JavaAnnotationArgument> getMirrorArguments(AnnotationMirror mirror, 
                CompilationController info) {
            Collection<JavaAnnotationArgument> arguments = Lists.newArrayList();
            
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                mirror.getElementValues().entrySet()){
                
                Object value = entry.getValue().getValue();
                Name name = Name.identifier(entry.getKey().getSimpleName().toString());
                
                
            }
            
            return arguments;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            List<? extends AnnotationMirror> annotationMirrors = elem.getAnnotationMirrors();
            for (AnnotationMirror mirror : annotationMirrors) {
                TypeMirrorHandle mirrorHandle = TypeMirrorHandle.create(mirror.getAnnotationType());
                JavaAnnotation annotation = new NetBeansJavaAnnotation(project, mirrorHandle, 
                        getMirrorArguments(mirror, info));
                annotations.add(annotation);
            }
            
        }
        
        public Collection<JavaAnnotation> getAnnotations() {
            return annotations;
        }
        
    } 
    
    
}
