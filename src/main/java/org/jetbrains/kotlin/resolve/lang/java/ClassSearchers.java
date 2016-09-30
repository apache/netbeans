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
import com.intellij.psi.CommonClassNames;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.name.SpecialNames;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClass;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClassifierType;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaConstructor;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaField;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaMethod;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaTypeParameter;
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
public class ClassSearchers {

    public static class NameSearcher implements Task<CompilationController> {

        private final ElemHandle<TypeElement> handle;
        private Name name = null;

        public NameSearcher(ElemHandle<TypeElement> handle) {
            this.handle = handle;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
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

        private final ElemHandle<TypeElement> handle;
        private final Project project;
        private final Collection<JavaClassifierType> superTypes = Lists.newArrayList();

        public SuperTypesSearcher(ElemHandle<TypeElement> handle, Project project) {
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
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }
            
            for (TypeMirror superType : getSuperTypesWithObject((TypeElement) elem, info)) {
                TypeMirrorHandle typeHandle = TypeMirrorHandle.create(superType);
                superTypes.add(new NetBeansJavaClassifierType(typeHandle, project));
            }
        }

        public Collection<JavaClassifierType> getSuperTypes() {
            return superTypes;
        }

    }

    public static class InnerClassesSearcher implements Task<CompilationController> {

        private final ElemHandle<TypeElement> handle;
        private final Project project;
        private final Collection<JavaClass> innerClasses = Lists.newArrayList();

        public InnerClassesSearcher(ElemHandle<TypeElement> handle, Project project) {
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

            List<? extends Element> members = elem.getEnclosedElements();//info.getElements().getAllMembers(elem);
            for (Element member : members) {
                if (member.asType().getKind() == TypeKind.DECLARED && member instanceof TypeElement){
                    innerClasses.add(new NetBeansJavaClass(ElemHandle.create((TypeElement) member, project), project));
                }
            }
        }

        public Collection<JavaClass> getInnerClasses() {
            return innerClasses;
        }

    }
    
    public static class OuterClassSearcher implements Task<CompilationController> {

        private final ElemHandle<TypeElement> handle;
        private final Project project;
        private JavaClass outerClass = null;

        public OuterClassSearcher(ElemHandle<TypeElement> handle, Project project) {
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

            Element outer = elem.getEnclosingElement();
            if (outer == null || outer.asType().getKind() != TypeKind.DECLARED){
                return;
            }
            
            outerClass = new NetBeansJavaClass(ElemHandle.create((TypeElement) outer, project), project);
        }

        public JavaClass getOuterClass() {
            return outerClass;
        }

    }
    
    public static class MethodsSearcher implements Task<CompilationController> {

        private final ElemHandle<TypeElement> handle;
        private final Project project;
        private final JavaClass containingClass;
        private final Collection<JavaMethod> methods = Lists.newArrayList();

        public MethodsSearcher(ElemHandle<TypeElement> handle, Project project, JavaClass javaClass) {
            this.handle = handle;
            this.project = project;
            this.containingClass = javaClass;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }

            List<? extends Element> members = elem.getEnclosedElements();//info.getElements().getAllMembers(elem);
            for (Element member : members) {
                if (member.getKind() == ElementKind.METHOD){
                    methods.add(new NetBeansJavaMethod(ElemHandle.create((ExecutableElement) member, project), containingClass, project));
                }
            }
        }

        public Collection<JavaMethod> getMethods() {
            return methods;
        }

    }
    
    public static class ConstructorsSearcher implements Task<CompilationController> {

        private final ElemHandle<TypeElement> handle;
        private final Project project;
        private final JavaClass containingClass;
        private final Collection<JavaConstructor> constructors = Lists.newArrayList();

        public ConstructorsSearcher(ElemHandle<TypeElement> handle, Project project, JavaClass javaClass) {
            this.handle = handle;
            this.project = project;
            this.containingClass = javaClass;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }

            List<? extends Element> members = elem.getEnclosedElements();
            for (Element member : members) {
                if (member.getKind() == ElementKind.CONSTRUCTOR){
                    constructors.add(new NetBeansJavaConstructor(ElemHandle.create((ExecutableElement) member, project), containingClass, project));
                }
            }
        }

        public Collection<JavaConstructor> getConstructors() {
            return constructors;
        }

    }
    
    public static class FieldsSearcher implements Task<CompilationController> {

        private final ElemHandle<TypeElement> handle;
        private final Project project;
        private final JavaClass containingClass;
        private final Collection<JavaField> fields = Lists.newArrayList();

        public FieldsSearcher(ElemHandle<TypeElement> handle, Project project, JavaClass javaClass) {
            this.handle = handle;
            this.project = project;
            this.containingClass = javaClass;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            if (elem == null) {
                return;
            }

            List<? extends Element> members = elem.getEnclosedElements();
            for (Element member : members) {
                if (member.getKind().isField()){
                    String name = member.getSimpleName().toString();
                    if (Name.isValidIdentifier(name)){
                        fields.add(new NetBeansJavaField(ElemHandle.create((VariableElement) member, project), containingClass, project));
                    }
                }
            }
        }

        public Collection<JavaField> getFields() {
            return fields;
        }

    }
    
    public static class TypeParametersSearcher implements Task<CompilationController> {

        private final ElemHandle<TypeElement> handle;
        private final Project project;
        private final List<JavaTypeParameter> typeParameters = Lists.newArrayList();

        public TypeParametersSearcher(ElemHandle<TypeElement> handle, Project project) {
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

            for (TypeParameterElement param : ((TypeElement) elem).getTypeParameters()) {
                typeParameters.add(new NetBeansJavaTypeParameter(ElemHandle.create(param, project), project));
            }
        }

        public List<JavaTypeParameter> getTypeParameters() {
            return typeParameters;
        }

    }
    
}
