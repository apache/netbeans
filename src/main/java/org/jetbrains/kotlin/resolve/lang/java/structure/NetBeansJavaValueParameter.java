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
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.NBElementUtils;
import org.jetbrains.kotlin.resolve.lang.java.NBParameterUtils;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaValueParameter extends NetBeansJavaElement implements JavaValueParameter {
    
    private final String name;
    private final boolean isVararg;
    
    public NetBeansJavaValueParameter(TypeMirrorHandle typeHandle, Project project, String name, boolean isVararg) {
        super(typeHandle, project);
        this.name = name;
        this.isVararg = isVararg;
    }

    @Override
    public Name getName() {
        return Name.identifier(name);
    }

    @Override
    public JavaType getType() {
        return NetBeansJavaType.create(getTypeHandle(), getProject());
    }

    @Override
    public boolean isVararg() {
        return isVararg;
    }

    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        return Collections.emptyList(); // temporary
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqname) {
        return null; // temporary
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return false; // temporary
    }
    
    @Override
    public int hashCode() {
        int hashCode = NBElementUtils.typeMirrorHandleHashCode(getTypeHandle(), getProject());
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NetBeansJavaValueParameter)) {
            return false;
        }
        NetBeansJavaValueParameter typeParameter = (NetBeansJavaValueParameter) obj;
        
        return NBElementUtils.typeMirrorHandleEquals(getTypeHandle(), typeParameter.getTypeHandle(), getProject());
    }
    
}
