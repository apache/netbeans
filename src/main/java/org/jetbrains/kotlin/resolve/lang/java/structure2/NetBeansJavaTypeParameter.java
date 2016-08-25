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
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java2.NBParameterUtils;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaTypeParameter extends NetBeansJavaClassifier implements JavaTypeParameter {

    public NetBeansJavaTypeParameter(TypeMirrorHandle typeHandle, Project project) {
        super(null, typeHandle, project);
    }

    @Override
    public Name getName() {
        return NBParameterUtils.getNameOfTypeParameter(getTypeHandle(), getProject());
    }

    @Override
    public Collection<JavaClassifierType> getUpperBounds() {
        return NBParameterUtils.getUpperBounds(getTypeHandle(), getProject());
    }
    
    @Override
    public String toString() {
        return getName().asString();
    }
    
    @Override
    public int hashCode() {
        int hashCode = NBParameterUtils.hashCode(getTypeHandle(), getProject());
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NetBeansJavaTypeParameter)) {
            return false;
        }
        NetBeansJavaTypeParameter typeParameter = (NetBeansJavaTypeParameter) obj;
        
        return NBParameterUtils.equals(getTypeHandle(), typeParameter.getTypeHandle(), getProject());
    }
    
}
