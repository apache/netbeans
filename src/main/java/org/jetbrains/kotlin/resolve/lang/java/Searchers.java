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

import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class Searchers {

    public static class TypeElementSearcher implements Task<CompilationController> {

        private ElemHandle<TypeElement> element;
        private final String fqName;
        private final Project project;
        
        public TypeElementSearcher(String fqName, Project project) {
            this.fqName = fqName;
            this.project = project;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement elem = info.getElements().getTypeElement(fqName);
            if (elem != null) {
                element = ElemHandle.create(elem, project);
            }
        }

        public ElemHandle<TypeElement> getElement() {
            return element;
        }

    }
    
    public static class TypeMirrorHandleSearcher implements Task<CompilationController> {

        private TypeMirrorHandle handle = null;
        private final String fqName;

        public TypeMirrorHandleSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement elem = info.getElements().getTypeElement(fqName);
            if (elem != null) {
                handle = TypeMirrorHandle.create(elem.asType());
            }
        }

        public TypeMirrorHandle getHandle() {
            return handle;
        }

    }

    public static class PackageElementSearcher implements Task<CompilationController> {

        private ElemHandle<PackageElement> element;
        private final String fqName;
        private final Project project;
        
        public PackageElementSearcher(String fqName, Project project) {
            this.fqName = fqName;
            this.project = project;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            PackageElement elem = info.getElements().getPackageElement(fqName);
            if (elem != null) {
                element = ElemHandle.create(elem, project);
            }
        }

        public ElemHandle<PackageElement> getPackage() {
            return element;
        }

    }

    public static class ClassIdComputer implements Task<CompilationController> {

        private final ElemHandle<TypeElement> handle;
        private ClassId classId = null;

        public ClassIdComputer(ElemHandle<TypeElement> handle) {
            this.handle = handle;
        }

        public static ClassId computeClassId(@NotNull TypeElement classBinding) {
            Element container = classBinding.getEnclosingElement();

            if (container.getKind() != ElementKind.PACKAGE) {
                ClassId parentClassId = computeClassId((TypeElement) container);
                return parentClassId == null ? null : parentClassId.createNestedClassId(
                        Name.identifier(classBinding.getSimpleName().toString()));
            }

            String fqName = classBinding.getQualifiedName().toString();
            return ClassId.topLevel(new FqName(fqName));
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            classId = computeClassId((TypeElement) elem);
        }

        public ClassId getClassId() {
            return classId;
        }

    }

    public static class ElementSearcher implements CancellableTask<CompilationController>{

        private ElementHandle element;
        private final int offset;
        
        public ElementSearcher(int offset){
            this.offset = offset;
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TreePath treePath = info.getTreeUtilities().pathFor(offset);
            Element elem = info.getTrees().getElement(treePath);
            if (elem == null) {
                return;
            }
            
            element = ElementHandle.create(elem);
        }
        
        public ElementHandle getElement(){
            return element;
        }
        
    }
    
    public static class ElementSimpleNameSearcher implements CancellableTask<CompilationController>{

        private final ElemHandle element;
        private String simpleName = null;
        
        public ElementSimpleNameSearcher(ElemHandle element){
            this.element = element;
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = element.resolve(info);
            if (elem == null) {
                return;
            }
            
            simpleName = elem.getSimpleName().toString();
        }
        
        public String getSimpleName(){
            return simpleName;
        }
        
    }
    
    public static class ElementHandleSimpleNameSearcher implements CancellableTask<CompilationController>{

        private final ElementHandle element;
        private String simpleName = null;
        
        public ElementHandleSimpleNameSearcher(ElementHandle element){
            this.element = element;
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = element.resolve(info);
            if (elem == null) {
                return;
            }
            
            simpleName = elem.getSimpleName().toString();
        }
        
        public String getSimpleName(){
            return simpleName;
        }
        
    }
    
    public static class FileObjectForFqNameSearcher implements CancellableTask<CompilationController>{

        private final String fqName;
        private final ClasspathInfo cpInfo;
        private FileObject fo = null;
        
        public FileObjectForFqNameSearcher(String fqName, ClasspathInfo cpInfo) {
            this.fqName = fqName;
            this.cpInfo = cpInfo;
        }
        
        @Override
        public void cancel() {}

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement te = info.getElements().getTypeElement(fqName);
            if (te == null) {
                return;
            }
            ElementHandle<TypeElement> handle = ElementHandle.create(te);
            fo = SourceUtils.getFile(handle, cpInfo);
        }
        
        public FileObject getFileObject() {
            return fo;
        }
        
    }
    
    public static class IsDeprecatedSearcher implements CancellableTask<CompilationController>{

        private final ElemHandle element;
        private boolean deprecated = false;
        
        public IsDeprecatedSearcher(ElemHandle element){
            this.element = element;
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            if (element == null) {
                return;
            }
            Element elem = element.resolve(info);
            if (elem == null) {
                return;
            }
            
            deprecated = info.getElements().isDeprecated(elem);
        }
        
        public boolean isDeprecated(){
            return deprecated;
        }
        
    }
    
}
