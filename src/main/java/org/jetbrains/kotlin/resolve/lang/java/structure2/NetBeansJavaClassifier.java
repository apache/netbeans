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
import java.util.Collections;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner;
import org.jetbrains.kotlin.load.java.structure.JavaClassifier;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public abstract class NetBeansJavaClassifier extends NetBeansJavaElement implements JavaClassifier, JavaAnnotationOwner {
    
    public NetBeansJavaClassifier(ElementHandle elementHandle, TypeMirrorHandle typeHandle, Project project) {
        super(elementHandle, typeHandle, project);
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
    
}
