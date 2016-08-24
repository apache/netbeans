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
import com.intellij.psi.CommonClassNames;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.name.SpecialNames;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaClass;
import org.jetbrains.kotlin.resolve.lang.java.structure2.NetBeansJavaClassifierType;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

/**
 *
 * @author Alexander.Baratynski
 */
public class ClassSearchers {

    public static class NameSearcher implements Task<CompilationController> {

        private final ElementHandle<TypeElement> handle;
        private Name name = null;

        public NameSearcher(ElementHandle<TypeElement> handle) {
            this.handle = handle;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement elem = handle.resolve(info);
            if (elem == null) {
                return;
            }

            name = SpecialNames.safeIdentifier(elem.getSimpleName().toString());
        }

        public Name getName() {
            return name;
        }

    }

    public static class SuperTypesSearcher implements Task<CompilationController> {

        private final ElementHandle<TypeElement> handle;
        private final Project project;
        private final Collection<JavaClassifierType> superTypes = Lists.newArrayList();

        public SuperTypesSearcher(ElementHandle<TypeElement> handle, Project project) {
            this.handle = handle;
            this.project = project;
        }

        private static List<TypeMirror> getSuperTypesMirrors(TypeElement typeBinding) {
            List<TypeMirror> superTypes = Lists.newArrayList();
            for (TypeMirror superInterface : typeBinding.getInterfaces()) {
                superTypes.add(superInterface);
            }

            TypeMirror superclass = typeBinding.getSuperclass();
            if (!(superclass instanceof NoType)) {
                superTypes.add(superclass);
            }

            return superTypes;
        }

        public TypeMirror[] getSuperTypesWithObject(TypeElement typeBinding, CompilationController info) {
            List<TypeMirror> allSuperTypes = Lists.newArrayList();

            boolean javaLangObjectInSuperTypes = false;
            for (TypeMirror superType : getSuperTypesMirrors(typeBinding)) {

                if (superType.toString().equals(CommonClassNames.JAVA_LANG_OBJECT)) {
                    javaLangObjectInSuperTypes = true;
                }

                allSuperTypes.add(superType);

            }

            if (!javaLangObjectInSuperTypes && !typeBinding.toString().
                    equals(CommonClassNames.JAVA_LANG_OBJECT)) {
                allSuperTypes.add(info.getElements().getTypeElement(CommonClassNames.JAVA_LANG_OBJECT).asType());
            }

            return allSuperTypes.toArray(new TypeMirror[allSuperTypes.size()]);
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            for (TypeMirror superType : getSuperTypesWithObject(elem, info)) {
                TypeMirrorHandle typeHandle = TypeMirrorHandle.create(superType);
                superTypes.add(new NetBeansJavaClassifierType(typeHandle, project));
            }
        }

        public Collection<JavaClassifierType> getSuperTypes() {
            return superTypes;
        }

    }

    public static class InnerClassesSearcher implements Task<CompilationController> {

        private final ElementHandle<TypeElement> handle;
        private final Project project;
        private final Collection<JavaClass> innerClasses = Lists.newArrayList();

        public InnerClassesSearcher(ElementHandle<TypeElement> handle, Project project) {
            this.handle = handle;
            this.project = project;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement elem = handle.resolve(info);
            if (elem == null) {
                return;
            }

            List<? extends Element> members = info.getElements().getAllMembers(elem);
            for (Element member : members) {
                if (member.asType().getKind() == TypeKind.DECLARED && member instanceof TypeElement){
                    innerClasses.add(new NetBeansJavaClass(ElementHandle.create(member), project));
                }
            }
        }

        public Collection<JavaClass> getInnerClasses() {
            return innerClasses;
        }

    }
    
    public static class OuterClassSearcher implements Task<CompilationController> {

        private final ElementHandle<TypeElement> handle;
        private final Project project;
        private JavaClass outerClass = null;

        public OuterClassSearcher(ElementHandle<TypeElement> handle, Project project) {
            this.handle = handle;
            this.project = project;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement elem = handle.resolve(info);
            if (elem == null) {
                return;
            }

            Element outer = elem.getEnclosingElement();
            if (outer == null || outer.asType().getKind() != TypeKind.DECLARED){
                return;
            }
            
            outerClass = new NetBeansJavaClass(ElementHandle.create(outer), project);
        }

        public JavaClass getOuterClass() {
            return outerClass;
        }

    }
    
}
