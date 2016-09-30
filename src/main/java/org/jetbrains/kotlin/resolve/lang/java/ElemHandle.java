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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

public class ElemHandle<T extends Element> {
    
    private ElementKind kind = null;
    private ElementHandle handle = null;
    private TypeMirrorHandle typeHandle = null;
    private final Project project;
    
    private ElemHandle(Element element, Project project) {
        kind = element.getKind();
        this.project = project;
        if (kind == ElementKind.PARAMETER || kind == ElementKind.TYPE_PARAMETER) {
            typeHandle = TypeMirrorHandle.create(element.asType());
        } else {
            handle = ElementHandle.create(element);
        }
    }

    private ElemHandle(ElementHandle handle, Project project) {
        this.handle = handle;
        this.project = project;
        kind = handle.getKind();
    }

    public ElementHandle getElementHandle() {
        return handle;
    }
    
    public TypeMirrorHandle getTypeMirrorHandle() {
        return typeHandle;
    }
    
    private Element resolveTypeMirror(CompilationInfo compilationInfo) {
        TypeMirror type = typeHandle.resolve(compilationInfo);
        if (type == null) {
            return null;
        }
        
        return ((TypeVariable) type).asElement();
    }
    
    public Element resolve (final CompilationInfo compilationInfo) {
        if (handle != null) {
            return handle.resolve(compilationInfo);
        } else 
            return resolveTypeMirror(compilationInfo);
    }
    
    public String getBinaryName() {
        if (handle != null) {
            return handle.getBinaryName();
        } else return null;
    }
    
    public String getQualifiedName() {
        if (handle != null) { 
            return handle.getQualifiedName(); 
        } else return null;
    }
    
    public ElementKind getKind() {
        return kind;
    }
    
    public static <T extends Element> ElemHandle<T> create(final T element, Project project) {
        return new ElemHandle(element, project);
    }
    
    public static ElemHandle<PackageElement> createPackageElementHandle (
        final String packageName, Project project) {
        ElementHandle elemHandle = ElementHandle.createPackageElementHandle(packageName);
        return new ElemHandle(elemHandle, project);
    }
    
    public static ElemHandle<TypeElement> createTypeElementHandle(
        final ElementKind kind, final String binaryName, Project project) {
        ElementHandle elemHandle = ElementHandle.createTypeElementHandle(kind, binaryName);
        return new ElemHandle(elemHandle, project);
    }
    
    public static ElemHandle<? extends TypeElement> from(
            TypeMirrorHandle typeMirrorHandle, Project project) {
        ElementHandle elemHandle = ElementHandle.from(typeMirrorHandle);
        return new ElemHandle(elemHandle, project);
    }
    
    @Override
    public boolean equals (Object other) {
        if (other instanceof ElemHandle) {
            if (handle != null) {
                return handle.equals(((ElemHandle) other).handle);
            } else {
                if (kind == ElementKind.TYPE_PARAMETER) {
                    return NbParameterUtilsKt.isEqual(typeHandle, ((ElemHandle)other).typeHandle, project);
                } else
                    return NBElementUtils.typeMirrorHandleEquals(typeHandle, 
                        ((ElemHandle)other).typeHandle, project);
            }
        }
        return false;
    }
    
    @Override
    public int hashCode () {
        if (handle != null) {
            return handle.hashCode();
        } else {
            if (kind == ElementKind.TYPE_PARAMETER) {
                return NbParameterUtilsKt.getHashCode(typeHandle, project);
            } else
                return NBElementUtils.typeMirrorHandleHashCode(typeHandle, project);
        }
    }
    
    @Override 
    public String toString() {
        if (handle != null) {
            return handle.toString();
        } else {
            return NbTypeUtilsKt.getName(typeHandle, project);
        }
    }
    
}
