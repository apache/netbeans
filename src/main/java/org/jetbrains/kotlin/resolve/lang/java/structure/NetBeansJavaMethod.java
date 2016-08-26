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

import java.util.List;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;
import org.jetbrains.kotlin.resolve.lang.java.NBExecutableUtils;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaMethod extends NetBeansJavaMember implements JavaMethod {

    public NetBeansJavaMethod(ElementHandle handle, JavaClass containingClass, Project project) {
        super(handle, containingClass, project);
    }

    @Override
    public List<JavaValueParameter> getValueParameters() {
        return NBExecutableUtils.getValueParameters(getElementHandle(), getProject());
    }

    @Override
    public JavaType getReturnType() {
        if (getContainingClass().getFqName().asString().equals("org.jetbrains.kotlin.model.KotlinEnvironment")) {
            JavaType type = NBExecutableUtils.getReturnType(getElementHandle(), getProject());
        }
        return NBExecutableUtils.getReturnType(getElementHandle(), getProject());
    }

    @Override
    public boolean getHasAnnotationParameterDefaultValue() {
        return NBExecutableUtils.hasAnnotationParameterDefaultValue(getElementHandle(), getProject());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        return NBExecutableUtils.getTypeParameters(getElementHandle(), getProject());
    }
    
}
