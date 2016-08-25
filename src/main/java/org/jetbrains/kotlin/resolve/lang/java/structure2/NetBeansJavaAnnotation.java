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
package org.jetbrains.kotlin.resolve.lang.java.structure2;

import java.util.Collection;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.resolve.lang.java2.NBElementUtils;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaAnnotation implements JavaAnnotation, JavaElement {

    private final Project project;
    private final TypeMirrorHandle handle;
    private final Collection<JavaAnnotationArgument> args;
    
    public NetBeansJavaAnnotation(Project project, TypeMirrorHandle handle, Collection<JavaAnnotationArgument> args) {
        this.project = project;
        this.handle = handle;
        this.args = args;
    }
    
    public TypeMirrorHandle getHandle() {
        return handle;
    }
    
    @Override
    public Collection<JavaAnnotationArgument> getArguments() {
        return args;
    }

    @Override
    public ClassId getClassId() {
        ElementHandle elementHandle = ElementHandle.from(handle);
        return NBElementUtils.computeClassId(elementHandle, project);
    }

    @Override
    public JavaClass resolve() {
        ElementHandle elementHandle = ElementHandle.from(handle);
        return new NetBeansJavaClass(elementHandle, project);
    }
    
    @Override
    public int hashCode() {
        return NBElementUtils.typeMirrorHandleHashCode(handle, project);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NetBeansJavaAnnotation)) {
            return false;
        }
        NetBeansJavaAnnotation annotation = (NetBeansJavaAnnotation) obj;
        
        return NBElementUtils.typeMirrorHandleEquals(handle, annotation.getHandle(), project);
    }
    
}
