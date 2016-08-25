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
import javax.lang.model.type.TypeKind;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.lang.java.NBAnnotationUtils;
import org.jetbrains.kotlin.resolve.lang.java.NBTypeUtils;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaType implements JavaType, JavaAnnotationOwner {

    private final Project project;
    private final TypeMirrorHandle handle;
    
    public NetBeansJavaType(TypeMirrorHandle handle, Project project) {
        this.project = project;
        this.handle = handle;
    }
    
    public Project getProject() {
        return project;
    }
    
    public TypeMirrorHandle getHandle() {
        return handle;
    }
    
    public static NetBeansJavaType create(TypeMirrorHandle typeHandle, Project project) {
        if (typeHandle.getKind().isPrimitive() || NBTypeUtils.getName(typeHandle, 
                project).equals("void")) {
            return new NetBeansJavaPrimitiveType(typeHandle, project);
        } else if (typeHandle.getKind() == TypeKind.ARRAY) {
            return new NetBeansJavaArrayType(typeHandle, project);
        } else if (typeHandle.getKind() == TypeKind.DECLARED ||
                typeHandle.getKind() == TypeKind.TYPEVAR) {
            return new NetBeansJavaClassifierType(typeHandle, project);
        } else if (typeHandle.getKind() == TypeKind.WILDCARD) {
            return new NetBeansJavaWildcardType(typeHandle, project);
        } else throw new UnsupportedOperationException("Unsupported NetBeans type: " + typeHandle);
    }
    
    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        return NBAnnotationUtils.getAnnotations(getHandle(), getProject());
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqName) {
        return NBAnnotationUtils.getAnnotation(getHandle(), getProject(), fqName);
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return false; // temporary
    }
    
    @Override
    public int hashCode() {
        return handle.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NetBeansJavaType &&
                handle.equals(((NetBeansJavaType) obj).getHandle());
    }
    
}
