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
import java.util.Collections;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaMember;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.NBAnnotationUtils;
import org.jetbrains.kotlin.resolve.lang.java.NBMemberUtils;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public abstract class NetBeansJavaMember extends NetBeansJavaElement implements JavaMember {
    
    private final JavaClass containingClass;
    
    public NetBeansJavaMember(ElementHandle handle, JavaClass containingClass, Project project) {
        super(handle, project);
        this.containingClass = containingClass;
    }

    @Override
    public JavaClass getContainingClass() {
        return containingClass;
    }

    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        return NBAnnotationUtils.getAnnotations(getElementHandle(), getProject());
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqName) {
        return NBAnnotationUtils.getAnnotation(getElementHandle(), getProject(), fqName);
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return false; // temporary
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
    public Name getName() {
        return NBMemberUtils.getName(getElementHandle(), getProject());
    }
    
}
