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
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaArrayAnnotationArgument;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaAnnotation;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaAnnotationAsAnnotationArgument;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaArrayAnnotationArgument;
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

    private static JavaArrayAnnotationArgument getArrayAnnotationArgument(Collection values, Name name, 
            CompilationController info, Project project) {
        List<JavaAnnotationArgument> args = Lists.newArrayList();

        for (Object value : values) {
            if (value instanceof Collection<?>) {
                args.add(getArrayAnnotationArgument((Collection) value, name, info, project));
            } else {
                args.add(create(value, name, info, project));
            }
        }

        return new NetBeansJavaArrayAnnotationArgument(args, name);
    }

    private static JavaAnnotationArgument create(Object value, Name name, CompilationController info, Project project) {
        if (value instanceof AnnotationMirror) {
            TypeMirrorHandle typeHandle = TypeMirrorHandle.create(
                    ((AnnotationMirror) value).getAnnotationType());
            return new NetBeansJavaAnnotationAsAnnotationArgument(project, name, typeHandle,
                    getMirrorArguments((AnnotationMirror) value, info, project));
        } else if (value instanceof VariableElement) {
            return new NetBeansJavaReferenceAnnotationArgument(ElementHandle.create(((VariableElement) value)), project);
        } else if (value instanceof String) {
            return new NetBeansJavaLiteralAnnotationArgument(value, name);
        } else if (value instanceof Class<?>) {
            return new NetBeansJavaClassObjectAnnotationArgument((Class) value, name, project);
        } else if (value instanceof Collection<?>) {
            return getArrayAnnotationArgument((Collection) value, name, info, project);
        } else if (value instanceof AnnotationValue) {
            return create(((AnnotationValue) value).getValue(), name, info, project);
        } else {
            return null;
        }
    }

    private static Collection<JavaAnnotationArgument> getMirrorArguments(AnnotationMirror mirror,
            CompilationController info, Project project) {
        Collection<JavaAnnotationArgument> arguments = Lists.newArrayList();

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                : mirror.getElementValues().entrySet()) {

            Object value = entry.getValue().getValue();
            Name name = Name.identifier(entry.getKey().getSimpleName().toString());

            arguments.add(create(value, name, info, project));
        }

        return arguments;
    }

    public static class AnnotationsSearcher implements Task<CompilationController> {

        private final Collection<JavaAnnotation> annotations = Lists.newArrayList();
        private final ElementHandle handle;
        private final Project project;

        public AnnotationsSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
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
                        getMirrorArguments(mirror, info, project));
                annotations.add(annotation);
            }

        }

        public Collection<JavaAnnotation> getAnnotations() {
            return annotations;
        }

    }

    public static class AnnotationsForTypeMirrorHandleSearcher implements Task<CompilationController> {

        private final Collection<JavaAnnotation> annotations = Lists.newArrayList();
        private final TypeMirrorHandle handle;
        private final Project project;

        public AnnotationsForTypeMirrorHandleSearcher(TypeMirrorHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeMirror elem = handle.resolve(info);
            if (elem == null) {
                return;
            }

            List<? extends AnnotationMirror> annotationMirrors = elem.getAnnotationMirrors();
            for (AnnotationMirror mirror : annotationMirrors) {
                TypeMirrorHandle mirrorHandle = TypeMirrorHandle.create(mirror.getAnnotationType());
                JavaAnnotation annotation = new NetBeansJavaAnnotation(project, mirrorHandle,
                        getMirrorArguments(mirror, info, project));
                annotations.add(annotation);
            }

        }

        public Collection<JavaAnnotation> getAnnotations() {
            return annotations;
        }

    }
    
    public static class AnnotationSearcher implements Task<CompilationController> {

        private JavaAnnotation annotation = null;
        private final ElementHandle handle;
        private final Project project;
        private final FqName fqName;

        public AnnotationSearcher(ElementHandle handle, Project project, FqName fqName) {
            this.handle = handle;
            this.project = project;
            this.fqName = fqName;
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
                String annotationFQName = mirror.getAnnotationType().toString();
                if (fqName.asString().equals(annotationFQName)){
                    TypeMirrorHandle mirrorHandle = TypeMirrorHandle.create(mirror.getAnnotationType());
                    annotation = new NetBeansJavaAnnotation(project, mirrorHandle,
                        getMirrorArguments(mirror, info, project));
                }
            }
        }
        
        public JavaAnnotation getAnnotation() {
            return annotation;
        }
    }
    
    public static class AnnotationForTypeMirrorHandleSearcher implements Task<CompilationController> {

        private JavaAnnotation annotation = null;
        private final TypeMirrorHandle handle;
        private final Project project;
        private final FqName fqName;

        public AnnotationForTypeMirrorHandleSearcher(TypeMirrorHandle handle, Project project, FqName fqName) {
            this.handle = handle;
            this.project = project;
            this.fqName = fqName;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeMirror elem = handle.resolve(info);
            if (elem == null) {
                return;
            }

            List<? extends AnnotationMirror> annotationMirrors = elem.getAnnotationMirrors();
            for (AnnotationMirror mirror : annotationMirrors) {
                String annotationFQName = mirror.getAnnotationType().toString();
                if (fqName.asString().equals(annotationFQName)){
                    TypeMirrorHandle mirrorHandle = TypeMirrorHandle.create(mirror.getAnnotationType());
                    annotation = new NetBeansJavaAnnotation(project, mirrorHandle,
                        getMirrorArguments(mirror, info, project));
                }
            }
        }
        
        public JavaAnnotation getAnnotation() {
            return annotation;
        }
    }
    
}
