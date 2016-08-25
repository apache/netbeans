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
import java.util.Collection;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.name.SpecialNames;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClassifierType;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class ParameterSearchers {
    
    public static class TypeParameterNameSearcher implements Task<CompilationController> {

        private final TypeMirrorHandle handle;
        private Name name = null;
        
        public TypeParameterNameSearcher(TypeMirrorHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeMirror type = handle.resolve(info);
            if (type == null) {
                return;
            }
            
            TypeParameterElement element = (TypeParameterElement) ((TypeVariable) type).asElement();
            name = SpecialNames.safeIdentifier(element.getSimpleName().toString());
        }
        
        public Name getName() {
            return name;
        }
        
    }
    
    public static class TypeParameterHashCodeSearcher implements Task<CompilationController> {
        private final TypeMirrorHandle handle;
        private int hashCode = -1;
        
        public TypeParameterHashCodeSearcher(TypeMirrorHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeMirror type = handle.resolve(info);
            if (type == null) {
                return;
            }
            
            TypeParameterElement element = (TypeParameterElement) ((TypeVariable) type).asElement();
            hashCode = element.hashCode();
        }
        
        public int getHashCode() {
            return hashCode;
        }
    }
    
    public static class TypeMirrorHandleHashCodeSearcher implements Task<CompilationController> {
        private final TypeMirrorHandle handle;
        private int hashCode = -1;
        
        public TypeMirrorHandleHashCodeSearcher(TypeMirrorHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeMirror type = handle.resolve(info);
            if (type == null) {
                return;
            }
            
            hashCode = type.hashCode();
        }
        
        public int getHashCode() {
            return hashCode;
        }
    }
    
    public static class UpperBoundsSearcher implements Task<CompilationController> {

        private final TypeMirrorHandle handle;
        private final Project project;
        private final Collection<JavaClassifierType> upperBounds = Lists.newArrayList();
        
        public UpperBoundsSearcher(TypeMirrorHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeMirror type = handle.resolve(info);
            if (type == null) {
                return;
            }
            
            TypeParameterElement element = (TypeParameterElement) ((TypeVariable) type).asElement();
            for (TypeMirror bound : element.getBounds()) {
                upperBounds.add(new NetBeansJavaClassifierType(TypeMirrorHandle.create(bound), project));
            }
            
        }
        
        public Collection<JavaClassifierType> getUpperBounds() {
            return upperBounds;
        }
        
    }
    
    public static class Equals implements Task<CompilationController> {
        private final TypeMirrorHandle handle;
        private final TypeMirrorHandle handle2;
        private boolean equals = false;
        
        public Equals(TypeMirrorHandle handle, TypeMirrorHandle handle2) {
            this.handle = handle;
            this.handle2 = handle2;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeMirror type = handle.resolve(info);
            TypeMirror type2 = handle2.resolve(info);
            if (type == null || type2 == null) {
                return;
            }
            
            TypeParameterElement element = (TypeParameterElement) ((TypeVariable) type).asElement();
            TypeParameterElement element2 = (TypeParameterElement) ((TypeVariable) type2).asElement();
            equals = element.equals(element2);
        }
        
        public boolean equals() {
            return equals;
        }
    }
    
    public static class TypeMirrorHandleEquals implements Task<CompilationController> {
        private final TypeMirrorHandle handle;
        private final TypeMirrorHandle handle2;
        private boolean equals = false;
        
        public TypeMirrorHandleEquals(TypeMirrorHandle handle, TypeMirrorHandle handle2) {
            this.handle = handle;
            this.handle2 = handle2;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeMirror type = handle.resolve(info);
            TypeMirror type2 = handle2.resolve(info);
            if (type == null || type2 == null) {
                return;
            }
            
            equals = type.equals(type2);
        }
        
        public boolean equals() {
            return equals;
        }
    }
    
}
