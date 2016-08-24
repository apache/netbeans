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

import java.util.Collections;
import java.util.List;
import javax.lang.model.type.TypeKind;
import org.jetbrains.kotlin.load.java.structure.JavaClassifier;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.resolve.lang.java2.NBTypeUtils;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaClassifierType extends NetBeansJavaType implements JavaClassifierType {
    
    public NetBeansJavaClassifierType(TypeMirrorHandle handle, Project project) {
        super(handle, project);
    }

    @Override
    public JavaClassifier getClassifier() {
        switch (getHandle().getKind()) {
            case DECLARED:
                ElementHandle elementHandle = ElementHandle.from(getHandle());
                return new NetBeansJavaClass(elementHandle, getProject());
            case TYPEVAR:
                return new NetBeansJavaTypeParameter(getHandle(), getProject());
            default:
                return null;
        }
    }

    @Override
    public List<JavaType> getTypeArguments() {
        if (getHandle().getKind() == TypeKind.DECLARED) {
            return NBTypeUtils.getTypeArguments(getHandle(), getProject());
        } else return Collections.emptyList();
    }

    @Override
    public boolean isRaw() {
        if (getHandle().getKind() == TypeKind.DECLARED) {
            return NBTypeUtils.isRaw(getHandle(), getProject());
        } else return true;
    }

    @Override
    public String getCanonicalText() {
        return NBTypeUtils.getName(getHandle(), getProject());
    }

    @Override
    public String getPresentableText() {
        return NBTypeUtils.getName(getHandle(), getProject());
    }
    
}
