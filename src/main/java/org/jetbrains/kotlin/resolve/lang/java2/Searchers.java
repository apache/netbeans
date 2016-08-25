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
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;

/**
 *
 * @author Alexander.Baratynski
 */
public class Searchers {

    public static class TypeElementSearcher implements Task<CompilationController> {

        private ElementHandle<TypeElement> element;
        private final String fqName;

        public TypeElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement elem = info.getElements().getTypeElement(fqName);
            if (elem != null) {
                element = ElementHandle.create(elem);
            }
        }

        public ElementHandle<TypeElement> getElement() {
            return element;
        }

    }

    public static class PackageElementSearcher implements Task<CompilationController> {

        private ElementHandle<PackageElement> element;
        private final String fqName;

        public PackageElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            PackageElement elem = info.getElements().getPackageElement(fqName);
            if (elem != null) {
                element = ElementHandle.create(elem);
            }
        }

        public ElementHandle<PackageElement> getPackage() {
            return element;
        }

    }

    public static class ClassIdComputer implements Task<CompilationController> {

        private final ElementHandle<TypeElement> handle;
        private ClassId classId = null;

        public ClassIdComputer(ElementHandle<TypeElement> handle) {
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
            TypeElement elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            classId = computeClassId(elem);
        }

        public ClassId getClassId() {
            return classId;
        }

    }

    public static class ElementSearcher implements CancellableTask<CompilationController>{

        private Element element;
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
            
            element = elem;
        }
        
        public Element getElement(){
            return element;
        }
        
    }
    
    public static class ElementSimpleNameSearcher implements CancellableTask<CompilationController>{

        private final ElementHandle element;
        private String simpleName = null;
        
        public ElementSimpleNameSearcher(ElementHandle element){
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
    
}
