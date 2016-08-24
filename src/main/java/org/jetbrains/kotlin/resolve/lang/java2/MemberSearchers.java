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

import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.descriptors.Visibilities;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.JavaVisibilities;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaType;
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
public class MemberSearchers {
    
    public static class IsAbstractSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isAbstract = false;
        
        public IsAbstractSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            isAbstract = elem.getModifiers().contains(Modifier.ABSTRACT);
        }
        
        public boolean isAbstract() {
            return isAbstract;
        }
        
    }
    
    public static class IsStaticSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isStatic = false;
        
        public IsStaticSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            isStatic = elem.getModifiers().contains(Modifier.STATIC);
        }
        
        public boolean isStatic() {
            return isStatic;
        }
        
    }
    
    public static class IsFinalSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isFinal = false;
        
        public IsFinalSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            isFinal = elem.getModifiers().contains(Modifier.FINAL);
        }
        
        public boolean isFinal() {
            return isFinal;
        }
        
    }
    
    public static class NameSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private Name name = null;
        
        public NameSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            name = Name.identifier(elem.getSimpleName().toString());
        }
        
        public Name getName() {
            return name;
        }
        
    }
    
    public static class VisibilitySearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private Visibility visibility = JavaVisibilities.PACKAGE_VISIBILITY;
        
        public VisibilitySearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            Set<Modifier> modifiers = elem.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)) {
                visibility = Visibilities.PUBLIC;
            } else if (modifiers.contains(Modifier.PRIVATE)) {
                visibility = Visibilities.PRIVATE;
            } else if (modifiers.contains(Modifier.PROTECTED)) {
                visibility = modifiers.contains(Modifier.STATIC) ? JavaVisibilities.PROTECTED_STATIC_VISIBILITY :
                    JavaVisibilities.PROTECTED_AND_PACKAGE;
            }
        }
        
        public Visibility getVisibility() {
            return visibility;
        }
        
    }
    
    public static class FieldTypeSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private final Project project;
        private JavaType type = null;
        
        public FieldTypeSearcher(ElementHandle handle, Project project) {
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
            
            TypeMirror mirror = ((VariableElement) elem).asType();
            type = NetBeansJavaType.create(TypeMirrorHandle.create(mirror), project);
        }
        
        public JavaType getType() {
            return type;
        }
        
    }
    
}
