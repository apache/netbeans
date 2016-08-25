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
package org.jetbrains.kotlin.resolve.lang.java.structure;

import java.util.Collection;
import java.util.List;
import javax.lang.model.element.ElementKind;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.NBClassUtils;
import org.jetbrains.kotlin.resolve.lang.java.NBMemberUtils;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaClass extends NetBeansJavaClassifier implements JavaClass {

    public NetBeansJavaClass(ElementHandle elementHandle, Project project) {
        super(elementHandle, null, project);
    }

    @Override
    public Name getName() {
        return NBClassUtils.getName(getElementHandle(), getProject());
    }

    @Override
    public FqName getFqName() {
        return new FqName(getElementHandle().getQualifiedName());
    }

    @Override
    public Collection<JavaClassifierType> getSupertypes() {
        return NBClassUtils.getSuperTypes(getElementHandle(), getProject());
    }

    @Override
    public Collection<JavaClass> getInnerClasses() {
        return NBClassUtils.getInnerClasses(getElementHandle(), getProject());
    }

    @Override
    public JavaClass getOuterClass() {
        return NBClassUtils.getOuterClass(getElementHandle(), getProject());
    }

    @Override
    public boolean isInterface() {
        return getElementHandle().getKind() == ElementKind.INTERFACE;
    }

    @Override
    public boolean isAnnotationType() {
        return getElementHandle().getKind() == ElementKind.ANNOTATION_TYPE;
    }

    @Override
    public boolean isEnum() {
        return getElementHandle().getKind() == ElementKind.ENUM;
    }

    @Override
    public boolean isKotlinLightClass() {
        return false;
    }

    @Override
    public Collection<JavaMethod> getMethods() {
        return NBClassUtils.getMethods(getElementHandle(), getProject(), this);
    }

    @Override
    public Collection<JavaField> getFields() {
        return NBClassUtils.getFields(getElementHandle(), getProject(), this);
    }

    @Override
    public Collection<JavaConstructor> getConstructors() {
        return NBClassUtils.getConstructors(getElementHandle(), getProject(), this);
    }

    @Override
    public boolean isAbstract() {
        return NBMemberUtils.isAbstract(getElementHandle(), getProject());
    }

    @Override
    public boolean isStatic() {
        return NBMemberUtils.isStatic(getElementHandle(), getProject());
    }

    @Override
    public boolean isFinal() {
        return NBMemberUtils.isFinal(getElementHandle(), getProject());
    }

    @Override
    public Visibility getVisibility() {
        return NBMemberUtils.getVisibility(getElementHandle(), getProject());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        return NBClassUtils.getTypeParameters(getElementHandle(), getProject());
    }
    
    @Override
    public String toString() {
        return getElementHandle().getQualifiedName();
    }
    
}
